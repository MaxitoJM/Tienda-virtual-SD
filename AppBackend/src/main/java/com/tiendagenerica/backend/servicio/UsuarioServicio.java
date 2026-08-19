package com.tiendagenerica.backend.servicio;

import com.tiendagenerica.backend.excepcion.DatosInvalidosException;
import com.tiendagenerica.backend.excepcion.RecursoNoEncontradoException;
import com.tiendagenerica.backend.modelo.Usuario;
import com.tiendagenerica.backend.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Modulo de Gestion de Usuarios (HU-002 a HU-005).
 * Las contrasenas se almacenan cifradas con BCrypt.
 */
@Service
public class UsuarioServicio {

    /** Valor que sustituye a la contrasena en las respuestas de la API. */
    public static final String PASSWORD_ENMASCARADO = "********";

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder codificador;
    private final String usuarioInicial;
    private final boolean enmascararPassword;

    public UsuarioServicio(UsuarioRepositorio repositorio,
                           PasswordEncoder codificador,
                           @Value("${tienda.usuario-inicial.usuario:admininicial}") String usuarioInicial,
                           @Value("${tienda.reportes.enmascarar-password:true}") boolean enmascararPassword) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.usuarioInicial = usuarioInicial;
        this.enmascararPassword = enmascararPassword;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repositorio.findAll();
    }

    /** Listado apto para exponerse por la API, sin el valor real de la contrasena. */
    @Transactional(readOnly = true)
    public List<Usuario> listarEnmascarados() {
        List<Usuario> usuarios = repositorio.findAll();
        List<Usuario> resultado = new ArrayList<>(usuarios.size());
        for (Usuario usuario : usuarios) {
            resultado.add(enmascarar(usuario));
        }
        return resultado;
    }

    /**
     * Devuelve una copia del usuario sin exponer el valor real de la contrasena.
     * El comportamiento se controla con tienda.reportes.enmascarar-password.
     */
    public Usuario enmascarar(Usuario usuario) {
        if (usuario == null || !enmascararPassword) {
            return usuario;
        }
        return new Usuario(usuario.getCedulaUsuario(), usuario.getNombreUsuario(),
                usuario.getEmailUsuario(), usuario.getUsuario(), PASSWORD_ENMASCARADO);
    }

    /** HU-003: consulta de los datos de un usuario por medio de la cedula. */
    @Transactional(readOnly = true)
    public Usuario consultar(Long cedula) {
        if (cedula == null) {
            throw new DatosInvalidosException("La cedula del usuario es obligatoria");
        }
        return repositorio.findById(cedula).orElseThrow(() -> new RecursoNoEncontradoException(
                "El usuario con cedula " + cedula + " no se encuentra registrado en la base de datos"));
    }

    /** HU-002: creacion de un nuevo usuario con todos sus datos completos. */
    @Transactional
    public Usuario guardar(Usuario usuario) {
        validarCompletitud(usuario);
        if (repositorio.existsById(usuario.getCedulaUsuario())) {
            throw new DatosInvalidosException(
                    "Ya existe un usuario registrado con la cedula " + usuario.getCedulaUsuario());
        }
        if (repositorio.existsByUsuario(usuario.getUsuario())) {
            throw new DatosInvalidosException(
                    "El nombre de usuario " + usuario.getUsuario() + " ya se encuentra en uso");
        }
        usuario.setPassword(codificador.encode(usuario.getPassword()));
        return repositorio.save(usuario);
    }

    /** HU-004: actualizacion de los datos de un usuario, previa consulta por cedula. */
    @Transactional
    public Usuario actualizar(Usuario usuario) {
        validarCompletitud(usuario);
        Usuario existente = consultar(usuario.getCedulaUsuario());

        existente.setNombreUsuario(usuario.getNombreUsuario());
        existente.setEmailUsuario(usuario.getEmailUsuario());
        existente.setUsuario(usuario.getUsuario());
        if (esBcrypt(usuario.getPassword())) {
            existente.setPassword(usuario.getPassword());
        } else {
            existente.setPassword(codificador.encode(usuario.getPassword()));
        }
        return repositorio.save(existente);
    }

    /** HU-005: borrado de un usuario, previa consulta por cedula. */
    @Transactional
    public void eliminar(Long cedula) {
        Usuario existente = consultar(cedula);
        repositorio.delete(existente);
    }

    /**
     * Regla del documento: en este modulo se desactivara el usuario admininicial.
     * El usuario inicial deja de estar habilitado en cuanto exista al menos un
     * usuario creado desde el modulo de gestion de usuarios.
     */
    @Transactional(readOnly = true)
    public boolean usuarioInicialActivo() {
        return repositorio.countByUsuarioNot(usuarioInicial) == 0;
    }

    private void validarCompletitud(Usuario usuario) {
        if (usuario == null) {
            throw new DatosInvalidosException("No se recibieron los datos del usuario");
        }
        if (usuario.getCedulaUsuario() == null) {
            throw new DatosInvalidosException("Datos incompletos: la cedula es obligatoria");
        }
        exigir(usuario.getNombreUsuario(), "el nombre completo");
        exigir(usuario.getEmailUsuario(), "el correo electronico");
        exigir(usuario.getUsuario(), "el usuario");
        exigir(usuario.getPassword(), "la contrasena");
    }

    private void exigir(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatosInvalidosException("Datos incompletos: " + campo + " es obligatorio");
        }
    }

    private boolean esBcrypt(String valor) {
        return valor != null && valor.startsWith("$2") && valor.length() == 60;
    }
}
