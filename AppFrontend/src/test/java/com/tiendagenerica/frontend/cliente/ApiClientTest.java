package com.tiendagenerica.frontend.cliente;

import com.tiendagenerica.frontend.BackendSimulado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas del cliente HTTP que comunica el frontend con la API del backend. */
class ApiClientTest {

    private BackendSimulado backend;
    private ApiClient api;

    @BeforeEach
    void iniciar() throws Exception {
        backend = new BackendSimulado();
        api = new ApiClient(backend.url());
    }

    @AfterEach
    void detener() {
        backend.detener();
    }

    @Test
    @DisplayName("GET recupera el cuerpo de la respuesta y lo interpreta como JSON")
    void peticionGetCorrecta() throws Exception {
        backend.responder("GET", "/clientes/consultar/52123456", 200,
                "{\"cedula_cliente\":52123456,\"nombre_cliente\":\"Maria Lopez\"}");

        ApiClient.RespuestaHttp respuesta = api.get("/clientes/consultar/52123456");

        assertTrue(respuesta.esExitosa());
        assertEquals(200, respuesta.getCodigo());
        assertEquals("Maria Lopez", respuesta.comoJson().get("nombre_cliente").asText());
    }

    @Test
    @DisplayName("POST envia el cuerpo serializado con los nombres del contrato")
    void peticionPostEnviaElCuerpo() throws Exception {
        backend.responder("POST", "/clientes/guardar", 201, "{\"cedula_cliente\":1}");
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("cedula_cliente", 1);
        cuerpo.put("nombre_cliente", "Ana Gomez");

        ApiClient.RespuestaHttp respuesta = api.post("/clientes/guardar", cuerpo);

        assertEquals(201, respuesta.getCodigo());
        BackendSimulado.PeticionRecibida recibida = backend.ultimaPeticion();
        assertEquals("POST", recibida.metodo);
        assertTrue(recibida.cuerpo.contains("Ana Gomez"), "El cuerpo debe viajar en la peticion");
    }

    @Test
    @DisplayName("PUT y DELETE usan el metodo HTTP correspondiente")
    void peticionesPutYDelete() throws Exception {
        backend.responder("PUT", "/clientes/actualizar", 200, "{}");
        backend.responder("DELETE", "/clientes/eliminar/1", 200, "{\"exitoso\":true}");

        api.put("/clientes/actualizar", new LinkedHashMap<String, Object>());
        assertEquals("PUT", backend.ultimaPeticion().metodo);

        ApiClient.RespuestaHttp borrado = api.delete("/clientes/eliminar/1");
        assertEquals("DELETE", backend.ultimaPeticion().metodo);
        assertTrue(borrado.esExitosa());
    }

    @Test
    @DisplayName("Una respuesta de error se marca como fallida y expone su mensaje")
    void respuestaDeError() throws Exception {
        backend.responder("GET", "/clientes/consultar/999", 404,
                "{\"exitoso\":false,\"mensaje\":\"La cedula 999 no se encuentra registrada\"}");

        ApiClient.RespuestaHttp respuesta = api.get("/clientes/consultar/999");

        assertFalse(respuesta.esExitosa());
        assertEquals(404, respuesta.getCodigo());
        assertEquals("La cedula 999 no se encuentra registrada", respuesta.mensaje());
    }

    @Test
    @DisplayName("Si la respuesta de error no trae mensaje se genera uno generico")
    void respuestaDeErrorSinMensaje() throws Exception {
        backend.responder("GET", "/clientes/listar", 500, "");

        ApiClient.RespuestaHttp respuesta = api.get("/clientes/listar");

        assertFalse(respuesta.esExitosa());
        assertTrue(respuesta.mensaje().contains("500"),
                "El mensaje generico debe indicar el codigo HTTP recibido");
    }

