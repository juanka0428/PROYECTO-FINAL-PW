package co.edu.usco.pw.tienda_cerca.config;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import co.edu.usco.pw.tienda_cerca.dto.Peticiones;
import co.edu.usco.pw.tienda_cerca.model.ItemRecurrente;
import co.edu.usco.pw.tienda_cerca.model.MovimientoFiado;
import co.edu.usco.pw.tienda_cerca.model.Pedido;
import co.edu.usco.pw.tienda_cerca.model.PedidoRecurrente;
import co.edu.usco.pw.tienda_cerca.model.Producto;
import co.edu.usco.pw.tienda_cerca.model.Usuario;
import co.edu.usco.pw.tienda_cerca.repository.ChatRepository;
import co.edu.usco.pw.tienda_cerca.repository.MovimientoFiadoRepository;
import co.edu.usco.pw.tienda_cerca.repository.PedidoRecurrenteRepository;
import co.edu.usco.pw.tienda_cerca.repository.ProductoRepository;
import co.edu.usco.pw.tienda_cerca.repository.UsuarioRepository;
import co.edu.usco.pw.tienda_cerca.service.PedidoService;

@Component
public class DatosIniciales implements CommandLineRunner {

	@Autowired
	private DataSource dataSource;

	@Autowired
	@Qualifier("mysqlDataSource")
	private DataSource mysqlDataSource;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private MovimientoFiadoRepository movimientoRepository;

	@Autowired
	private PedidoRecurrenteRepository recurrenteRepository;

	@Autowired
	private ChatRepository chatRepository;

	@Autowired
	private PedidoService pedidoService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private boolean mysqlDisponible;

	@Override
	public void run(String... args) {
		ResourceDatabasePopulator postgres = new ResourceDatabasePopulator(
				new ClassPathResource("sql/triggers_postgresql.sql"));
		postgres.setSeparator(";;");
		postgres.setSqlScriptEncoding("UTF-8");
		postgres.execute(dataSource);
		System.out.println("Triggers de PostgreSQL listos");

		try {
			ResourceDatabasePopulator mysql = new ResourceDatabasePopulator(
					new ClassPathResource("sql/esquema_mysql.sql"));
			mysql.setSeparator("$$");
			mysql.setSqlScriptEncoding("UTF-8");
			mysql.execute(mysqlDataSource);
			mysqlDisponible = true;
			System.out.println("Tablas y triggers de MySQL listos");
		} catch (Exception e) {
			System.out.println("ATENCION: no se pudo conectar a MySQL (" + e.getMessage()
					+ "). El chat y el formulario de contacto no funcionaran.");
		}

		if (productoRepository.count() == 0) {
			ResourceDatabasePopulator catalogo = new ResourceDatabasePopulator(
					new ClassPathResource("sql/catalogo.sql"));
			catalogo.setSqlScriptEncoding("UTF-8");
			catalogo.execute(dataSource);
			System.out.println("Catalogo cargado: " + productoRepository.count() + " productos");
		}

		ResourceDatabasePopulator fotos = new ResourceDatabasePopulator(new ClassPathResource("sql/fotos_catalogo.sql"));
		fotos.setSqlScriptEncoding("UTF-8");
		fotos.execute(dataSource);

		if (usuarioRepository.count() == 0) {
			System.out.println("Base de datos vacia, guardando datos de prueba...");
			guardarDatosDePrueba();
		}
	}

