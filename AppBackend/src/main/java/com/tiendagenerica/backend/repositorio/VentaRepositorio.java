package com.tiendagenerica.backend.repositorio;

import com.tiendagenerica.backend.dto.TotalVentasClienteDto;
import com.tiendagenerica.backend.modelo.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    List<Venta> findByCedulaCliente(Long cedulaCliente);

    /**
     * Reporte HU-023: total de ventas acumulado por cada cliente.
     * Se incluyen todos los clientes registrados; los que aun no tienen
     * ventas aparecen con valor total en cero.
     */
    @Query("SELECT new com.tiendagenerica.backend.dto.TotalVentasClienteDto("
         + "  c.cedulaCliente, c.nombreCliente, COALESCE(SUM(v.totalVenta), 0.0)) "
         + "FROM Cliente c LEFT JOIN Venta v ON v.cedulaCliente = c.cedulaCliente "
         + "GROUP BY c.cedulaCliente, c.nombreCliente "
         + "ORDER BY c.cedulaCliente")
    List<TotalVentasClienteDto> totalVentasPorCliente();
}
