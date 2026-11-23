package co.edu.usco.pw.tienda_cerca.model;

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
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@Schema(description = "Usuario del sistema (cliente, tendero, repartidor o asesor)")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "ID unico del usuario", example = "1")
	private Long id;

	@Column(nullable = false)
	@Schema(description = "Nombre completo", example = "María Gómez")
	private String nombre;

	@Column(nullable = false, unique = true)
	@Schema(description = "Correo, se usa para iniciar sesion", example = "maria@correo.com")
	private String correo;

	@JsonIgnore
	@Column(nullable = false)
	private String clave;

	@Schema(description = "Telefono de contacto", example = "310 555 1234")
	private String telefono;

	@Column(nullable = false)
	@Schema(description = "Rol del usuario", example = "CLIENTE", allowableValues = { "CLIENTE", "TENDERO", "REPARTIDOR", "ASESOR" })
	private String rol;

	@Schema(description = "Direccion de entrega", example = "Calle 8 # 12-34")
	private String direccion;

	@Schema(description = "Barrio", example = "Altico")
	private String barrio;

	@Schema(description = "Latitud de la direccion (para el mapa)", example = "2.9268")
	private Double lat;

	@Schema(description = "Longitud de la direccion (para el mapa)", example = "-75.2862")
	private Double lng;

	@Schema(description = "Cupo maximo de fiado que asigna el tendero", example = "100000")
	private Integer cupoFiado = 0;

	@Schema(description = "Lo que el cliente debe (lo actualiza un trigger)", example = "19900")
	private Integer saldoFiado = 0;

	@Schema(description = "Si la cuenta esta activa", example = "true")
	private Boolean activo = true;

	@Schema(description = "Proveedor de la cuenta", example = "LOCAL")
	private String proveedor = "LOCAL";
}
