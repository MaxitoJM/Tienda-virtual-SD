package com.tiendagenerica.backend.excepcion;

/** Se lanza cuando los datos recibidos no superan las validaciones de negocio. */
public class DatosInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
