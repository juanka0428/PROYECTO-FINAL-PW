package co.edu.usco.pw.tienda_cerca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info().title("Agropita - API REST").version("1.0")
						.description("Servicios web del proyecto final de Programación Web (USCO): pedidos, "
								+ "domicilios, fiado digital, pedidos recurrentes, chat y reportes para tiendas de barrio.")
						.contact(new Contact().name("Juan Camilo Mayor Escobar")))
				.components(new Components().addSecuritySchemes("jwt",
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList("jwt"));
	}
}
