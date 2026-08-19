package com.tiendagenerica.backend.servicio;

import com.tiendagenerica.backend.dto.CargueProductosDto;
import com.tiendagenerica.backend.excepcion.DatosInvalidosException;
import com.tiendagenerica.backend.modelo.Producto;
import com.tiendagenerica.backend.repositorio.ProductoRepositorio;
import com.tiendagenerica.backend.repositorio.ProveedorRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * HU-014: carga de la tabla de productos desde un archivo plano separado por comas.
 *
 * El proceso es todo o nada: primero se valida la totalidad del archivo y solo
 * cuando no hay ningun error se borran los productos anteriores y se insertan los
 * registros leidos, tal como exige la prueba SP3-QA-1.
 *
 * Estructura esperada (6 columnas):
 * codigo_producto, nombre_producto, nitproveedor, precio_compra, ivacompra, precio_venta
 */
@Service
public class CargueProductosServicio {

    private static final int COLUMNAS_ESPERADAS = 6;
    private static final int LONGITUD_MAX_NOMBRE = 50;

    private final ProductoRepositorio productoRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;

    public CargueProductosServicio(ProductoRepositorio productoRepositorio,
                                   ProveedorRepositorio proveedorRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
    }

    @Transactional
    public CargueProductosDto cargar(MultipartFile archivo) {
        validarArchivo(archivo);

        List<String> lineas = leerLineas(archivo);
        if (lineas.isEmpty()) {
            throw new DatosInvalidosException(
                    "Error en el formato del archivo: el archivo se encuentra vacio");
        }

        List<String> errores = new ArrayList<>();
        List<Producto> productos = new ArrayList<>();
        Set<Long> codigosVistos = new HashSet<>();

        int numeroLinea = 0;
        for (String linea : lineas) {
            numeroLinea++;
            if (linea.trim().isEmpty()) {
                continue;
            }
            if (numeroLinea == 1 && esEncabezado(linea)) {
                continue;
            }
            interpretarLinea(linea, numeroLinea, codigosVistos, productos, errores);
        }

        if (!errores.isEmpty()) {
            CargueProductosDto fallido = new CargueProductosDto(false,
                    "Error en los datos leidos del archivo. No se cargo ningun registro");
            fallido.setRegistrosLeidos(productos.size() + errores.size());
            fallido.setErrores(errores);
            throw new CargueInvalidoException(fallido);
        }

        if (productos.isEmpty()) {
            throw new DatosInvalidosException(
                    "Error en el formato del archivo: no se encontraron registros para cargar");
        }

        // Reemplazo completo de la tabla de productos (HU-014).
        productoRepositorio.deleteAllInBatch();
        productoRepositorio.saveAll(productos);

        CargueProductosDto respuesta = new CargueProductosDto(true, "Archivo Cargado correctamente");
        respuesta.setRegistrosLeidos(productos.size());
        respuesta.setRegistrosCargados(productos.size());
        return respuesta;
    }

    /** SP3-QA-2 y SP3-QA-3: validaciones previas sobre el archivo recibido. */
    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new DatosInvalidosException("No se selecciono archivo para cargar");
        }
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new DatosInvalidosException("No se selecciono archivo para cargar");
        }
        if (!nombre.toLowerCase().endsWith(".csv")) {
            throw new DatosInvalidosException(
                    "Error en el formato del archivo: se esperaba un archivo separado por comas (CSV)");
        }
    }

    private List<String> leerLineas(MultipartFile archivo) {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                lineas.add(linea);
            }
        } catch (IOException ex) {
            throw new DatosInvalidosException(
                    "Error en el formato del archivo: no fue posible leer su contenido");
        }
        return lineas;
    }

    private boolean esEncabezado(String linea) {
        return linea.toLowerCase().contains("codigo_producto");
    }

    /** SP3-QA-4: validacion de tipos de dato de cada registro leido. */
    private void interpretarLinea(String linea, int numeroLinea, Set<Long> codigosVistos,
                                  List<Producto> productos, List<String> errores) {
        String[] campos = linea.split(",", -1);
        if (campos.length != COLUMNAS_ESPERADAS) {
            errores.add("Linea " + numeroLinea + ": se esperaban " + COLUMNAS_ESPERADAS
                    + " columnas separadas por comas y se encontraron " + campos.length);
            return;
        }

        Long codigo = aEntero(campos[0], numeroLinea, "codigo_producto", errores);
        String nombre = campos[1].trim();
        Long nit = aEntero(campos[2], numeroLinea, "nitproveedor", errores);
        Double precioCompra = aDecimal(campos[3], numeroLinea, "precio_compra", errores);
        Double ivaCompra = aDecimal(campos[4], numeroLinea, "ivacompra", errores);
        Double precioVenta = aDecimal(campos[5], numeroLinea, "precio_venta", errores);

        if (nombre.isEmpty()) {
            errores.add("Linea " + numeroLinea + ": el campo nombre_producto no puede estar vacio");
        } else if (nombre.length() > LONGITUD_MAX_NOMBRE) {
            errores.add("Linea " + numeroLinea + ": el campo nombre_producto excede "
                    + LONGITUD_MAX_NOMBRE + " caracteres");
        }

        if (codigo != null && !codigosVistos.add(codigo)) {
            errores.add("Linea " + numeroLinea + ": el codigo de producto " + codigo
                    + " se encuentra repetido en el archivo");
        }

        // Validacion exigida por el documento: el NIT debe existir en la base de datos.
        if (nit != null && !proveedorRepositorio.existsById(nit)) {
            errores.add("Linea " + numeroLinea + ": el NIT de proveedor " + nit
                    + " no existe en la base de datos");
        }

        if (codigo == null || nit == null || precioCompra == null
                || ivaCompra == null || precioVenta == null || nombre.isEmpty()) {
            return;
        }
        productos.add(new Producto(codigo, nombre, nit, precioCompra, ivaCompra, precioVenta));
    }

    private Long aEntero(String valor, int linea, String campo, List<String> errores) {
        try {
            return Long.valueOf(valor.trim());
        } catch (NumberFormatException ex) {
            errores.add("Linea " + linea + ": el campo " + campo
                    + " debe ser un numero entero y se recibio: " + valor.trim());
            return null;
        }
    }

    private Double aDecimal(String valor, int linea, String campo, List<String> errores) {
        try {
            return Double.valueOf(valor.trim());
        } catch (NumberFormatException ex) {
            errores.add("Linea " + linea + ": el campo " + campo
                    + " debe ser un numero y se recibio: " + valor.trim());
            return null;
        }
    }

    /** Excepcion interna que transporta el detalle de los errores de validacion. */
    public static class CargueInvalidoException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final transient CargueProductosDto detalle;

        public CargueInvalidoException(CargueProductosDto detalle) {
            super(detalle.getMensaje());
            this.detalle = detalle;
        }

        public CargueProductosDto getDetalle() {
            return detalle;
        }
    }
}
