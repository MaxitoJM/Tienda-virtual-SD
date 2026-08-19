package com.tiendagenerica.backend.dto;

/** Fila del reporte "Total de Ventas por Cliente" (HU-023). */
public class TotalVentasClienteDto {

    private Long cedulaCliente;
    private String nombreCliente;
    private Double valorTotalVentas;

    public TotalVentasClienteDto() {
    }

    public TotalVentasClienteDto(Long cedulaCliente, String nombreCliente, Double valorTotalVentas) {
        this.cedulaCliente = cedulaCliente;
        this.nombreCliente = nombreCliente;
        this.valorTotalVentas = valorTotalVentas;
    }

    public Long getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(Long cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public Double getValorTotalVentas() { return valorTotalVentas; }
    public void setValorTotalVentas(Double valorTotalVentas) { this.valorTotalVentas = valorTotalVentas; }
}
