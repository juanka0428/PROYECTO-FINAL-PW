package co.edu.usco.pw.tienda_cerca.dto;

import co.edu.usco.pw.tienda_cerca.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT y datos del usuario")
public record Sesion(String token, Usuario usuario) {
}
