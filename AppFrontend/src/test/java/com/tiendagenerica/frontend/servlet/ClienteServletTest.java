package com.tiendagenerica.frontend.servlet;

import com.tiendagenerica.frontend.PruebaServletBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del modulo de gestion de clientes del frontend, que ejercitan el
 * comportamiento comun a todos los modulos CRUD: consultar, crear, actualizar
 * y borrar, con sus mensajes de resultado.
 */
class ClienteServletTest extends PruebaServletBase {

    private ClienteServlet servlet;

    /**
     * El servlet se construye aqui y no como campo de la clase: su cliente HTTP
     * resuelve la direccion del backend al instanciarse, y esta prueba necesita
     * que ya apunte al backend simulado que prepara la clase base.
     */
    @BeforeEach
    void crearServlet() {
        servlet = new ClienteServlet();
    }

    @Test
    @DisplayName("Consultar recupera los datos y los deja disponibles para el formulario")
    void consultaCorrecta() throws Exception {
        backend.responder("GET", "/clientes/consultar/52123456", 200,
                "{\"cedula_cliente\":52123456,\"nombre_cliente\":\"Maria Lopez\","
                + "\"direccion_cliente\":\"Calle 45\",\"telefono_cliente\":\"3109876543\","
                + "\"email_cliente\":\"maria@correo.com\"}");
        parametro("accion", "consultar");
        parametro("cedula_cliente", "52123456");

        servlet.doPost(peticion, respuesta);

        assertEquals("Datos recuperados correctamente", info());
        assertNull(error());
        assertEquals("Maria Lopez", datos().get("nombre_cliente"));
        assertEquals("maria@correo.com", datos().get("email_cliente"));
        assertEquals("/clientes.jsp", vistaReenviada);
    }

    @Test
    @DisplayName("Consultar una cedula inexistente muestra el mensaje de error del backend")
    void consultaFallida() throws Exception {
        backend.responder("GET", "/clientes/consultar/999", 404,
                "{\"exitoso\":false,\"mensaje\":\"La cedula 999 no se encuentra registrada\"}");
        parametro("accion", "consultar");
        parametro("cedula_cliente", "999");

        servlet.doPost(peticion, respuesta);

        assertEquals("La cedula 999 no se encuentra registrada", error());
        assertNull(info());
        assertNull(datos());
    }

    @Test
    @DisplayName("Consultar sin identificador no llama al backend")
    void consultaSinIdentificador() throws Exception {
        parametro("accion", "consultar");

        servlet.doPost(peticion, respuesta);

        assertEquals("Debe escribir el identificador para realizar la consulta", error());
        assertTrue(backend.peticiones().isEmpty(), "No debe realizarse ninguna llamada");
    }

    @Test
    @DisplayName("Crear envia los datos del formulario y confirma el registro")
    void creacionCorrecta() throws Exception {
        backend.responder("POST", "/clientes/guardar", 201, "{\"cedula_cliente\":52123456}");
        parametro("accion", "crear");
        parametro("cedula_cliente", "52123456");
        parametro("nombre_cliente", "Maria Lopez");
        parametro("direccion_cliente", "Calle 45");
        parametro("telefono_cliente", "3109876543");
        parametro("email_cliente", "maria@correo.com");

        servlet.doPost(peticion, respuesta);

        assertEquals("Registro creado correctamente", info());
        assertNull(error());
        String enviado = backend.ultimaPeticion().cuerpo;
        assertTrue(enviado.contains("Maria Lopez"));
        assertTrue(enviado.contains("52123456"));
    }

    @Test
    @DisplayName("Crear con datos incompletos conserva lo escrito y muestra el error")
    void creacionConDatosIncompletos() throws Exception {
        backend.responder("POST", "/clientes/guardar", 400,
                "{\"exitoso\":false,\"mensaje\":\"Datos incompletos: el telefono es obligatorio\"}");
        parametro("accion", "crear");
        parametro("cedula_cliente", "52123456");
        parametro("nombre_cliente", "Maria Lopez");

        servlet.doPost(peticion, respuesta);

        assertEquals("Datos incompletos: el telefono es obligatorio", error());
        assertNotNull(datos(), "Los datos escritos deben conservarse en el formulario");
        assertEquals("Maria Lopez", datos().get("nombre_cliente"));
    }

    @Test
    @DisplayName("Actualizar usa el metodo PUT del contrato")
    void actualizacionCorrecta() throws Exception {
        backend.responder("PUT", "/clientes/actualizar", 200, "{\"cedula_cliente\":52123456}");
        parametro("accion", "actualizar");
        parametro("cedula_cliente", "52123456");
        parametro("nombre_cliente", "Maria Lopez Actualizada");

        servlet.doPost(peticion, respuesta);

        assertEquals("Registro actualizado correctamente", info());
        assertEquals("PUT", backend.ultimaPeticion().metodo);
    }

    @Test
    @DisplayName("Borrar usa el metodo DELETE sobre el identificador indicado")
    void borradoCorrecto() throws Exception {
        backend.responder("DELETE", "/clientes/eliminar/52123456", 200, "{\"exitoso\":true}");
        parametro("accion", "borrar");
        parametro("cedula_cliente", "52123456");

        servlet.doPost(peticion, respuesta);

        assertEquals("Registro borrado correctamente", info());
        assertEquals("DELETE", backend.ultimaPeticion().metodo);
        assertEquals("/clientes/eliminar/52123456", backend.ultimaPeticion().ruta);
    }

    @Test
    @DisplayName("Borrar sin identificador no llama al backend")
    void borradoSinIdentificador() throws Exception {
        parametro("accion", "borrar");

        servlet.doPost(peticion, respuesta);

        assertEquals("Debe escribir el identificador del registro a borrar", error());
        assertTrue(backend.peticiones().isEmpty());
    }

    @Test
    @DisplayName("Una accion desconocida se rechaza con un mensaje claro")
    void accionDesconocida() throws Exception {
        parametro("accion", "exportar");

        servlet.doPost(peticion, respuesta);

        assertEquals("Debe seleccionar una accion valida", error());
    }

    @Test
    @DisplayName("Si el backend no responde se informa el fallo de comunicacion")
    void backendNoDisponible() throws Exception {
        backend.detener();
        parametro("accion", "consultar");
        parametro("cedula_cliente", "52123456");

        servlet.doPost(peticion, respuesta);

        assertNotNull(error());
        assertTrue(error().startsWith("No fue posible comunicarse con el backend"));
    }

    @Test
    @DisplayName("La peticion GET muestra el formulario vacio")
    void peticionGetMuestraElFormulario() throws Exception {
        servlet.doGet(peticion, respuesta);

        assertEquals("/clientes.jsp", vistaReenviada);
        assertTrue(backend.peticiones().isEmpty());
    }
}
