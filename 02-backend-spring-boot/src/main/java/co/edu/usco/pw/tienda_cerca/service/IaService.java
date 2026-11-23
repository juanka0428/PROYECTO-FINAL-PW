package co.edu.usco.pw.tienda_cerca.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class IaService {

	private static final Set<String> TIPOS = Set.of("image/jpeg", "image/png", "image/webp", "image/heic");

	private static final String INSTRUCCION = """
			Eres el asistente de una tienda de barrio en Colombia. La imagen es una lista de mercado
			(escrita a mano o impresa) o una formula medica. Escribe solo los productos con su cantidad,
			separados por comas, en espanol y en minusculas, por ejemplo: "2 leches, 1 arroz, 1 panela".
			Si no hay cantidad escribe 1. No agregues explicaciones. Si la imagen no es una lista
			de productos responde exactamente: NO_ES_LISTA
			""";

	@Value("${app.ia.gemini-key:}")
	private String clave;

	@Value("${app.ia.modelo:gemini-2.5-flash}")
	private String modelo;

	@Value("${app.ia.url:https://generativelanguage.googleapis.com/v1beta/models/}")
	private String url;

	private final RestClient http = RestClient.create();

	public boolean disponible() {
		return clave != null && !clave.isBlank();
	}

	public String leerFoto(MultipartFile foto) {
		if (!disponible()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "error.ia.noConfigurada");
		}
		if (foto == null || foto.isEmpty() || foto.getContentType() == null || !TIPOS.contains(foto.getContentType())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.imagen.tipo");
		}
		try {
			String base64 = Base64.getEncoder().encodeToString(foto.getBytes());
			Map<String, Object> cuerpo = Map.of("contents", List.of(Map.of("parts", List.of(
					Map.of("text", INSTRUCCION),
					Map.of("inline_data", Map.of("mime_type", foto.getContentType(), "data", base64))))),
					"generationConfig", Map.of("temperature", 0));
			JsonNode respuesta = http.post().uri(url + modelo + ":generateContent")
					.header("x-goog-api-key", clave)
					.contentType(MediaType.APPLICATION_JSON).body(cuerpo).retrieve().body(JsonNode.class);
			String texto = respuesta.path("candidates").path(0).path("content").path("parts").path(0).path("text")
					.asText("").trim();
			if (texto.isEmpty() || texto.contains("NO_ES_LISTA")) {
				throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
						"error.ia.noEsLista");
			}
			return texto;
		} catch (ResponseStatusException e) {
			throw e;
		} catch (RestClientException | java.io.IOException e) {
			System.out.println("Error con la IA: " + e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "error.ia.noResponde");
		}
	}
}
