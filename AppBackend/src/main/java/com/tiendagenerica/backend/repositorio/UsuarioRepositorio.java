package com.tiendagenerica.backend.repositorio;

import com.tiendagenerica.backend.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);

    /** Cantidad de usuarios distintos del usuario inicial del sistema. */
    long countByUsuarioNot(String usuario);
}
