package com.tiendagenerica.frontend.servlet;

import com.tiendagenerica.frontend.PruebaServletBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas del modulo de registro de ventas del frontend. */
class VentaServletTest extends PruebaServletBase {

    private VentaServlet servlet;

    @BeforeEach
    void crearServlet() {
        servlet = new VentaServlet();
        sesionAtributos.put("cedulaUsuario", 1020304050L);
    }

    @Test
    @DisplayName("Consultar el cliente por cedula despliega su nombre")
    void consultaDelCliente() throws Exception {
        backend.responder("GET", "/clientes/consultar/52123456", 200,
                "{\"cedula_cliente\":52123456,\"nombre_cliente\":\"Maria Lopez\"}");
        parametro("accion", "consultarCliente");
        parametro("cedula_cliente", "52123456");

        servlet.doPost(peticion, respuesta);

        assertEquals("Maria Lopez", atributos.get("nombre_cliente"));
        assertNull(error());
        assertEquals("/ventas.jsp", vistaReenviada);
    }

    @Test
    @DisplayName("Consultar una cedula no registrada muestra el error del backend")
    void consultaDeClienteFallida() throws Exception {
        backend.responder("GET", "/clientes/consultar/999", 404,
                "{\"exitoso\":false,\"mensaje\":\"La cedula 999 no se encuentra registrada\"}");
        parametro("accion", "consultarCliente");
        parametro("cedula_cliente", "999");

        servlet.doPost(peticion, respuesta);

        assertEquals("La cedula 999 no se encuentra registrada", error());
    }

    @Test
    @DisplayName("Consultar el producto calcula el valor total de la linea")
    void consultaDeProductoCalculaElTotal() throws Exception {
        backend.responder("GET", "/productos/consultar/1", 200,
                "{\"codigo_producto\":1,\"nombre_producto\":\"Melocotones\",\"precio_venta\":30351.0}");
        parametro("accion", "consultarProducto1");
        parametro("codigo_producto1", "1");
        parametro("cantidad_producto1", "2");

        servlet.doPost(peticion, respuesta);

        assertEquals("Melocotones", atributos.get("nombre_producto1"));
        assertEquals(60702.0, (Double) atributos.get("valor_total1"), 0.001);
    }

    @Test
    @DisplayName("Una cantidad de cero o negativa se rechaza")
    void cantidadInvalida() throws Exception {
        backend.responder("GET", "/productos/consultar/1", 200,
                "{\"codigo_producto\":1,\"nombre_producto\":\"Melocotones\",\"precio_venta\":30351.0}");
        parametro("accion", "consultarProducto1");
        parametro("codigo_producto1", "1");
        parametro("cantidad_producto1", "0");

        servlet.doPost(peticion, respuesta);

        assertEquals("El valor de cantidad es incorrecto: debe ser un numero mayor que cero", error());
        assertNull(atributos.get("valor_total1"));
    }

    @Test
    @DisplayName("Una cantidad que no es numerica se rechaza")
    void cantidadNoNumerica() throws Exception {
        backend.responder("GET", "/productos/consultar/1", 200,
                "{\"codigo_producto\":1,\"nombre_producto\":\"Melocotones\",\"precio_venta\":30351.0}");
        parametro("accion", "consultarProducto1");
        parametro("codigo_producto1", "1");
        parametro("cantidad_producto1", "dos");

        servlet.doPost(peticion, respuesta);

        assertEquals("El valor de cantidad es incorrecto: debe ser un numero entero", error());
    }

    @Test
    @DisplayName("Confirmar envia la venta y despliega el consecutivo con los totales")
    void confirmacionDeVenta() throws Exception {
        backend.responder("POST", "/Ventas/registrar", 201,
                "{\"codigo_venta\":1,\"cedula_cliente\":52123456,\"nombre_cliente\":\"Maria Lopez\","
                + "\"valor_venta\":153989.0,\"ivaventa\":29257.91,\"total_venta\":183246.91,"
                + "\"detalles\":[{\"codigo_producto\":1,\"valor_total\":60702.0}],"
                + "\"mensaje\":\"Venta registrada correctamente con el consecutivo 1\"}");
        backend.responder("GET", "/productos/consultar/1", 200,
                "{\"codigo_producto\":1,\"nombre_producto\":\"Melocotones\"}");
        parametro("accion", "confirmar");
        parametro("cedula_cliente", "52123456");
        parametro("codigo_producto1", "1");
        parametro("cantidad_producto1", "2");

        servlet.doPost(peticion, respuesta);

        assertEquals(1L, atributos.get("codigo_venta"));
        assertEquals(153989.0, (Double) atributos.get("total_venta_sin_iva"), 0.001);
        assertEquals(29257.91, (Double) atributos.get("total_iva"), 0.001);
        assertEquals(183246.91, (Double) atributos.get("total_con_iva"), 0.001);
        assertEquals("Melocotones", atributos.get("nombre_producto1"));
        assertEquals("Venta registrada correctamente con el consecutivo 1", info());
    }

    @Test
    @DisplayName("Confirmar sin la cedula del cliente no llama al backend")
    void confirmacionSinCliente() throws Exception {
        parametro("accion", "confirmar");

        servlet.doPost(peticion, respuesta);

        assertEquals("Debe consultar primero la cedula del cliente", error());
        assertTrue(backend.peticiones().isEmpty());
    }

    @Test
    @DisplayName("Confirmar sin ningun producto se rechaza")
    void confirmacionSinProductos() throws Exception {
        parametro("accion", "confirmar");
        parametro("cedula_cliente", "52123456");

        servlet.doPost(peticion, respuesta);

        assertEquals("Debe registrar al menos un producto para la venta", error());
    }

    @Test
    @DisplayName("Una linea con codigo pero sin cantidad se rechaza indicando el numero de linea")
    void lineaIncompleta() throws Exception {
        parametro("accion", "confirmar");
        parametro("cedula_cliente", "52123456");
        parametro("codigo_producto2", "10");

        servlet.doPost(peticion, respuesta);

        assertEquals("La linea 2 esta incompleta: indique codigo de producto y cantidad", error());
    }

    @Test
    @DisplayName("Si la sesion expiro se pide ingresar de nuevo")
    void sesionExpirada() throws Exception {
        sesionAtributos.clear();
        parametro("accion", "confirmar");
        parametro("cedula_cliente", "52123456");
        parametro("codigo_producto1", "1");
        parametro("cantidad_producto1", "2");

        servlet.doPost(peticion, respuesta);

        assertEquals("La sesion expiro. Ingrese nuevamente al sistema", error());
    }

    @Test
    @DisplayName("El backend puede rechazar la venta y su mensaje se traslada a la pantalla")
    void ventaRechazadaPorElBackend() throws Exception {
        backend.responder("POST", "/Ventas/registrar", 400,
                "{\"exitoso\":false,\"mensaje\":\"La venta admite un maximo de 3 productos\"}");
        parametro("accion", "confirmar");
        parametro("cedula_cliente", "52123456");
        parametro("codigo_producto1", "1");
        parametro("cantidad_producto1", "2");

        servlet.doPost(peticion, respuesta);

        assertEquals("La venta admite un maximo de 3 productos", error());
        assertNull(atributos.get("codigo_venta"));
    }

    @Test
    @DisplayName("Una accion desconocida se rechaza")
    void accionDesconocida() throws Exception {
        parametro("accion", "anular");

        servlet.doPost(peticion, respuesta);

        assertEquals("Debe seleccionar una accion valida", error());
    }
}
