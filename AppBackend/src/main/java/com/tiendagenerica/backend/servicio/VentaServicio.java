package com.tiendagenerica.backend.servicio;

import com.tiendagenerica.backend.dto.ItemVentaDto;
import com.tiendagenerica.backend.dto.RegistrarVentaDto;
import com.tiendagenerica.backend.dto.VentaRegistradaDto;
import com.tiendagenerica.backend.excepcion.DatosInvalidosException;
import com.tiendagenerica.backend.excepcion.RecursoNoEncontradoException;
import com.tiendagenerica.backend.modelo.Cliente;
import com.tiendagenerica.backend.modelo.DetalleVenta;
import com.tiendagenerica.backend.modelo.Producto;
import com.tiendagenerica.backend.modelo.Venta;
import com.tiendagenerica.backend.repositorio.DetalleVentaRepositorio;
import com.tiendagenerica.backend.repositorio.UsuarioRepositorio;
import com.tiendagenerica.backend.repositorio.VentaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Modulo de Gestion de Ventas (HU-015 a HU-020).
 *
 * El documento limita cada venta a un maximo de tres (3) productos y define
 * el calculo del IVA a partir del porcentaje configurado en cada producto.
 */
@Service
public class VentaServicio {

    /** Maximo de productos por venta, segun la especificacion funcional. */
    public static final int MAX_PRODUCTOS_POR_VENTA = 3;

    private final VentaRepositorio ventaRepositorio;
    private final DetalleVentaRepositorio detalleVentaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ClienteServicio clienteServicio;
    private final ProductoServicio productoServicio;

    public VentaServicio(VentaRepositorio ventaRepositorio,
                         DetalleVentaRepositorio detalleVentaRepositorio,
                         UsuarioRepositorio usuarioRepositorio,
                         ClienteServicio clienteServicio,
                         ProductoServicio productoServicio) {
        this.ventaRepositorio = ventaRepositorio;
        this.detalleVentaRepositorio = detalleVentaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.clienteServicio = clienteServicio;
        this.productoServicio = productoServicio;
    }

