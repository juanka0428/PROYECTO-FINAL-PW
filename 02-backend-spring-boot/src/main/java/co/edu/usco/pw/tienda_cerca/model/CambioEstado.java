package co.edu.usco.pw.tienda_cerca.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "historial_pedido")
@Data
@NoArgsConstructor
@Schema(description = "Cambio de estado de un pedido (lo guarda un trigger)")
public class CambioEstado {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	@Column(name = "pedido_id")
	private Long pedidoId;

	@Schema(description = "Estado nuevo", example = "PREPARANDO")
	private String estado;

	@Schema(description = "Fecha del cambio")
	private LocalDateTime fecha;

	@Schema(description = "Quien hizo el cambio", example = "Pedro Ramírez")
	private String usuario;
}
