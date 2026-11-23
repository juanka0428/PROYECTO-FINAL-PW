package co.edu.usco.pw.tienda_cerca.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Mensaje del chat")
public class Mensaje {

	@Schema(description = "ID", example = "1")
	private Long id;

	@Schema(description = "Quien lo escribio", example = "CLIENTE", allowableValues = { "CLIENTE", "IA", "ASESOR" })
	private String autor;

	@Schema(description = "Texto del mensaje", example = "¿A qué hora abren?")
	private String texto;

	@Schema(description = "Fecha")
	private LocalDateTime fecha;
}
