package co.edu.usco.pw.tienda_cerca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.usco.pw.tienda_cerca.model.PedidoRecurrente;

@Repository
public interface PedidoRecurrenteRepository extends JpaRepository<PedidoRecurrente, Long> {

	List<PedidoRecurrente> findByClienteIdOrderById(Long clienteId);

	List<PedidoRecurrente> findByActivoTrueAndDia(String dia);
}
