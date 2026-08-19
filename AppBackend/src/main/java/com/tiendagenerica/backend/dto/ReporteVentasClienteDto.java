package com.tiendagenerica.backend.dto;

import java.util.List;

/**
 * Reporte completo de ventas por cliente: el detalle por cliente y el
 * total consolidado que debe totalizarse al final del listado (SP5-QA-3).
 */
public class ReporteVentasClienteDto {

    private List<TotalVentasClienteDto> clientes;
    private Double totalGeneralVentas;
    private String mensaje;

    public ReporteVentasClienteDto() {
    }

    public ReporteVentasClienteDto(List<TotalVentasClienteDto> clientes, Double totalGeneralVentas, String mensaje) {
        this.clientes = clientes;
        this.totalGeneralVentas = totalGeneralVentas;
        this.mensaje = mensaje;
    }

    public List<TotalVentasClienteDto> getClientes() { return clientes; }
    public void setClientes(List<TotalVentasClienteDto> clientes) { this.clientes = clientes; }

    public Double getTotalGeneralVentas() { return totalGeneralVentas; }
    public void setTotalGeneralVentas(Double totalGeneralVentas) { this.totalGeneralVentas = totalGeneralVentas; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
