package co.edu.usco.pw.tienda_cerca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import co.edu.usco.pw.tienda_cerca.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findAllByOrderByFechaDesc();

	List<Pedido> findByClienteIdOrderByFechaDesc(Long clienteId);

	List<Pedido> findByRepartidorIdOrderByFechaDesc(Long repartidorId);

	@Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(codigo FROM 5) AS INTEGER)), 0) FROM pedidos", nativeQuery = true)
	int ultimoNumero();
}
