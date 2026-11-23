package co.edu.usco.pw.tienda_cerca.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedidos_recurrentes")
@Data
@NoArgsConstructor
@Schema(description = "Pedido recurrente del cliente")
public class PedidoRecurrente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "ID", example = "1")
	private Long id;

	@Schema(description = "ID del cliente", example = "1")
	private Long clienteId;

	@Schema(description = "Nombre que le pone el cliente", example = "Desayuno de la semana")
	private String nombre;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "items_recurrente", joinColumns = @JoinColumn(name = "recurrente_id"))
	private List<ItemRecurrente> items = new ArrayList<>();

	@Schema(description = "Cada cuanto se repite", example = "SEMANAL", allowableValues = { "SEMANAL", "QUINCENAL", "MENSUAL" })
	private String frecuencia;

	@Schema(description = "Dia de la semana en que se pide", example = "LUNES")
	private String dia;

	@Schema(description = "Si esta activo", example = "true")
	private Boolean activo = true;

	@Schema(description = "Ultima vez que se creo el pedido automaticamente")
	private LocalDate ultimaEjecucion;
}