	private void guardarDatosDePrueba() {
		Usuario maria = usuario("María Gómez", "maria@correo.com", "310 555 1234", "CLIENTE", "Calle 8 # 12-34", "Altico", 2.9268, -75.2862, 100000);
		Usuario jorge = usuario("Jorge Perdomo", "jorge@correo.com", "315 222 9876", "CLIENTE", "Carrera 5 # 20-15", "Quirinal", 2.9402, -75.2851, 50000);
		Usuario rosa = usuario("Rosa Cuéllar", "rosa@correo.com", "312 444 1111", "CLIENTE", "Calle 10 # 14-02", "Altico", 2.9281, -75.2879, 80000);
		Usuario pedro = usuario("Pedro Ramírez", "tendero@tienda.com", "320 000 0000", "TENDERO", null, null, null, null, 0);
		Usuario luis = usuario("Luis Torres", "repartidor@tienda.com", "318 777 3333", "REPARTIDOR", null, null, null, null, 0);
		usuario("Andrés Silva", "andres@tienda.com", "317 888 4444", "REPARTIDOR", null, null, null, null, 0);
		usuario("Camila Ruiz", "asesor@tienda.com", "311 999 2222", "ASESOR", null, null, null, null, 0);

		Producto leche = buscar("Leche entera 1 L");
		Producto huevos = buscar("Huevos AA x 12");
		Producto queso = buscar("Queso campesino 250 g");
		Producto pan = buscar("Pan tajado");
		Producto arepas = buscar("Arepas x 5");
		Producto arroz = buscar("Arroz 1 kg");
		Producto cafe = buscar("Café molido 250 g");
		Producto aceite = buscar("Aceite 1 L");
		Producto azucar = buscar("Azúcar 1 kg");
		Producto gaseosa = buscar("Gaseosa 1.5 L");
		Producto papa = buscar("Papa pastusa 2 kg");
		Producto platano = buscar("Plátano verde x 3");

		movimientoRepository.saveAndFlush(new MovimientoFiado(maria.getId(), "CARGO", 29900, "Saldo anterior (cuaderno)", null));
		movimientoRepository.saveAndFlush(new MovimientoFiado(maria.getId(), "ABONO", 10000, "Abono en efectivo", null));
		movimientoRepository.saveAndFlush(new MovimientoFiado(rosa.getId(), "CARGO", 50000, "Saldo anterior (cuaderno)", null));

		Pedido p1 = pedidoService.crear(pedido("CONTRA_ENTREGA", "CATALOGO", jorge, item(arroz, 2), item(aceite, 1), item(papa, 1)), recargar(jorge));
		pedidoService.asignarRepartidor(p1.getId(), luis.getId());
		pedidoService.cambiarEstado(p1.getId(), "PREPARANDO", pedro);
		pedidoService.cambiarEstado(p1.getId(), "EN_CAMINO", pedro);
		pedidoService.cambiarEstado(p1.getId(), "ENTREGADO", luis);

		Pedido p2 = pedidoService.crear(pedido("CONTRA_ENTREGA", "TEXTO", jorge, item(gaseosa, 2), item(arepas, 2), item(queso, 1)), recargar(jorge));
		pedidoService.cambiarEstado(p2.getId(), "PREPARANDO", pedro);

		pedidoService.crear(pedido("FIADO", "FOTO", rosa, item(cafe, 1), item(azucar, 1), item(platano, 2)), recargar(rosa));
		pedidoService.crear(pedido("EN_LINEA", "VOZ", maria, item(leche, 2), item(pan, 1), item(huevos, 1)), recargar(maria));

		PedidoRecurrente r = new PedidoRecurrente();
		r.setClienteId(maria.getId());
		r.setNombre("Desayuno de la semana");
		r.setFrecuencia("SEMANAL");
		r.setDia("LUNES");
		r.getItems().add(new ItemRecurrente(leche.getId(), leche.getNombre(), leche.getImagen(), leche.getPrecio(), 2));
		r.getItems().add(new ItemRecurrente(pan.getId(), pan.getNombre(), pan.getImagen(), pan.getPrecio(), 1));
		r.getItems().add(new ItemRecurrente(huevos.getId(), huevos.getNombre(), huevos.getImagen(), huevos.getPrecio(), 1));
		recurrenteRepository.save(r);

		if (mysqlDisponible) {
			Long chat = chatRepository.crear(jorge.getId(), jorge.getNombre());
			chatRepository.agregarMensaje(chat, "CLIENTE", "Hola, me llegó la leche vencida en el último pedido");
			chatRepository.agregarMensaje(chat, "IA", "Listo, te comunico con un asesor de la tienda. En un momento te responde.");
			chatRepository.cambiarEstado(chat, "ESPERANDO_ASESOR");
		}
	}

	private Usuario usuario(String nombre, String correo, String telefono, String rol, String direccion, String barrio,
			Double lat, Double lng, int cupo) {
		Usuario u = new Usuario();
		u.setNombre(nombre);
		u.setCorreo(correo);
		u.setClave(passwordEncoder.encode("123456"));
		u.setTelefono(telefono);
		u.setRol(rol);
		u.setDireccion(direccion);
		u.setBarrio(barrio);
		u.setLat(lat);
		u.setLng(lng);
		u.setCupoFiado(cupo);
		u.setSaldoFiado(0);
		return usuarioRepository.save(u);
	}

	private Producto buscar(String nombre) {
		return productoRepository.findByNombre(nombre).orElseThrow();
	}

	private Peticiones.Item item(Producto p, int cantidad) {
		return new Peticiones.Item(p.getId(), cantidad);
	}

	private Peticiones.NuevoPedido pedido(String metodo, String origen, Usuario cliente, Peticiones.Item... items) {
		Peticiones.Tarjeta tarjeta = metodo.equals("EN_LINEA")
				? new Peticiones.Tarjeta(cliente.getNombre().toUpperCase(), "4242 4242 4242 4242", "12/30", "123")
				: null;
		return new Peticiones.NuevoPedido(cliente.getDireccion(), cliente.getBarrio(), cliente.getTelefono(), null,
				metodo, origen, List.of(items), tarjeta);
	}

	private Usuario recargar(Usuario u) {
		return usuarioRepository.findById(u.getId()).orElseThrow();
	}
}
