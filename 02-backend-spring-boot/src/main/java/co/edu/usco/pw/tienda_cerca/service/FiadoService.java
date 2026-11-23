package co.edu.usco.pw.tienda_cerca.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.usco.pw.tienda_cerca.model.MovimientoFiado;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.repository.MovimientoFiadoRepository;

@Service
public class FiadoService {

	@Autowired
	private MovimientoFiadoRepository movimientoRepository;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private AvisoService avisoService;

	public Map<String, Object> cuenta(Long clienteId, Usuario quien) {
		if ("CLIENTE".equals(quien.getRol()) && !quien.getId().equals(clienteId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "error.fiado.soloTuyo");
		}
		if (!"CLIENTE".equals(quien.getRol()) && !"TENDERO".equals(quien.getRol())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "error.sinPermiso");
		}
		Usuario c = usuarioService.buscarCliente(clienteId);
		Map<String, Object> cuenta = new LinkedHashMap<>();
		cuenta.put("clienteId", c.getId());
		cuenta.put("cupo", c.getCupoFiado());
		cuenta.put("saldo", c.getSaldoFiado());
		cuenta.put("disponible", c.getCupoFiado() - c.getSaldoFiado());
		cuenta.put("movimientos", movimientoRepository.findByClienteIdOrderByFechaDescIdDesc(clienteId));
		return cuenta;
	}

	@Transactional
	public MovimientoFiado abonar(Long clienteId, Integer valor, String descripcion) {
		usuarioService.buscarCliente(clienteId);
		String texto = (descripcion == null || descripcion.isBlank()) ? "Abono en efectivo" : descripcion;
		avisoService.cambio("fiado");
		return movimientoRepository.saveAndFlush(new MovimientoFiado(clienteId, "ABONO", valor, texto, null));
	}
}
