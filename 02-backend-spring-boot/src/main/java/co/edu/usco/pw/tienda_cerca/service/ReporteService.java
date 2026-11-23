package co.edu.usco.pw.tienda_cerca.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReporteService {

	private static final String VERDE = "035A2C";

	@Autowired
	private JdbcTemplate jdbc;

	public Map<String, Object> ventas(int dias) {
		int d = Math.max(1, Math.min(365, dias)) - 1;
		String desde = " fecha >= CURRENT_DATE - CAST(? AS INTEGER) AND estado <> 'CANCELADO' ";

		Map<String, Object> resumen = jdbc.queryForMap(
				"SELECT COUNT(*) AS pedidos, COALESCE(SUM(total), 0) AS ventas, "
						+ "COALESCE(SUM(CASE WHEN metodo_pago = 'FIADO' THEN total ELSE 0 END), 0) AS fiado, "
						+ "COALESCE(ROUND(AVG(total)), 0) AS promedio FROM pedidos WHERE" + desde, d);

		List<Map<String, Object>> porDia = jdbc.queryForList(
				"SELECT TO_CHAR(fecha, 'YYYY-MM-DD') AS dia, COUNT(*) AS pedidos, SUM(total) AS valor "
						+ "FROM pedidos WHERE" + desde + "GROUP BY dia ORDER BY dia", d);

		List<Map<String, Object>> masVendidos = jdbc.queryForList(
				"SELECT i.nombre, SUM(i.cantidad) AS unidades, SUM(i.cantidad * i.precio) AS valor "
						+ "FROM items_pedido i JOIN pedidos p ON p.id = i.pedido_id "
						+ "WHERE p.fecha >= CURRENT_DATE - CAST(? AS INTEGER) AND p.estado <> 'CANCELADO' "
						+ "GROUP BY i.nombre ORDER BY valor DESC LIMIT 8", d);

		List<Map<String, Object>> porPago = jdbc.queryForList("SELECT metodo_pago AS tipo, COUNT(*) AS pedidos, "
				+ "SUM(total) AS valor FROM pedidos WHERE" + desde + "GROUP BY metodo_pago ORDER BY valor DESC", d);

		List<Map<String, Object>> porOrigen = jdbc.queryForList("SELECT origen AS tipo, COUNT(*) AS pedidos, "
				+ "SUM(total) AS valor FROM pedidos WHERE" + desde + "GROUP BY origen ORDER BY valor DESC", d);

		Map<String, Object> respuesta = new LinkedHashMap<>();
		respuesta.put("dias", d + 1);
		respuesta.put("resumen", resumen);
		respuesta.put("porDia", porDia);
		respuesta.put("masVendidos", masVendidos);
		respuesta.put("porPago", porPago);
		respuesta.put("porOrigen", porOrigen);
		return respuesta;
	}

	public List<Map<String, Object>> inventarioBajo() {
		return jdbc.queryForList("SELECT id, nombre, stock, stock_minimo FROM productos "
				+ "WHERE activo = TRUE AND stock <= stock_minimo ORDER BY stock");
	}

	@SuppressWarnings("unchecked")
	public byte[] ventasPdf(int dias) {
		Map<String, Object> datos = ventas(dias);
		Map<String, Object> r = (Map<String, Object>) datos.get("resumen");
		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		Document doc = new Document(PageSize.LETTER, 50, 50, 50, 50);
		PdfWriter.getInstance(doc, salida);
		doc.open();

		Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(0x03, 0x5A, 0x2C));
		Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
		Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

		doc.add(new Paragraph("Agropita - Reporte de ventas", titulo));
		doc.add(new Paragraph("Últimos " + datos.get("dias") + " días · generado el "
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normal));
		doc.add(new Paragraph(" "));

		PdfPTable resumen = tabla(new String[] { "Pedidos", "Ventas", "Fiado", "Ticket promedio" });
		celdas(resumen, normal, String.valueOf(r.get("pedidos")), pesos(r.get("ventas")), pesos(r.get("fiado")),
				pesos(r.get("promedio")));
		doc.add(resumen);

		doc.add(new Paragraph("Ventas por día", subtitulo));
		PdfPTable porDia = tabla(new String[] { "Día", "Pedidos", "Valor" });
		for (Map<String, Object> f : (List<Map<String, Object>>) datos.get("porDia")) {
			celdas(porDia, normal, String.valueOf(f.get("dia")), String.valueOf(f.get("pedidos")), pesos(f.get("valor")));
		}
		doc.add(porDia);

		doc.add(new Paragraph("Productos más vendidos", subtitulo));
		PdfPTable mas = tabla(new String[] { "Producto", "Unidades", "Valor" });
		for (Map<String, Object> f : (List<Map<String, Object>>) datos.get("masVendidos")) {
			celdas(mas, normal, String.valueOf(f.get("nombre")), String.valueOf(f.get("unidades")), pesos(f.get("valor")));
		}
		doc.add(mas);

		doc.add(new Paragraph("Por forma de pago", subtitulo));
		PdfPTable pago = tabla(new String[] { "Forma de pago", "Pedidos", "Valor" });
		for (Map<String, Object> f : (List<Map<String, Object>>) datos.get("porPago")) {
			celdas(pago, normal, String.valueOf(f.get("tipo")), String.valueOf(f.get("pedidos")), pesos(f.get("valor")));
		}
		doc.add(pago);

		doc.add(new Paragraph("Productos por agotarse", subtitulo));
		PdfPTable bajo = tabla(new String[] { "Producto", "Stock", "Mínimo" });
		for (Map<String, Object> f : inventarioBajo()) {
			celdas(bajo, normal, String.valueOf(f.get("nombre")), String.valueOf(f.get("stock")),
					String.valueOf(f.get("stock_minimo")));
		}
		doc.add(bajo);

		doc.close();
		return salida.toByteArray();
	}

	@SuppressWarnings("unchecked")
	public byte[] ventasExcel(int dias) throws IOException {
		Map<String, Object> datos = ventas(dias);
		Map<String, Object> r = (Map<String, Object>) datos.get("resumen");
		try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
			CellStyle encabezado = libro.createCellStyle();
			var fuente = libro.createFont();
			fuente.setBold(true);
			fuente.setColor(IndexedColors.WHITE.getIndex());
			encabezado.setFont(fuente);
			encabezado.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
			encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			encabezado.setBorderBottom(BorderStyle.THIN);
			CellStyle dinero = libro.createCellStyle();
			dinero.setDataFormat(libro.createDataFormat().getFormat("\"$\" #,##0"));

			Sheet hoja = libro.createSheet("Resumen");
			fila(hoja, 0, encabezado, "Últimos días", "Pedidos", "Ventas", "Fiado", "Ticket promedio");
			Row f1 = hoja.createRow(1);
			f1.createCell(0).setCellValue(((Number) datos.get("dias")).doubleValue());
			f1.createCell(1).setCellValue(num(r.get("pedidos")));
			celdaDinero(f1, 2, r.get("ventas"), dinero);
			celdaDinero(f1, 3, r.get("fiado"), dinero);
			celdaDinero(f1, 4, r.get("promedio"), dinero);
			ajustar(hoja, 5);

			hojaTabla(libro, "Ventas por día", encabezado, dinero, (List<Map<String, Object>>) datos.get("porDia"),
					new String[] { "Día", "Pedidos", "Valor" }, new String[] { "dia", "pedidos", "valor" });
			hojaTabla(libro, "Más vendidos", encabezado, dinero, (List<Map<String, Object>>) datos.get("masVendidos"),
					new String[] { "Producto", "Unidades", "Valor" }, new String[] { "nombre", "unidades", "valor" });
			hojaTabla(libro, "Forma de pago", encabezado, dinero, (List<Map<String, Object>>) datos.get("porPago"),
					new String[] { "Forma de pago", "Pedidos", "Valor" }, new String[] { "tipo", "pedidos", "valor" });
			hojaTabla(libro, "Origen", encabezado, dinero, (List<Map<String, Object>>) datos.get("porOrigen"),
					new String[] { "Origen", "Pedidos", "Valor" }, new String[] { "tipo", "pedidos", "valor" });
			hojaTabla(libro, "Por agotarse", encabezado, dinero, inventarioBajo(),
					new String[] { "Producto", "Stock", "Mínimo" }, new String[] { "nombre", "stock", "stock_minimo" });

			libro.write(salida);
			return salida.toByteArray();
		}
	}

	private void hojaTabla(XSSFWorkbook libro, String nombre, CellStyle encabezado, CellStyle dinero,
			List<Map<String, Object>> filas, String[] titulos, String[] columnas) {
		Sheet hoja = libro.createSheet(nombre);
		fila(hoja, 0, encabezado, titulos);
		int i = 1;
		for (Map<String, Object> f : filas) {
			Row row = hoja.createRow(i++);
			for (int c = 0; c < columnas.length; c++) {
				Object v = f.get(columnas[c]);
				if (v instanceof Number) {
					if (columnas[c].equals("valor")) {
						celdaDinero(row, c, v, dinero);
					} else {
						row.createCell(c).setCellValue(num(v));
					}
				} else {
					row.createCell(c).setCellValue(String.valueOf(v));
				}
			}
		}
		ajustar(hoja, titulos.length);
	}

	private void fila(Sheet hoja, int n, CellStyle estilo, String... textos) {
		Row row = hoja.createRow(n);
		for (int i = 0; i < textos.length; i++) {
			var celda = row.createCell(i);
			celda.setCellValue(textos[i]);
			celda.setCellStyle(estilo);
		}
	}

	private void celdaDinero(Row row, int c, Object v, CellStyle dinero) {
		var celda = row.createCell(c);
		celda.setCellValue(num(v));
		celda.setCellStyle(dinero);
	}

	private void ajustar(Sheet hoja, int columnas) {
		for (int i = 0; i < columnas; i++) {
			hoja.setColumnWidth(i, 22 * 256);
		}
	}

	private PdfPTable tabla(String[] titulos) {
		PdfPTable t = new PdfPTable(titulos.length);
		t.setWidthPercentage(100);
		t.setSpacingBefore(6);
		t.setSpacingAfter(14);
		Font blanca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
		for (String s : titulos) {
			PdfPCell c = new PdfPCell(new Phrase(s, blanca));
			c.setBackgroundColor(Color.decode("#" + VERDE));
			c.setPadding(5);
			t.addCell(c);
		}
		t.setHeaderRows(1);
		return t;
	}

	private void celdas(PdfPTable t, Font f, String... valores) {
		for (int i = 0; i < valores.length; i++) {
			PdfPCell c = new PdfPCell(new Phrase(valores[i], f));
			c.setPadding(4);
			if (i > 0) {
				c.setHorizontalAlignment(Element.ALIGN_RIGHT);
			}
			t.addCell(c);
		}
	}

	private static double num(Object v) {
		return v == null ? 0 : ((Number) v).doubleValue();
	}

	private static String pesos(Object v) {
		return "$ " + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO")).format(num(v));
	}
}
