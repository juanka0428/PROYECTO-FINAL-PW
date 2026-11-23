package co.edu.usco.pw.tienda_cerca.service;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Service
public class AvisoService extends TextWebSocketHandler {

	private final Set<WebSocketSession> sesiones = new CopyOnWriteArraySet<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession sesion) {
		sesiones.add(sesion);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession sesion, CloseStatus estado) {
		sesiones.remove(sesion);
	}

	public void cambio(String tipo) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					enviar(tipo);
				}
			});
		} else {
			enviar(tipo);
		}
	}

	private void enviar(String tipo) {
		TextMessage mensaje = new TextMessage("{\"tipo\":\"" + tipo + "\"}");
		for (WebSocketSession s : sesiones) {
			try {
				synchronized (s) {
					if (s.isOpen()) {
						s.sendMessage(mensaje);
					}
				}
			} catch (IOException e) {
				sesiones.remove(s);
			}
		}
	}

	public int conectados() {
		return sesiones.size();
	}
}
