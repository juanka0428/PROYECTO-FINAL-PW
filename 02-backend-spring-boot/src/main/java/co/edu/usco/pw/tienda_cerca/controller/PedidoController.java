package co.edu.usco.pw.tienda_cerca.controller;

import java.util.List;

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
import co.edu.usco.pw.tienda_cerca.model.Pedido;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Pedidos a domicilio, estados, repartidor y ubicación GPS")
public class PedidoController {

	@Autowired
	private PedidoService pedidoService;

	@Operation(summary = "Todos los pedidos (tendero o asesor)", description = "El asesor los usa para ver el último pedido del cliente que atiende.")
	@PreAuthorize("hasAnyRole('TENDERO', 'ASESOR')")
	@GetMapping
	public List<Pedido> todos() {
		return pedidoService.todos();
	}

	@Operation(summary = "Mis pedidos (cliente)")
	@PreAuthorize("hasRole('CLIENTE')")
	@GetMapping("/mios")
	public List<Pedido> mios(@AuthenticationPrincipal Usuario yo) {
		return pedidoService.deCliente(yo.getId());
	}

	@Operation(summary = "Mis entregas (repartidor)")
	@PreAuthorize("hasRole('REPARTIDOR')")
	@GetMapping("/entregas")
	public List<Pedido> entregas(@AuthenticationPrincipal Usuario yo) {
		return pedidoService.deRepartidor(yo.getId());
	}

	@Operation(summary = "Ver un pedido", description = "El cliente solo ve los suyos y el repartidor los asignados.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
			@ApiResponse(responseCode = "403", description = "No es tuyo"),
			@ApiResponse(responseCode = "404", description = "No existe") })
	@GetMapping("/{id}")
	public Pedido ver(@Parameter(description = "ID del pedido", required = true) @PathVariable Long id,
			@AuthenticationPrincipal Usuario yo) {
		return pedidoService.verPedido(id, yo);
	}

	@Operation(summary = "Hacer un pedido (cliente)", description = "Los triggers descuentan el inventario, guardan el historial "
			+ "y, si es fiado, revisan el cupo.")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Pedido creado"),
			@ApiResponse(responseCode = "400", description = "Sin inventario, cupo insuficiente o datos inválidos") })
	@PreAuthorize("hasRole('CLIENTE')")
	@PostMapping
	public ResponseEntity<Pedido> crear(@Valid @RequestBody Peticiones.NuevoPedido datos,
			@AuthenticationPrincipal Usuario yo) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(datos, yo));
	}

	@Operation(summary = "Cambiar el estado (tendero o repartidor)", description = "Orden: RECIBIDO → PREPARANDO → EN_CAMINO → ENTREGADO. "
			+ "CANCELADO solo antes de salir.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Estado cambiado"),
			@ApiResponse(responseCode = "400", description = "Cambio no permitido") })
	@PreAuthorize("hasAnyRole('TENDERO', 'REPARTIDOR')")
	@PutMapping("/{id}/estado")
	public Pedido cambiarEstado(@PathVariable Long id, @Valid @RequestBody Peticiones.CambioEstado datos,
			@AuthenticationPrincipal Usuario yo) {
		return pedidoService.cambiarEstado(id, datos.estado(), yo);
	}

	@Operation(summary = "Asignar repartidor (tendero)")
	@PreAuthorize("hasRole('TENDERO')")
	@PutMapping("/{id}/repartidor")
	public Pedido asignarRepartidor(@PathVariable Long id, @Valid @RequestBody Peticiones.AsignarRepartidor datos) {
		return pedidoService.asignarRepartidor(id, datos.repartidorId());
	}

	@Operation(summary = "Enviar ubicación GPS (repartidor)")
	@PreAuthorize("hasRole('REPARTIDOR')")
	@PutMapping("/{id}/ubicacion")
	public Pedido ubicacion(@PathVariable Long id, @Valid @RequestBody Peticiones.Ubicacion datos,
			@AuthenticationPrincipal Usuario yo) {
		return pedidoService.actualizarUbicacion(id, datos.lat(), datos.lng(), yo);
	}
}
