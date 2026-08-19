package com.tiendagenerica.backend.servicio;

import com.tiendagenerica.backend.dto.LoginDto;
import com.tiendagenerica.backend.dto.LoginRespuestaDto;
import com.tiendagenerica.backend.modelo.Usuario;
import com.tiendagenerica.backend.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Modulo de Login del sistema (HU-001, pruebas SP1-QA-1 y SP1-QA-2). */
@Service
public class LoginServicio {

    private static final String MENSAJE_ERROR =
            "Usuario y/o contrasena errados, intente de nuevo";

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder codificador;
    private final UsuarioServicio usuarioServicio;
    private final String usuarioInicial;
    private final boolean desactivarUsuarioInicial;

    public LoginServicio(UsuarioRepositorio repositorio,
                         PasswordEncoder codificador,
                         UsuarioServicio usuarioServicio,
                         @Value("${tienda.usuario-inicial.usuario:admininicial}") String usuarioInicial,
                         @Value("${tienda.usuario-inicial.desactivar-al-crear-usuarios:true}") boolean desactivar) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.usuarioServicio = usuarioServicio;
        this.usuarioInicial = usuarioInicial;
        this.desactivarUsuarioInicial = desactivar;
    }

    /**
     * SP1-QA-1: ingreso correcto con el usuario inicial o con un usuario creado.
     * SP1-QA-2: ingreso incorrecto por error o ausencia de usuario/contrasena.
     */
    @Transactional(readOnly = true)
    public LoginRespuestaDto autenticar(LoginDto credenciales) {
        if (credenciales == null
                || esVacio(credenciales.getUsuario())
                || esVacio(credenciales.getPassword())) {
            return new LoginRespuestaDto(false, MENSAJE_ERROR);
        }

        Optional<Usuario> encontrado = repositorio.findByUsuario(credenciales.getUsuario().trim());
        if (!encontrado.isPresent()) {
            return new LoginRespuestaDto(false, MENSAJE_ERROR);
        }

        Usuario usuario = encontrado.get();
        if (!codificador.matches(credenciales.getPassword(), usuario.getPassword())) {
            return new LoginRespuestaDto(false, MENSAJE_ERROR);
        }

        if (desactivarUsuarioInicial
                && usuarioInicial.equals(usuario.getUsuario())
                && !usuarioServicio.usuarioInicialActivo()) {
            return new LoginRespuestaDto(false,
                    "El usuario " + usuarioInicial + " fue desactivado. Ingrese con un usuario del sistema");
        }

        LoginRespuestaDto respuesta = new LoginRespuestaDto(true, "Ingreso correcto al sistema");
        respuesta.setCedulaUsuario(usuario.getCedulaUsuario());
        respuesta.setNombreUsuario(usuario.getNombreUsuario());
        respuesta.setUsuario(usuario.getUsuario());
        return respuesta;
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
