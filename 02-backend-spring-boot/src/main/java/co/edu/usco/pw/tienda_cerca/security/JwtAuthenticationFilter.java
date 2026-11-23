package co.edu.usco.pw.tienda_cerca.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			String token = authorizationHeader.substring(7);
			if (jwtUtil.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
				String correo = jwtUtil.getUsernameFromToken(token);
				Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
				if (usuario != null && Boolean.TRUE.equals(usuario.getActivo())) {
					var permisos = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()));
					var authentication = new UsernamePasswordAuthenticationToken(usuario, null, permisos);
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		}

		filterChain.doFilter(request, response);
	}
}
