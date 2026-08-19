package com.tiendagenerica.frontend.servlet;

import com.tiendagenerica.frontend.cliente.ApiClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Modulo de Consultas y Reportes del frontend (Sprint 5).
 * Cada consulta lleva a una pagina nueva con el listado correspondiente.
 */
@WebServlet("/reportes")
public class ReporteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient ApiClient api = new ApiClient();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String tipo = peticion.getParameter("tipo");
        if (tipo == null || tipo.trim().isEmpty()) {
            peticion.getRequestDispatcher("/reportes.jsp").forward(peticion, respuesta);
            return;
        }

        try {
            switch (tipo) {
                case "usuarios":
                    consultar(peticion, "/reportes/usuarios", "usuarios",
                            "No existen usuarios registrados en el sistema");
                    peticion.getRequestDispatcher("/reportes/usuarios.jsp").forward(peticion, respuesta);
                    return;
                case "clientes":
                    consultar(peticion, "/reportes/clientes", "clientes",
                            "No existen clientes registrados en el sistema");
                    peticion.getRequestDispatcher("/reportes/clientes.jsp").forward(peticion, respuesta);
                    return;
                case "ventasporcliente":
                    consultar(peticion, "/reportes/ventasporcliente", "reporte", null);
                    peticion.getRequestDispatcher("/reportes/ventas-por-cliente.jsp")
                            .forward(peticion, respuesta);
                    return;
                default:
                    peticion.setAttribute("error", "El tipo de reporte solicitado no existe");
            }
        } catch (IOException ex) {
            peticion.setAttribute("error", "No fue posible comunicarse con el backend: " + ex.getMessage());
        }
        peticion.getRequestDispatcher("/reportes.jsp").forward(peticion, respuesta);
    }

    private void consultar(HttpServletRequest peticion, String ruta, String atributo, String mensajeVacio)
            throws IOException {
        ApiClient.RespuestaHttp resultado = api.get(ruta);
        if (!resultado.esExitosa()) {
            peticion.setAttribute("error", resultado.mensaje());
            return;
        }
        if ("reporte".equals(atributo)) {
            peticion.setAttribute(atributo, resultado.comoMapa());
            return;
        }
        java.util.List<java.util.Map<String, Object>> datos = resultado.comoLista();
        peticion.setAttribute(atributo, datos);
        if (mensajeVacio != null && datos.isEmpty()) {
            peticion.setAttribute("info", mensajeVacio);
        }
    }
}
