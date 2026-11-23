package co.edu.usco.pw.tienda_cerca.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimientos_fiado")
@Data
@NoArgsConstructor
@Schema(description = "Movimiento de la cuenta de fiado")
public class MovimientoFiado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "ID del movimiento", example = "1")
	private Long id;

	@Schema(description = "ID del cliente", example = "1")
	private Long clienteId;

	@Schema(description = "Tipo de movimiento", example = "ABONO", allowableValues = { "CARGO", "ABONO" })
	private String tipo;

	@Schema(description = "Valor en pesos", example = "10000")
	private Integer valor;

	@Schema(description = "Descripcion", example = "Abono en efectivo")
	private String descripcion;

	@Schema(description = "Pedido relacionado (si es un cargo)", example = "3")
	private Long pedidoId;

	@Schema(description = "Fecha del movimiento")
	private LocalDateTime fecha;

	public MovimientoFiado(Long clienteId, String tipo, Integer valor, String descripcion, Long pedidoId) {
		this.clienteId = clienteId;
		this.tipo = tipo;
		this.valor = valor;
		this.descripcion = descripcion;
		this.pedidoId = pedidoId;
		this.fecha = LocalDateTime.now();
	}
}
