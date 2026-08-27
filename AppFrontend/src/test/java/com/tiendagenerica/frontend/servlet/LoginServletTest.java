package com.tiendagenerica.frontend.servlet;

import com.tiendagenerica.frontend.PruebaServletBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

/** Pruebas del modulo de ingreso al sistema. */
class LoginServletTest extends PruebaServletBase {

    private LoginServlet servlet;

    @BeforeEach
    void crearServlet() {
        servlet = new LoginServlet();
    }

    @Test
    @DisplayName("Un ingreso correcto abre la sesion y lleva al menu general")
    void ingresoCorrecto() throws Exception {
        backend.responder("POST", "/usuarios/login", 200,
                "{\"autenticado\":true,\"mensaje\":\"Ingreso correcto al sistema\","
                + "\"cedula_usuario\":1020304050,\"nombre_usuario\":\"Juan Perez\",\"usuario\":\"jperez\"}");
        parametro("usuario", "jperez");
        parametro("password", "clave12345");

        servlet.doPost(peticion, respuesta);

        assertEquals("/ciclo3demo/menu.jsp", redireccion);
        assertEquals("jperez", sesionAtributos.get("usuario"));
        assertEquals("Juan Perez", sesionAtributos.get("nombreUsuario"));
        assertEquals(1020304050L, sesionAtributos.get("cedulaUsuario"));
    }

    @Test
    @DisplayName("Un ingreso incorrecto devuelve a la pantalla de ingreso con el mensaje del backend")
    void ingresoIncorrecto() throws Exception {
        backend.responder("POST", "/usuarios/login", 401,
                "{\"autenticado\":false,\"mensaje\":\"Usuario y/o contrasena errados, intente de nuevo\"}");
        parametro("usuario", "jperez");
        parametro("password", "clave-errada");

        servlet.doPost(peticion, respuesta);

        assertEquals("Usuario y/o contrasena errados, intente de nuevo", error());
        assertEquals("/inicio.jsp", vistaReenviada);
        assertNull(redireccion, "No debe avanzar al menu");
        assertTrue(sesionAtributos.isEmpty(), "No debe abrirse la sesion");
    }

    @Test
    @DisplayName("Las credenciales viajan al backend en el cuerpo de la peticion")
    void lasCredencialesViajanAlBackend() throws Exception {
        backend.responder("POST", "/usuarios/login", 401, "{\"autenticado\":false,\"mensaje\":\"error\"}");
        parametro("usuario", "admininicial");
        parametro("password", "admin123456");

        servlet.doPost(peticion, respuesta);

        String enviado = backend.ultimaPeticion().cuerpo;
        assertTrue(enviado.contains("admininicial"));
        assertTrue(enviado.contains("admin123456"));
        assertEquals("/usuarios/login", backend.ultimaPeticion().ruta);
    }

    @Test
    @DisplayName("Si el backend no responde se informa con la direccion configurada")
    void backendNoDisponible() throws Exception {
        backend.detener();
        parametro("usuario", "jperez");
        parametro("password", "clave12345");

        servlet.doPost(peticion, respuesta);

        assertNotNull(error());
        assertTrue(error().startsWith("No fue posible comunicarse con el backend"));
        assertEquals("/inicio.jsp", vistaReenviada);
    }

    @Test
    @DisplayName("La ruta de salida invalida la sesion y regresa a la pantalla de ingreso")
    void salidaDelSistema() throws Exception {
        servlet.doGet(peticion, respuesta);

        verify(sesion).invalidate();
        assertEquals("/ciclo3demo/inicio.jsp", redireccion);
    }
}
