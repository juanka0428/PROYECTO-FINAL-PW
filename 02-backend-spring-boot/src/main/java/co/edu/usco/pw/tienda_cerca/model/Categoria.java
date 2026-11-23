package co.edu.usco.pw.tienda_cerca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Categoria de productos")
public class Categoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "ID de la categoria", example = "1")
	private Long id;

	@Column(nullable = false)
	@Schema(description = "Nombre", example = "Lácteos y huevos")
	private String nombre;

	@Schema(description = "Icono (emoji)", example = "🥛")
	private String icono;

	public Categoria(String nombre, String icono) {
		this.nombre = nombre;
		this.icono = icono;
	}
}
