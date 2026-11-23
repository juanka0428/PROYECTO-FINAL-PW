package co.edu.usco.pw.tienda_cerca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.usco.pw.tienda_cerca.model.MovimientoFiado;

@Repository
public interface MovimientoFiadoRepository extends JpaRepository<MovimientoFiado, Long> {

	List<MovimientoFiado> findByClienteIdOrderByFechaDescIdDesc(Long clienteId);
}
