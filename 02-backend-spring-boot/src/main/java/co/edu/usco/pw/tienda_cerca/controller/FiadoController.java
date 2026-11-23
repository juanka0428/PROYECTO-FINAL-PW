package co.edu.usco.pw.tienda_cerca.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.usco.pw.tienda_cerca.dto.Peticiones;
import co.edu.usco.pw.tienda_cerca.model.MovimientoFiado;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.service.FiadoService;
import co.edu.usco.pw.tienda_cerca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Fiado y clientes", description = "Cuenta de fiado, abonos, cupos, clientes y repartidores")
public class FiadoController {

	@Autowired
	private FiadoService fiadoService;

	@Autowired
	private UsuarioService usuarioService;

	@Operation(summary = "Ver la cuenta de fiado", description = "El cliente solo puede ver la suya; el tendero la de cualquiera.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Cuenta de fiado"),
			@ApiResponse(responseCode = "403", description = "No es tuya") })
	@GetMapping("/fiado/{clienteId}")
	public Map<String, Object> cuenta(@PathVariable Long clienteId, @AuthenticationPrincipal Usuario yo) {
		return fiadoService.cuenta(clienteId, yo);
	}

	@Operation(summary = "Registrar un abono (tendero)", description = "El trigger trg_fiado_validar no deja abonar más de lo que se debe.")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Abono registrado"),
			@ApiResponse(responseCode = "400", description = "El abono es mayor que la deuda") })
	@PreAuthorize("hasRole('TENDERO')")
	@PostMapping("/fiado/{clienteId}/abonos")
	public ResponseEntity<MovimientoFiado> abonar(@PathVariable Long clienteId,
			@Valid @RequestBody Peticiones.Abono datos) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(fiadoService.abonar(clienteId, datos.valor(), datos.descripcion()));
	}

	@Operation(summary = "Listar clientes (tendero o asesor)")
	@GetMapping("/clientes")
	public List<Usuario> clientes() {
		return usuarioService.porRol("CLIENTE");
	}

	@Operation(summary = "Cambiar el cupo de fiado (tendero)")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Cupo actualizado"),
			@ApiResponse(responseCode = "400", description = "El cupo es menor que la deuda") })
	@PutMapping("/clientes/{id}/cupo")
	public Usuario cambiarCupo(@PathVariable Long id, @Valid @RequestBody Peticiones.Cupo datos) {
		return usuarioService.cambiarCupo(id, datos.cupo());
	}

	@Operation(summary = "Activar o desactivar un cliente (tendero)")
	@PutMapping("/clientes/{id}/activo")
	public Usuario alternarActivo(@PathVariable Long id) {
		return usuarioService.alternarActivo(id);
	}

	@Operation(summary = "Listar repartidores (tendero)")
	@GetMapping("/repartidores")
	public List<Usuario> repartidores() {
		return usuarioService.porRol("REPARTIDOR");
	}
}
