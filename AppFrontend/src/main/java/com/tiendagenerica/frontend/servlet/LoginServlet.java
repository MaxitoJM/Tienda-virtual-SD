package com.tiendagenerica.frontend.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.tiendagenerica.frontend.cliente.ApiClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Modulo de Login del frontend (Sprint 1, HU-001). */
@WebServlet({ "/login", "/salir" })
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient ApiClient api = new ApiClient();

    /** La ruta /salir cierra la sesion y regresa a la pagina de ingreso. */
    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        HttpSession sesion = peticion.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        respuesta.sendRedirect(peticion.getContextPath() + "/inicio.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String usuario = peticion.getParameter("usuario");
        String password = peticion.getParameter("password");

        Map<String, Object> credenciales = new LinkedHashMap<>();
        credenciales.put("usuario", usuario);
        credenciales.put("password", password);

        try {
            ApiClient.RespuestaHttp resultado = api.post("/usuarios/login", credenciales);
            if (resultado.esExitosa()) {
                JsonNode datos = resultado.comoJson();
                HttpSession sesion = peticion.getSession(true);
                sesion.setAttribute("usuario", datos.path("usuario").asText());
                sesion.setAttribute("nombreUsuario", datos.path("nombre_usuario").asText());
                sesion.setAttribute("cedulaUsuario", datos.path("cedula_usuario").asLong());
                respuesta.sendRedirect(peticion.getContextPath() + "/menu.jsp");
                return;
            }
            peticion.setAttribute("error", resultado.mensaje());
        } catch (IOException ex) {
            peticion.setAttribute("error",
                    "No fue posible comunicarse con el backend en " + api.getUrlBase()
                            + ". Verifique que la aplicacion de backend se encuentre en ejecucion.");
        }
        peticion.getRequestDispatcher("/inicio.jsp").forward(peticion, respuesta);
    }
}
