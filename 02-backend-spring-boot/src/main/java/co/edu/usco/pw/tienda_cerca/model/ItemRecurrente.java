package co.edu.usco.pw.tienda_cerca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Producto de un pedido recurrente")
public class ItemRecurrente {

	@Schema(description = "ID del producto", example = "1")
	private Long productoId;

	@Schema(description = "Nombre", example = "Leche entera 1 L")
	private String nombre;

	@Schema(description = "Imagen", example = "🥛")
	private String imagen;

	@Schema(description = "Precio de referencia", example = "4200")
	private Integer precio;

	@Schema(description = "Cantidad", example = "2")
	private Integer cantidad;
}
