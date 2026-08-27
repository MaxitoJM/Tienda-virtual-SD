package com.tiendagenerica.backend.api;

import com.tiendagenerica.backend.PruebaBase;
import com.tiendagenerica.backend.dto.ItemVentaDto;
import com.tiendagenerica.backend.dto.RegistrarVentaDto;
import com.tiendagenerica.backend.modelo.DetalleVenta;
import com.tiendagenerica.backend.modelo.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Operaciones sobre ventas y detalle de ventas declaradas en la especificacion
 * de la API del documento del proyecto.
 */
class ApiVentasTest extends PruebaBase {

    private static final Long CLIENTE = 52123456L;
    private static final Long USUARIO = 1020304050L;

    @BeforeEach
    void prepararDatos() {
        crearUsuario(USUARIO, "jperez", "clave12345");
        crearCliente(CLIENTE, "Maria Lopez");
        crearProveedor(1L, "Proveedor Uno");
        crearProducto(1L, "Melocotones", 1L, 1000d, 19d);
    }

    private Long registrarVenta() throws Exception {
        RegistrarVentaDto solicitud = new RegistrarVentaDto(CLIENTE, USUARIO,
                Collections.singletonList(new ItemVentaDto(1L, 2)));
        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isCreated());
        return ventaRepositorio.findAll().get(0).getCodigoVenta();
    }

    @Test
    @DisplayName("POST /Ventas/registrar rechaza una cedula de usuario inexistente")
    void registrarVentaConUsuarioInexistente() throws Exception {
        RegistrarVentaDto solicitud = new RegistrarVentaDto(CLIENTE, 999999L,
                Collections.singletonList(new ItemVentaDto(1L, 1)));

        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(
                        "La cedula del usuario 999999 no se encuentra registrada en la base de datos"));
    }

    @Test
    @DisplayName("POST /Ventas/registrar exige al menos un producto")
    void registrarVentaSinProductos() throws Exception {
        RegistrarVentaDto solicitud = new RegistrarVentaDto(CLIENTE, USUARIO, Collections.emptyList());

        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /Ventas/consultar recupera la venta registrada")
    void consultarVenta() throws Exception {
        Long codigo = registrarVenta();

        mockMvc.perform(get("/Ventas/consultar/{id}", codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cedula_cliente").value(CLIENTE))
                .andExpect(jsonPath("$.total_venta").value(2380d));
    }

    @Test
    @DisplayName("GET /Ventas/consultar sobre una venta inexistente devuelve 404")
    void consultarVentaInexistente() throws Exception {
        mockMvc.perform(get("/Ventas/consultar/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(
                        "La venta con codigo 999 no se encuentra registrada en la base de datos"));
    }

    @Test
    @DisplayName("PUT /Ventas/actualizar modifica la cabecera de la venta")
    void actualizarVenta() throws Exception {
        Long codigo = registrarVenta();
        Venta cambios = new Venta(codigo, CLIENTE, USUARIO, 3000d, 570d, 3570d);

        mockMvc.perform(put("/Ventas/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_venta").value(3570d));
    }

    @Test
    @DisplayName("PUT /Ventas/actualizar exige el codigo de venta")
    void actualizarVentaSinCodigo() throws Exception {
        Venta cambios = new Venta(null, CLIENTE, USUARIO, 3000d, 570d, 3570d);

        mockMvc.perform(put("/Ventas/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(cambios)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El codigo de venta es obligatorio"));
    }

    @Test
    @DisplayName("DELETE /Ventas/eliminar borra la venta junto con su detalle")
    void eliminarVentaConSuDetalle() throws Exception {
        Long codigo = registrarVenta();
        assertEquals(1, detalleVentaRepositorio.findByCodigoVenta(codigo).size());

        mockMvc.perform(delete("/Ventas/eliminar/{id}", codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));

        assertEquals(0, ventaRepositorio.count());
        assertEquals(0, detalleVentaRepositorio.count(), "El detalle debe borrarse con la venta");
    }

    @Test
    @DisplayName("GET /detalleventas/venta devuelve las lineas de una venta")
    void listarDetallePorVenta() throws Exception {
        Long codigo = registrarVenta();

        mockMvc.perform(get("/detalleventas/venta/{codigo}", codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigo_producto").value(1L))
                .andExpect(jsonPath("$[0].cantidad_producto").value(2));
    }

    @Test
    @DisplayName("GET /detalleventas/consultar sobre un detalle inexistente devuelve 404")
    void consultarDetalleInexistente() throws Exception {
        mockMvc.perform(get("/detalleventas/consultar/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(
                        "El detalle de venta 999 no se encuentra registrado en la base de datos"));
    }

    @Test
    @DisplayName("POST, PUT y DELETE sobre /detalleventas gestionan una linea de detalle")
    void gestionarLineaDeDetalle() throws Exception {
        Long codigo = registrarVenta();
        DetalleVenta nuevo = new DetalleVenta(null, codigo, 1L, 3, 1000d, 570d, 3000d);

        String creado = mockMvc.perform(post("/detalleventas/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidad_producto").value(3))
                .andReturn().getResponse().getContentAsString();

        DetalleVenta guardado = json.readValue(creado, DetalleVenta.class);
        guardado.setCantidadProducto(5);

        mockMvc.perform(put("/detalleventas/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(guardado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad_producto").value(5));

        mockMvc.perform(delete("/detalleventas/eliminar/{id}", guardado.getCodigoDetalleVenta()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));
    }

    @Test
    @DisplayName("PUT /detalleventas/actualizar exige el codigo de detalle")
    void actualizarDetalleSinCodigo() throws Exception {
        DetalleVenta sinCodigo = new DetalleVenta(null, 1L, 1L, 1, 1000d, 190d, 1000d);

        mockMvc.perform(put("/detalleventas/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(sinCodigo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El codigo de detalle de venta es obligatorio"));
    }

    @Test
    @DisplayName("Los listados de ventas y de detalle devuelven los registros")
    void listados() throws Exception {
        registrarVenta();

        mockMvc.perform(get("/Ventas/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/detalleventas/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
