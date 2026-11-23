package co.edu.usco.pw.tienda_cerca.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.usco.pw.tienda_cerca.config.I18nConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/i18n")
@Tag(name = "Idiomas (i18n)", description = "Textos de la aplicación en español, inglés y portugués")
public class I18nController {

	@Operation(summary = "Textos de un idioma", description = "Público. Ejemplo: /api/i18n?lang=pt")
	@GetMapping
	public Map<String, String> textos(
			@Parameter(description = "es, en o pt") @org.springframework.web.bind.annotation.RequestParam(required = false) String lang)
			throws IOException {
		Locale locale = LocaleContextHolder.getLocale();
		String idioma = Arrays.asList(I18nConfig.IDIOMAS).contains(locale.getLanguage()) ? locale.getLanguage() : "es";
		Properties archivo = new Properties();
		try (Reader lector = new InputStreamReader(
				new ClassPathResource("i18n/messages_" + idioma + ".properties").getInputStream(), StandardCharsets.UTF_8)) {
			archivo.load(lector);
		}
		Map<String, String> textos = new TreeMap<>();
		archivo.forEach((k, v) -> textos.put(k.toString(), v.toString()));
		textos.put("idioma", idioma);
		return textos;
	}

	@Operation(summary = "Idiomas disponibles")
	@GetMapping("/idiomas")
	public String[] idiomas() {
		return I18nConfig.IDIOMAS;
	}
}
