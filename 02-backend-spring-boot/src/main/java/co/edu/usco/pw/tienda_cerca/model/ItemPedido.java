package co.edu.usco.pw.tienda_cerca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items_pedido")
@Data
@NoArgsConstructor
@Schema(description = "Producto dentro de un pedido")
public class ItemPedido {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Schema(description = "ID del producto", example = "1")
	private Long productoId;

	@Schema(description = "Nombre al momento de la compra", example = "Leche entera 1 L")
	private String nombre;

	@Schema(description = "Imagen", example = "🥛")
	private String imagen;

	@Schema(description = "Precio unitario al momento de la compra", example = "4200")
	private Integer precio;

	@Schema(description = "Cantidad", example = "2")
	private Integer cantidad;
}
