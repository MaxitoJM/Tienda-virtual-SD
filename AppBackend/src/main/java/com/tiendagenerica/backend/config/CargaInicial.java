package com.tiendagenerica.backend.config;

import com.tiendagenerica.backend.modelo.Usuario;
import com.tiendagenerica.backend.repositorio.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el usuario por defecto exigido por el Modulo de Login del documento:
 * usuario admininicial con contrasena admin123456 para el primer ingreso.
 * Solo se crea si aun no existe en la base de datos.
 */
@Component
public class CargaInicial implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CargaInicial.class);

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder codificador;

    @Value("${tienda.usuario-inicial.cedula:1}")
    private Long cedula;

    @Value("${tienda.usuario-inicial.nombre:Administrador Inicial}")
    private String nombre;

    @Value("${tienda.usuario-inicial.email:admininicial@tiendagenerica.com}")
    private String email;

    @Value("${tienda.usuario-inicial.usuario:admininicial}")
    private String usuario;

    @Value("${tienda.usuario-inicial.password:admin123456}")
    private String password;

    public CargaInicial(UsuarioRepositorio repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    @Override
    public void run(String... args) {
        if (repositorio.existsByUsuario(usuario)) {
            return;
        }
        repositorio.save(new Usuario(cedula, nombre, email, usuario, codificador.encode(password)));
        LOG.info("Usuario inicial [{}] creado para el primer ingreso al sistema", usuario);
    }
}
