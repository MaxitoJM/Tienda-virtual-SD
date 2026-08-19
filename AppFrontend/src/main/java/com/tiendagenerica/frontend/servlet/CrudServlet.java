package com.tiendagenerica.frontend.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.tiendagenerica.frontend.cliente.ApiClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base comun de los modulos CRUD del frontend (usuarios, clientes y proveedores).
 *
 * Implementa el comportamiento descrito en las interfaces graficas del documento:
 * un unico formulario con los botones Consultar, Crear, Actualizar y Borrar, que
 * al terminar muestra un mensaje y limpia los datos del formulario.
 */
public abstract class CrudServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected transient ApiClient api = new ApiClient();

    /** Ruta base del recurso en el backend, por ejemplo /clientes. */
    protected abstract String recurso();

    /** Pagina JSP que renderiza el formulario del modulo. */
    protected abstract String vista();

    /** Nombre del campo identificador dentro del formulario. */
    protected abstract String campoId();

    /** Construye el cuerpo JSON a partir de los campos del formulario. */
    protected abstract Map<String, Object> desdeFormulario(HttpServletRequest peticion);

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        reenviar(peticion, respuesta);
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String accion = valor(peticion, "accion");
        try {
            switch (accion == null ? "" : accion) {
                case "consultar":
                    consultar(peticion);
                    break;
                case "crear":
                    enviar(peticion, true);
                    break;
                case "actualizar":
                    enviar(peticion, false);
                    break;
                case "borrar":
                    borrar(peticion);
                    break;
                default:
                    peticion.setAttribute("error", "Debe seleccionar una accion valida");
            }
        } catch (IOException ex) {
            peticion.setAttribute("error", "No fue posible comunicarse con el backend: " + ex.getMessage());
        }
        reenviar(peticion, respuesta);
    }

    private void consultar(HttpServletRequest peticion) throws IOException {
        String id = valor(peticion, campoId());
        if (id == null || id.isEmpty()) {
            peticion.setAttribute("error", "Debe escribir el identificador para realizar la consulta");
            return;
        }
        ApiClient.RespuestaHttp respuesta = api.get(recurso() + "/consultar/" + id);
        if (!respuesta.esExitosa()) {
            peticion.setAttribute("error", respuesta.mensaje());
            return;
        }
        JsonNode datos = respuesta.comoJson();
        Map<String, String> formulario = new LinkedHashMap<>();
        Iterator<String> campos = datos.fieldNames();
        while (campos.hasNext()) {
            String campo = campos.next();
            formulario.put(campo, datos.get(campo).isNull() ? "" : datos.get(campo).asText());
        }
        peticion.setAttribute("datos", formulario);
        peticion.setAttribute("info", "Datos recuperados correctamente");
    }

    private void enviar(HttpServletRequest peticion, boolean creacion) throws IOException {
        Map<String, Object> cuerpo = desdeFormulario(peticion);
        ApiClient.RespuestaHttp respuesta = creacion
                ? api.post(recurso() + "/guardar", cuerpo)
                : api.put(recurso() + "/actualizar", cuerpo);
        if (respuesta.esExitosa()) {
            peticion.setAttribute("info", creacion
                    ? "Registro creado correctamente"
                    : "Registro actualizado correctamente");
        } else {
            peticion.setAttribute("error", respuesta.mensaje());
            peticion.setAttribute("datos", soloTexto(cuerpo));
        }
    }

    private void borrar(HttpServletRequest peticion) throws IOException {
        String id = valor(peticion, campoId());
        if (id == null || id.isEmpty()) {
            peticion.setAttribute("error", "Debe escribir el identificador del registro a borrar");
            return;
        }
        ApiClient.RespuestaHttp respuesta = api.delete(recurso() + "/eliminar/" + id);
        if (respuesta.esExitosa()) {
            peticion.setAttribute("info", "Registro borrado correctamente");
        } else {
            peticion.setAttribute("error", respuesta.mensaje());
        }
    }

    private Map<String, String> soloTexto(Map<String, Object> cuerpo) {
        Map<String, String> resultado = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entrada : cuerpo.entrySet()) {
            resultado.put(entrada.getKey(), entrada.getValue() == null ? "" : String.valueOf(entrada.getValue()));
        }
        return resultado;
    }

    private void reenviar(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        peticion.getRequestDispatcher(vista()).forward(peticion, respuesta);
    }

    /** Devuelve el parametro recortado, o null si viene vacio. */
    protected static String valor(HttpServletRequest peticion, String nombre) {
        String contenido = peticion.getParameter(nombre);
        if (contenido == null) {
            return null;
        }
        contenido = contenido.trim();
        return contenido.isEmpty() ? null : contenido;
    }

    /** Convierte a Long o devuelve null si el dato no es numerico. */
    protected static Long aLong(String contenido) {
        try {
            return contenido == null ? null : Long.valueOf(contenido);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
