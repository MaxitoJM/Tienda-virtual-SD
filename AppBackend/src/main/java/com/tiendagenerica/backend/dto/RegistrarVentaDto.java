package com.tiendagenerica.backend.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Solicitud para registrar una venta completa (HU-015 a HU-020).
 * El documento limita la venta a un maximo de tres (3) productos.
 */
public class RegistrarVentaDto {

    @NotNull(message = "La cedula del cliente es obligatoria")
    private Long cedulaCliente;

    @NotNull(message = "La cedula del usuario es obligatoria")
    private Long cedulaUsuario;

    @Valid
    @NotEmpty(message = "La venta debe tener al menos un producto")
    private List<ItemVentaDto> productos = new ArrayList<>();

    public RegistrarVentaDto() {
    }

    public RegistrarVentaDto(Long cedulaCliente, Long cedulaUsuario, List<ItemVentaDto> productos) {
        this.cedulaCliente = cedulaCliente;
        this.cedulaUsuario = cedulaUsuario;
        this.productos = productos;
    }

    public Long getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(Long cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public Long getCedulaUsuario() { return cedulaUsuario; }
    public void setCedulaUsuario(Long cedulaUsuario) { this.cedulaUsuario = cedulaUsuario; }

    public List<ItemVentaDto> getProductos() { return productos; }
    public void setProductos(List<ItemVentaDto> productos) { this.productos = productos; }
}
