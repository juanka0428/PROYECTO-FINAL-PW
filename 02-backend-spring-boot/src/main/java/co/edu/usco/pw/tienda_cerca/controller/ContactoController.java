package co.edu.usco.pw.tienda_cerca.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.usco.pw.tienda_cerca.dto.Peticiones;
import co.edu.usco.pw.tienda_cerca.repository.ContactoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contacto")
@Tag(name = "Contacto", description = "Formulario público de la página de inicio (MySQL)")
public class ContactoController {

	@Autowired
	private ContactoRepository contactoRepository;

	@Operation(summary = "Guardar los datos de un tendero interesado", description = "Público.")
	@PostMapping
	public ResponseEntity<Map<String, Object>> guardar(@Valid @RequestBody Peticiones.Contacto datos) {
		contactoRepository.guardarContacto(datos.nombre(), datos.tienda(), datos.telefono());
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ok", true));
	}
}
