package com.tiendagenerica.frontend.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/** Modulo de Gestion de Proveedores del frontend (Sprint 2). */
@WebServlet("/proveedores")
public class ProveedorServlet extends CrudServlet {

    private static final long serialVersionUID = 1L;

    @Override protected String recurso() { return "/proveedores"; }
    @Override protected String vista() { return "/proveedores.jsp"; }
    @Override protected String campoId() { return "nitproveedor"; }

    @Override
    protected Map<String, Object> desdeFormulario(HttpServletRequest peticion) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("nitproveedor", aLong(valor(peticion, "nitproveedor")));
        cuerpo.put("nombre_proveedor", valor(peticion, "nombre_proveedor"));
        cuerpo.put("direccion_proveedor", valor(peticion, "direccion_proveedor"));
        cuerpo.put("telefono_proveedor", valor(peticion, "telefono_proveedor"));
        cuerpo.put("ciudad_proveedor", valor(peticion, "ciudad_proveedor"));
        return cuerpo;
    }
}
