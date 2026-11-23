package co.edu.usco.pw.tienda_cerca.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.edu.usco.pw.tienda_cerca.service.IaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/ia")
@Tag(name = "Inteligencia artificial", description = "Lectura de fotos con un modelo de visión (Google Gemini)")
public class IaController {

	@Autowired
	private IaService iaService;

	@Operation(summary = "¿Está configurada la IA?")
	@GetMapping("/estado")
	public Map<String, Boolean> estado() {
		return Map.of("disponible", iaService.disponible());
	}

	@Operation(summary = "Leer la foto de una lista (cliente)", description = "Envía la imagen al modelo de visión y devuelve "
			+ "los productos como texto. El front lo pasa luego al asistente que arma el carrito.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Texto leído"),
			@ApiResponse(responseCode = "400", description = "Archivo no válido"),
			@ApiResponse(responseCode = "422", description = "La foto no tiene una lista"),
			@ApiResponse(responseCode = "503", description = "La IA no está configurada (falta la clave)") })
	@PostMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Map<String, String> leerFoto(@RequestParam("foto") MultipartFile foto) {
		return Map.of("texto", iaService.leerFoto(foto), "fuente", "GEMINI");
	}
}
