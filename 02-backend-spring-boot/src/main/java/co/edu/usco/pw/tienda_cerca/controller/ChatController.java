package co.edu.usco.pw.tienda_cerca.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import co.edu.usco.pw.tienda_cerca.model.Conversacion;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Atención con asistente virtual y asesor humano (MySQL)")
public class ChatController {

	@Autowired
	private ChatService chatService;

	@Operation(summary = "Mi conversación abierta (cliente)")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Conversación"),
			@ApiResponse(responseCode = "204", description = "No tiene conversación abierta") })
	@PreAuthorize("hasRole('CLIENTE')")
	@GetMapping("/mia")
	public ResponseEntity<Conversacion> mia(@AuthenticationPrincipal Usuario yo) {
		Conversacion c = chatService.miConversacion(yo);
		return c == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(c);
	}

	@Operation(summary = "Enviar mensaje (cliente)", description = "Si la conversación está con la IA, el asistente responde.")
	@PreAuthorize("hasRole('CLIENTE')")
	@PostMapping("/mensajes")
	public Conversacion enviar(@Valid @RequestBody Peticiones.MensajeChat datos, @AuthenticationPrincipal Usuario yo) {
		return chatService.enviarComoCliente(yo, datos.texto());
	}

	@Operation(summary = "Pedir un asesor (cliente)")
	@PreAuthorize("hasRole('CLIENTE')")
	@PostMapping("/asesor")
	public Conversacion pedirAsesor(@AuthenticationPrincipal Usuario yo) {
		return chatService.pedirAsesor(yo);
	}

	@Operation(summary = "Todas las conversaciones (asesor)")
	@PreAuthorize("hasRole('ASESOR')")
	@GetMapping
	public List<Conversacion> todas() {
		return chatService.todas();
	}

	@Operation(summary = "Conversaciones en espera (asesor)")
	@PreAuthorize("hasRole('ASESOR')")
	@GetMapping("/pendientes")
	public Map<String, Integer> pendientes() {
		return Map.of("pendientes", chatService.pendientes());
	}

	@Operation(summary = "Ver una conversación")
	@PreAuthorize("hasAnyRole('ASESOR', 'CLIENTE')")
	@GetMapping("/{id}")
	public Conversacion ver(@PathVariable Long id, @AuthenticationPrincipal Usuario yo) {
		return chatService.ver(id, yo);
	}

	@Operation(summary = "Tomar conversación (asesor)")
	@PreAuthorize("hasRole('ASESOR')")
	@PutMapping("/{id}/tomar")
	public Conversacion tomar(@PathVariable Long id, @AuthenticationPrincipal Usuario yo) {
		return chatService.tomar(id, yo);
	}

	@Operation(summary = "Responder (asesor)", description = "El trigger tr_mensaje_validar de MySQL no deja escribir en una conversación cerrada.")
	@PreAuthorize("hasRole('ASESOR')")
	@PostMapping("/{id}/mensajes")
	public Conversacion responder(@PathVariable Long id, @Valid @RequestBody Peticiones.MensajeChat datos,
			@AuthenticationPrincipal Usuario yo) {
		return chatService.enviarComoAsesor(id, yo, datos.texto());
	}

	@Operation(summary = "Cerrar conversación")
	@PreAuthorize("hasAnyRole('ASESOR', 'CLIENTE')")
	@PutMapping("/{id}/cerrar")
	public Conversacion cerrar(@PathVariable Long id, @AuthenticationPrincipal Usuario yo) {
		return chatService.cerrar(id, yo);
	}
}
