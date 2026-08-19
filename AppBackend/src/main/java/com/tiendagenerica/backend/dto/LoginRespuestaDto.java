package com.tiendagenerica.backend.dto;

/** Resultado de la autenticacion contra el modulo de login. */
public class LoginRespuestaDto {

    private boolean autenticado;
    private String mensaje;
    private Long cedulaUsuario;
    private String nombreUsuario;
    private String usuario;

    public LoginRespuestaDto() {
    }

    public LoginRespuestaDto(boolean autenticado, String mensaje) {
        this.autenticado = autenticado;
        this.mensaje = mensaje;
    }

    public boolean isAutenticado() { return autenticado; }
    public void setAutenticado(boolean autenticado) { this.autenticado = autenticado; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Long getCedulaUsuario() { return cedulaUsuario; }
    public void setCedulaUsuario(Long cedulaUsuario) { this.cedulaUsuario = cedulaUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
