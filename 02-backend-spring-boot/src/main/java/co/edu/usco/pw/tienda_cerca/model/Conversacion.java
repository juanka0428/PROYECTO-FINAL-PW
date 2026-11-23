package co.edu.usco.pw.tienda_cerca.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Conversacion del chat de atencion (MySQL)")
public class Conversacion {

	@Schema(description = "ID", example = "1")
	private Long id;

	@Schema(description = "ID del cliente", example = "1")
	private Long clienteId;

	@Schema(description = "Nombre del cliente", example = "María Gómez")
	private String clienteNombre;

	@Schema(description = "Estado", example = "IA", allowableValues = { "IA", "ESPERANDO_ASESOR", "CON_ASESOR", "CERRADA" })
	private String estado;

	private Long asesorId;
	private String asesorNombre;

	private List<Mensaje> mensajes = new ArrayList<>();

	@Schema(description = "Fecha del ultimo mensaje (la actualiza un trigger de MySQL)")
	private LocalDateTime actualizada;
}
