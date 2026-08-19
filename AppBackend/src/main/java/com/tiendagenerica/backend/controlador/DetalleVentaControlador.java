package com.tiendagenerica.backend.controlador;

import com.tiendagenerica.backend.dto.RespuestaDto;
import com.tiendagenerica.backend.modelo.DetalleVenta;
import com.tiendagenerica.backend.servicio.DetalleVentaServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API de Detalle de Ventas. */
@RestController
@RequestMapping("/detalleventas")
public class DetalleVentaControlador {

    private final DetalleVentaServicio servicio;

    public DetalleVentaControlador(DetalleVentaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/listar")
    public List<DetalleVenta> listar() {
        return servicio.listar();
    }

    @GetMapping("/consultar/{id}")
    public DetalleVenta consultar(@PathVariable("id") Long id) {
        return servicio.consultar(id);
    }

    @GetMapping("/venta/{codigoVenta}")
    public List<DetalleVenta> listarPorVenta(@PathVariable("codigoVenta") Long codigoVenta) {
        return servicio.listarPorVenta(codigoVenta);
    }

    @PostMapping("/guardar")
    public ResponseEntity<DetalleVenta> guardar(@RequestBody DetalleVenta detalle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(detalle));
    }

    @PutMapping("/actualizar")
    public DetalleVenta actualizar(@RequestBody DetalleVenta detalle) {
        return servicio.actualizar(detalle);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<RespuestaDto> eliminar(@PathVariable("id") Long id) {
        servicio.eliminar(id);
        return ResponseEntity.ok(RespuestaDto.ok("Detalle de venta eliminado correctamente"));
    }
}