    @Test
    @DisplayName("comoLista convierte un arreglo JSON en una lista de mapas")
    void conversionAListaDeMapas() throws Exception {
        backend.responder("GET", "/reportes/clientes", 200,
                "[{\"cedula_cliente\":1,\"nombre_cliente\":\"Ana\"},"
                + "{\"cedula_cliente\":2,\"nombre_cliente\":\"Luis\"}]");

        List<Map<String, Object>> filas = api.get("/reportes/clientes").comoLista();

        assertEquals(2, filas.size());
        assertEquals("Ana", filas.get(0).get("nombre_cliente"));
        assertEquals(2, ((Number) filas.get(1).get("cedula_cliente")).intValue());
    }

    @Test
    @DisplayName("comoMapa convierte un objeto JSON en un mapa")
    void conversionAMapa() throws Exception {
        backend.responder("GET", "/reportes/ventasporcliente", 200,
                "{\"total_general_ventas\":3570.0,\"clientes\":[]}");

        Map<String, Object> reporte = api.get("/reportes/ventasporcliente").comoMapa();

        assertEquals(3570.0, ((Number) reporte.get("total_general_ventas")).doubleValue());
        assertTrue(reporte.containsKey("clientes"));
    }

    @Test
    @DisplayName("Una respuesta con cuerpo vacio produce estructuras vacias, no un error")
    void cuerpoVacio() throws Exception {
        backend.responder("GET", "/productos/listar", 200, "");

        ApiClient.RespuestaHttp respuesta = api.get("/productos/listar");

        assertTrue(respuesta.comoLista().isEmpty());
        assertTrue(respuesta.comoMapa().isEmpty());
        assertEquals(0, respuesta.comoJson().size());
    }

    @Test
    @DisplayName("subirArchivo envia el contenido como multipart con su nombre de archivo")
    void envioDeArchivoMultipart() throws Exception {
        backend.responder("POST", "/productos/cargar", 200,
                "{\"exitoso\":true,\"registros_cargados\":18}");
        byte[] contenido = "codigo_producto,nombre_producto\n1,Melocotones\n"
                .getBytes(StandardCharsets.UTF_8);

        ApiClient.RespuestaHttp respuesta =
                api.subirArchivo("/productos/cargar", "archivo", "productos.csv", contenido);

        assertTrue(respuesta.esExitosa());
        String enviado = backend.ultimaPeticion().cuerpo;
        assertTrue(enviado.contains("productos.csv"), "Debe viajar el nombre del archivo");
        assertTrue(enviado.contains("archivo"), "Debe viajar el nombre del campo");
        assertTrue(enviado.contains("Melocotones"), "Debe viajar el contenido del archivo");
    }

    @Test
    @DisplayName("La direccion base se normaliza quitando la barra final")
    void normalizacionDeLaUrlBase() {
        assertEquals("http://servidor:5000", new ApiClient("http://servidor:5000/").getUrlBase());
        assertEquals("http://servidor:5000", new ApiClient("http://servidor:5000").getUrlBase());
    }

    @Test
    @DisplayName("Sin configuracion explicita se usa la direccion local por defecto")
    void direccionPorDefecto() {
        String anterior = System.getProperty("backend.url");
        System.clearProperty("backend.url");
        try {
            assertEquals("http://localhost:5000", ApiClient.resolverUrlBase());
        } finally {
            if (anterior != null) {
                System.setProperty("backend.url", anterior);
            }
        }
    }

    @Test
    @DisplayName("La propiedad backend.url tiene prioridad sobre el valor por defecto")
    void direccionDesdePropiedadDeSistema() {
        String anterior = System.getProperty("backend.url");
        System.setProperty("backend.url", "http://entorno-aws:5000");
        try {
            assertEquals("http://entorno-aws:5000", ApiClient.resolverUrlBase());
        } finally {
            if (anterior == null) {
                System.clearProperty("backend.url");
            } else {
                System.setProperty("backend.url", anterior);
            }
        }
    }
}
