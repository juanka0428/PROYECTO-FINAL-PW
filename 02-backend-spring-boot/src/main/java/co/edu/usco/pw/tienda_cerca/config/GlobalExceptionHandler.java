package co.edu.usco.pw.tienda_cerca.config;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Map<String, String> TRIGGERS = new LinkedHashMap<>();
	static {
		TRIGGERS.put("El pedido ya terminó", "trigger.pedidoTerminado");
		TRIGGERS.put("No se puede cancelar un pedido que ya fue pagado", "trigger.cancelarPagado");
		TRIGGERS.put("No hay suficientes unidades de ", "trigger.sinStock");
		TRIGGERS.put("El valor debe ser mayor que cero", "trigger.valorCero");
		TRIGGERS.put("El pedido supera el cupo", "trigger.cupo");
		TRIGGERS.put("El abono es mayor", "trigger.abono");
		TRIGGERS.put("La conversación está cerrada", "trigger.chatCerrado");
	}

	@Autowired
	private MessageSource mensajes;

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, String>> errorPropio(ResponseStatusException e) {
		return ResponseEntity.status(e.getStatusCode()).body(Map.of("mensaje", traducir(e.getReason())));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> datosInvalidos(MethodArgumentNotValidException e) {
		var campo = e.getBindingResult().getFieldErrors().get(0);
		return ResponseEntity.badRequest()
				.body(Map.of("mensaje", traducir("error.campo|" + campo.getField() + "|" + campo.getDefaultMessage())));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, String>> archivoGrande(MaxUploadSizeExceededException e) {
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("mensaje", traducir("error.imagen.grande")));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException e) {
		return ResponseEntity.badRequest().body(Map.of("mensaje", traducir("error.json")));
	}

	@ExceptionHandler(AuthorizationDeniedException.class)
	public ResponseEntity<Map<String, String>> sinPermiso(AuthorizationDeniedException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", traducir("error.sinPermiso")));
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<Map<String, String>> errorBaseDatos(DataAccessException e) {
		Throwable causa = e;
		while (causa != null) {
			if (causa instanceof SQLException sql
					&& ("P0001".equals(sql.getSQLState()) || "45000".equals(sql.getSQLState()))) {
				return ResponseEntity.badRequest().body(Map.of("mensaje", mensajeTrigger(limpiar(sql.getMessage()))));
			}
			causa = causa.getCause();
		}
		e.printStackTrace();
		return ResponseEntity.internalServerError().body(Map.of("mensaje", traducir("error.baseDatos")));
	}

	private String mensajeTrigger(String texto) {
		for (Map.Entry<String, String> t : TRIGGERS.entrySet()) {
			if (texto.startsWith(t.getKey())) {
				String dato = texto.substring(t.getKey().length()).trim();
				return traducir(t.getValue() + "|" + dato);
			}
		}
		return texto;
	}

	private String traducir(String texto) {
		if (texto == null) {
			return "";
		}
		String[] partes = texto.split("\\|");
		Object[] datos = new Object[partes.length - 1];
		for (int i = 1; i < partes.length; i++) {
			datos[i - 1] = mensajes.getMessage("estados." + partes[i], null, partes[i], LocaleContextHolder.getLocale());
		}
		return mensajes.getMessage(partes[0], datos.length == 0 ? null : datos, texto, LocaleContextHolder.getLocale());
	}

	private String limpiar(String mensaje) {
		if (mensaje == null) {
			return "";
		}
		String m = mensaje.startsWith("ERROR: ") ? mensaje.substring(7) : mensaje;
		int salto = m.indexOf('\n');
		return salto > 0 ? m.substring(0, salto).trim() : m.trim();
	}
}
