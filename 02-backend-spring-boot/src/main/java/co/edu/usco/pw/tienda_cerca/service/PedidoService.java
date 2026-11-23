package co.edu.usco.pw.tienda_cerca.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.usco.pw.tienda_cerca.dto.Peticiones;
import co.edu.usco.pw.tienda_cerca.model.Coordenada;
import co.edu.usco.pw.tienda_cerca.model.ItemPedido;
import co.edu.usco.pw.tienda_cerca.model.MovimientoFiado;
import co.edu.usco.pw.tienda_cerca.model.Pedido;
import co.edu.usco.pw.tienda_cerca.model.Producto;
import co.edu.usco.pw.tienda_cerca.model.Ubicacion;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.repository.MovimientoFiadoRepository;
import co.edu.usco.pw.tienda_cerca.repository.PedidoRepository;
import co.edu.usco.pw.tienda_cerca.repository.ProductoRepository;
import co.edu.usco.pw.tienda_cerca.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;

@Service
public class PedidoService {

	public static final int DOMICILIO = 2000;

	public static final int DOMICILIO_GRATIS_DESDE = 40000;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private MovimientoFiadoRepository movimientoRepository;

	@Autowired
	private EntityManager em;

	@Autowired
	private AvisoService avisoService;

	@Autowired
	private PagoService pagoService;

	@Transactional
	public Pedido crear(Peticiones.NuevoPedido datos, Usuario cliente) {
		String metodo = datos.metodoPago();
		if (!metodo.equals("EN_LINEA") && !metodo.equals("CONTRA_ENTREGA") && !metodo.equals("FIADO")) {
			throw error("error.pedido.formaPago");
		}

		Pedido p = new Pedido();
		int subtotal = 0;
		for (Peticiones.Item it : datos.items()) {
			Producto prod = productoRepository.findById(it.productoId()).orElse(null);
			if (prod == null || !Boolean.TRUE.equals(prod.getActivo())) {
				throw error("error.producto.noDisponible");
			}
			ItemPedido item = new ItemPedido();
			item.setProductoId(prod.getId());
			item.setNombre(prod.getNombre());
			item.setImagen(prod.getImagen());
			item.setPrecio(prod.getPrecio());
			item.setCantidad(it.cantidad());
			p.getItems().add(item);
			subtotal += prod.getPrecio() * it.cantidad();
		}

		int domicilio = subtotal >= DOMICILIO_GRATIS_DESDE ? 0 : DOMICILIO;

		p.setCodigo(String.format("PED-%04d", pedidoRepository.ultimoNumero() + 1));
		p.setClienteId(cliente.getId());
		p.setClienteNombre(cliente.getNombre());
		p.setTelefono(datos.telefono());
		p.setDireccion(datos.direccion());
		p.setBarrio(datos.barrio());
		p.setDestino(new Coordenada(cliente.getLat(), cliente.getLng()));
		p.setNotas(datos.notas());
		p.setSubtotal(subtotal);
		p.setDomicilio(domicilio);
		p.setTotal(subtotal + domicilio);
		p.setMetodoPago(metodo);
		if (metodo.equals("FIADO")) {
			p.setEstadoPago("FIADO");
		} else if (metodo.equals("EN_LINEA")) {
			PagoService.Resultado pago = pagoService.cobrar(subtotal + domicilio, datos.pago());
			p.setReferenciaPago(pago.referencia());
			p.setTarjetaPago(pago.tarjeta());
			p.setEstadoPago("PAGADO");
		} else {
			p.setEstadoPago("PENDIENTE");
		}
		p.setEstado("RECIBIDO");
		p.setOrigen(datos.origen() == null ? "CATALOGO" : datos.origen());
		p.setUltimoUsuario(cliente.getNombre());
		p.setFecha(LocalDateTime.now());

		pedidoRepository.saveAndFlush(p);

		if (metodo.equals("FIADO")) {
			movimientoRepository.saveAndFlush(
					new MovimientoFiado(cliente.getId(), "CARGO", p.getTotal(), "Pedido " + p.getCodigo(), p.getId()));
		}

		em.refresh(p);
		avisoService.cambio("pedidos");
		return p;
	}

