package com.tiendagenerica.backend.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Tabla <b>ventas</b> del modelo entidad-relacion.
 * El codigo_venta es un consecutivo generado por el sistema.
 */
@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_venta", nullable = false)
    private Long codigoVenta;

    @Column(name = "cedula_cliente")
    private Long cedulaCliente;

    @Column(name = "cedula_usuario")
    private Long cedulaUsuario;

    /** Valor total de la venta sin IVA. */
    @Column(name = "valor_venta")
    private Double valorVenta;

    /** Valor total del IVA de la venta. */
    @Column(name = "ivaventa")
    private Double ivaventa;

    /** Valor total de la venta incluyendo el IVA. */
    @Column(name = "total_venta")
    private Double totalVenta;

    public Venta() {
    }

    public Venta(Long codigoVenta, Long cedulaCliente, Long cedulaUsuario,
                 Double valorVenta, Double ivaventa, Double totalVenta) {
        this.codigoVenta = codigoVenta;
        this.cedulaCliente = cedulaCliente;
        this.cedulaUsuario = cedulaUsuario;
        this.valorVenta = valorVenta;
        this.ivaventa = ivaventa;
        this.totalVenta = totalVenta;
    }

    public Long getCodigoVenta() { return codigoVenta; }
    public void setCodigoVenta(Long codigoVenta) { this.codigoVenta = codigoVenta; }

    public Long getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(Long cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public Long getCedulaUsuario() { return cedulaUsuario; }
    public void setCedulaUsuario(Long cedulaUsuario) { this.cedulaUsuario = cedulaUsuario; }

    public Double getValorVenta() { return valorVenta; }
    public void setValorVenta(Double valorVenta) { this.valorVenta = valorVenta; }

    public Double getIvaventa() { return ivaventa; }
    public void setIvaventa(Double ivaventa) { this.ivaventa = ivaventa; }

    public Double getTotalVenta() { return totalVenta; }
    public void setTotalVenta(Double totalVenta) { this.totalVenta = totalVenta; }
}
