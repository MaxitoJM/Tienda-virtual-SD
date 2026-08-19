package com.tiendagenerica.backend.controlador;

import com.tiendagenerica.backend.dto.LoginDto;
import com.tiendagenerica.backend.dto.LoginRespuestaDto;
import com.tiendagenerica.backend.dto.RespuestaDto;
import com.tiendagenerica.backend.modelo.Usuario;
import com.tiendagenerica.backend.servicio.LoginServicio;
import com.tiendagenerica.backend.servicio.UsuarioServicio;
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

/** API de Usuarios y Login, segun la especificacion de la Parte 4 del documento. */
@RestController
@RequestMapping("/usuarios")
public class UsuarioControlador {

    private final UsuarioServicio servicio;
    private final LoginServicio loginServicio;

    public UsuarioControlador(UsuarioServicio servicio, LoginServicio loginServicio) {
        this.servicio = servicio;
        this.loginServicio = loginServicio;
    }

    @GetMapping("/listar")
    public List<Usuario> listar() {
        return servicio.listarEnmascarados();
    }

    @GetMapping("/consultar/{id}")
    public Usuario consultar(@PathVariable("id") Long id) {
        return servicio.enmascarar(servicio.consultar(id));
    }

    @PostMapping("/guardar")
    public ResponseEntity<Usuario> guardar(@RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicio.enmascarar(servicio.guardar(usuario)));
    }

    @PutMapping("/actualizar")
    public Usuario actualizar(@RequestBody Usuario usuario) {
        return servicio.enmascarar(servicio.actualizar(usuario));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<RespuestaDto> eliminar(@PathVariable("id") Long id) {
        servicio.eliminar(id);
        return ResponseEntity.ok(RespuestaDto.ok("Usuario eliminado correctamente"));
    }

    /** Modulo de Login del sistema (HU-001). */
    @PostMapping("/login")
    public ResponseEntity<LoginRespuestaDto> login(@Valid @RequestBody LoginDto credenciales) {
        LoginRespuestaDto respuesta = loginServicio.autenticar(credenciales);
        if (!respuesta.isAutenticado()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
        }
        return ResponseEntity.ok(respuesta);
    }

    /** Indica si el usuario inicial admininicial sigue habilitado. */
    @GetMapping("/usuario-inicial-activo")
    public RespuestaDto usuarioInicialActivo() {
        boolean activo = servicio.usuarioInicialActivo();
        return new RespuestaDto(activo, activo
                ? "El usuario inicial se encuentra activo"
                : "El usuario inicial fue desactivado");
    }
}
