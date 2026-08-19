package com.tiendagenerica.backend.sprint5;

import com.tiendagenerica.backend.PruebaBase;
import com.tiendagenerica.backend.dto.ItemVentaDto;
import com.tiendagenerica.backend.dto.RegistrarVentaDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sprint 5 - Modulo de Consultas y Reportes. Casos SP5-QA-1 a SP5-QA-3. */
class Sprint5ReportesTest extends PruebaBase {

    @Test
    @DisplayName("SP5-QA-1: generacion del listado de usuarios exitosa")
    void sp5QA1_listadoDeUsuarios() throws Exception {
        crearUsuario(1020304050L, "jperez", "clave12345");

        mockMvc.perform(get("/reportes/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cedula_usuario").exists())
                .andExpect(jsonPath("$[0].nombre_usuario").exists())
                .andExpect(jsonPath("$[0].email_usuario").exists())
                .andExpect(jsonPath("$[0].usuario").exists())
                .andExpect(jsonPath("$[0].password").value("********"));
    }

    @Test
    @DisplayName("SP5-QA-2: generacion del listado de clientes exitosa")
    void sp5QA2_listadoDeClientes() throws Exception {
        crearCliente(52123456L, "Maria Lopez");
        crearCliente(79987654L, "Carlos Ruiz");

        mockMvc.perform(get("/reportes/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cedula_cliente").value(52123456L))
                .andExpect(jsonPath("$[0].nombre_cliente").value("Maria Lopez"))
                .andExpect(jsonPath("$[0].email_cliente").exists())
                .andExpect(jsonPath("$[0].direccion_cliente").exists())
                .andExpect(jsonPath("$[0].telefono_cliente").exists());
    }

    @Test
    @DisplayName("SP5-QA-2: el listado de clientes vacio no produce error")
    void sp5QA2_listadoDeClientesVacio() throws Exception {
        mockMvc.perform(get("/reportes/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("SP5-QA-3: total de ventas por cliente con total consolidado")
    void sp5QA3_totalDeVentasPorCliente() throws Exception {
        Long usuario = 1020304050L;
        crearUsuario(usuario, "jperez", "clave12345");
        crearCliente(52123456L, "Maria Lopez");
        crearCliente(79987654L, "Carlos Ruiz");
        crearProveedor(1L, "Proveedor Uno");
        crearProducto(1L, "Melocotones", 1L, 1000d, 19d);

        // Maria Lopez: 2 x 1000 = 2000 + IVA 380 = 2380
        registrarVenta(52123456L, usuario, 2);
        // Carlos Ruiz: 1 x 1000 = 1000 + IVA 190 = 1190
        registrarVenta(79987654L, usuario, 1);

        mockMvc.perform(get("/reportes/ventasporcliente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientes.length()").value(2))
                .andExpect(jsonPath("$.clientes[0].cedula_cliente").value(52123456L))
                .andExpect(jsonPath("$.clientes[0].nombre_cliente").value("Maria Lopez"))
                .andExpect(jsonPath("$.clientes[0].valor_total_ventas").value(2380d))
                .andExpect(jsonPath("$.clientes[1].valor_total_ventas").value(1190d))
                .andExpect(jsonPath("$.total_general_ventas").value(3570d));
    }

    @Test
    @DisplayName("SP5-QA-3: si no existen clientes se genera un mensaje")
    void sp5QA3_sinClientesGeneraMensaje() throws Exception {
        mockMvc.perform(get("/reportes/ventasporcliente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientes.length()").value(0))
                .andExpect(jsonPath("$.total_general_ventas").value(0d))
                .andExpect(jsonPath("$.mensaje").value("No existen clientes registrados en el sistema"));
    }

    private void registrarVenta(Long cedulaCliente, Long cedulaUsuario, int cantidad) throws Exception {
        RegistrarVentaDto venta = new RegistrarVentaDto(cedulaCliente, cedulaUsuario,
                Collections.singletonList(new ItemVentaDto(1L, cantidad)));
        mockMvc.perform(post("/Ventas/registrar")
                        .contentType("application/json")
                        .content(json.writeValueAsString(venta)))
                .andExpect(status().isCreated());
    }
}
