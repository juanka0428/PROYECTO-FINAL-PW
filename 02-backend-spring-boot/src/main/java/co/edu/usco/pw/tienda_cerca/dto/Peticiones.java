package co.edu.usco.pw.tienda_cerca.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Peticiones {

	private Peticiones() {
	}

	@Schema(description = "Datos para iniciar sesion")
	public record Login(@Schema(example = "maria@correo.com") @NotBlank @Email String correo,
			@Schema(example = "123456") @NotBlank String clave) {
	}

	@Schema(description = "Datos para registrar un cliente")
	public record Registro(@Schema(example = "Ana Pérez") @NotBlank String nombre,
			@Schema(example = "ana@correo.com") @NotBlank @Email String correo,
			@Schema(example = "123456") @NotBlank @Size(min = 6) String clave,
			@Schema(example = "313 000 1111") @NotBlank String telefono,
			@Schema(example = "Calle 5 # 10-20") String direccion, @Schema(example = "Centro") String barrio) {
	}

	@Schema(description = "Producto del carrito")
	public record Item(@Schema(example = "1") @NotNull Long productoId,
			@Schema(example = "2") @NotNull @Min(1) Integer cantidad) {
	}

	@Schema(description = "Pedido nuevo")
	public record NuevoPedido(@Schema(example = "Calle 8 # 12-34") @NotBlank String direccion,
			@Schema(example = "Altico") @NotBlank String barrio,
			@Schema(example = "310 555 1234") @NotBlank String telefono,
			@Schema(example = "Timbre dañado, llamar al llegar") String notas,
			@Schema(example = "CONTRA_ENTREGA") @NotBlank String metodoPago,
			@Schema(example = "CATALOGO") String origen, @NotEmpty @Valid List<Item> items, @Valid Tarjeta pago) {
	}

	@Schema(description = "Tarjeta de prueba (4242 4242 4242 4242 aprueba, 4000 0000 0000 0002 rechaza)")
	public record Tarjeta(@Schema(example = "MARIA GOMEZ") @NotBlank String titular,
			@Schema(example = "4242 4242 4242 4242") @NotBlank String numero,
			@Schema(example = "12/30") @NotBlank String vence, @Schema(example = "123") @NotBlank String cvc) {
	}

	public record CambioEstado(@Schema(example = "PREPARANDO") @NotBlank String estado) {
	}

	public record AsignarRepartidor(@Schema(example = "5") @NotNull Long repartidorId) {
	}

	public record Ubicacion(@Schema(example = "2.9301") @NotNull Double lat,
			@Schema(example = "-75.2840") @NotNull Double lng) {
	}

	public record Abono(@Schema(example = "10000") @NotNull @Min(1) Integer valor,
			@Schema(example = "Abono en efectivo") String descripcion) {
	}

	public record Cupo(@Schema(example = "120000") @NotNull @Min(0) Integer cupo) {
	}

	public record Contacto(@Schema(example = "Carlos Díaz") @NotBlank String nombre,
			@Schema(example = "Tienda La Esquina") @NotBlank String tienda,
			@Schema(example = "300 123 4567") @NotBlank String telefono) {
	}

	@Schema(description = "Pedido recurrente nuevo")
	public record NuevoRecurrente(@Schema(example = "Desayuno de la semana") @NotBlank String nombre,
			@Schema(example = "SEMANAL") @NotBlank String frecuencia, @Schema(example = "LUNES") @NotBlank String dia,
			@NotEmpty @Valid List<Item> items) {
	}

	public record MensajeChat(@Schema(example = "¿A qué hora abren?") @NotBlank @Size(max = 1000) String texto) {
	}
}
