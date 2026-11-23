package co.edu.usco.pw.tienda_cerca.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.usco.pw.tienda_cerca.repository.ContactoRepository;
import co.edu.usco.pw.tienda_cerca.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Ventas, inventario, contactos y auditoría (JSON, PDF y Excel)")
public class ReporteController {

	@Autowired
	private ReporteService reporteService;

	@Autowired
	private ContactoRepository contactoRepository;

	@Operation(summary = "Reporte de ventas (JSON)", description = "Consultas SQL con JDBC (GROUP BY, SUM, COUNT) sobre PostgreSQL.")
	@GetMapping("/ventas")
	public Map<String, Object> ventas(
			@Parameter(description = "Días hacia atrás (7, 30 o 90)") @RequestParam(defaultValue = "7") int dias) {
		return reporteService.ventas(dias);
	}

	@Operation(summary = "Descargar reporte de ventas en PDF")
	@GetMapping("/ventas/pdf")
	public ResponseEntity<byte[]> ventasPdf(@RequestParam(defaultValue = "7") int dias) {
		return archivo(reporteService.ventasPdf(dias), "pdf", MediaType.APPLICATION_PDF);
	}

	@Operation(summary = "Descargar reporte de ventas en Excel")
	@GetMapping("/ventas/excel")
	public ResponseEntity<byte[]> ventasExcel(@RequestParam(defaultValue = "7") int dias) throws IOException {
		return archivo(reporteService.ventasExcel(dias), "xlsx", MediaType
				.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	}

	@Operation(summary = "Productos con poco inventario")
	@GetMapping("/inventario-bajo")
	public List<Map<String, Object>> inventarioBajo() {
		return reporteService.inventarioBajo();
	}

	@Operation(summary = "Contactos recibidos (MySQL)")
	@GetMapping("/contactos")
	public List<Map<String, Object>> contactos() {
		return contactoRepository.contactos();
	}

	@Operation(summary = "Auditoría de inicios de sesión (MySQL)")
	@GetMapping("/accesos")
	public List<Map<String, Object>> accesos() {
		return contactoRepository.accesos(100);
	}

	private ResponseEntity<byte[]> archivo(byte[] datos, String extension, MediaType tipo) {
		String nombre = "reporte-ventas-" + LocalDate.now() + "." + extension;
		return ResponseEntity.ok().contentType(tipo)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"").body(datos);
	}
}
