package co.edu.usco.pw.tienda_cerca.service;

import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.usco.pw.tienda_cerca.dto.Peticiones;

@Service
public class PagoService {

	public record Resultado(String referencia, String tarjeta) {
	}

	private static final Map<String, String> RECHAZOS = Map.of("4000000000000002",
			"error.pago.rechazada", "4000000000009995",
			"error.pago.fondos");

	private static final Map<String, String> APROBADAS = Map.of("4242424242424242", "VISA", "5555555555554444",
			"MASTERCARD");

	public Resultado cobrar(int valor, Peticiones.Tarjeta tarjeta) {
		if (tarjeta == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.pago.sinTarjeta");
		}
		String numero = tarjeta.numero().replaceAll("[\\s-]", "");
		if (!numero.matches("\\d{13,19}") || !luhn(numero)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.pago.numero");
		}
		if (!vigente(tarjeta.vence())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.pago.vence");
		}
		if (!tarjeta.cvc().matches("\\d{3,4}")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.pago.cvc");
		}
		if (RECHAZOS.containsKey(numero)) {
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, RECHAZOS.get(numero));
		}
		String marca = APROBADAS.get(numero);
		if (marca == null) {
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
					"error.pago.soloPrueba");
		}
		String referencia = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		return new Resultado(referencia, marca + " •••• " + numero.substring(numero.length() - 4));
	}

	static boolean luhn(String numero) {
		int suma = 0;
		boolean doble = false;
		for (int i = numero.length() - 1; i >= 0; i--) {
			int d = numero.charAt(i) - '0';
			if (doble) {
				d *= 2;
				if (d > 9) {
					d -= 9;
				}
			}
			suma += d;
			doble = !doble;
		}
		return suma % 10 == 0;
	}

	private boolean vigente(String vence) {
		if (vence == null || !vence.matches("(0[1-9]|1[0-2])/\\d{2}")) {
			return false;
		}
		YearMonth fecha = YearMonth.of(2000 + Integer.parseInt(vence.substring(3)), Integer.parseInt(vence.substring(0, 2)));
		return !fecha.isBefore(YearMonth.now());
	}
}
