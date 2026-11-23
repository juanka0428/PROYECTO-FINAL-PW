package co.edu.usco.pw.tienda_cerca.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import co.edu.usco.pw.tienda_cerca.model.Conversacion;
import co.edu.usco.pw.tienda_cerca.model.Mensaje;

@Repository
public class ChatRepository {

	@Autowired
	@Qualifier("mysqlJdbc")
	private JdbcTemplate jdbc;

	public Conversacion buscar(Long id) {
		List<Conversacion> lista = jdbc.query("SELECT * FROM conversaciones WHERE id = ?", this::mapearConversacion, id);
		if (lista.isEmpty()) {
			return null;
		}
		Conversacion c = lista.get(0);
		c.setMensajes(mensajes(id));
		return c;
	}

	public Conversacion abiertaDeCliente(Long clienteId) {
		List<Long> ids = jdbc.queryForList(
				"SELECT id FROM conversaciones WHERE cliente_id = ? AND estado <> 'CERRADA' ORDER BY id DESC LIMIT 1",
				Long.class, clienteId);
		return ids.isEmpty() ? null : buscar(ids.get(0));
	}

	public List<Conversacion> todas() {
		List<Conversacion> lista = jdbc.query("SELECT * FROM conversaciones ORDER BY actualizada DESC, id DESC",
				this::mapearConversacion);
		for (Conversacion c : lista) {
			c.setMensajes(mensajes(c.getId()));
		}
		return lista;
	}

	public Long crear(Long clienteId, String clienteNombre) {
		KeyHolder llave = new GeneratedKeyHolder();
		jdbc.update(con -> {
			PreparedStatement ps = con.prepareStatement(
					"INSERT INTO conversaciones (cliente_id, cliente_nombre, estado, actualizada) VALUES (?, ?, 'IA', ?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setLong(1, clienteId);
			ps.setString(2, clienteNombre);
			ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
			return ps;
		}, llave);
		return llave.getKey().longValue();
	}

	public void agregarMensaje(Long conversacionId, String autor, String texto) {
		jdbc.update("INSERT INTO mensajes (conversacion_id, autor, texto, fecha) VALUES (?, ?, ?, ?)", conversacionId,
				autor, texto, Timestamp.valueOf(LocalDateTime.now()));
	}

	public void cambiarEstado(Long id, String estado) {
		jdbc.update("UPDATE conversaciones SET estado = ?, actualizada = ? WHERE id = ?", estado,
				Timestamp.valueOf(LocalDateTime.now()), id);
	}

	public void asignarAsesor(Long id, Long asesorId, String asesorNombre) {
		jdbc.update("UPDATE conversaciones SET estado = 'CON_ASESOR', asesor_id = ?, asesor_nombre = ? WHERE id = ?",
				asesorId, asesorNombre, id);
	}

	public int enEspera() {
		Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM conversaciones WHERE estado = 'ESPERANDO_ASESOR'",
				Integer.class);
		return n == null ? 0 : n;
	}

	private List<Mensaje> mensajes(Long conversacionId) {
		return jdbc.query("SELECT * FROM mensajes WHERE conversacion_id = ? ORDER BY fecha, id", (rs, n) -> {
			Mensaje m = new Mensaje();
			m.setId(rs.getLong("id"));
			m.setAutor(rs.getString("autor"));
			m.setTexto(rs.getString("texto"));
			m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
			return m;
		}, conversacionId);
	}

	private Conversacion mapearConversacion(ResultSet rs, int fila) throws SQLException {
		Conversacion c = new Conversacion();
		c.setId(rs.getLong("id"));
		c.setClienteId(rs.getLong("cliente_id"));
		c.setClienteNombre(rs.getString("cliente_nombre"));
		c.setEstado(rs.getString("estado"));
		long asesor = rs.getLong("asesor_id");
		c.setAsesorId(rs.wasNull() ? null : asesor);
		c.setAsesorNombre(rs.getString("asesor_nombre"));
		c.setActualizada(rs.getTimestamp("actualizada").toLocalDateTime());
		return c;
	}
}
