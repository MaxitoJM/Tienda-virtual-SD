package com.tiendagenerica.backend.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Tabla <b>proveedores</b> del modelo entidad-relacion.
 */
@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @Column(name = "nitproveedor", nullable = false)
    private Long nitproveedor;

    @Column(name = "nombre_proveedor")
    private String nombreProveedor;

    @Column(name = "direccion_proveedor")
    private String direccionProveedor;

    @Column(name = "telefono_proveedor")
    private String telefonoProveedor;

    @Column(name = "ciudad_proveedor")
    private String ciudadProveedor;

    public Proveedor() {
    }

    public Proveedor(Long nitproveedor, String nombreProveedor, String direccionProveedor,
                     String telefonoProveedor, String ciudadProveedor) {
        this.nitproveedor = nitproveedor;
        this.nombreProveedor = nombreProveedor;
        this.direccionProveedor = direccionProveedor;
        this.telefonoProveedor = telefonoProveedor;
        this.ciudadProveedor = ciudadProveedor;
    }

    public Long getNitproveedor() { return nitproveedor; }
    public void setNitproveedor(Long nitproveedor) { this.nitproveedor = nitproveedor; }

    public String getNombreProveedor() { return nombreProveedor; }
    public void setNombreProveedor(String nombreProveedor) { this.nombreProveedor = nombreProveedor; }

    public String getDireccionProveedor() { return direccionProveedor; }
    public void setDireccionProveedor(String direccionProveedor) { this.direccionProveedor = direccionProveedor; }

    public String getTelefonoProveedor() { return telefonoProveedor; }
    public void setTelefonoProveedor(String telefonoProveedor) { this.telefonoProveedor = telefonoProveedor; }

    public String getCiudadProveedor() { return ciudadProveedor; }
    public void setCiudadProveedor(String ciudadProveedor) { this.ciudadProveedor = ciudadProveedor; }
}
