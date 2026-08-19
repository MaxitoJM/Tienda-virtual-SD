package com.tiendagenerica.backend.dto;

import javax.validation.constraints.NotNull;

/** Una linea de producto dentro del formulario de ventas. */
public class ItemVentaDto {

    @NotNull(message = "El codigo de producto es obligatorio")
    private Long codigoProducto;

    @NotNull(message = "La cantidad de producto es obligatoria")
    private Integer cantidadProducto;

    public ItemVentaDto() {
    }

    public ItemVentaDto(Long codigoProducto, Integer cantidadProducto) {
        this.codigoProducto = codigoProducto;
        this.cantidadProducto = cantidadProducto;
    }

    public Long getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(Long codigoProducto) { this.codigoProducto = codigoProducto; }

    public Integer getCantidadProducto() { return cantidadProducto; }
    public void setCantidadProducto(Integer cantidadProducto) { this.cantidadProducto = cantidadProducto; }
}
