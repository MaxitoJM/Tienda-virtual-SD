package com.tiendagenerica.frontend.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/** Modulo de Gestion de Usuarios del frontend (Sprint 1). */
@WebServlet("/usuarios")
public class UsuarioServlet extends CrudServlet {

    private static final long serialVersionUID = 1L;

    @Override protected String recurso() { return "/usuarios"; }
    @Override protected String vista() { return "/usuarios.jsp"; }
    @Override protected String campoId() { return "cedula_usuario"; }

    @Override
    protected Map<String, Object> desdeFormulario(HttpServletRequest peticion) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("cedula_usuario", aLong(valor(peticion, "cedula_usuario")));
        cuerpo.put("nombre_usuario", valor(peticion, "nombre_usuario"));
        cuerpo.put("email_usuario", valor(peticion, "email_usuario"));
        cuerpo.put("usuario", valor(peticion, "usuario"));
        cuerpo.put("password", valor(peticion, "password"));
        return cuerpo;
    }
}
