package com.tiendagenerica.backend.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Tabla <b>usuarios</b> del modelo entidad-relacion.
 * Almacena los usuarios que operan el sistema de la tienda.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(name = "cedula_usuario", nullable = false)
    private Long cedulaUsuario;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column(name = "email_usuario")
    private String emailUsuario;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "password")
    private String password;

    public Usuario() {
    }

    public Usuario(Long cedulaUsuario, String nombreUsuario, String emailUsuario, String usuario, String password) {
        this.cedulaUsuario = cedulaUsuario;
        this.nombreUsuario = nombreUsuario;
        this.emailUsuario = emailUsuario;
        this.usuario = usuario;
        this.password = password;
    }

    public Long getCedulaUsuario() { return cedulaUsuario; }
    public void setCedulaUsuario(Long cedulaUsuario) { this.cedulaUsuario = cedulaUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
