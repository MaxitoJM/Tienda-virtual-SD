package com.tiendagenerica.frontend.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Impide el acceso a los modulos del sistema sin haber pasado por el login.
 * Las paginas publicas son la de ingreso y los recursos estaticos.
 */
@WebFilter("/*")
public class SesionFiltro implements Filter {

    @Override
    public void doFilter(ServletRequest peticion, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) peticion;
        HttpServletResponse httpRespuesta = (HttpServletResponse) respuesta;

        String ruta = http.getRequestURI().substring(http.getContextPath().length());
        boolean publica = ruta.isEmpty() || "/".equals(ruta)
                || ruta.startsWith("/inicio.jsp") || ruta.startsWith("/login")
                || ruta.startsWith("/css/") || ruta.startsWith("/js/");

        HttpSession sesion = http.getSession(false);
        boolean autenticado = sesion != null && sesion.getAttribute("usuario") != null;

        if (publica || autenticado) {
            cadena.doFilter(peticion, respuesta);
            return;
        }
        httpRespuesta.sendRedirect(http.getContextPath() + "/inicio.jsp");
    }
}
