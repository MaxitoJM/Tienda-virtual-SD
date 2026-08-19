package com.tiendagenerica.backend.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Tabla <b>clientes</b> del modelo entidad-relacion.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @Column(name = "cedula_cliente", nullable = false)
    private Long cedulaCliente;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(name = "direccion_cliente")
    private String direccionCliente;

    @Column(name = "telefono_cliente")
    private String telefonoCliente;

    @Column(name = "email_cliente")
    private String emailCliente;

    public Cliente() {
    }

    public Cliente(Long cedulaCliente, String nombreCliente, String direccionCliente,
                   String telefonoCliente, String emailCliente) {
        this.cedulaCliente = cedulaCliente;
        this.nombreCliente = nombreCliente;
        this.direccionCliente = direccionCliente;
        this.telefonoCliente = telefonoCliente;
        this.emailCliente = emailCliente;
    }

    public Long getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(Long cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getDireccionCliente() { return direccionCliente; }
    public void setDireccionCliente(String direccionCliente) { this.direccionCliente = direccionCliente; }

    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }

    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }
}
