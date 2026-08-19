package com.tiendagenerica.backend.controlador;

import com.tiendagenerica.backend.dto.RespuestaDto;
import com.tiendagenerica.backend.modelo.Cliente;
import com.tiendagenerica.backend.servicio.ClienteServicio;
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

/** API de Clientes. */
@RestController
@RequestMapping("/clientes")
public class ClienteControlador {

    private final ClienteServicio servicio;

    public ClienteControlador(ClienteServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/listar")
    public List<Cliente> listar() {
        return servicio.listar();
    }

    @GetMapping("/consultar/{id}")
    public Cliente consultar(@PathVariable("id") Long id) {
        return servicio.consultar(id);
    }

    @PostMapping("/guardar")
    public ResponseEntity<Cliente> guardar(@RequestBody Cliente cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(cliente));
    }

    @PutMapping("/actualizar")
    public Cliente actualizar(@RequestBody Cliente cliente) {
        return servicio.actualizar(cliente);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<RespuestaDto> eliminar(@PathVariable("id") Long id) {
        servicio.eliminar(id);
        return ResponseEntity.ok(RespuestaDto.ok("Cliente eliminado correctamente"));
    }
}
