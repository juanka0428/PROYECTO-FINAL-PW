package co.edu.usco.pw.tienda_cerca.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.usco.pw.tienda_cerca.dto.Peticiones;
import co.edu.usco.pw.tienda_cerca.dto.Sesion;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.repository.ContactoRepository;
import co.edu.usco.pw.tienda_cerca.repository.UsuarioRepository;
import co.edu.usco.pw.tienda_cerca.security.JwtUtil;

@Service
public class UsuarioService {

	private static final double TIENDA_LAT = 2.9345;
	private static final double TIENDA_LNG = -75.2809;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ContactoRepository contactoRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AvisoService avisoService;

	public Sesion login(Peticiones.Login datos, String ip) {
		String correo = datos.correo().trim().toLowerCase();
		Usuario u = usuarioRepository.findByCorreo(correo).orElse(null);
		if (u == null || !passwordEncoder.matches(datos.clave(), u.getClave())) {
			contactoRepository.auditarAcceso(correo, false, ip);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "error.login.credenciales");
		}
		if (!Boolean.TRUE.equals(u.getActivo())) {
			contactoRepository.auditarAcceso(correo, false, ip);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "error.login.inactivo");
		}
		contactoRepository.auditarAcceso(correo, true, ip);
		return new Sesion(jwtUtil.generateToken(u), u);
	}

	@Transactional
	public Sesion registrar(Peticiones.Registro datos) {
		String correo = datos.correo().trim().toLowerCase();
		if (usuarioRepository.existsByCorreo(correo)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "error.registro.correoExiste");
		}
		Usuario u = nuevoCliente(datos.nombre(), correo);
		u.setClave(passwordEncoder.encode(datos.clave()));
		u.setTelefono(datos.telefono());
		u.setDireccion(datos.direccion());
		u.setBarrio(datos.barrio());
		usuarioRepository.save(u);
		return new Sesion(jwtUtil.generateToken(u), u);
	}

	@Transactional
	public Usuario buscarOCrearDesdeGoogle(String correo, String nombre) {
		String c = correo.trim().toLowerCase();
		return usuarioRepository.findByCorreo(c).orElseGet(() -> {
			Usuario u = nuevoCliente(nombre == null ? c : nombre, c);
			u.setClave(passwordEncoder.encode(UUID.randomUUID().toString()));
			u.setProveedor("GOOGLE");
			return usuarioRepository.save(u);
		});
	}

	public List<Usuario> porRol(String rol) {
		return usuarioRepository.findByRolOrderByNombre(rol);
	}

	public Usuario buscarCliente(Long id) {
		Usuario c = usuarioRepository.findById(id).orElse(null);
		if (c == null || !"CLIENTE".equals(c.getRol())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.cliente.noEncontrado");
		}
		return c;
	}

	@Transactional
	public Usuario cambiarCupo(Long id, Integer cupo) {
		Usuario c = buscarCliente(id);
		if (cupo < c.getSaldoFiado()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"error.fiado.cupoMenor");
		}
		c.setCupoFiado(cupo);
		avisoService.cambio("fiado");
		return usuarioRepository.save(c);
	}

	@Transactional
	public Usuario alternarActivo(Long id) {
		Usuario c = buscarCliente(id);
		c.setActivo(!Boolean.TRUE.equals(c.getActivo()));
		return usuarioRepository.save(c);
	}

	public Usuario recargar(Long id) {
		return usuarioRepository.findById(id).orElseThrow();
	}

	private Usuario nuevoCliente(String nombre, String correo) {
		Usuario u = new Usuario();
		u.setNombre(nombre);
		u.setCorreo(correo);
		u.setLat(TIENDA_LAT + (Math.random() - 0.5) * 0.02);
		u.setLng(TIENDA_LNG + (Math.random() - 0.5) * 0.02);
		u.setRol("CLIENTE");
		u.setCupoFiado(0);
		u.setSaldoFiado(0);
		return u;
	}
}
