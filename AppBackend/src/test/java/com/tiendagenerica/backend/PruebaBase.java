package com.tiendagenerica.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendagenerica.backend.modelo.Cliente;
import com.tiendagenerica.backend.modelo.Producto;
import com.tiendagenerica.backend.modelo.Proveedor;
import com.tiendagenerica.backend.modelo.Usuario;
import com.tiendagenerica.backend.repositorio.ClienteRepositorio;
import com.tiendagenerica.backend.repositorio.DetalleVentaRepositorio;
import com.tiendagenerica.backend.repositorio.ProductoRepositorio;
import com.tiendagenerica.backend.repositorio.ProveedorRepositorio;
import com.tiendagenerica.backend.repositorio.UsuarioRepositorio;
import com.tiendagenerica.backend.repositorio.VentaRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base comun de las pruebas automatizadas del conjunto de pruebas QA del documento.
 * Cada metodo de prueba lleva el identificador del caso al que corresponde.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class PruebaBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected PasswordEncoder codificador;

    @Autowired protected UsuarioRepositorio usuarioRepositorio;
    @Autowired protected ClienteRepositorio clienteRepositorio;
    @Autowired protected ProveedorRepositorio proveedorRepositorio;
    @Autowired protected ProductoRepositorio productoRepositorio;
    @Autowired protected VentaRepositorio ventaRepositorio;
    @Autowired protected DetalleVentaRepositorio detalleVentaRepositorio;

    @BeforeEach
    void limpiarBaseDeDatos() {
        detalleVentaRepositorio.deleteAll();
        ventaRepositorio.deleteAll();
        productoRepositorio.deleteAll();
        proveedorRepositorio.deleteAll();
        clienteRepositorio.deleteAll();
        usuarioRepositorio.deleteAll();
        crearUsuarioInicial();
    }

    /** Usuario por defecto exigido por el modulo de login. */
    protected Usuario crearUsuarioInicial() {
        return usuarioRepositorio.save(new Usuario(1L, "Administrador Inicial",
                "admininicial@tiendagenerica.com", "admininicial", codificador.encode("admin123456")));
    }

    protected Usuario crearUsuario(Long cedula, String nombreUsuario, String clave) {
        return usuarioRepositorio.save(new Usuario(cedula, "Usuario de Pruebas",
                nombreUsuario + "@tiendagenerica.com", nombreUsuario, codificador.encode(clave)));
    }

    protected Cliente crearCliente(Long cedula, String nombre) {
        return clienteRepositorio.save(new Cliente(cedula, nombre, "Calle 1 No 2-3",
                "3001234567", nombre.toLowerCase().replace(" ", "") + "@correo.com"));
    }

    protected Proveedor crearProveedor(Long nit, String nombre) {
        return proveedorRepositorio.save(new Proveedor(nit, nombre, "Carrera 10 No 20-30",
                "6011234567", "Bogota"));
    }

    protected Producto crearProducto(Long codigo, String nombre, Long nit,
                                     double precioVenta, double iva) {
        return productoRepositorio.save(new Producto(codigo, nombre, nit,
                precioVenta * 0.8, iva, precioVenta));
    }
}
