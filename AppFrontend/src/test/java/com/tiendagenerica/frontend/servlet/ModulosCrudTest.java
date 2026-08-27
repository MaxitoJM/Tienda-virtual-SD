package com.tiendagenerica.frontend.servlet;

import com.tiendagenerica.frontend.PruebaServletBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que cada modulo CRUD apunte al recurso correcto de la API, use su
 * campo identificador y traslade al backend todos los campos de su formulario.
 * Es la comprobacion que detecta una errata en el nombre de un campo, que de
 * otro modo solo se manifestaria en tiempo de ejecucion.
 */
class ModulosCrudTest extends PruebaServletBase {

    @Test
    @DisplayName("El modulo de usuarios envia los cinco campos de la tabla usuarios")
    void moduloDeUsuarios() throws Exception {
        backend.responder("POST", "/usuarios/guardar", 201, "{}");
        UsuarioServlet servlet = new UsuarioServlet();
        parametro("accion", "crear");
        parametro("cedula_usuario", "1020304050");
        parametro("nombre_usuario", "Juan Perez Gomez");
        parametro("email_usuario", "juan@tiendagenerica.com");
        parametro("usuario", "jperez");
        parametro("password", "clave12345");

        servlet.doPost(peticion, respuesta);

        assertEquals("Registro creado correctamente", info());
        String enviado = backend.ultimaPeticion().cuerpo;
        for (String campo : new String[] { "cedula_usuario", "nombre_usuario", "email_usuario",
                                           "usuario", "password" }) {
            assertTrue(enviado.contains(campo), "Falta el campo " + campo + " en la peticion");
        }
        assertTrue(enviado.contains("Juan Perez Gomez"));
    }

    @Test
    @DisplayName("El modulo de usuarios consulta por cedula")
    void consultaDeUsuarioPorCedula() throws Exception {
        backend.responder("GET", "/usuarios/consultar/1020304050", 200,
                "{\"cedula_usuario\":1020304050,\"usuario\":\"jperez\"}");
        UsuarioServlet servlet = new UsuarioServlet();
        parametro("accion", "consultar");
        parametro("cedula_usuario", "1020304050");

        servlet.doPost(peticion, respuesta);

        assertEquals("/usuarios/consultar/1020304050", backend.ultimaPeticion().ruta);
        assertEquals("jperez", datos().get("usuario"));
        assertEquals("/usuarios.jsp", vistaReenviada);
    }

    @Test
    @DisplayName("El modulo de proveedores envia los cinco campos de la tabla proveedores")
    void moduloDeProveedores() throws Exception {
        backend.responder("POST", "/proveedores/guardar", 201, "{}");
        ProveedorServlet servlet = new ProveedorServlet();
        parametro("accion", "crear");
        parametro("nitproveedor", "900123456");
        parametro("nombre_proveedor", "Distribuidora Nacional");
        parametro("direccion_proveedor", "Carrera 15 No 80-25");
        parametro("telefono_proveedor", "6017654321");
        parametro("ciudad_proveedor", "Bogota");

        servlet.doPost(peticion, respuesta);

        assertEquals("Registro creado correctamente", info());
        String enviado = backend.ultimaPeticion().cuerpo;
        for (String campo : new String[] { "nitproveedor", "nombre_proveedor", "direccion_proveedor",
                                           "telefono_proveedor", "ciudad_proveedor" }) {
            assertTrue(enviado.contains(campo), "Falta el campo " + campo + " en la peticion");
        }
    }

    @Test
    @DisplayName("El modulo de proveedores consulta por NIT")
    void consultaDeProveedorPorNit() throws Exception {
        backend.responder("GET", "/proveedores/consultar/900123456", 200,
                "{\"nitproveedor\":900123456,\"ciudad_proveedor\":\"Bogota\"}");
        ProveedorServlet servlet = new ProveedorServlet();
        parametro("accion", "consultar");
        parametro("nitproveedor", "900123456");

        servlet.doPost(peticion, respuesta);

        assertEquals("/proveedores/consultar/900123456", backend.ultimaPeticion().ruta);
        assertEquals("Bogota", datos().get("ciudad_proveedor"));
        assertEquals("/proveedores.jsp", vistaReenviada);
    }

    @Test
    @DisplayName("Un identificador que no es numerico viaja como nulo y el backend lo rechaza")
    void identificadorNoNumerico() throws Exception {
        backend.responder("POST", "/usuarios/guardar", 400,
                "{\"exitoso\":false,\"mensaje\":\"Datos incompletos: la cedula es obligatoria\"}");
        UsuarioServlet servlet = new UsuarioServlet();
        parametro("accion", "crear");
        parametro("cedula_usuario", "mil veinte");
        parametro("nombre_usuario", "Juan Perez");

        servlet.doPost(peticion, respuesta);

        assertEquals("Datos incompletos: la cedula es obligatoria", error());
        assertTrue(backend.ultimaPeticion().cuerpo.contains("null"),
                "La cedula no numerica debe viajar como nula");
    }
}
