package com.tiendagenerica.backend.dto;

import com.tiendagenerica.backend.modelo.DetalleVenta;

import java.util.List;

/** Confirmacion de la transaccion de venta (HU-020, SP4-QA-10). */
public class VentaRegistradaDto {

    private Long codigoVenta;
    private Long cedulaCliente;
    private String nombreCliente;
    private Double valorVenta;
    private Double ivaventa;
    private Double totalVenta;
    private List<DetalleVenta> detalles;
    private String mensaje;

    public VentaRegistradaDto() {
    }

    public Long getCodigoVenta() { return codigoVenta; }
    public void setCodigoVenta(Long codigoVenta) { this.codigoVenta = codigoVenta; }

    public Long getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(Long cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public Double getValorVenta() { return valorVenta; }
    public void setValorVenta(Double valorVenta) { this.valorVenta = valorVenta; }

    public Double getIvaventa() { return ivaventa; }
    public void setIvaventa(Double ivaventa) { this.ivaventa = ivaventa; }

    public Double getTotalVenta() { return totalVenta; }
    public void setTotalVenta(Double totalVenta) { this.totalVenta = totalVenta; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
