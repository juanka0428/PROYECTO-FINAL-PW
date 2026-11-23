package co.edu.usco.pw.tienda_cerca.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@Schema(description = "Pedido a domicilio")
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "ID del pedido", example = "1")
	private Long id;

	@Column(unique = true)
	@Schema(description = "Codigo visible para el cliente", example = "PED-0001")
	private String codigo;

	@Schema(description = "ID del cliente", example = "1")
	private Long clienteId;

	@Schema(description = "Nombre del cliente", example = "María Gómez")
	private String clienteNombre;

	private String telefono;
	private String direccion;
	private String barrio;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "lat", column = @Column(name = "destino_lat")),
			@AttributeOverride(name = "lng", column = @Column(name = "destino_lng")) })
	private Coordenada destino;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	@JoinColumn(name = "pedido_id", nullable = false)
	private List<ItemPedido> items = new ArrayList<>();

	@Schema(description = "Suma de los productos", example = "17500")
	private Integer subtotal;

	@Schema(description = "Valor del domicilio", example = "2000")
	private Integer domicilio;

	@Schema(description = "Total a pagar", example = "19500")
	private Integer total;

	@Schema(description = "Forma de pago", example = "CONTRA_ENTREGA", allowableValues = { "EN_LINEA", "CONTRA_ENTREGA", "FIADO" })
	private String metodoPago;

	@Schema(description = "Estado del pago", example = "PENDIENTE", allowableValues = { "PENDIENTE", "PAGADO", "FIADO" })
	private String estadoPago;

	@Schema(description = "Estado del pedido", example = "RECIBIDO", allowableValues = { "RECIBIDO", "PREPARANDO", "EN_CAMINO", "ENTREGADO", "CANCELADO" })
	private String estado;

	@Schema(description = "Como se hizo el pedido", example = "CATALOGO", allowableValues = { "CATALOGO", "TEXTO", "VOZ", "FOTO", "RECURRENTE" })
	private String origen;

	private String notas;

	@Schema(description = "Referencia de la pasarela de pagos (pago en línea)", example = "PAY-1A2B3C4D")
	private String referenciaPago;

	@Schema(description = "Tarjeta con la que se pagó (enmascarada)", example = "VISA •••• 4242")
	private String tarjetaPago;

	private Long repartidorId;
	private String repartidorNombre;

	@Embedded
	private Ubicacion ubicacionRepartidor;

	@OneToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	@JoinColumn(name = "pedido_id", insertable = false, updatable = false)
	@OrderBy("fecha, id")
	private List<CambioEstado> historial = new ArrayList<>();

	@JsonIgnore
	private String ultimoUsuario;

	@Schema(description = "Fecha en que se hizo el pedido")
	private LocalDateTime fecha;
}
