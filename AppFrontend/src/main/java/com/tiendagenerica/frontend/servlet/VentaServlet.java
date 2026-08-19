package com.tiendagenerica.frontend.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.tiendagenerica.frontend.cliente.ApiClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Modulo de Gestion de Ventas del frontend (Sprint 4).
 * Reproduce el formulario del documento: consulta del cliente por cedula,
 * hasta tres (3) lineas de producto y confirmacion de la venta.
 */
@WebServlet("/ventas")
public class VentaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int MAX_PRODUCTOS = 3;

    private final transient ApiClient api = new ApiClient();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        reenviar(peticion, respuesta);
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String accion = peticion.getParameter("accion");
        conservarFormulario(peticion);
        try {
            if ("consultarCliente".equals(accion)) {
                consultarCliente(peticion);
            } else if (accion != null && accion.startsWith("consultarProducto")) {
                consultarProducto(peticion, accion.substring("consultarProducto".length()));
            } else if ("confirmar".equals(accion)) {
                confirmar(peticion);
            } else {
                peticion.setAttribute("error", "Debe seleccionar una accion valida");
            }
        } catch (IOException ex) {
            peticion.setAttribute("error", "No fue posible comunicarse con el backend: " + ex.getMessage());
        }
        reenviar(peticion, respuesta);
    }

    /** SP4-QA-1 y SP4-QA-2: consulta de los datos del cliente por cedula. */
    private void consultarCliente(HttpServletRequest peticion) throws IOException {
        String cedula = texto(peticion, "cedula_cliente");
        if (cedula == null) {
            peticion.setAttribute("error", "Debe escribir la cedula del cliente");
            return;
        }
        ApiClient.RespuestaHttp resultado = api.get("/clientes/consultar/" + cedula);
        if (resultado.esExitosa()) {
            peticion.setAttribute("nombre_cliente", resultado.comoJson().path("nombre_cliente").asText());
        } else {
            peticion.setAttribute("error", resultado.mensaje());
        }
    }

    /** SP4-QA-3 y SP4-QA-4: consulta del producto por codigo. */
    private void consultarProducto(HttpServletRequest peticion, String indice) throws IOException {
        String codigo = texto(peticion, "codigo_producto" + indice);
        if (codigo == null) {
            peticion.setAttribute("error", "Debe escribir el codigo del producto");
            return;
        }
        ApiClient.RespuestaHttp resultado = api.get("/productos/consultar/" + codigo);
        if (!resultado.esExitosa()) {
            peticion.setAttribute("error", resultado.mensaje());
            return;
        }
        JsonNode producto = resultado.comoJson();
        peticion.setAttribute("nombre_producto" + indice, producto.path("nombre_producto").asText());
        peticion.setAttribute("precio_venta" + indice, producto.path("precio_venta").asDouble());
        calcularTotalLinea(peticion, indice, producto.path("precio_venta").asDouble());
    }

    /** SP4-QA-5 y SP4-QA-6: valida la cantidad y calcula cantidad x precio de venta. */
    private void calcularTotalLinea(HttpServletRequest peticion, String indice, double precioVenta) {
        String cantidad = texto(peticion, "cantidad_producto" + indice);
        if (cantidad == null) {
            return;
        }
        try {
            int unidades = Integer.parseInt(cantidad);
            if (unidades > 0) {
                peticion.setAttribute("valor_total" + indice, unidades * precioVenta);
            } else {
                peticion.setAttribute("error",
                        "El valor de cantidad es incorrecto: debe ser un numero mayor que cero");
            }
        } catch (NumberFormatException ex) {
            peticion.setAttribute("error",
                    "El valor de cantidad es incorrecto: debe ser un numero entero");
        }
    }

    /** HU-020: confirmacion y registro definitivo de la venta. */
    private void confirmar(HttpServletRequest peticion) throws IOException {
        String cedulaCliente = texto(peticion, "cedula_cliente");
        if (cedulaCliente == null) {
            peticion.setAttribute("error", "Debe consultar primero la cedula del cliente");
            return;
        }
        Object cedulaUsuario = peticion.getSession().getAttribute("cedulaUsuario");
        if (cedulaUsuario == null) {
            peticion.setAttribute("error", "La sesion expiro. Ingrese nuevamente al sistema");
            return;
        }

        List<Map<String, Object>> productos = new ArrayList<>();
        for (int i = 1; i <= MAX_PRODUCTOS; i++) {
            String codigo = texto(peticion, "codigo_producto" + i);
            String cantidad = texto(peticion, "cantidad_producto" + i);
            if (codigo == null && cantidad == null) {
                continue;
            }
            if (codigo == null || cantidad == null) {
                peticion.setAttribute("error",
                        "La linea " + i + " esta incompleta: indique codigo de producto y cantidad");
                return;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("codigo_producto", codigo);
            item.put("cantidad_producto", cantidad);
            productos.add(item);
        }
        if (productos.isEmpty()) {
            peticion.setAttribute("error", "Debe registrar al menos un producto para la venta");
            return;
        }

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("cedula_cliente", cedulaCliente);
        cuerpo.put("cedula_usuario", cedulaUsuario);
        cuerpo.put("productos", productos);

        ApiClient.RespuestaHttp resultado = api.post("/Ventas/registrar", cuerpo);
        if (!resultado.esExitosa()) {
            peticion.setAttribute("error", resultado.mensaje());
            return;
        }
        JsonNode venta = resultado.comoJson();
        peticion.setAttribute("codigo_venta", venta.path("codigo_venta").asLong());
        peticion.setAttribute("total_venta_sin_iva", venta.path("valor_venta").asDouble());
        peticion.setAttribute("total_iva", venta.path("ivaventa").asDouble());
        peticion.setAttribute("total_con_iva", venta.path("total_venta").asDouble());
        peticion.setAttribute("nombre_cliente", venta.path("nombre_cliente").asText());
        peticion.setAttribute("info", venta.path("mensaje").asText());

        JsonNode detalles = venta.path("detalles");
        for (int i = 0; i < detalles.size() && i < MAX_PRODUCTOS; i++) {
            int linea = i + 1;
            peticion.setAttribute("valor_total" + linea, detalles.get(i).path("valor_total").asDouble());
            // Se recupera el nombre para que la pantalla de confirmacion quede completa.
            ApiClient.RespuestaHttp producto =
                    api.get("/productos/consultar/" + detalles.get(i).path("codigo_producto").asLong());
            if (producto.esExitosa()) {
                peticion.setAttribute("nombre_producto" + linea,
                        producto.comoJson().path("nombre_producto").asText());
            }
        }
    }

    /** Mantiene en pantalla los datos ya escritos por el usuario. */
    private void conservarFormulario(HttpServletRequest peticion) {
        peticion.setAttribute("form", peticion.getParameterMap());
    }

    private void reenviar(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        peticion.getRequestDispatcher("/ventas.jsp").forward(peticion, respuesta);
    }

    private String texto(HttpServletRequest peticion, String nombre) {
        String contenido = peticion.getParameter(nombre);
        if (contenido == null) {
            return null;
        }
        contenido = contenido.trim();
        return contenido.isEmpty() ? null : contenido;
    }
}
