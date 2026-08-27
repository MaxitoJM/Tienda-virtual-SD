package com.tiendagenerica.frontend.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del filtro que impide entrar a los modulos del sistema sin haber
 * pasado por la pantalla de ingreso.
 */
class SesionFiltroTest {

    private SesionFiltro filtro;
    private HttpServletRequest peticion;
    private HttpServletResponse respuesta;
    private FilterChain cadena;
    private HttpSession sesion;
    private String redireccion;

    @BeforeEach
    void preparar() throws Exception {
        filtro = new SesionFiltro();
        peticion = mock(HttpServletRequest.class);
        respuesta = mock(HttpServletResponse.class);
        cadena = mock(FilterChain.class);
        sesion = mock(HttpSession.class);
        redireccion = null;

        when(peticion.getContextPath()).thenReturn("/ciclo3demo");
        doAnswer(i -> {
            redireccion = i.getArgument(0);
            return null;
        }).when(respuesta).sendRedirect(anyString());
    }

    private void solicitar(String ruta, boolean autenticado) throws Exception {
        when(peticion.getRequestURI()).thenReturn("/ciclo3demo" + ruta);
        when(peticion.getSession(false)).thenReturn(autenticado ? sesion : null);
        when(sesion.getAttribute("usuario")).thenReturn(autenticado ? "jperez" : null);
        filtro.doFilter(peticion, respuesta, cadena);
    }

    @Test
    @DisplayName("La pantalla de ingreso es publica")
    void laPantallaDeIngresoEsPublica() throws Exception {
        solicitar("/inicio.jsp", false);
        verify(cadena).doFilter(any(), any());
    }

    @Test
    @DisplayName("La ruta de autenticacion es publica")
    void laRutaDeAutenticacionEsPublica() throws Exception {
        solicitar("/login", false);
        verify(cadena).doFilter(any(), any());
    }

    @Test
    @DisplayName("Los recursos estaticos son publicos")
    void losRecursosEstaticosSonPublicos() throws Exception {
        solicitar("/css/estilo.css", false);
        verify(cadena).doFilter(any(), any());
    }

    @Test
    @DisplayName("La raiz del sitio es publica")
    void laRaizEsPublica() throws Exception {
        solicitar("/", false);
        verify(cadena).doFilter(any(), any());
    }

    @Test
    @DisplayName("Un modulo del sistema sin sesion redirige a la pantalla de ingreso")
    void moduloSinSesionRedirige() throws Exception {
        solicitar("/ventas", false);

        verify(cadena, never()).doFilter(any(), any());
        org.junit.jupiter.api.Assertions.assertEquals("/ciclo3demo/inicio.jsp", redireccion);
    }

    @Test
    @DisplayName("Un modulo del sistema con sesion abierta continua su curso")
    void moduloConSesionContinua() throws Exception {
        solicitar("/ventas", true);

        verify(cadena).doFilter(any(), any());
        org.junit.jupiter.api.Assertions.assertNull(redireccion);
    }

    @Test
    @DisplayName("Los reportes tambien quedan protegidos")
    void losReportesQuedanProtegidos() throws Exception {
        solicitar("/reportes?tipo=usuarios", false);

        verify(cadena, never()).doFilter(any(), any());
        org.junit.jupiter.api.Assertions.assertEquals("/ciclo3demo/inicio.jsp", redireccion);
    }
}
