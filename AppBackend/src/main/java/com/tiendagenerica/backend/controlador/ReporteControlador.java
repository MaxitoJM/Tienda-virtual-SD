package com.tiendagenerica.backend.controlador;

import com.tiendagenerica.backend.dto.ReporteVentasClienteDto;
import com.tiendagenerica.backend.modelo.Cliente;
import com.tiendagenerica.backend.modelo.Usuario;
import com.tiendagenerica.backend.servicio.ReporteServicio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API del Modulo de Consultas y Reportes (HU-021 a HU-023). */
@RestController
@RequestMapping("/reportes")
public class ReporteControlador {

    private final ReporteServicio servicio;

    public ReporteControlador(ReporteServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/usuarios")
    public List<Usuario> listadoUsuarios() {
        return servicio.listadoUsuarios();
    }

    @GetMapping("/clientes")
    public List<Cliente> listadoClientes() {
        return servicio.listadoClientes();
    }

    @GetMapping("/ventasporcliente")
    public ReporteVentasClienteDto totalVentasPorCliente() {
        return servicio.totalVentasPorCliente();
    }
}
