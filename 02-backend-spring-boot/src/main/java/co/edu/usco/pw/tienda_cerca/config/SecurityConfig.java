package co.edu.usco.pw.tienda_cerca.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import co.edu.usco.pw.tienda_cerca.security.JwtAuthenticationFilter;
import co.edu.usco.pw.tienda_cerca.security.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Value("${app.front}")
	private String front;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
			OAuth2LoginSuccessHandler oauth2LoginSuccessHandler) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(cors -> {
				})
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/login", "/api/auth/registro", "/api/contacto", "/api/i18n/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**", "/api/imagenes/**").permitAll()
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/oauth2/**", "/login/oauth2/**", "/error", "/ws/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/clientes").hasAnyRole("TENDERO", "ASESOR")
						.requestMatchers("/api/productos/**", "/api/clientes/**", "/api/reportes/**",
								"/api/repartidores/**")
						.hasRole("TENDERO")
						.requestMatchers("/api/recurrentes/**", "/api/ia/foto").hasRole("CLIENTE")
						.anyRequest().authenticated())
				.oauth2Login(oauth2 -> oauth2.successHandler(oauth2LoginSuccessHandler)
						.failureHandler((req, res, ex) -> res.sendRedirect(front.split(",")[0].trim() + "/#/login?error=google")));

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(front.split("\\s*,\\s*")));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Content-Disposition"));
		UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
		fuente.registerCorsConfiguration("/**", config);
		return fuente;
	}
}
