package com.tiendagenerica.backend.api;

import com.tiendagenerica.backend.PruebaBase;
import com.tiendagenerica.backend.modelo.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Operaciones unitarias sobre productos declaradas en la especificacion de la API
 * del documento del proyecto, que no forman parte del conjunto de pruebas QA pero
 * si del contrato publicado.
 */
class ApiProductosTest extends PruebaBase {

    @BeforeEach
    void prepararProveedor() {
        crearProveedor(1L, "Proveedor Uno");
    }

    private Producto nuevoProducto() {
        return new Producto(100L, "Melocotones", 1L, 25505d, 19d, 30351d);
    }

    @Test
    @DisplayName("POST /productos/guardar crea el producto y devuelve 201")
    void guardarProductoCorrecto() throws Exception {
        mockMvc.perform(post("/productos/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(nuevoProducto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo_producto").value(100L))
                .andExpect(jsonPath("$.nombre_producto").value("Melocotones"));

        assertEquals(1, productoRepositorio.count());
    }

    @Test
    @DisplayName("POST /productos/guardar rechaza un NIT de proveedor inexistente")
    void guardarProductoConProveedorInexistente() throws Exception {
        Producto producto = nuevoProducto();
        producto.setNitproveedor(999L);

        mockMvc.perform(post("/productos/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(producto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "El NIT de proveedor 999 no existe en la base de datos"));

        assertEquals(0, productoRepositorio.count());
    }

    @Test
    @DisplayName("POST /productos/guardar rechaza un codigo de producto repetido")
    void guardarProductoDuplicado() throws Exception {
        crearProducto(100L, "Melocotones", 1L, 30351d, 19d);

        mockMvc.perform(post("/productos/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(nuevoProducto())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Ya existe un producto registrado con el codigo 100"));
    }

    @Test
    @DisplayName("POST /productos/guardar exige el nombre del producto")
    void guardarProductoSinNombre() throws Exception {
        Producto producto = nuevoProducto();
        producto.setNombreProducto("   ");

        mockMvc.perform(post("/productos/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(producto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Datos incompletos: el nombre del producto es obligatorio"));
    }

    @Test
    @DisplayName("POST /productos/guardar exige el codigo del producto")
    void guardarProductoSinCodigo() throws Exception {
        Producto producto = nuevoProducto();
        producto.setCodigoProducto(null);

        mockMvc.perform(post("/productos/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(producto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Datos incompletos: el codigo de producto es obligatorio"));
    }

    @Test
    @DisplayName("PUT /productos/actualizar modifica los datos del producto")
    void actualizarProducto() throws Exception {
        crearProducto(100L, "Melocotones", 1L, 30351d, 19d);
        Producto cambios = new Producto(100L, "Melocotones en almibar", 1L, 26000d, 5d, 31000d);

        mockMvc.perform(put("/productos/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_producto").value("Melocotones en almibar"))
                .andExpect(jsonPath("$.ivacompra").value(5d))
                .andExpect(jsonPath("$.precio_venta").value(31000d));
    }

    @Test
    @DisplayName("PUT /productos/actualizar sobre un producto inexistente devuelve 404")
    void actualizarProductoInexistente() throws Exception {
        mockMvc.perform(put("/productos/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(nuevoProducto())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(
                        "El codigo de producto 100 no se encuentra registrado en la base de datos"));
    }

    @Test
    @DisplayName("DELETE /productos/eliminar borra el producto")
    void eliminarProducto() throws Exception {
        crearProducto(100L, "Melocotones", 1L, 30351d, 19d);

        mockMvc.perform(delete("/productos/eliminar/{id}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));

        assertFalse(productoRepositorio.existsById(100L));
    }

    @Test
    @DisplayName("DELETE /productos/eliminar sobre un producto inexistente devuelve 404")
    void eliminarProductoInexistente() throws Exception {
        mockMvc.perform(delete("/productos/eliminar/{id}", 100L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false));
    }

    @Test
    @DisplayName("GET /productos/listar devuelve el catalogo completo")
    void listarProductos() throws Exception {
        crearProducto(100L, "Melocotones", 1L, 30351d, 19d);
        crearProducto(101L, "Manzanas", 1L, 21549d, 19d);

        mockMvc.perform(get("/productos/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
