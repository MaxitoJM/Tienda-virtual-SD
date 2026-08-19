package com.tiendagenerica.backend.sprint2;

import com.tiendagenerica.backend.PruebaBase;
import com.tiendagenerica.backend.modelo.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sprint 2 - Modulo de Gestion de Clientes. Casos SP2-QA-1 a SP2-QA-8. */
class Sprint2ClientesTest extends PruebaBase {

    private Cliente nuevoCliente() {
        return new Cliente(52123456L, "Maria Lopez Diaz", "Calle 45 No 12-34",
                "3109876543", "maria.lopez@correo.com");
    }

    @Test
    @DisplayName("SP2-QA-1: creacion de un nuevo cliente correcto")
    void sp2QA1_creacionDeClienteCorrecta() throws Exception {
        mockMvc.perform(post("/clientes/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(nuevoCliente())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cedula_cliente").value(52123456L))
                .andExpect(jsonPath("$.nombre_cliente").value("Maria Lopez Diaz"));

        assertEquals(1, clienteRepositorio.count());
    }

    @Test
    @DisplayName("SP2-QA-2: creacion de cliente con errores por falta de completitud")
    void sp2QA2_creacionDeClienteConDatosIncompletos() throws Exception {
        Cliente incompleto = nuevoCliente();
        incompleto.setTelefonoCliente("");

        mockMvc.perform(post("/clientes/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(incompleto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos incompletos: el telefono es obligatorio"));

        assertFalse(clienteRepositorio.existsById(52123456L));
    }

    @Test
    @DisplayName("SP2-QA-3: consulta de cliente existente por cedula")
    void sp2QA3_consultaDeClienteExistente() throws Exception {
        crearCliente(52123456L, "Maria Lopez");

        mockMvc.perform(get("/clientes/consultar/{id}", 52123456L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cedula_cliente").value(52123456L))
                .andExpect(jsonPath("$.nombre_cliente").value("Maria Lopez"))
                .andExpect(jsonPath("$.direccion_cliente").value("Calle 1 No 2-3"))
                .andExpect(jsonPath("$.telefono_cliente").value("3001234567"))
                .andExpect(jsonPath("$.email_cliente").value("marialopez@correo.com"));
    }

    @Test
    @DisplayName("SP2-QA-4: consulta de cliente inexistente")
    void sp2QA4_consultaDeClienteInexistente() throws Exception {
        mockMvc.perform(get("/clientes/consultar/{id}", 777777L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(
                        "La cedula 777777 no se encuentra registrada en la base de datos"));
    }

    @Test
    @DisplayName("SP2-QA-5: actualizacion correcta de los datos del cliente")
    void sp2QA5_actualizacionCorrectaDeCliente() throws Exception {
        crearCliente(52123456L, "Maria Lopez");
        Cliente cambios = new Cliente(52123456L, "Maria Lopez Actualizada", "Avenida 68 No 1-11",
                "3200000000", "maria.actualizada@correo.com");

        mockMvc.perform(put("/clientes/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_cliente").value("Maria Lopez Actualizada"))
                .andExpect(jsonPath("$.direccion_cliente").value("Avenida 68 No 1-11"));
    }

    @Test
    @DisplayName("SP2-QA-6: actualizacion de cliente con errores por datos en blanco")
    void sp2QA6_actualizacionDeClienteConDatosEnBlanco() throws Exception {
        crearCliente(52123456L, "Maria Lopez");
        Cliente cambios = new Cliente(52123456L, "Maria Lopez", "", "3200000000", "maria@correo.com");

        mockMvc.perform(put("/clientes/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(cambios)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos incompletos: la direccion es obligatorio"));
    }

    @Test
    @DisplayName("SP2-QA-7: borrado correcto de los datos del cliente")
    void sp2QA7_borradoCorrectoDeCliente() throws Exception {
        crearCliente(52123456L, "Maria Lopez");

        mockMvc.perform(delete("/clientes/eliminar/{id}", 52123456L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));

        assertFalse(clienteRepositorio.existsById(52123456L));
    }

    @Test
    @DisplayName("SP2-QA-8: borrado de cliente con cedula alterada o inexistente")
    void sp2QA8_borradoDeClienteInexistente() throws Exception {
        mockMvc.perform(delete("/clientes/eliminar/{id}", 666666L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false));
    }
}
