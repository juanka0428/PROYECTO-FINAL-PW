package co.edu.usco.pw.tienda_cerca.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import co.edu.usco.pw.tienda_cerca.service.AvisoService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Value("${app.front}")
	private String front;

	@Autowired
	private AvisoService avisoService;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(avisoService, "/ws").setAllowedOrigins(front.split("\\s*,\\s*"));
	}
}
