package co.edu.usco.pw.tienda_cerca.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import co.edu.usco.pw.tienda_cerca.model.Producto;
import co.edu.usco.pw.tienda_cerca.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Imágenes", description = "Fotos de los productos")
public class ImagenController {

	private static final Map<String, String> EXTENSIONES = Map.of("image/jpeg", ".jpg", "image/png", ".png",
			"image/webp", ".webp");

	@Value("${app.imagenes.carpeta:imagenes}")
	private String carpeta;

	@Autowired
	private ProductoService productoService;

	@Operation(summary = "Subir la foto de un producto (tendero)")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Foto guardada"),
			@ApiResponse(responseCode = "400", description = "El archivo no es una imagen"),
			@ApiResponse(responseCode = "413", description = "La imagen pesa más de 5 MB") })
	@PostMapping(value = "/productos/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Producto subir(@PathVariable Long id, @RequestParam("foto") MultipartFile foto) throws IOException {
		String extension = EXTENSIONES.get(foto.getContentType());
		if (foto.isEmpty() || extension == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.imagen.tipo");
		}
		Path dir = Paths.get(carpeta).toAbsolutePath().normalize();
		Files.createDirectories(dir);
		String nombre = "producto-" + id + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;
		Files.copy(foto.getInputStream(), dir.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
		return productoService.cambiarImagen(id, "/api/imagenes/" + nombre);
	}

	@Operation(summary = "Ver la foto de un producto", description = "Público.")
	@GetMapping("/imagenes/{nombre:.+}")
	public ResponseEntity<Resource> ver(@PathVariable String nombre) throws IOException {
		Path dir = Paths.get(carpeta).toAbsolutePath().normalize();
		Path archivo = dir.resolve(nombre).normalize();
		if (!archivo.startsWith(dir) || !Files.isRegularFile(archivo)) {
			return ResponseEntity.notFound().build();
		}
		Resource recurso = new UrlResource(archivo.toUri());
		MediaType tipo = MediaTypeFactory.getMediaType(recurso).orElse(MediaType.APPLICATION_OCTET_STREAM);
		return ResponseEntity.ok().contentType(tipo).cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(7)))
				.body(recurso);
	}
}
