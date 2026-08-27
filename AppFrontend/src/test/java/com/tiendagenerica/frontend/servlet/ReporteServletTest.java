package com.tiendagenerica.frontend.servlet;

import com.tiendagenerica.frontend.PruebaServletBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas del modulo de consultas y reportes del frontend. */
class ReporteServletTest extends PruebaServletBase {

    private ReporteServlet servlet;

    @BeforeEach
    void crearServlet() {
        servlet = new ReporteServlet();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> filas(String atributo) {
        return (List<Map<String, Object>>) atributos.get(atributo);
    }

    @Test
    @DisplayName("Sin tipo de reporte se muestra el menu de consultas")
    void sinTipoMuestraElMenu() throws Exception {
        servlet.doGet(peticion, respuesta);

        assertEquals("/reportes.jsp", vistaReenviada);
        assertTrue(backend.peticiones().isEmpty());
    }

    @Test
    @DisplayName("El listado de usuarios llega como filas recorribles por la vista")
    void listadoDeUsuarios() throws Exception {
        backend.responder("GET", "/reportes/usuarios", 200,
                "[{\"cedula_usuario\":1,\"nombre_usuario\":\"Administrador\",\"password\":\"********\"}]");
        parametro("tipo", "usuarios");

        servlet.doGet(peticion, respuesta);

        assertEquals("/reportes/usuarios.jsp", vistaReenviada);
        assertEquals(1, filas("usuarios").size());
        assertEquals("********", filas("usuarios").get(0).get("password"));
        assertNull(info());
    }

    @Test
    @DisplayName("Un listado de usuarios vacio genera un mensaje informativo")
    void listadoDeUsuariosVacio() throws Exception {
        backend.responder("GET", "/reportes/usuarios", 200, "[]");
        parametro("tipo", "usuarios");

        servlet.doGet(peticion, respuesta);

        assertEquals("No existen usuarios registrados en el sistema", info());
        assertTrue(filas("usuarios").isEmpty());
    }

    @Test
    @DisplayName("El listado de clientes llega como filas recorribles por la vista")
    void listadoDeClientes() throws Exception {
        backend.responder("GET", "/reportes/clientes", 200,
                "[{\"cedula_cliente\":52123456,\"nombre_cliente\":\"Maria Lopez\"},"
                + "{\"cedula_cliente\":79987654,\"nombre_cliente\":\"Carlos Ruiz\"}]");
        parametro("tipo", "clientes");

        servlet.doGet(peticion, respuesta);

        assertEquals("/reportes/clientes.jsp", vistaReenviada);
        assertEquals(2, filas("clientes").size());
    }

    @Test
    @DisplayName("El total de ventas por cliente llega como mapa con su consolidado")
    void totalDeVentasPorCliente() throws Exception {
        backend.responder("GET", "/reportes/ventasporcliente", 200,
                "{\"clientes\":[{\"cedula_cliente\":52123456,\"valor_total_ventas\":183246.91}],"
                + "\"total_general_ventas\":183246.91,\"mensaje\":null}");
        parametro("tipo", "ventasporcliente");

        servlet.doGet(peticion, respuesta);

        assertEquals("/reportes/ventas-por-cliente.jsp", vistaReenviada);
        @SuppressWarnings("unchecked")
        Map<String, Object> reporte = (Map<String, Object>) atributos.get("reporte");
        assertEquals(183246.91, ((Number) reporte.get("total_general_ventas")).doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Un tipo de reporte inexistente se rechaza")
    void tipoDeReporteInexistente() throws Exception {
        parametro("tipo", "inventario");

        servlet.doGet(peticion, respuesta);

        assertEquals("El tipo de reporte solicitado no existe", error());
        assertEquals("/reportes.jsp", vistaReenviada);
    }

    @Test
    @DisplayName("Si el backend responde con error se traslada su mensaje")
    void errorDelBackend() throws Exception {
        backend.responder("GET", "/reportes/clientes", 500,
                "{\"exitoso\":false,\"mensaje\":\"Error interno del sistema\"}");
        parametro("tipo", "clientes");

        servlet.doGet(peticion, respuesta);

        assertEquals("Error interno del sistema", error());
    }
}
