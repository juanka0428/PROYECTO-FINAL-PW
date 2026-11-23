package co.edu.usco.pw.tienda_cerca.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.usco.pw.tienda_cerca.model.Categoria;
import co.edu.usco.pw.tienda_cerca.model.Producto;
import co.edu.usco.pw.tienda_cerca.repository.CategoriaRepository;
import co.edu.usco.pw.tienda_cerca.repository.ProductoRepository;

@Service
public class ProductoService {

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private AvisoService avisoService;

	public List<Categoria> categorias() {
		return categoriaRepository.findAll();
	}

	public List<Producto> listar() {
		return productoRepository.findAll();
	}

	public Producto buscar(Long id) {
		return productoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "error.producto.noEncontrado"));
	}

	@Transactional
	public Producto crear(Producto p) {
		p.setId(null);
		revisarCategoria(p.getCategoriaId());
		avisoService.cambio("productos");
		return productoRepository.save(p);
	}

	@Transactional
	public Producto actualizar(Long id, Producto datos) {
		Producto p = buscar(id);
		revisarCategoria(datos.getCategoriaId());
		p.setNombre(datos.getNombre());
		p.setCategoriaId(datos.getCategoriaId());
		p.setPrecio(datos.getPrecio());
		p.setStock(datos.getStock());
		p.setStockMinimo(datos.getStockMinimo());
		p.setUnidad(datos.getUnidad());
		p.setImagen(datos.getImagen());
		p.getPalabrasClave().clear();
		if (datos.getPalabrasClave() != null) {
			p.getPalabrasClave().addAll(datos.getPalabrasClave());
		}
		p.setActivo(datos.getActivo() == null || datos.getActivo());
		avisoService.cambio("productos");
		return productoRepository.save(p);
	}

	@Transactional
	public Producto cambiarImagen(Long id, String imagen) {
		Producto p = buscar(id);
		p.setImagen(imagen);
		avisoService.cambio("productos");
		return productoRepository.save(p);
	}

	@Transactional
	public void eliminar(Long id) {
		if (!productoRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.producto.noEncontrado");
		}
		productoRepository.deleteById(id);
		avisoService.cambio("productos");
	}

	private void revisarCategoria(Long id) {
		if (id == null || !categoriaRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.categoria.noExiste");
		}
	}
}
