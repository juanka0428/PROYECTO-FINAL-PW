package co.edu.usco.pw.tienda_cerca.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@Schema(description = "Producto del catalogo de la tienda")
public class Producto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "ID del producto", example = "1")
	private Long id;

	@NotBlank
	@Column(nullable = false)
	@Schema(description = "Nombre", example = "Leche entera 1 L", requiredMode = Schema.RequiredMode.REQUIRED)
	private String nombre;

	@NotNull
	@Schema(description = "ID de la categoria", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	private Long categoriaId;

	@NotNull
	@Min(0)
	@Schema(description = "Precio en pesos", example = "4200", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer precio;

	@NotNull
	@Min(0)
	@Schema(description = "Unidades disponibles", example = "40", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer stock;

	@Min(0)
	@Schema(description = "Cantidad minima antes de avisar que se esta agotando", example = "10")
	private Integer stockMinimo = 0;

	@Schema(description = "Unidad de venta", example = "bolsa")
	private String unidad;

	@Schema(description = "Imagen (por ahora un emoji)", example = "🥛")
	private String imagen;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "producto_palabras", joinColumns = @JoinColumn(name = "producto_id"))
	@Column(name = "palabra")
	@Schema(description = "Palabras clave para el asistente", example = "[\"leche\", \"leches\"]")
	private List<String> palabrasClave = new ArrayList<>();

	@Schema(description = "Si se muestra en el catalogo", example = "true")
	private Boolean activo = true;
}
