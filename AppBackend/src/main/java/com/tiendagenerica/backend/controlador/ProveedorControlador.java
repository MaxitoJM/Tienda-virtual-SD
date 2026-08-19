package com.tiendagenerica.backend.controlador;

import com.tiendagenerica.backend.dto.RespuestaDto;
import com.tiendagenerica.backend.modelo.Proveedor;
import com.tiendagenerica.backend.servicio.ProveedorServicio;
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

/** API de Proveedores. */
@RestController
@RequestMapping("/proveedores")
public class ProveedorControlador {

    private final ProveedorServicio servicio;

    public ProveedorControlador(ProveedorServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/listar")
    public List<Proveedor> listar() {
        return servicio.listar();
    }

    @GetMapping("/consultar/{id}")
    public Proveedor consultar(@PathVariable("id") Long id) {
        return servicio.consultar(id);
    }

    @PostMapping("/guardar")
    public ResponseEntity<Proveedor> guardar(@RequestBody Proveedor proveedor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(proveedor));
    }

    @PutMapping("/actualizar")
    public Proveedor actualizar(@RequestBody Proveedor proveedor) {
        return servicio.actualizar(proveedor);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<RespuestaDto> eliminar(@PathVariable("id") Long id) {
        servicio.eliminar(id);
        return ResponseEntity.ok(RespuestaDto.ok("Proveedor eliminado correctamente"));
    }
}
