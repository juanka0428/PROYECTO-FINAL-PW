package co.edu.usco.pw.tienda_cerca.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.usco.pw.tienda_cerca.dto.Peticiones;
import co.edu.usco.pw.tienda_cerca.model.ItemRecurrente;
import co.edu.usco.pw.tienda_cerca.model.Pedido;
import co.edu.usco.pw.tienda_cerca.model.PedidoRecurrente;
import co.edu.usco.pw.tienda_cerca.model.Producto;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.repository.PedidoRecurrenteRepository;
import co.edu.usco.pw.tienda_cerca.repository.ProductoRepository;

@Service
public class RecurrenteService {

	private static final Set<String> FRECUENCIAS = Set.of("SEMANAL", "QUINCENAL", "MENSUAL");
	private static final List<String> DIAS = List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO",
			"DOMINGO");

	@Autowired
	private PedidoRecurrenteRepository recurrenteRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private PedidoService pedidoService;

	@Autowired
	private UsuarioService usuarioService;

	public List<PedidoRecurrente> deCliente(Long clienteId) {
		return recurrenteRepository.findByClienteIdOrderById(clienteId);
	}

	@Transactional
	public PedidoRecurrente crear(Peticiones.NuevoRecurrente datos, Usuario cliente) {
		String frecuencia = datos.frecuencia().toUpperCase();
		String dia = normalizarDia(datos.dia());
		if (!FRECUENCIAS.contains(frecuencia)) {
			throw error("error.recurrente.frecuencia");
		}
		if (!DIAS.contains(dia)) {
			throw error("error.recurrente.dia");
		}
		PedidoRecurrente r = new PedidoRecurrente();
		r.setClienteId(cliente.getId());
		r.setNombre(datos.nombre());
		r.setFrecuencia(frecuencia);
		r.setDia(dia);
		r.setActivo(true);
		for (Peticiones.Item it : datos.items()) {
			Producto p = productoRepository.findById(it.productoId())
					.orElseThrow(() -> error("error.producto.noExiste"));
			r.getItems().add(new ItemRecurrente(p.getId(), p.getNombre(), p.getImagen(), p.getPrecio(), it.cantidad()));
		}
		return recurrenteRepository.save(r);
	}

	@Transactional
	public PedidoRecurrente alternar(Long id, Usuario cliente) {
		PedidoRecurrente r = buscarPropio(id, cliente);
		r.setActivo(!Boolean.TRUE.equals(r.getActivo()));
		return recurrenteRepository.save(r);
	}

	@Transactional
	public void eliminar(Long id, Usuario cliente) {
		recurrenteRepository.delete(buscarPropio(id, cliente));
	}

	@Transactional
	public Pedido pedirAhora(Long id, Usuario cliente) {
		PedidoRecurrente r = buscarPropio(id, cliente);
		Pedido p = crearPedido(r);
		r.setUltimaEjecucion(LocalDate.now());
		recurrenteRepository.save(r);
		return p;
	}

	@Scheduled(cron = "${app.recurrentes.cron:0 0 7 * * *}", zone = "America/Bogota")
	public void ejecutarProgramados() {
		LocalDate hoy = LocalDate.now();
		String dia = DIAS.get(hoy.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
		for (PedidoRecurrente r : recurrenteRepository.findByActivoTrueAndDia(dia)) {
			if (!tocaHoy(r, hoy)) {
				continue;
			}
			try {
				crearPedido(r);
				r.setUltimaEjecucion(hoy);
				recurrenteRepository.save(r);
				System.out.println("Pedido recurrente creado: " + r.getNombre());
			} catch (Exception e) {
				System.out.println("No se pudo crear el recurrente " + r.getId() + ": " + e.getMessage());
			}
		}
	}

	boolean tocaHoy(PedidoRecurrente r, LocalDate hoy) {
		if (r.getUltimaEjecucion() == null) {
			return true;
		}
		long dias = ChronoUnit.DAYS.between(r.getUltimaEjecucion(), hoy);
		return switch (r.getFrecuencia()) {
		case "QUINCENAL" -> dias >= 14;
		case "MENSUAL" -> dias >= 28;
		default -> dias >= 7;
		};
	}

	private Pedido crearPedido(PedidoRecurrente r) {
		Usuario cliente = usuarioService.recargar(r.getClienteId());
		List<Peticiones.Item> items = r.getItems().stream()
				.map(i -> new Peticiones.Item(i.getProductoId(), i.getCantidad())).toList();
		String direccion = cliente.getDireccion() == null ? "Sin dirección" : cliente.getDireccion();
		String barrio = cliente.getBarrio() == null ? "-" : cliente.getBarrio();
		String telefono = cliente.getTelefono() == null ? "-" : cliente.getTelefono();
		return pedidoService.crear(new Peticiones.NuevoPedido(direccion, barrio, telefono,
				"Pedido recurrente: " + r.getNombre(), "CONTRA_ENTREGA", "RECURRENTE", items, null), cliente);
	}

	private PedidoRecurrente buscarPropio(Long id, Usuario cliente) {
		PedidoRecurrente r = recurrenteRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "error.recurrente.noEncontrado"));
		if (!r.getClienteId().equals(cliente.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "error.recurrente.noEsTuyo");
		}
		return r;
	}

	private String normalizarDia(String dia) {
		String d = java.text.Normalizer.normalize(dia.trim().toUpperCase(), java.text.Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
		try {
			return DIAS.get(DayOfWeek.valueOf(d).getValue() - 1);
		} catch (IllegalArgumentException e) {
			return d;
		}
	}

	private ResponseStatusException error(String mensaje) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
	}
}
