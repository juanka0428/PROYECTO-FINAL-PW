package co.edu.usco.pw.tienda_cerca.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.usco.pw.tienda_cerca.model.Categoria;
import co.edu.usco.pw.tienda_cerca.model.Producto;
import co.edu.usco.pw.tienda_cerca.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Productos", description = "CRUD del catálogo de la tienda")
public class ProductoController {

	@Autowired
	private ProductoService productoService;

	@Operation(summary = "Listar categorías", description = "Público.")
	@GetMapping("/categorias")
	public List<Categoria> categorias() {
		return productoService.categorias();
	}

	@Operation(summary = "Listar productos", description = "Público. Incluye los inactivos para que el tendero los vea.")
	@GetMapping("/productos")
	public List<Producto> listar() {
		return productoService.listar();
	}

	@Operation(summary = "Ver un producto")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Producto encontrado"),
			@ApiResponse(responseCode = "404", description = "error.producto.noEncontrado") })
	@GetMapping("/productos/{id}")
	public Producto ver(@Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
		return productoService.buscar(id);
	}

	@Operation(summary = "Crear producto (tendero)")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Producto creado"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos o categoría inexistente"),
			@ApiResponse(responseCode = "403", description = "Solo el tendero") })
	@PostMapping("/productos")
	public ResponseEntity<Producto> crear(@Valid @RequestBody Producto p) {
		return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(p));
	}

	@Operation(summary = "Actualizar producto (tendero)")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Producto actualizado"),
			@ApiResponse(responseCode = "404", description = "error.producto.noEncontrado"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos") })
	@PutMapping("/productos/{id}")
	public Producto actualizar(@Parameter(description = "ID del producto", required = true) @PathVariable Long id,
			@Valid @RequestBody Producto datos) {
		return productoService.actualizar(id, datos);
	}

	@Operation(summary = "Eliminar producto (tendero)")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Producto eliminado"),
			@ApiResponse(responseCode = "404", description = "error.producto.noEncontrado") })
	@DeleteMapping("/productos/{id}")
	public ResponseEntity<Void> eliminar(@Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
		productoService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
