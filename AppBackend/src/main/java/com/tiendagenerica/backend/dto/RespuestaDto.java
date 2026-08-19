package com.tiendagenerica.backend.dto;

/** Respuesta generica del sistema: indicador de exito y mensaje para el usuario. */
public class RespuestaDto {

    private boolean exitoso;
    private String mensaje;

    public RespuestaDto() {
    }

    public RespuestaDto(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
    }

    public static RespuestaDto ok(String mensaje) { return new RespuestaDto(true, mensaje); }
    public static RespuestaDto error(String mensaje) { return new RespuestaDto(false, mensaje); }

    public boolean isExitoso() { return exitoso; }
    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
