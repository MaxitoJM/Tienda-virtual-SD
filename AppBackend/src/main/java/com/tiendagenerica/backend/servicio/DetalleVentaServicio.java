package com.tiendagenerica.backend.servicio;

import com.tiendagenerica.backend.excepcion.DatosInvalidosException;
import com.tiendagenerica.backend.excepcion.RecursoNoEncontradoException;
import com.tiendagenerica.backend.modelo.DetalleVenta;
import com.tiendagenerica.backend.repositorio.DetalleVentaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Operaciones sobre la tabla detalle_ventas expuestas por la API del documento. */
@Service
public class DetalleVentaServicio {

    private final DetalleVentaRepositorio repositorio;

    public DetalleVentaServicio(DetalleVentaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<DetalleVenta> listar() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public DetalleVenta consultar(Long codigo) {
        if (codigo == null) {
            throw new DatosInvalidosException("El codigo de detalle de venta es obligatorio");
        }
        return repositorio.findById(codigo).orElseThrow(() -> new RecursoNoEncontradoException(
                "El detalle de venta " + codigo + " no se encuentra registrado en la base de datos"));
    }

    @Transactional(readOnly = true)
    public List<DetalleVenta> listarPorVenta(Long codigoVenta) {
        return repositorio.findByCodigoVenta(codigoVenta);
    }

    @Transactional
    public DetalleVenta guardar(DetalleVenta detalle) {
        if (detalle == null) {
            throw new DatosInvalidosException("No se recibieron los datos del detalle de venta");
        }
        return repositorio.save(detalle);
    }

    @Transactional
    public DetalleVenta actualizar(DetalleVenta detalle) {
        if (detalle == null || detalle.getCodigoDetalleVenta() == null) {
            throw new DatosInvalidosException("El codigo de detalle de venta es obligatorio");
        }
        consultar(detalle.getCodigoDetalleVenta());
        return repositorio.save(detalle);
    }

    @Transactional
    public void eliminar(Long codigo) {
        DetalleVenta existente = consultar(codigo);
        repositorio.delete(existente);
    }
}
