package co.edu.usco.pw.tienda_cerca.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import co.edu.usco.pw.tienda_cerca.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

	List<Producto> findByActivoTrueOrderByNombre();

	Optional<Producto> findByNombre(String nombre);

	@Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= p.stockMinimo ORDER BY p.stock")
	List<Producto> buscarPorAgotarse();
}
