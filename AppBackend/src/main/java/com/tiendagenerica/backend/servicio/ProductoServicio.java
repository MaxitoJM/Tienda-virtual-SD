package com.tiendagenerica.backend.servicio;

import com.tiendagenerica.backend.excepcion.DatosInvalidosException;
import com.tiendagenerica.backend.excepcion.RecursoNoEncontradoException;
import com.tiendagenerica.backend.modelo.Producto;
import com.tiendagenerica.backend.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Modulo de Gestion de Productos: operaciones unitarias sobre la tabla productos. */
@Service
public class ProductoServicio {

    private final ProductoRepositorio repositorio;
    private final ProveedorServicio proveedorServicio;

    public ProductoServicio(ProductoRepositorio repositorio, ProveedorServicio proveedorServicio) {
        this.repositorio = repositorio;
        this.proveedorServicio = proveedorServicio;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return repositorio.findAll();
    }

    /** HU-017: consulta de los datos del producto por medio del codigo. */
    @Transactional(readOnly = true)
    public Producto consultar(Long codigo) {
        if (codigo == null) {
            throw new DatosInvalidosException("El codigo de producto es obligatorio");
        }
        return repositorio.findById(codigo).orElseThrow(() -> new RecursoNoEncontradoException(
                "El codigo de producto " + codigo + " no se encuentra registrado en la base de datos"));
    }

    @Transactional
    public Producto guardar(Producto producto) {
        validar(producto);
        if (repositorio.existsById(producto.getCodigoProducto())) {
            throw new DatosInvalidosException(
                    "Ya existe un producto registrado con el codigo " + producto.getCodigoProducto());
        }
        return repositorio.save(producto);
    }

    @Transactional
    public Producto actualizar(Producto producto) {
        validar(producto);
        Producto existente = consultar(producto.getCodigoProducto());
        existente.setNombreProducto(producto.getNombreProducto());
        existente.setNitproveedor(producto.getNitproveedor());
        existente.setPrecioCompra(producto.getPrecioCompra());
        existente.setIvacompra(producto.getIvacompra());
        existente.setPrecioVenta(producto.getPrecioVenta());
        return repositorio.save(existente);
    }

    @Transactional
    public void eliminar(Long codigo) {
        Producto existente = consultar(codigo);
        repositorio.delete(existente);
    }

    private void validar(Producto producto) {
        if (producto == null) {
            throw new DatosInvalidosException("No se recibieron los datos del producto");
        }
        if (producto.getCodigoProducto() == null) {
            throw new DatosInvalidosException("Datos incompletos: el codigo de producto es obligatorio");
        }
        if (producto.getNombreProducto() == null || producto.getNombreProducto().trim().isEmpty()) {
            throw new DatosInvalidosException("Datos incompletos: el nombre del producto es obligatorio");
        }
        if (!proveedorServicio.existe(producto.getNitproveedor())) {
            throw new DatosInvalidosException(
                    "El NIT de proveedor " + producto.getNitproveedor()
                            + " no existe en la base de datos");
        }
    }
}
