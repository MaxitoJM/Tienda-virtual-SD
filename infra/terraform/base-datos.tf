# ============================================================
#  RDS MySQL 8.4 en las subredes privadas.
#
#  No es accesible desde internet: la unica ruta hacia el puerto 3306
#  es el grupo de seguridad de la instancia de aplicacion. Para
#  administrarla desde un puesto de trabajo se usa el tunel de SSM
#  descrito en el README.
#
#  Las contrasenas se generan aqui y se guardan cifradas en SSM
#  Parameter Store. No aparecen en el codigo ni hay que exportarlas
#  como variables de entorno del intérprete de ordenes.
# ============================================================

# El juego de simbolos evita los caracteres que RDS rechaza (/ @ " y el
# espacio) y tambien los que complican al interprete de ordenes y al
# formato EnvironmentFile de systemd (comillas, barra invertida, $, &, #).
resource "random_password" "maestro_bd" {
  length           = 32
  special          = true
  override_special = "!*()-_=+[]{}:?."
}

# Contrasena del usuario con el que se conecta la aplicacion. La consume
# el modulo db-bootstrap al crear el usuario, y la instancia al arrancar.
resource "random_password" "app_bd" {
  length           = 32
  special          = true
  override_special = "!*()-_=+[]{}:?."
}

resource "aws_db_subnet_group" "principal" {
  name        = "${local.nombre}-privadas"
  description = "Subredes privadas de ${local.nombre}"
  subnet_ids  = aws_subnet.privadas[*].id

  tags = { Name = "${local.nombre}-privadas" }
}

resource "aws_db_instance" "principal" {
  identifier = "${local.nombre}-mysql"

  engine         = "mysql"
  engine_version = var.version_motor_bd
  instance_class = var.clase_instancia_bd

  allocated_storage     = var.almacenamiento_bd_gb
  max_allocated_storage = 0
  storage_type          = "gp3"
  storage_encrypted     = true

  # No se define db_name: la base de datos y el usuario de la aplicacion
  # los crea el modulo db-bootstrap, para que el usuario que usa la
  # aplicacion no sea el maestro.
  username = var.usuario_maestro_bd
  password = random_password.maestro_bd.result

  multi_az               = false
  publicly_accessible    = false
  db_subnet_group_name   = aws_db_subnet_group.principal.name
  vpc_security_group_ids = [aws_security_group.base_datos.id]

  backup_retention_period = var.retencion_copias_dias
  backup_window           = "06:00-07:00"
  maintenance_window      = "Mon:07:30-Mon:08:30"
  copy_tags_to_snapshot   = true

  auto_minor_version_upgrade = true
  deletion_protection        = var.proteccion_borrado_bd

  skip_final_snapshot       = var.omitir_instantanea_final
  final_snapshot_identifier = var.omitir_instantanea_final ? null : "${local.nombre}-mysql-final"

  tags = { Name = "${local.nombre}-mysql" }
}
