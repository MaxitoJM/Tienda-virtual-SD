package com.tiendagenerica.backend.servicio;

import com.tiendagenerica.backend.excepcion.DatosInvalidosException;
import com.tiendagenerica.backend.excepcion.RecursoNoEncontradoException;
import com.tiendagenerica.backend.modelo.Proveedor;
import com.tiendagenerica.backend.repositorio.ProveedorRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Modulo de Gestion de Proveedores (HU-010 a HU-013). */
@Service
public class ProveedorServicio {

    private final ProveedorRepositorio repositorio;

    public ProveedorServicio(ProveedorRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listar() {
        return repositorio.findAll();
    }

    /** HU-011: consulta de los datos del proveedor por medio del NIT. */
    @Transactional(readOnly = true)
    public Proveedor consultar(Long nit) {
        if (nit == null) {
            throw new DatosInvalidosException("El NIT del proveedor es obligatorio");
        }
        return repositorio.findById(nit).orElseThrow(() -> new RecursoNoEncontradoException(
                "El NIT " + nit + " no se encuentra registrado en la base de datos"));
    }

    /** HU-010: creacion de un nuevo proveedor. */
    @Transactional
    public Proveedor guardar(Proveedor proveedor) {
        validarCompletitud(proveedor);
        if (repositorio.existsById(proveedor.getNitproveedor())) {
            throw new DatosInvalidosException(
                    "Ya existe un proveedor registrado con el NIT " + proveedor.getNitproveedor());
        }
        return repositorio.save(proveedor);
    }

    /** HU-012: actualizacion de los datos del proveedor, previa consulta por NIT. */
    @Transactional
    public Proveedor actualizar(Proveedor proveedor) {
        validarCompletitud(proveedor);
        Proveedor existente = consultar(proveedor.getNitproveedor());
        existente.setNombreProveedor(proveedor.getNombreProveedor());
        existente.setDireccionProveedor(proveedor.getDireccionProveedor());
        existente.setTelefonoProveedor(proveedor.getTelefonoProveedor());
        existente.setCiudadProveedor(proveedor.getCiudadProveedor());
        return repositorio.save(existente);
    }

    /** HU-013: borrado de los datos del proveedor, previa consulta por NIT. */
    @Transactional
    public void eliminar(Long nit) {
        Proveedor existente = consultar(nit);
        repositorio.delete(existente);
    }

    @Transactional(readOnly = true)
    public boolean existe(Long nit) {
        return nit != null && repositorio.existsById(nit);
    }

    private void validarCompletitud(Proveedor proveedor) {
        if (proveedor == null) {
            throw new DatosInvalidosException("No se recibieron los datos del proveedor");
        }
        if (proveedor.getNitproveedor() == null) {
            throw new DatosInvalidosException("Datos incompletos: el NIT es obligatorio");
        }
        exigir(proveedor.getNombreProveedor(), "el nombre del proveedor");
        exigir(proveedor.getDireccionProveedor(), "la direccion");
        exigir(proveedor.getTelefonoProveedor(), "el telefono");
        exigir(proveedor.getCiudadProveedor(), "la ciudad");
    }

    private void exigir(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatosInvalidosException("Datos incompletos: " + campo + " es obligatorio");
        }
    }
}
