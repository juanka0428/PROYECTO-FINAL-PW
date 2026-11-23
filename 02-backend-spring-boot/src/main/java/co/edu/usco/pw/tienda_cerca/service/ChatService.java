package co.edu.usco.pw.tienda_cerca.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.usco.pw.tienda_cerca.model.Conversacion;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.repository.ChatRepository;

@Service
public class ChatService {

	@Autowired
	private ChatRepository chatRepository;

	@Autowired
	private AsistenteService asistenteService;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private AvisoService avisoService;

	public Conversacion miConversacion(Usuario cliente) {
		return chatRepository.abiertaDeCliente(cliente.getId());
	}

	public Conversacion enviarComoCliente(Usuario cliente, String texto) {
		Long id = idAbierta(cliente);
		chatRepository.agregarMensaje(id, "CLIENTE", texto.trim());
		Conversacion c = chatRepository.buscar(id);
		if ("IA".equals(c.getEstado())) {
			AsistenteService.Respuesta r = asistenteService.responder(texto, usuarioService.recargar(cliente.getId()));
			chatRepository.agregarMensaje(id, "IA", r.texto());
			if (r.escalar()) {
				chatRepository.cambiarEstado(id, "ESPERANDO_ASESOR");
			}
		}
		avisoService.cambio("chat");
		return chatRepository.buscar(id);
	}

	public Conversacion pedirAsesor(Usuario cliente) {
		Long id = idAbierta(cliente);
		Conversacion c = chatRepository.buscar(id);
		if ("IA".equals(c.getEstado())) {
			chatRepository.agregarMensaje(id, "IA", asistenteService.textoEscalar());
			chatRepository.cambiarEstado(id, "ESPERANDO_ASESOR");
		}
		avisoService.cambio("chat");
		return chatRepository.buscar(id);
	}

	public List<Conversacion> todas() {
		return chatRepository.todas();
	}

	public Conversacion ver(Long id, Usuario quien) {
		Conversacion c = buscar(id);
		if ("CLIENTE".equals(quien.getRol()) && !c.getClienteId().equals(quien.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "error.chat.noEsTuya");
		}
		return c;
	}

	public Conversacion tomar(Long id, Usuario asesor) {
		Conversacion c = buscar(id);
		if ("CERRADA".equals(c.getEstado())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.chat.cerrada");
		}
		chatRepository.asignarAsesor(id, asesor.getId(), asesor.getNombre());
		chatRepository.agregarMensaje(id, "ASESOR", asistenteService.saludoAsesor(asesor.getNombre()));
		avisoService.cambio("chat");
		return chatRepository.buscar(id);
	}

	public Conversacion enviarComoAsesor(Long id, Usuario asesor, String texto) {
		Conversacion c = buscar(id);
		if (c.getAsesorId() == null) {
			chatRepository.asignarAsesor(id, asesor.getId(), asesor.getNombre());
		}
		chatRepository.agregarMensaje(id, "ASESOR", texto.trim());
		avisoService.cambio("chat");
		return chatRepository.buscar(id);
	}

	public Conversacion cerrar(Long id, Usuario quien) {
		ver(id, quien);
		chatRepository.cambiarEstado(id, "CERRADA");
		avisoService.cambio("chat");
		return chatRepository.buscar(id);
	}

	public int pendientes() {
		return chatRepository.enEspera();
	}

	private Long idAbierta(Usuario cliente) {
		Conversacion c = chatRepository.abiertaDeCliente(cliente.getId());
		return c != null ? c.getId() : chatRepository.crear(cliente.getId(), cliente.getNombre());
	}

	private Conversacion buscar(Long id) {
		Conversacion c = chatRepository.buscar(id);
		if (c == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.chat.noEncontrado");
		}
		return c;
	}
}
