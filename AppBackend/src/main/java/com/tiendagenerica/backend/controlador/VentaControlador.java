package com.tiendagenerica.backend.controlador;

import com.tiendagenerica.backend.dto.RegistrarVentaDto;
import com.tiendagenerica.backend.dto.RespuestaDto;
import com.tiendagenerica.backend.dto.VentaRegistradaDto;
import com.tiendagenerica.backend.modelo.Venta;
import com.tiendagenerica.backend.servicio.VentaServicio;
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

import javax.validation.Valid;
import java.util.List;

/**
 * API de Ventas. La ruta base conserva la mayuscula inicial tal como aparece
 * en la especificacion de la API del documento del proyecto.
 */
@RestController
@RequestMapping("/Ventas")
public class VentaControlador {

    private final VentaServicio servicio;

    public VentaControlador(VentaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/listar")
    public List<Venta> listar() {
        return servicio.listar();
    }

    @GetMapping("/consultar/{id}")
    public Venta consultar(@PathVariable("id") Long id) {
        return servicio.consultar(id);
    }

    @PostMapping("/guardar")
    public ResponseEntity<Venta> guardar(@RequestBody Venta venta) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(venta));
    }

    @PutMapping("/actualizar")
    public Venta actualizar(@RequestBody Venta venta) {
        return servicio.actualizar(venta);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<RespuestaDto> eliminar(@PathVariable("id") Long id) {
        servicio.eliminar(id);
        return ResponseEntity.ok(RespuestaDto.ok("Venta eliminada correctamente"));
    }

    /**
     * Registra la venta completa desde el formulario de ventas: calcula totales,
     * genera el consecutivo y guarda la cabecera junto con su detalle (HU-015 a HU-020).
     */
    @PostMapping("/registrar")
    public ResponseEntity<VentaRegistradaDto> registrar(@Valid @RequestBody RegistrarVentaDto solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrar(solicitud));
    }
}
