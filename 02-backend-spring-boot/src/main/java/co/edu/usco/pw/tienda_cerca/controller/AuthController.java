package co.edu.usco.pw.tienda_cerca.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import co.edu.usco.pw.tienda_cerca.dto.Peticiones;
import co.edu.usco.pw.tienda_cerca.dto.Sesion;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login con JWT, registro y Google OAuth")
public class AuthController {

	@Autowired
	private UsuarioService usuarioService;

	@Operation(summary = "Iniciar sesión", description = "Valida el correo y la clave (BCrypt) y devuelve un token JWT. "
			+ "Usuarios de prueba: maria@correo.com, tendero@tienda.com, repartidor@tienda.com, asesor@tienda.com (clave 123456).")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Sesión iniciada"),
			@ApiResponse(responseCode = "401", description = "error.login.credenciales"),
			@ApiResponse(responseCode = "403", description = "Cuenta desactivada") })
	@PostMapping("/login")
	public Sesion login(@Valid @RequestBody Peticiones.Login datos, HttpServletRequest request) {
		return usuarioService.login(datos, request.getRemoteAddr());
	}

	@Operation(summary = "Registrar un cliente", description = "Crea una cuenta con rol CLIENTE. La clave se guarda encriptada con BCrypt.")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Cuenta creada"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "409", description = "error.registro.correoExiste") })
	@PostMapping("/registro")
	@ResponseStatus(HttpStatus.CREATED)
	public Sesion registro(@Valid @RequestBody Peticiones.Registro datos) {
		return usuarioService.registrar(datos);
	}

	@Operation(summary = "Ver mi usuario", description = "Devuelve los datos del usuario dueño del token.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Datos del usuario"),
			@ApiResponse(responseCode = "401", description = "Sin token o token vencido") })
	@GetMapping("/yo")
	public Usuario yo(@AuthenticationPrincipal Usuario yo) {
		if (yo == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "error.sinSesion");
		}
		return usuarioService.recargar(yo.getId());
	}
}
