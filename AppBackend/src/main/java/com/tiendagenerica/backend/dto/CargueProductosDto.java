package com.tiendagenerica.backend.dto;

import java.util.ArrayList;
import java.util.List;

/** Resultado de la carga masiva de productos desde archivo CSV (HU-014). */
public class CargueProductosDto {

    private boolean exitoso;
    private String mensaje;
    private int registrosLeidos;
    private int registrosCargados;
    private List<String> errores = new ArrayList<>();

    public CargueProductosDto() {
    }

    public CargueProductosDto(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
    }

    public boolean isExitoso() { return exitoso; }
    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public int getRegistrosLeidos() { return registrosLeidos; }
    public void setRegistrosLeidos(int registrosLeidos) { this.registrosLeidos = registrosLeidos; }

    public int getRegistrosCargados() { return registrosCargados; }
    public void setRegistrosCargados(int registrosCargados) { this.registrosCargados = registrosCargados; }

    public List<String> getErrores() { return errores; }
    public void setErrores(List<String> errores) { this.errores = errores; }
}
