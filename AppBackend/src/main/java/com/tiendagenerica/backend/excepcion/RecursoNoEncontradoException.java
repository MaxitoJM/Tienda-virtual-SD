package com.tiendagenerica.backend.excepcion;

/** Se lanza cuando una consulta no recupera informacion de la base de datos. */
public class RecursoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
