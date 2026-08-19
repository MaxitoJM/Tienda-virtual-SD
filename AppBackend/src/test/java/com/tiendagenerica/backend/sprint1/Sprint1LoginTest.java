package com.tiendagenerica.backend.sprint1;

import com.tiendagenerica.backend.PruebaBase;
import com.tiendagenerica.backend.dto.LoginDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sprint 1 - Modulo de Login del sistema. Casos SP1-QA-1 y SP1-QA-2. */
class Sprint1LoginTest extends PruebaBase {

    @Test
    @DisplayName("SP1-QA-1: ingreso correcto con el usuario inicial admininicial")
    void sp1QA1_ingresoCorrectoConUsuarioInicial() throws Exception {
        mockMvc.perform(post("/usuarios/login")
                        .contentType("application/json")
                        .content(json.writeValueAsString(new LoginDto("admininicial", "admin123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.usuario").value("admininicial"));
    }

    @Test
    @DisplayName("SP1-QA-1: ingreso correcto con un usuario ya creado en el sistema")
    void sp1QA1_ingresoCorrectoConUsuarioCreado() throws Exception {
        crearUsuario(1020304050L, "jperez", "clave12345");

        mockMvc.perform(post("/usuarios/login")
                        .contentType("application/json")
                        .content(json.writeValueAsString(new LoginDto("jperez", "clave12345"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.cedula_usuario").value(1020304050L));
    }

    @Test
    @DisplayName("SP1-QA-2: ingreso incorrecto por contrasena errada")
    void sp1QA2_ingresoIncorrectoPorContrasenaErrada() throws Exception {
        mockMvc.perform(post("/usuarios/login")
                        .contentType("application/json")
                        .content(json.writeValueAsString(new LoginDto("admininicial", "clave-errada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.autenticado").value(false))
                .andExpect(jsonPath("$.mensaje").value("Usuario y/o contrasena errados, intente de nuevo"));
    }

    @Test
    @DisplayName("SP1-QA-2: ingreso incorrecto por usuario inexistente")
    void sp1QA2_ingresoIncorrectoPorUsuarioInexistente() throws Exception {
        mockMvc.perform(post("/usuarios/login")
                        .contentType("application/json")
                        .content(json.writeValueAsString(new LoginDto("noexiste", "admin123456"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.autenticado").value(false));
    }

    @Test
    @DisplayName("SP1-QA-2: ingreso incorrecto por omitir alguno de los datos solicitados")
    void sp1QA2_ingresoIncorrectoPorDatosEnBlanco() throws Exception {
        mockMvc.perform(post("/usuarios/login")
                        .contentType("application/json")
                        .content(json.writeValueAsString(new LoginDto("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false));
    }

    @Test
    @DisplayName("Regla funcional: el usuario admininicial se desactiva al existir otros usuarios")
    void usuarioInicialSeDesactivaAlCrearOtrosUsuarios() throws Exception {
        crearUsuario(1020304050L, "jperez", "clave12345");

        mockMvc.perform(post("/usuarios/login")
                        .contentType("application/json")
                        .content(json.writeValueAsString(new LoginDto("admininicial", "admin123456"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.autenticado").value(false));
    }
}
