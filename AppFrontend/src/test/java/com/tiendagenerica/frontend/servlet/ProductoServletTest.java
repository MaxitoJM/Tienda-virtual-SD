package com.tiendagenerica.frontend.servlet;

import com.tiendagenerica.frontend.PruebaServletBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas del modulo de carga de productos desde archivo separado por comas. */
class ProductoServletTest extends PruebaServletBase {

    private static final String CSV =
            "codigo_producto,nombre_producto,nitproveedor,precio_compra,ivacompra,precio_venta\n"
            + "1,Melocotones,1,25505,19,30351\n";

    private ProductoServlet servlet;

    @BeforeEach
    void crearServlet() {
        servlet = new ProductoServlet();
    }

    /** Simula el archivo que el navegador adjunta al formulario. */
    private Part archivo(String nombre, String contenido) throws Exception {
        Part parte = mock(Part.class);
        byte[] datos = contenido.getBytes(StandardCharsets.UTF_8);
        when(parte.getSize()).thenReturn((long) datos.length);
        when(parte.getSubmittedFileName()).thenReturn(nombre);
        when(parte.getInputStream()).thenReturn(new ByteArrayInputStream(datos));
        when(peticion.getPart("archivo")).thenReturn(parte);
        return parte;
    }

    @Test
    @DisplayName("Una carga correcta informa los registros cargados y lista los productos")
    void cargaCorrecta() throws Exception {
        archivo("productos.csv", CSV);
        backend.responder("POST", "/productos/cargar", 200,
                "{\"exitoso\":true,\"mensaje\":\"Archivo Cargado correctamente\","
                + "\"registros_leidos\":18,\"registros_cargados\":18,\"errores\":[]}");
        backend.responder("GET", "/productos/listar", 200,
                "[{\"codigo_producto\":1,\"nombre_producto\":\"Melocotones\"}]");

        servlet.doPost(peticion, respuesta);

        assertEquals("Archivo Cargado correctamente. Registros cargados: 18", info());
        assertNull(error());
        assertEquals("/productos.jsp", vistaReenviada);
        assertTrue(backend.ultimaPeticion().ruta.equals("/productos/listar"),
                "Tras cargar debe recuperarse el catalogo para mostrarlo");
    }

    @Test
    @DisplayName("El contenido del archivo viaja al backend como multipart")
    void elArchivoViajaAlBackend() throws Exception {
        archivo("productos.csv", CSV);
        backend.responder("POST", "/productos/cargar", 200,
                "{\"exitoso\":true,\"mensaje\":\"Archivo Cargado correctamente\",\"registros_cargados\":1}");
        backend.responder("GET", "/productos/listar", 200, "[]");

        servlet.doPost(peticion, respuesta);

        String enviado = backend.peticiones().get(0).cuerpo;
        assertTrue(enviado.contains("productos.csv"), "Debe viajar el nombre del archivo");
        assertTrue(enviado.contains("Melocotones"), "Debe viajar el contenido del archivo");
    }

    @Test
    @DisplayName("Oprimir Cargar sin seleccionar archivo no llama al backend")
    void cargaSinArchivo() throws Exception {
        when(peticion.getPart("archivo")).thenReturn(null);

        servlet.doPost(peticion, respuesta);

        assertEquals("No se selecciono archivo para cargar", error());
        assertTrue(backend.peticiones().isEmpty());
        assertEquals("/productos.jsp", vistaReenviada);
    }

    @Test
    @DisplayName("Un archivo vacio se trata como si no se hubiera seleccionado")
    void archivoVacio() throws Exception {
        Part parte = mock(Part.class);
        when(parte.getSize()).thenReturn(0L);
        when(parte.getSubmittedFileName()).thenReturn("productos.csv");
        when(peticion.getPart("archivo")).thenReturn(parte);

        servlet.doPost(peticion, respuesta);

        assertEquals("No se selecciono archivo para cargar", error());
        assertTrue(backend.peticiones().isEmpty());
    }

    @Test
    @DisplayName("Un archivo sin nombre se trata como si no se hubiera seleccionado")
    void archivoSinNombre() throws Exception {
        Part parte = mock(Part.class);
        when(parte.getSize()).thenReturn(120L);
        when(parte.getSubmittedFileName()).thenReturn("   ");
        when(peticion.getPart("archivo")).thenReturn(parte);

        servlet.doPost(peticion, respuesta);

        assertEquals("No se selecciono archivo para cargar", error());
    }

    @Test
    @DisplayName("Los errores de validacion del archivo se muestran linea por linea")
    void erroresDeValidacion() throws Exception {
        archivo("productos.csv", CSV);
        backend.responder("POST", "/productos/cargar", 400,
                "{\"exitoso\":false,\"mensaje\":\"Error en los datos leidos del archivo\","
                + "\"errores\":[\"Linea 3: el campo codigo_producto debe ser un numero entero\","
                + "\"Linea 5: el NIT de proveedor 99999 no existe en la base de datos\"]}");

        servlet.doPost(peticion, respuesta);

        assertEquals("Error en los datos leidos del archivo", error());
        @SuppressWarnings("unchecked")
        List<String> errores = (List<String>) atributos.get("errores");
        assertEquals(2, errores.size());
        assertTrue(errores.get(0).startsWith("Linea 3:"));
        assertTrue(errores.get(1).contains("99999"));
    }

    @Test
    @DisplayName("Un archivo con formato invalido muestra el mensaje del backend")
    void formatoInvalido() throws Exception {
        archivo("productos.txt", "esto no es un csv");
        backend.responder("POST", "/productos/cargar", 400,
                "{\"exitoso\":false,\"mensaje\":\"Error en el formato del archivo: "
                + "se esperaba un archivo separado por comas (CSV)\",\"errores\":[]}");

        servlet.doPost(peticion, respuesta);

        assertTrue(error().startsWith("Error en el formato del archivo"));
        assertNull(atributos.get("productos"), "No debe listarse el catalogo si la carga fallo");
    }

    @Test
    @DisplayName("Si el backend no responde se informa el fallo de comunicacion")
    void backendNoDisponible() throws Exception {
        archivo("productos.csv", CSV);
        backend.detener();

        servlet.doPost(peticion, respuesta);

        assertTrue(error().startsWith("No fue posible comunicarse con el backend"));
    }

    @Test
    @DisplayName("La peticion GET muestra el formulario de carga")
    void peticionGetMuestraElFormulario() throws Exception {
        servlet.doGet(peticion, respuesta);

        assertEquals("/productos.jsp", vistaReenviada);
        assertTrue(backend.peticiones().isEmpty());
    }
}
