package co.edu.usco.pw.tienda_cerca.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.usco.pw.tienda_cerca.dto.Peticiones;
import co.edu.usco.pw.tienda_cerca.model.Pedido;
import co.edu.usco.pw.tienda_cerca.model.PedidoRecurrente;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.service.RecurrenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recurrentes")
@Tag(name = "Pedidos recurrentes", description = "Pedidos que se repiten solos cada semana, quincena o mes")
public class RecurrenteController {

	@Autowired
	private RecurrenteService recurrenteService;

	@Operation(summary = "Mis pedidos recurrentes")
	@GetMapping("/mios")
	public List<PedidoRecurrente> mios(@AuthenticationPrincipal Usuario yo) {
		return recurrenteService.deCliente(yo.getId());
	}

	@Operation(summary = "Crear pedido recurrente", description = "Una tarea programada (@Scheduled) crea el pedido el día indicado.")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Creado"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos") })
	@PostMapping
	public ResponseEntity<PedidoRecurrente> crear(@Valid @RequestBody Peticiones.NuevoRecurrente datos,
			@AuthenticationPrincipal Usuario yo) {
		return ResponseEntity.status(HttpStatus.CREATED).body(recurrenteService.crear(datos, yo));
	}

	@Operation(summary = "Pausar o reanudar")
	@PutMapping("/{id}/activo")
	public PedidoRecurrente alternar(@PathVariable Long id, @AuthenticationPrincipal Usuario yo) {
		return recurrenteService.alternar(id, yo);
	}

	@Operation(summary = "Pedir ahora", description = "Crea de una vez el pedido con los productos del recurrente.")
	@PostMapping("/{id}/pedir")
	public ResponseEntity<Pedido> pedirAhora(@PathVariable Long id, @AuthenticationPrincipal Usuario yo) {
		return ResponseEntity.status(HttpStatus.CREATED).body(recurrenteService.pedirAhora(id, yo));
	}

	@Operation(summary = "Eliminar pedido recurrente")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Eliminado"),
			@ApiResponse(responseCode = "404", description = "No existe") })
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario yo) {
		recurrenteService.eliminar(id, yo);
		return ResponseEntity.noContent().build();
	}
}
