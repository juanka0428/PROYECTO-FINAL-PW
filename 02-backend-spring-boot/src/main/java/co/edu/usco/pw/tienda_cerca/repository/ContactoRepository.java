package co.edu.usco.pw.tienda_cerca.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ContactoRepository {

	@Autowired
	@Qualifier("mysqlJdbc")
	private JdbcTemplate jdbc;

	public void guardarContacto(String nombre, String tienda, String telefono) {
		jdbc.update("INSERT INTO contactos (nombre, tienda, telefono, fecha) VALUES (?, ?, ?, ?)", nombre, tienda,
				telefono, Timestamp.valueOf(LocalDateTime.now()));
	}

	public List<Map<String, Object>> contactos() {
		return jdbc.queryForList("SELECT id, nombre, tienda, telefono, fecha FROM contactos ORDER BY fecha DESC");
	}

	public void auditarAcceso(String correo, boolean exito, String ip) {
		try {
			jdbc.update("INSERT INTO auditoria_accesos (correo, exito, ip, fecha) VALUES (?, ?, ?, ?)", correo, exito,
					ip, Timestamp.valueOf(LocalDateTime.now()));
		} catch (Exception e) {
			System.out.println("No se pudo guardar la auditoria en MySQL: " + e.getMessage());
		}
	}

	public List<Map<String, Object>> accesos(int limite) {
		return jdbc.queryForList(
				"SELECT correo, exito, ip, fecha FROM auditoria_accesos ORDER BY fecha DESC LIMIT ?", limite);
	}
}
