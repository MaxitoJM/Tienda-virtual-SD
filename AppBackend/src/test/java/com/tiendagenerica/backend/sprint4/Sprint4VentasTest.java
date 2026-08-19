package com.tiendagenerica.backend.sprint4;

import com.tiendagenerica.backend.PruebaBase;
import com.tiendagenerica.backend.dto.ItemVentaDto;
import com.tiendagenerica.backend.dto.RegistrarVentaDto;
import com.tiendagenerica.backend.modelo.DetalleVenta;
import com.tiendagenerica.backend.modelo.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sprint 4 - Modulo de Gestion de Ventas. Casos SP4-QA-1 a SP4-QA-10. */
class Sprint4VentasTest extends PruebaBase {

    private static final Long CEDULA_CLIENTE = 52123456L;
    private static final Long CEDULA_USUARIO = 1020304050L;

    @BeforeEach
    void prepararDatosDeVenta() {
        crearUsuario(CEDULA_USUARIO, "jperez", "clave12345");
        crearCliente(CEDULA_CLIENTE, "Maria Lopez");
        crearProveedor(1L, "Proveedor Uno");
        crearProducto(1L, "Melocotones", 1L, 1000d, 19d);
        crearProducto(2L, "Manzanas", 1L, 2000d, 19d);
        crearProducto(3L, "Platanos", 1L, 500d, 5d);
    }

    private RegistrarVentaDto ventaDeTresProductos() {
        List<ItemVentaDto> items = Arrays.asList(
                new ItemVentaDto(1L, 2),
                new ItemVentaDto(2L, 3),
                new ItemVentaDto(3L, 4));
        return new RegistrarVentaDto(CEDULA_CLIENTE, CEDULA_USUARIO, items);
    }

    @Test
    @DisplayName("SP4-QA-1: consulta exitosa de la cedula del cliente")
    void sp4QA1_consultaExitosaDeCliente() throws Exception {
        mockMvc.perform(get("/clientes/consultar/{id}", CEDULA_CLIENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_cliente").value("Maria Lopez"));
    }

    @Test
    @DisplayName("SP4-QA-2: consulta fallida de la cedula del cliente")
    void sp4QA2_consultaFallidaDeCliente() throws Exception {
        mockMvc.perform(get("/clientes/consultar/{id}", 123L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(
                        "La cedula 123 no se encuentra registrada en la base de datos"));
    }

    @Test
    @DisplayName("SP4-QA-3: consulta exitosa de producto por codigo")
    void sp4QA3_consultaExitosaDeProducto() throws Exception {
        mockMvc.perform(get("/productos/consultar/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_producto").value("Melocotones"))
                .andExpect(jsonPath("$.precio_venta").value(1000d));
    }

    @Test
    @DisplayName("SP4-QA-4: consulta fallida de producto por codigo inexistente")
    void sp4QA4_consultaFallidaDeProducto() throws Exception {
        mockMvc.perform(get("/productos/consultar/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(
                        "El codigo de producto 99 no se encuentra registrado en la base de datos"));
    }

    @Test
    @DisplayName("SP4-QA-5: validacion del campo cantidad de productos (cero, negativo o nulo)")
    void sp4QA5_validacionDeCantidadIncorrecta() throws Exception {
        RegistrarVentaDto venta = new RegistrarVentaDto(CEDULA_CLIENTE, CEDULA_USUARIO,
                Collections.singletonList(new ItemVentaDto(1L, 0)));

        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(venta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "El valor de cantidad es incorrecto para el producto 1: debe ser un numero mayor que cero"));

        assertEquals(0, ventaRepositorio.count(), "No debe registrarse la venta");
    }

    @Test
    @DisplayName("SP4-QA-6: validacion del valor total por cada producto (cantidad x precio de venta)")
    void sp4QA6_validacionDelValorTotalPorProducto() throws Exception {
        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(ventaDeTresProductos())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.detalles[0].valor_total").value(2000d))
                .andExpect(jsonPath("$.detalles[1].valor_total").value(6000d))
                .andExpect(jsonPath("$.detalles[2].valor_total").value(2000d));
    }

    @Test
    @DisplayName("SP4-QA-7: validacion del campo Total Venta de los tres productos")
    void sp4QA7_validacionDelTotalVenta() throws Exception {
        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(ventaDeTresProductos())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor_venta").value(10000d));
    }

    @Test
    @DisplayName("SP4-QA-8: validacion del campo Total IVA de los tres productos")
    void sp4QA8_validacionDelTotalIva() throws Exception {
        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(ventaDeTresProductos())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ivaventa").value(1620d));
    }

    @Test
    @DisplayName("SP4-QA-9: validacion del campo Total con IVA")
    void sp4QA9_validacionDelTotalConIva() throws Exception {
        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(ventaDeTresProductos())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total_venta").value(11620d));
    }

    @Test
    @DisplayName("SP4-QA-10: el consecutivo de la venta se genera exitosamente y se guarda el detalle")
    void sp4QA10_generacionDelConsecutivoYDetalleDeVenta() throws Exception {
        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(ventaDeTresProductos())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo_venta").isNumber())
                .andExpect(jsonPath("$.mensaje").isString());

        List<Venta> ventas = ventaRepositorio.findAll();
        assertEquals(1, ventas.size());
        Venta venta = ventas.get(0);
        assertNotNull(venta.getCodigoVenta(), "El codigo de venta debe ser un consecutivo generado");
        assertEquals(CEDULA_CLIENTE, venta.getCedulaCliente());
        assertEquals(CEDULA_USUARIO, venta.getCedulaUsuario());

        List<DetalleVenta> detalles = detalleVentaRepositorio.findByCodigoVenta(venta.getCodigoVenta());
        assertEquals(3, detalles.size(), "Debe guardarse una fila de detalle por cada producto vendido");

        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(ventaDeTresProductos())))
                .andExpect(status().isCreated());
        List<Venta> todas = ventaRepositorio.findAll();
        assertEquals(2, todas.size());
        assertTrue(todas.get(1).getCodigoVenta() > todas.get(0).getCodigoVenta(),
                "El codigo de venta debe ser consecutivo");
    }

    @Test
    @DisplayName("Regla funcional: la venta admite un maximo de tres (3) productos")
    void ventaConMasDeTresProductosEsRechazada() throws Exception {
        crearProducto(4L, "Lechuga", 1L, 700d, 19d);
        List<ItemVentaDto> cuatro = Arrays.asList(
                new ItemVentaDto(1L, 1), new ItemVentaDto(2L, 1),
                new ItemVentaDto(3L, 1), new ItemVentaDto(4L, 1));

        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(
                                new RegistrarVentaDto(CEDULA_CLIENTE, CEDULA_USUARIO, cuatro))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La venta admite un maximo de 3 productos"));
    }
}
