package com.tiendagenerica.backend.dto;

import javax.validation.constraints.NotBlank;

/** Credenciales enviadas por el modulo de login (HU-001). */
public class LoginDto {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String usuario;

    @NotBlank(message = "La contrasena es obligatoria")
    private String password;

    public LoginDto() {
    }

    public LoginDto(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
