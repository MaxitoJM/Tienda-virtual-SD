output "base_datos" {
  description = "Base de datos creada."
  value       = mysql_database.tienda.name
}

output "usuario_aplicacion" {
  description = "Usuario de la aplicacion, con el origen desde el que se admite."
  value       = "${var.usuario_app}@${var.host_usuario_app}"
}

output "privilegios" {
  description = "Privilegios concedidos, acotados a la base de la aplicacion."
  value       = sort(mysql_grant.aplicacion.privileges)
}

output "comprobacion" {
  description = "Orden para verificar el complemento de autenticacion del usuario creado."
  value       = "SELECT user, host, plugin FROM mysql.user WHERE user = '${var.usuario_app}';"
}
