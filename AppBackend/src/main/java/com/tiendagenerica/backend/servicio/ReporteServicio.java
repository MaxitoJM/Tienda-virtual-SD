package com.tiendagenerica.backend.servicio;

import com.tiendagenerica.backend.dto.ReporteVentasClienteDto;
import com.tiendagenerica.backend.dto.TotalVentasClienteDto;
import com.tiendagenerica.backend.modelo.Cliente;
import com.tiendagenerica.backend.modelo.Usuario;
import com.tiendagenerica.backend.repositorio.ClienteRepositorio;
import com.tiendagenerica.backend.repositorio.VentaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Modulo de Consultas y Reportes (HU-021 a HU-023). */
@Service
public class ReporteServicio {

    private final UsuarioServicio usuarioServicio;
    private final ClienteRepositorio clienteRepositorio;
    private final VentaRepositorio ventaRepositorio;

    public ReporteServicio(UsuarioServicio usuarioServicio,
                           ClienteRepositorio clienteRepositorio,
                           VentaRepositorio ventaRepositorio) {
        this.usuarioServicio = usuarioServicio;
        this.clienteRepositorio = clienteRepositorio;
        this.ventaRepositorio = ventaRepositorio;
    }

    /**
     * HU-021: listado de usuarios del sistema.
     * Las contrasenas se muestran enmascaradas por seguridad; el comportamiento
     * se controla con la propiedad tienda.reportes.enmascarar-password.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listadoUsuarios() {
        return usuarioServicio.listarEnmascarados();
    }

    /** HU-022: listado de clientes registrados. */
    @Transactional(readOnly = true)
    public List<Cliente> listadoClientes() {
        return clienteRepositorio.findAll();
    }

    /**
     * HU-023: total de ventas por cliente, con el total consolidado que debe
     * totalizarse al final del listado (SP5-QA-3).
     */
    @Transactional(readOnly = true)
    public ReporteVentasClienteDto totalVentasPorCliente() {
        List<TotalVentasClienteDto> filas = ventaRepositorio.totalVentasPorCliente();
        double total = 0d;
        for (TotalVentasClienteDto fila : filas) {
            total += fila.getValorTotalVentas() == null ? 0d : fila.getValorTotalVentas();
        }
        String mensaje = filas.isEmpty() ? "No existen clientes registrados en el sistema" : null;
        return new ReporteVentasClienteDto(filas, Math.round(total * 100d) / 100d, mensaje);
    }
}
