package com.tiendagenerica.frontend.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/** Modulo de Gestion de Clientes del frontend (Sprint 2). */
@WebServlet("/clientes")
public class ClienteServlet extends CrudServlet {

    private static final long serialVersionUID = 1L;

    @Override protected String recurso() { return "/clientes"; }
    @Override protected String vista() { return "/clientes.jsp"; }
    @Override protected String campoId() { return "cedula_cliente"; }

    @Override
    protected Map<String, Object> desdeFormulario(HttpServletRequest peticion) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("cedula_cliente", aLong(valor(peticion, "cedula_cliente")));
        cuerpo.put("nombre_cliente", valor(peticion, "nombre_cliente"));
        cuerpo.put("direccion_cliente", valor(peticion, "direccion_cliente"));
        cuerpo.put("telefono_cliente", valor(peticion, "telefono_cliente"));
        cuerpo.put("email_cliente", valor(peticion, "email_cliente"));
        return cuerpo;
    }
}
