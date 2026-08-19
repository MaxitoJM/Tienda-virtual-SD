package com.tiendagenerica.frontend.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.tiendagenerica.frontend.cliente.ApiClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Modulo de Gestion de Productos del frontend (Sprint 3, HU-014).
 * Recibe el archivo CSV desde el formulario y lo reenvia al backend.
 */
@WebServlet("/productos")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 5 * 1024 * 1024)
public class ProductoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient ApiClient api = new ApiClient();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        peticion.getRequestDispatcher("/productos.jsp").forward(peticion, respuesta);
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        Part parte = null;
        try {
            parte = peticion.getPart("archivo");
        } catch (ServletException | IllegalStateException ex) {
            peticion.setAttribute("error", "No fue posible leer el archivo seleccionado");
        }

        // SP3-QA-2: el usuario oprime Cargar sin haber seleccionado un archivo.
        if (parte == null || parte.getSize() == 0 || vacio(parte.getSubmittedFileName())) {
            peticion.setAttribute("error", "No se selecciono archivo para cargar");
            peticion.getRequestDispatcher("/productos.jsp").forward(peticion, respuesta);
            return;
        }

        try (InputStream entrada = parte.getInputStream()) {
            ApiClient.RespuestaHttp resultado = api.subirArchivo("/productos/cargar", "archivo",
                    parte.getSubmittedFileName(), leerTodo(entrada));
            JsonNode datos = resultado.comoJson();

            if (resultado.esExitosa()) {
                peticion.setAttribute("info", datos.path("mensaje").asText()
                        + ". Registros cargados: " + datos.path("registros_cargados").asInt());
                cargarListado(peticion);
            } else {
                peticion.setAttribute("error", resultado.mensaje());
                peticion.setAttribute("errores", detalleErrores(datos));
            }
        } catch (IOException ex) {
            peticion.setAttribute("error",
                    "No fue posible comunicarse con el backend: " + ex.getMessage());
        }
        peticion.getRequestDispatcher("/productos.jsp").forward(peticion, respuesta);
    }

    private void cargarListado(HttpServletRequest peticion) throws IOException {
        ApiClient.RespuestaHttp listado = api.get("/productos/listar");
        if (listado.esExitosa()) {
            peticion.setAttribute("productos", listado.comoLista());
        }
    }

    private List<String> detalleErrores(JsonNode datos) {
        List<String> errores = new ArrayList<>();
        if (datos.has("errores") && datos.get("errores").isArray()) {
            for (JsonNode error : datos.get("errores")) {
                errores.add(error.asText());
            }
        }
        return errores;
    }

    private byte[] leerTodo(InputStream entrada) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        byte[] bloque = new byte[8192];
        int leidos;
        while ((leidos = entrada.read(bloque)) != -1) {
            salida.write(bloque, 0, leidos);
        }
        return salida.toByteArray();
    }

    private boolean vacio(String contenido) {
        return contenido == null || contenido.trim().isEmpty();
    }
}
