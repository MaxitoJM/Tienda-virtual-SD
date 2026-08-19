package com.tiendagenerica.backend.repositorio;

import com.tiendagenerica.backend.modelo.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepositorio extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByCodigoVenta(Long codigoVenta);
}
