# ============================================================
#  Base de datos, usuario de la aplicacion y privilegios.
# ============================================================

resource "mysql_database" "tienda" {
  name                  = var.nombre_bd
  default_character_set = "utf8mb4"
  default_collation     = "utf8mb4_0900_ai_ci"
}

resource "mysql_user" "aplicacion" {
  user = var.usuario_app
  host = var.host_usuario_app

  # MySQL 8.4 ya no trae mysql_native_password. Se declara de forma
  # explicita el complemento vigente en lugar de confiar en el valor por
  # defecto del servidor. El proveedor genera:
  #   CREATE USER 'usuario'@'host'
  #     IDENTIFIED WITH caching_sha2_password BY '<contrasena>'
  auth_plugin        = "caching_sha2_password"
  plaintext_password = data.aws_ssm_parameter.password_app.value

  lifecycle {
    # El nombre del usuario se toma de la variable, no del parametro, para
    # que las salidas de este modulo no arrastren la marca de sensible que
    # llevan los valores leidos de SSM. Esta comprobacion garantiza que
    # ambos coinciden: si no, el backend no podria autenticarse y el fallo
    # solo se veria al arrancar la instancia.
    precondition {
      condition     = var.usuario_app == data.aws_ssm_parameter.usuario_app.value
      error_message = "var.usuario_app no coincide con el parametro /db/user de SSM. Ajuste la variable al valor que publico el modulo raiz."
    }
  }
}

resource "mysql_grant" "aplicacion" {
  user     = mysql_user.aplicacion.user
  host     = mysql_user.aplicacion.host
  database = mysql_database.tienda.name
  table    = "*"

  # Nada de GRANT ALL y nada de privilegios globales: estos permisos
  # valen unicamente dentro de la base tiendagenerica.
  #
  # Los cuatro ultimos son de definicion de esquema y hacen falta porque
  # el backend arranca con spring.jpa.hibernate.ddl-auto=update, es
  # decir, crea y ajusta sus propias tablas. El dia que el proyecto
  # incorpore migraciones versionadas (Flyway o Liquibase) se pueden
  # retirar y dejar solo los cuatro primeros.
  #
  # DROP no se concede: "update" nunca elimina tablas ni columnas.
  privileges = [
    "SELECT",
    "INSERT",
    "UPDATE",
    "DELETE",
    "CREATE",
    "ALTER",
    "INDEX",
    "REFERENCES",
  ]
}
