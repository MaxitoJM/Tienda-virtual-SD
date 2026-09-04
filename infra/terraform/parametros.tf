# ============================================================
#  SSM Parameter Store: unica fuente de la configuracion que la
#  instancia lee al arrancar.
#
#  Las contrasenas se guardan como SecureString, cifradas con la clave
#  gestionada de AWS para SSM. La instancia las descifra gracias al
#  permiso kms:Decrypt de su rol.
# ============================================================

locals {
  prefijo_ssm = "/${var.proyecto}/${var.entorno}"
}

resource "aws_ssm_parameter" "bd_host" {
  name        = "${local.prefijo_ssm}/db/host"
  description = "Punto de enlace de la instancia de RDS"
  type        = "String"
  value       = aws_db_instance.principal.address
}

resource "aws_ssm_parameter" "bd_puerto" {
  name  = "${local.prefijo_ssm}/db/port"
  type  = "String"
  value = tostring(aws_db_instance.principal.port)
}

resource "aws_ssm_parameter" "bd_nombre" {
  name  = "${local.prefijo_ssm}/db/name"
  type  = "String"
  value = var.nombre_bd
}

resource "aws_ssm_parameter" "bd_usuario" {
  name        = "${local.prefijo_ssm}/db/user"
  description = "Usuario de la aplicacion, creado por el modulo db-bootstrap"
  type        = "String"
  value       = var.usuario_app_bd
}

resource "aws_ssm_parameter" "bd_password" {
  name        = "${local.prefijo_ssm}/db/password"
  description = "Contrasena del usuario de la aplicacion"
  type        = "SecureString"
  value       = random_password.app_bd.result
}

resource "aws_ssm_parameter" "bd_password_maestro" {
  name        = "${local.prefijo_ssm}/db/master-password"
  description = "Contrasena del usuario maestro de RDS. La usa db-bootstrap a traves del tunel de SSM."
  type        = "SecureString"
  value       = random_password.maestro_bd.result
}

resource "aws_ssm_parameter" "bd_usuario_maestro" {
  name  = "${local.prefijo_ssm}/db/master-user"
  type  = "String"
  value = var.usuario_maestro_bd
}

resource "aws_ssm_parameter" "backend_puerto" {
  name  = "${local.prefijo_ssm}/app/server-port"
  type  = "String"
  value = "5000"
}

resource "aws_ssm_parameter" "backend_url" {
  name        = "${local.prefijo_ssm}/app/backend-url"
  description = "URL del backend vista por el frontend: ambos comparten la instancia"
  type        = "String"
  value       = "http://localhost:5000"
}

resource "aws_ssm_parameter" "bucket_artefactos" {
  name  = "${local.prefijo_ssm}/app/artifacts-bucket"
  type  = "String"
  value = aws_s3_bucket.artefactos.bucket
}
