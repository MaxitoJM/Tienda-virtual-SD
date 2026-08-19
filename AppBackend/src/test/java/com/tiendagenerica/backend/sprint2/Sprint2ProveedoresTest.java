package com.tiendagenerica.backend.sprint2;

import com.tiendagenerica.backend.PruebaBase;
import com.tiendagenerica.backend.modelo.Proveedor;
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

/** Sprint 2 - Modulo de Gestion de Proveedores. Casos SP2-QA-9 a SP2-QA-16. */
class Sprint2ProveedoresTest extends PruebaBase {

    private Proveedor nuevoProveedor() {
        return new Proveedor(900123456L, "Distribuidora Nacional SAS",
                "Carrera 15 No 80-25", "6017654321", "Bogota");
    }

    @Test
    @DisplayName("SP2-QA-9: creacion de un nuevo proveedor correcto")
    void sp2QA9_creacionDeProveedorCorrecta() throws Exception {
        mockMvc.perform(post("/proveedores/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(nuevoProveedor())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nitproveedor").value(900123456L))
                .andExpect(jsonPath("$.nombre_proveedor").value("Distribuidora Nacional SAS"));

        assertEquals(1, proveedorRepositorio.count());
    }

    @Test
    @DisplayName("SP2-QA-10: creacion de proveedor con errores por falta de completitud")
    void sp2QA10_creacionDeProveedorConDatosIncompletos() throws Exception {
        Proveedor incompleto = nuevoProveedor();
        incompleto.setCiudadProveedor("");

        mockMvc.perform(post("/proveedores/guardar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(incompleto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos incompletos: la ciudad es obligatorio"));

        assertFalse(proveedorRepositorio.existsById(900123456L));
    }

    @Test
    @DisplayName("SP2-QA-11: consulta de proveedor existente por NIT")
    void sp2QA11_consultaDeProveedorExistente() throws Exception {
        crearProveedor(900123456L, "Distribuidora Nacional SAS");

        mockMvc.perform(get("/proveedores/consultar/{id}", 900123456L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nitproveedor").value(900123456L))
                .andExpect(jsonPath("$.nombre_proveedor").value("Distribuidora Nacional SAS"))
                .andExpect(jsonPath("$.direccion_proveedor").value("Carrera 10 No 20-30"))
                .andExpect(jsonPath("$.telefono_proveedor").value("6011234567"))
                .andExpect(jsonPath("$.ciudad_proveedor").value("Bogota"));
    }

    @Test
    @DisplayName("SP2-QA-12: consulta de proveedor inexistente")
    void sp2QA12_consultaDeProveedorInexistente() throws Exception {
        mockMvc.perform(get("/proveedores/consultar/{id}", 555555L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(
                        "El NIT 555555 no se encuentra registrado en la base de datos"));
    }

    @Test
    @DisplayName("SP2-QA-13: actualizacion correcta de los datos del proveedor")
    void sp2QA13_actualizacionCorrectaDeProveedor() throws Exception {
        crearProveedor(900123456L, "Distribuidora Nacional SAS");
        Proveedor cambios = new Proveedor(900123456L, "Distribuidora Nacional Actualizada",
                "Calle 100 No 11-22", "6019999999", "Medellin");

        mockMvc.perform(put("/proveedores/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre_proveedor").value("Distribuidora Nacional Actualizada"))
                .andExpect(jsonPath("$.ciudad_proveedor").value("Medellin"));
    }

    @Test
    @DisplayName("SP2-QA-14: actualizacion de proveedor con errores por datos en blanco")
    void sp2QA14_actualizacionDeProveedorConDatosEnBlanco() throws Exception {
        crearProveedor(900123456L, "Distribuidora Nacional SAS");
        Proveedor cambios = new Proveedor(900123456L, "", "Calle 100", "6019999999", "Medellin");

        mockMvc.perform(put("/proveedores/actualizar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(cambios)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Datos incompletos: el nombre del proveedor es obligatorio"));
    }

    @Test
    @DisplayName("SP2-QA-15: borrado correcto de los datos del proveedor")
    void sp2QA15_borradoCorrectoDeProveedor() throws Exception {
        crearProveedor(900123456L, "Distribuidora Nacional SAS");

        mockMvc.perform(delete("/proveedores/eliminar/{id}", 900123456L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));

        assertFalse(proveedorRepositorio.existsById(900123456L));
    }

    @Test
    @DisplayName("SP2-QA-16: borrado de proveedor con NIT alterado o inexistente")
    void sp2QA16_borradoDeProveedorInexistente() throws Exception {
        mockMvc.perform(delete("/proveedores/eliminar/{id}", 444444L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false));
    }
}
