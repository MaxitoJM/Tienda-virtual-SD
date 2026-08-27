package com.tiendagenerica.frontend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base de las pruebas de los servlets.
 *
 * Levanta el backend simulado, apunta el cliente HTTP hacia el y prepara los
 * dobles de la peticion y la respuesta, registrando los atributos que el
 * servlet deposita y la vista a la que reenvia.
 */
public abstract class PruebaServletBase {

    protected BackendSimulado backend;
    protected HttpServletRequest peticion;
    protected HttpServletResponse respuesta;
    protected HttpSession sesion;

    /** Atributos que el servlet coloca en la peticion. */
    protected final Map<String, Object> atributos = new HashMap<>();
    /** Parametros del formulario que simula enviar el usuario. */
    protected final Map<String, String> parametros = new HashMap<>();
    /** Atributos de la sesion. */
    protected final Map<String, Object> sesionAtributos = new HashMap<>();
    /** Vista a la que el servlet reenvio la peticion. */
    protected String vistaReenviada;
    /** Direccion a la que el servlet redirigio. */
    protected String redireccion;

    private String propiedadAnterior;

    @BeforeEach
    void prepararEntorno() throws Exception {
        backend = new BackendSimulado();
        propiedadAnterior = System.getProperty("backend.url");
        System.setProperty("backend.url", backend.url());

        atributos.clear();
        parametros.clear();
        sesionAtributos.clear();
        vistaReenviada = null;
        redireccion = null;

        peticion = mock(HttpServletRequest.class);
        respuesta = mock(HttpServletResponse.class);
        sesion = mock(HttpSession.class);

        when(peticion.getParameter(anyString())).thenAnswer(i -> parametros.get(i.getArgument(0)));
        when(peticion.getParameterMap()).thenReturn(new HashMap<>());
        when(peticion.getContextPath()).thenReturn("/ciclo3demo");
        when(peticion.getSession()).thenReturn(sesion);
        when(peticion.getSession(true)).thenReturn(sesion);
        when(peticion.getSession(false)).thenReturn(sesion);

        doAnswer(i -> atributos.put(i.getArgument(0), i.getArgument(1)))
                .when(peticion).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        when(peticion.getAttribute(anyString())).thenAnswer(i -> atributos.get(i.getArgument(0)));

        when(sesion.getAttribute(anyString())).thenAnswer(i -> sesionAtributos.get(i.getArgument(0)));
        doAnswer(i -> sesionAtributos.put(i.getArgument(0), i.getArgument(1)))
                .when(sesion).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());

        when(peticion.getRequestDispatcher(anyString())).thenAnswer(i -> {
            vistaReenviada = i.getArgument(0);
            RequestDispatcher despachador = mock(RequestDispatcher.class);
            return despachador;
        });

        doAnswer(i -> {
            redireccion = i.getArgument(0);
            return null;
        }).when(respuesta).sendRedirect(anyString());
    }

    @AfterEach
    void limpiarEntorno() {
        backend.detener();
        if (propiedadAnterior == null) {
            System.clearProperty("backend.url");
        } else {
            System.setProperty("backend.url", propiedadAnterior);
        }
    }

    protected void parametro(String nombre, String valor) {
        parametros.put(nombre, valor);
    }

    protected String info() {
        Object valor = atributos.get("info");
        return valor == null ? null : String.valueOf(valor);
    }

    protected String error() {
        Object valor = atributos.get("error");
        return valor == null ? null : String.valueOf(valor);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> datos() {
        return (Map<String, String>) atributos.get("datos");
    }
}
