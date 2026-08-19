package com.tiendagenerica.backend.sprint3;

import com.tiendagenerica.backend.PruebaBase;
import com.tiendagenerica.backend.modelo.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sprint 3 - Modulo de Gestion de Productos (carga CSV). Casos SP3-QA-1 a SP3-QA-4. */
class Sprint3ProductosTest extends PruebaBase {

    private static final String CSV_VALIDO =
            "codigo_producto,nombre_producto,nitproveedor,precio_compra,ivacompra,precio_venta\n"
            + "1,Melocotones,1,25505,19,30351\n"
            + "2,Manzanas,3,18108,19,21549\n"
            + "3,Platanos,4,29681,19,35320\n";

    @BeforeEach
    void prepararProveedores() {
        crearProveedor(1L, "Proveedor Uno");
        crearProveedor(2L, "Proveedor Dos");
        crearProveedor(3L, "Proveedor Tres");
        crearProveedor(4L, "Proveedor Cuatro");
        crearProveedor(5L, "Proveedor Cinco");
    }

    private MockMultipartFile archivo(String nombre, String contenido) {
        return new MockMultipartFile("archivo", nombre, "text/csv",
                contenido.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("SP3-QA-1: carga exitosa del archivo CSV con reemplazo de la tabla productos")
    void sp3QA1_cargaExitosaDelArchivo() throws Exception {
        // Producto preexistente que debe ser reemplazado por la carga (HU-014).
        crearProducto(99L, "Producto Anterior", 1L, 1000d, 19d);

        mockMvc.perform(multipart("/productos/cargar").file(archivo("productos.csv", CSV_VALIDO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Archivo Cargado correctamente"))
                .andExpect(jsonPath("$.registros_cargados").value(3));

        assertEquals(3, productoRepositorio.count(), "La tabla debe contener solo los productos del archivo");
        Producto melocotones = productoRepositorio.findById(1L).orElseThrow(AssertionError::new);
        assertEquals("Melocotones", melocotones.getNombreProducto());
        assertEquals(30351d, melocotones.getPrecioVenta());
        assertEquals(19d, melocotones.getIvacompra());
    }

    @Test
    @DisplayName("SP3-QA-2: carga fallida por no seleccionar archivo")
    void sp3QA2_cargaFallidaSinArchivo() throws Exception {
        mockMvc.perform(multipart("/productos/cargar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se selecciono archivo para cargar"));
    }

    @Test
    @DisplayName("SP3-QA-3: carga fallida por errores en el formato del archivo")
    void sp3QA3_cargaFallidaPorFormatoInvalido() throws Exception {
        mockMvc.perform(multipart("/productos/cargar")
                        .file(archivo("productos.txt", "esto no es un csv valido")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Error en el formato del archivo: se esperaba un archivo separado por comas (CSV)"));

        assertEquals(0, productoRepositorio.count());
    }

    @Test
    @DisplayName("SP3-QA-4: carga fallida por errores en la validacion de los datos leidos")
    void sp3QA4_cargaFallidaPorTiposDeDatoInvalidos() throws Exception {
        String csvConErrores =
                "codigo_producto,nombre_producto,nitproveedor,precio_compra,ivacompra,precio_venta\n"
                + "1,Melocotones,1,25505,19,30351\n"
                + "abc,Manzanas,3,18108,19,21549\n";

        mockMvc.perform(multipart("/productos/cargar").file(archivo("productos.csv", csvConErrores)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.errores[0]").value(
                        "Linea 3: el campo codigo_producto debe ser un numero entero y se recibio: abc"));

        assertEquals(0, productoRepositorio.count(), "No debe cargarse ningun registro si hay errores");
    }

    @Test
    @DisplayName("SP3-QA-4: carga fallida porque el NIT del proveedor no existe en la base de datos")
    void sp3QA4_cargaFallidaPorNitDeProveedorInexistente() throws Exception {
        String csvNitInvalido =
                "codigo_producto,nombre_producto,nitproveedor,precio_compra,ivacompra,precio_venta\n"
                + "1,Melocotones,99999,25505,19,30351\n";

        mockMvc.perform(multipart("/productos/cargar").file(archivo("productos.csv", csvNitInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0]").value(
                        "Linea 2: el NIT de proveedor 99999 no existe en la base de datos"));

        assertEquals(0, productoRepositorio.count());
    }
}
