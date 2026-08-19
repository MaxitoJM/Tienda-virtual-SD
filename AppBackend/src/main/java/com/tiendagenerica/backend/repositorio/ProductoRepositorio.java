package com.tiendagenerica.backend.repositorio;

import com.tiendagenerica.backend.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {
}
