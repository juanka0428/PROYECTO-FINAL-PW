package co.edu.usco.pw.tienda_cerca.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class Ubicacion {

	@Column(name = "rep_lat")
	private Double lat;

	@Column(name = "rep_lng")
	private Double lng;

	@Column(name = "rep_actualizado")
	private LocalDateTime actualizado;
}
