# ============================================================
#  Salidas. Ninguna revela una contrasena: las que hay se leen de
#  SSM con la orden que se indica mas abajo.
# ============================================================

output "ip_publica" {
  description = "Direccion IP elastica de la instancia de aplicacion."
  value       = aws_eip.aplicacion.public_ip
}

output "url_aplicacion" {
  description = "Direccion con la que se abre el frontend."
  value       = "http://${aws_eip.aplicacion.public_ip}:8080/ciclo3demo/inicio.jsp"
}

output "id_instancia" {
  description = "Identificador de la instancia. Es el destino del tunel de SSM."
  value       = aws_instance.aplicacion.id
}

output "punto_enlace_bd" {
  description = "Punto de enlace de RDS. Solo alcanzable desde la instancia o por el tunel."
  value       = aws_db_instance.principal.address
}

output "bucket_artefactos" {
  description = "Bucket donde se suben appbackend.jar y ciclo3demo.war."
  value       = aws_s3_bucket.artefactos.bucket
}

output "comando_tunel_ssm" {
  description = "Abre el tunel local hacia RDS. Necesario para el modulo db-bootstrap."
  value = join(" ", [
    "aws ssm start-session",
    "--region ${var.region}",
    "--target ${aws_instance.aplicacion.id}",
    "--document-name AWS-StartPortForwardingSessionToRemoteHost",
    "--parameters '{\"host\":[\"${aws_db_instance.principal.address}\"],\"portNumber\":[\"3306\"],\"localPortNumber\":[\"3306\"]}'",
  ])
}

output "comando_sesion_ssm" {
  description = "Abre un interprete de ordenes en la instancia, sin SSH."
  value       = "aws ssm start-session --region ${var.region} --target ${aws_instance.aplicacion.id}"
}

output "comando_subir_artefactos" {
  description = "Sube los dos artefactos ya compilados al bucket."
  value = join("\n", [
    "aws s3 cp AppBackend/target/appbackend.jar  s3://${aws_s3_bucket.artefactos.bucket}/appbackend.jar",
    "aws s3 cp AppFrontend/target/ciclo3demo.war s3://${aws_s3_bucket.artefactos.bucket}/ciclo3demo.war",
  ])
}

output "prefijo_parametros_ssm" {
  description = "Prefijo bajo el que estan todos los parametros del proyecto."
  value       = local.prefijo_ssm
}

output "comando_leer_password_maestro" {
  description = "Muestra la contrasena del usuario maestro de RDS. No se imprime en las salidas."
  value = join(" ", [
    "aws ssm get-parameter --region ${var.region}",
    "--name ${local.prefijo_ssm}/db/master-password",
    "--with-decryption --query Parameter.Value --output text",
  ])
}
