package co.edu.usco.pw.tienda_cerca.service;

import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import co.edu.usco.pw.tienda_cerca.model.Pedido;
import co.edu.usco.pw.tienda_cerca.model.Producto;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.repository.ProductoRepository;

@Service
public class AsistenteService {

	public record Respuesta(String texto, boolean escalar) {
	}

	private static final List<Long> PRIORIDAD_CANASTA = List.of(7L, 1L, 2L, 8L, 4L, 13L, 17L, 19L, 18L, 10L, 9L, 11L,
			5L, 3L);

	private static final Map<String, Integer> NUMEROS = Map.ofEntries(Map.entry("un", 1), Map.entry("una", 1),
			Map.entry("uno", 1), Map.entry("dos", 2), Map.entry("tres", 3), Map.entry("cuatro", 4),
			Map.entry("cinco", 5), Map.entry("seis", 6), Map.entry("siete", 7), Map.entry("ocho", 8),
			Map.entry("nueve", 9), Map.entry("diez", 10), Map.entry("one", 1), Map.entry("two", 2),
			Map.entry("three", 3), Map.entry("four", 4), Map.entry("five", 5));

	@Autowired
	private MessageSource mensajes;

	@Autowired
	private PedidoService pedidoService;

	@Autowired
	private ProductoRepository productoRepository;

	public Respuesta responder(String texto, Usuario cliente) {
		String m = normalizar(texto);

		if (busca(m, "asesor|persona|humano|alguien real|agente|queja|reclamo|advisor|human|person|complaint")) {
			return new Respuesta(t("asistente.escalar"), true);
		}
		if (m.matches("^(hola|buenas|buenos|buen dia|hello|hi|hey)\\b.*")) {
			return new Respuesta(t("asistente.saludo", cliente.getNombre().split(" ")[0]), false);
		}
		if (busca(m, "horario|\\bhora\\b|abren|cierran|abierto|open|hours")) {
			return new Respuesta(t("asistente.horario", t("asistente.horarioTienda")), false);
		}
		if (busca(m, "domicilio|envio|delivery") && busca(m, "cuanto|vale|cuesta|precio|cost|how much")) {
			return new Respuesta(t("asistente.domicilio", pesos(PedidoService.DOMICILIO),
					pesos(PedidoService.DOMICILIO_GRATIS_DESDE)), false);
		}
		if (busca(m, "fiado|debo|deuda|saldo|cupo|tab|owe|credit")) {
			int saldo = cliente.getSaldoFiado() == null ? 0 : cliente.getSaldoFiado();
			int cupo = cliente.getCupoFiado() == null ? 0 : cliente.getCupoFiado();
			return new Respuesta(t("asistente.fiado", pesos(saldo), pesos(cupo - saldo)), false);
		}
		if (busca(m, "mi pedido|donde va|llega|demora|estado|my order|where is")) {
			List<Pedido> pedidos = pedidoService.deCliente(cliente.getId());
			if (pedidos.isEmpty()) {
				return new Respuesta(t("asistente.sinPedidos"), false);
			}
			Pedido p = pedidos.get(0);
			return new Respuesta(t("asistente.pedido", p.getCodigo(), t("estados." + p.getEstado()).toLowerCase()), false);
		}
		Integer presupuesto = leerPresupuesto(m);
		if (presupuesto != null) {
			List<Producto> canasta = canastaPorPresupuesto(presupuesto);
			int total = canasta.stream().mapToInt(Producto::getPrecio).sum();
			String lista = String.join(", ", canasta.stream().map(Producto::getNombre).toList());
			return new Respuesta(t("asistente.presupuesto", pesos(presupuesto), lista, pesos(total)), false);
		}
		Map<Producto, Integer> productos = reconocerProductos(m);
		if (!productos.isEmpty()) {
			List<String> partes = new ArrayList<>();
			productos.forEach((p, c) -> partes.add(c + " × " + p.getNombre()));
			return new Respuesta(t("asistente.productos", String.join(", ", partes)), false);
		}
		return new Respuesta(t("asistente.noEntiendo"), false);
	}

	public String textoEscalar() {
		return t("asistente.escalar");
	}

	public String saludoAsesor(String nombre) {
		return t("asistente.asesor", nombre.split(" ")[0]);
	}

	private String t(String clave, Object... datos) {
		return mensajes.getMessage(clave, datos.length == 0 ? null : datos, clave, LocaleContextHolder.getLocale());
	}

	List<Producto> canastaPorPresupuesto(int presupuesto) {
		List<Producto> resultado = new ArrayList<>();
		int total = 0;
		for (Long id : PRIORIDAD_CANASTA) {
			Producto p = productoRepository.findById(id).orElse(null);
			if (p != null && Boolean.TRUE.equals(p.getActivo()) && p.getStock() > 0 && total + p.getPrecio() <= presupuesto) {
				resultado.add(p);
				total += p.getPrecio();
			}
		}
		return resultado;
	}

	Map<Producto, Integer> reconocerProductos(String m) {
		List<Producto> activos = productoRepository.findByActivoTrueOrderByNombre();
		Map<Producto, Integer> resultado = new LinkedHashMap<>();
		for (String trozo : m.split(",|;|\\.|\\by\\b|\\bcon\\b|\\bmas\\b|\\band\\b")) {
			String tr = trozo.trim();
			if (tr.isEmpty()) {
				continue;
			}
			String[] palabras = tr.split("\\s+");
			for (Producto p : activos) {
				if (coincide(p, palabras)) {
					resultado.merge(p, cantidad(palabras), Integer::sum);
					break;
				}
			}
		}
		return resultado;
	}

	private boolean coincide(Producto p, String[] palabras) {
		for (String clave : p.getPalabrasClave()) {
			String c = normalizar(clave);
			for (String w : palabras) {
				if (w.equals(c) || w.equals(c + "s") || w.equals(c + "es") || w.replaceAll("s$", "").equals(c)) {
					return true;
				}
			}
		}
		return false;
	}

	private int cantidad(String[] palabras) {
		for (String w : palabras) {
			if (w.matches("\\d{1,2}")) {
				return Math.max(1, Integer.parseInt(w));
			}
			if (NUMEROS.containsKey(w)) {
				return NUMEROS.get(w);
			}
		}
		return 1;
	}

	private Integer leerPresupuesto(String m) {
		if (!busca(m, "tengo|presupuesto|alcanza|budget|mil|\\$")) {
			return null;
		}
		Matcher mil = Pattern.compile("(\\d+)\\s*mil").matcher(m);
		if (mil.find()) {
			return Integer.parseInt(mil.group(1)) * 1000;
		}
		Matcher num = Pattern.compile("\\d{4,7}").matcher(m.replaceAll("(\\d)[.,](\\d{3})", "$1$2"));
		return num.find() ? Integer.parseInt(num.group()) : null;
	}

	private static boolean busca(String texto, String patron) {
		return Pattern.compile(patron).matcher(texto).find();
	}

	private static String normalizar(String s) {
		return Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "")
				.replaceAll("[¿?¡!\"()]", " ").replaceAll("\\s+", " ").trim();
	}

	private static String pesos(int valor) {
		return "$ " + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO")).format(valor);
	}
}
