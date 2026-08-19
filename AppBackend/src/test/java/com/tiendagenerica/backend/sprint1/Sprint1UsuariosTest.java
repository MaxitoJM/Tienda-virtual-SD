package com.tiendagenerica.backend.sprint1;

import com.tiendagenerica.backend.PruebaBase;
import com.tiendagenerica.backend.modelo.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sprint 1 - Modulo de Gestion de Usuarios. Casos SP1-QA-3 a SP1-QA-10. */
class Sprint1UsuariosTest extends PruebaBase {

    private Usuario nuevoUsuario() {
        return new Usuario(1020304050L, "Juan Perez Gomez",
                "juan.perez@tiendagenerica.com", "jperez", "clave12345");
    }

    @Test
    @DisplayName("SP1-QA-3: creacion de un nuevo usuario correcto")
    void sp1QA3_creacionDeUsuarioCorrecta() throws Exception {
        mockMvc.perform(post("/usuarios/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(nuevoUsuario())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cedula_usuario").value(1020304050L))
                .andExpect(jsonPath("$.usuario").value("jperez"))
                .andExpect(jsonPath("$.password").value("********"));

        Usuario guardado = usuarioRepositorio.findById(1020304050L).orElseThrow(AssertionError::new);
        assertEquals("Juan Perez Gomez", guardado.getNombreUsuario());
        assertTrue(codificador.matches("clave12345", guardado.getPassword()),
                "La contrasena debe quedar almacenada cifrada");
    }

    @Test
    @DisplayName("SP1-QA-4: creacion de usuario con errores por falta de completitud")
    void sp1QA4_creacionDeUsuarioConDatosIncompletos() throws Exception {
        Usuario incompleto = nuevoUsuario();
        incompleto.setEmailUsuario("");

        mockMvc.perform(post("/usuarios/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(incompleto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value(
                        "Datos incompletos: el correo electronico es obligatorio"));

        assertFalse(usuarioRepositorio.existsById(1020304050L),
                "No debe insertarse el usuario cuando faltan datos");
    }

    @Test
    @DisplayName("SP1-QA-5: consulta de usuario existente por cedula")
    void sp1QA5_consultaDeUsuarioExistente() throws Exception {
        crearUsuario(1020304050L, "jperez", "clave12345");

        mockMvc.perform(get("/usuarios/consultar/{id}", 1020304050L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_usuario").value("Usuario de Pruebas"))
                .andExpect(jsonPath("$.email_usuario").value("jperez@tiendagenerica.com"))
                .andExpect(jsonPath("$.usuario").value("jperez"));
    }

    @Test
    @DisplayName("SP1-QA-6: consulta de usuario inexistente")
    void sp1QA6_consultaDeUsuarioInexistente() throws Exception {
        mockMvc.perform(get("/usuarios/consultar/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value(
                        "El usuario con cedula 999999 no se encuentra registrado en la base de datos"));
    }

    @Test
    @DisplayName("SP1-QA-7: actualizacion correcta de los datos de usuario")
    void sp1QA7_actualizacionCorrectaDeUsuario() throws Exception {
        crearUsuario(1020304050L, "jperez", "clave12345");
        Usuario cambios = new Usuario(1020304050L, "Juan Perez Actualizado",
                "nuevo.correo@tiendagenerica.com", "jperez2", "nuevaClave1");

        mockMvc.perform(put("/usuarios/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_usuario").value("Juan Perez Actualizado"))
                .andExpect(jsonPath("$.usuario").value("jperez2"));

        Usuario actualizado = usuarioRepositorio.findById(1020304050L).orElseThrow(AssertionError::new);
        assertEquals("nuevo.correo@tiendagenerica.com", actualizado.getEmailUsuario());
        assertTrue(codificador.matches("nuevaClave1", actualizado.getPassword()));
    }

    @Test
    @DisplayName("SP1-QA-8: actualizacion de usuario con errores por datos en blanco")
    void sp1QA8_actualizacionDeUsuarioConDatosEnBlanco() throws Exception {
        crearUsuario(1020304050L, "jperez", "clave12345");
        Usuario cambios = new Usuario(1020304050L, "", "correo@tiendagenerica.com", "jperez", "clave12345");

        mockMvc.perform(put("/usuarios/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(cambios)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Datos incompletos: el nombre completo es obligatorio"));
    }

    @Test
    @DisplayName("SP1-QA-9: borrado correcto de los datos de usuario")
    void sp1QA9_borradoCorrectoDeUsuario() throws Exception {
        crearUsuario(1020304050L, "jperez", "clave12345");

        mockMvc.perform(delete("/usuarios/eliminar/{id}", 1020304050L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));

        assertFalse(usuarioRepositorio.existsById(1020304050L));
    }

    @Test
    @DisplayName("SP1-QA-10: borrado de usuario con cedula alterada o inexistente")
    void sp1QA10_borradoDeUsuarioInexistente() throws Exception {
        mockMvc.perform(delete("/usuarios/eliminar/{id}", 888888L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false));
    }
}