    @Transactional(readOnly = true)
    public List<Venta> listar() {
        return ventaRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Venta consultar(Long codigoVenta) {
        if (codigoVenta == null) {
            throw new DatosInvalidosException("El codigo de venta es obligatorio");
        }
        return ventaRepositorio.findById(codigoVenta).orElseThrow(() -> new RecursoNoEncontradoException(
                "La venta con codigo " + codigoVenta + " no se encuentra registrada en la base de datos"));
    }

    @Transactional
    public Venta guardar(Venta venta) {
        if (venta == null) {
            throw new DatosInvalidosException("No se recibieron los datos de la venta");
        }
        return ventaRepositorio.save(venta);
    }

    @Transactional
    public Venta actualizar(Venta venta) {
        if (venta == null || venta.getCodigoVenta() == null) {
            throw new DatosInvalidosException("El codigo de venta es obligatorio");
        }
        consultar(venta.getCodigoVenta());
        return ventaRepositorio.save(venta);
    }

    @Transactional
    public void eliminar(Long codigoVenta) {
        Venta existente = consultar(codigoVenta);
        detalleVentaRepositorio.deleteAll(detalleVentaRepositorio.findByCodigoVenta(codigoVenta));
        ventaRepositorio.delete(existente);
    }

    /**
     * Registra una venta completa: cabecera en la tabla ventas y una fila por
     * producto en la tabla detalle_ventas. La operacion es transaccional, de
     * modo que la venta y su detalle se guardan o fallan en conjunto.
     */
    @Transactional
    public VentaRegistradaDto registrar(RegistrarVentaDto solicitud) {
        if (solicitud == null) {
            throw new DatosInvalidosException("No se recibieron los datos de la venta");
        }

        // HU-016: los datos del cliente se obtienen mediante consulta por cedula.
        Cliente cliente = clienteServicio.consultar(solicitud.getCedulaCliente());

        if (solicitud.getCedulaUsuario() == null
                || !usuarioRepositorio.existsById(solicitud.getCedulaUsuario())) {
            throw new RecursoNoEncontradoException("La cedula del usuario "
                    + solicitud.getCedulaUsuario() + " no se encuentra registrada en la base de datos");
        }

        List<ItemVentaDto> items = solicitud.getProductos();
        if (items == null || items.isEmpty()) {
            throw new DatosInvalidosException("La venta debe tener al menos un producto");
        }
        if (items.size() > MAX_PRODUCTOS_POR_VENTA) {
            throw new DatosInvalidosException("La venta admite un maximo de "
                    + MAX_PRODUCTOS_POR_VENTA + " productos");
        }

        double totalSinIva = 0d;
        double totalIva = 0d;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (ItemVentaDto item : items) {
            // HU-017: los datos del producto se obtienen por codigo de producto.
            Producto producto = productoServicio.consultar(item.getCodigoProducto());

            // SP4-QA-5: la cantidad debe ser un valor mayor que cero.
            Integer cantidad = item.getCantidadProducto();
            if (cantidad == null || cantidad <= 0) {
                throw new DatosInvalidosException("El valor de cantidad es incorrecto para el producto "
                        + producto.getCodigoProducto() + ": debe ser un numero mayor que cero");
            }

            // HU-018 / SP4-QA-6: valor total por producto = cantidad x precio de venta.
            double precioVenta = producto.getPrecioVenta() == null ? 0d : producto.getPrecioVenta();
            double porcentajeIva = producto.getIvacompra() == null ? 0d : producto.getIvacompra();
            double valorTotalLinea = redondear(cantidad * precioVenta);
            double valorIvaLinea = redondear(valorTotalLinea * porcentajeIva / 100d);

            totalSinIva += valorTotalLinea;
            totalIva += valorIvaLinea;

            DetalleVenta detalle = new DetalleVenta();
            detalle.setCodigoProducto(producto.getCodigoProducto());
            detalle.setCantidadProducto(cantidad);
            detalle.setValorVenta(precioVenta);
            detalle.setValoriva(valorIvaLinea);
            detalle.setValorTotal(valorTotalLinea);
            detalles.add(detalle);
        }

        // HU-019 / SP4-QA-7, QA-8, QA-9: totales de la venta.
        totalSinIva = redondear(totalSinIva);
        totalIva = redondear(totalIva);
        double totalConIva = redondear(totalSinIva + totalIva);

        // HU-020 / SP4-QA-10: el codigo de venta es un consecutivo generado por el sistema.
        Venta venta = new Venta();
        venta.setCedulaCliente(cliente.getCedulaCliente());
        venta.setCedulaUsuario(solicitud.getCedulaUsuario());
        venta.setValorVenta(totalSinIva);
        venta.setIvaventa(totalIva);
        venta.setTotalVenta(totalConIva);
        Venta guardada = ventaRepositorio.save(venta);

        for (DetalleVenta detalle : detalles) {
            detalle.setCodigoVenta(guardada.getCodigoVenta());
        }
        List<DetalleVenta> detallesGuardados = detalleVentaRepositorio.saveAll(detalles);

        VentaRegistradaDto respuesta = new VentaRegistradaDto();
        respuesta.setCodigoVenta(guardada.getCodigoVenta());
        respuesta.setCedulaCliente(cliente.getCedulaCliente());
        respuesta.setNombreCliente(cliente.getNombreCliente());
        respuesta.setValorVenta(totalSinIva);
        respuesta.setIvaventa(totalIva);
        respuesta.setTotalVenta(totalConIva);
        respuesta.setDetalles(detallesGuardados);
        respuesta.setMensaje("Venta registrada correctamente con el consecutivo "
                + guardada.getCodigoVenta());
        return respuesta;
    }

    /** Redondea a dos decimales para evitar arrastre de error en los totales. */
    private double redondear(double valor) {
        return Math.round(valor * 100d) / 100d;
    }
}