	@Transactional
	public Pedido cambiarEstado(Long id, String nuevo, Usuario quien) {
		Pedido p = buscar(id);
		String actual = p.getEstado();

		if ("REPARTIDOR".equals(quien.getRol())) {
			if (!quien.getId().equals(p.getRepartidorId())) {
				throw error("error.pedido.noAsignado");
			}
			if (!nuevo.equals("EN_CAMINO") && !nuevo.equals("ENTREGADO")) {
				throw error("error.pedido.repartidorSolo");
			}
		}

		if (nuevo.equals("CANCELADO")) {
			if (!actual.equals("RECIBIDO") && !actual.equals("PREPARANDO")) {
				throw error("error.pedido.soloAntesDeSalir");
			}
		} else if (!nuevo.equals(siguiente(actual))) {
			throw error("error.pedido.cambioNoPermitido|" + actual + "|" + nuevo);
		}

		if (nuevo.equals("EN_CAMINO") && p.getRepartidorId() == null) {
			throw error("error.pedido.asignaRepartidor");
		}

		p.setEstado(nuevo);
		p.setUltimoUsuario(quien.getNombre());
		if (nuevo.equals("ENTREGADO") && p.getMetodoPago().equals("CONTRA_ENTREGA")) {
			p.setEstadoPago("PAGADO");
		}
		pedidoRepository.saveAndFlush(p);

		if (nuevo.equals("CANCELADO") && p.getMetodoPago().equals("FIADO")) {
			movimientoRepository.saveAndFlush(new MovimientoFiado(p.getClienteId(), "ABONO", p.getTotal(),
					"Pedido " + p.getCodigo() + " cancelado", p.getId()));
		}

		em.refresh(p);
		avisoService.cambio("pedidos");
		return p;
	}

	@Transactional
	public Pedido asignarRepartidor(Long id, Long repartidorId) {
		Pedido p = buscar(id);
		Usuario rep = usuarioRepository.findById(repartidorId).orElse(null);
		if (rep == null || !"REPARTIDOR".equals(rep.getRol())) {
			throw error("error.pedido.repartidorNoValido");
		}
		if (p.getEstado().equals("ENTREGADO") || p.getEstado().equals("CANCELADO")) {
			throw error("error.pedido.terminado");
		}
		p.setRepartidorId(rep.getId());
		p.setRepartidorNombre(rep.getNombre());
		avisoService.cambio("pedidos");
		return pedidoRepository.save(p);
	}

	@Transactional
	public Pedido actualizarUbicacion(Long id, double lat, double lng, Usuario repartidor) {
		Pedido p = buscar(id);
		if (!repartidor.getId().equals(p.getRepartidorId())) {
			throw error("error.pedido.noAsignado");
		}
		Ubicacion u = new Ubicacion();
		u.setLat(lat);
		u.setLng(lng);
		u.setActualizado(LocalDateTime.now());
		p.setUbicacionRepartidor(u);
		avisoService.cambio("pedidos");
		return pedidoRepository.save(p);
	}

	public Pedido verPedido(Long id, Usuario quien) {
		Pedido p = buscar(id);
		if ("CLIENTE".equals(quien.getRol()) && !quien.getId().equals(p.getClienteId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "error.pedido.noEsTuyo");
		}
		if ("REPARTIDOR".equals(quien.getRol()) && !quien.getId().equals(p.getRepartidorId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "error.pedido.noAsignado");
		}
		return p;
	}

	public List<Pedido> todos() {
		return pedidoRepository.findAllByOrderByFechaDesc();
	}

	public List<Pedido> deCliente(Long clienteId) {
		return pedidoRepository.findByClienteIdOrderByFechaDesc(clienteId);
	}

	public List<Pedido> deRepartidor(Long repartidorId) {
		return pedidoRepository.findByRepartidorIdOrderByFechaDesc(repartidorId);
	}

	private Pedido buscar(Long id) {
		return pedidoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "error.pedido.noEncontrado"));
	}

	private String siguiente(String estado) {
		return switch (estado) {
		case "RECIBIDO" -> "PREPARANDO";
		case "PREPARANDO" -> "EN_CAMINO";
		case "EN_CAMINO" -> "ENTREGADO";
		default -> "";
		};
	}

	private ResponseStatusException error(String mensaje) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
	}
}
