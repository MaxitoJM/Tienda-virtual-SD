package com.tiendagenerica.backend.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Tabla <b>detalle_ventas</b> del modelo entidad-relacion.
 * Guarda cada uno de los productos vendidos dentro de una venta.
 */
@Entity
@Table(name = "detalle_ventas")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_detalle_venta", nullable = false)
    private Long codigoDetalleVenta;

    @Column(name = "codigo_venta")
    private Long codigoVenta;

    @Column(name = "codigo_producto")
    private Long codigoProducto;

    @Column(name = "cantidad_producto")
    private Integer cantidadProducto;

    /** Valor unitario del producto (precio de venta). */
    @Column(name = "valor_venta")
    private Double valorVenta;

    /** Valor del IVA calculado para esta linea de detalle. */
    @Column(name = "valoriva")
    private Double valoriva;

    /** cantidad_producto x valor_venta (sin IVA). */
    @Column(name = "valor_total")
    private Double valorTotal;

    public DetalleVenta() {
    }

    public DetalleVenta(Long codigoDetalleVenta, Long codigoVenta, Long codigoProducto,
                        Integer cantidadProducto, Double valorVenta, Double valoriva, Double valorTotal) {
        this.codigoDetalleVenta = codigoDetalleVenta;
        this.codigoVenta = codigoVenta;
        this.codigoProducto = codigoProducto;
        this.cantidadProducto = cantidadProducto;
        this.valorVenta = valorVenta;
        this.valoriva = valoriva;
        this.valorTotal = valorTotal;
    }

    public Long getCodigoDetalleVenta() { return codigoDetalleVenta; }
    public void setCodigoDetalleVenta(Long codigoDetalleVenta) { this.codigoDetalleVenta = codigoDetalleVenta; }

    public Long getCodigoVenta() { return codigoVenta; }
    public void setCodigoVenta(Long codigoVenta) { this.codigoVenta = codigoVenta; }

    public Long getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(Long codigoProducto) { this.codigoProducto = codigoProducto; }

    public Integer getCantidadProducto() { return cantidadProducto; }
    public void setCantidadProducto(Integer cantidadProducto) { this.cantidadProducto = cantidadProducto; }

    public Double getValorVenta() { return valorVenta; }
    public void setValorVenta(Double valorVenta) { this.valorVenta = valorVenta; }

    public Double getValoriva() { return valoriva; }
    public void setValoriva(Double valoriva) { this.valoriva = valoriva; }

    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }
}
