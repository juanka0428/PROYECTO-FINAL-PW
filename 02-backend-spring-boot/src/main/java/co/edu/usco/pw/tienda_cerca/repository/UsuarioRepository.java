package co.edu.usco.pw.tienda_cerca.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.usco.pw.tienda_cerca.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByCorreo(String correo);

	boolean existsByCorreo(String correo);

	List<Usuario> findByRolOrderByNombre(String rol);
}
