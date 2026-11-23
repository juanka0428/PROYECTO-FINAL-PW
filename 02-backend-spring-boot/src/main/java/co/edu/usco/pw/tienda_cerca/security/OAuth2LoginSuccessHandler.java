package co.edu.usco.pw.tienda_cerca.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private JwtUtil jwtUtil;

	@Value("${app.front}")
	private String fronts;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		String front = fronts.split(",")[0].trim();
		OAuth2User google = (OAuth2User) authentication.getPrincipal();
		String correo = google.getAttribute("email");
		String nombre = google.getAttribute("name");

		Usuario usuario = usuarioService.buscarOCrearDesdeGoogle(correo, nombre);
		if (!Boolean.TRUE.equals(usuario.getActivo())) {
			response.sendRedirect(front + "/#/login?error=inactivo");
			return;
		}
		String token = jwtUtil.generateToken(usuario);
		if (request.getSession(false) != null) {
			request.getSession(false).invalidate();
		}
		response.sendRedirect(front + "/#/auth/google?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8));
	}
}
