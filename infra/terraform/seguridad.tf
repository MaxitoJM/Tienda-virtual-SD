# ============================================================
#  Grupos de seguridad.
#
#  El de la base de datos se referencia contra el de la instancia, no
#  contra un rango de direcciones: si la instancia cambia de IP, la
#  regla sigue siendo correcta y en ningun momento hay un rango de la
#  red abierto al puerto 3306.
#
#  No se abre el puerto 22 en ningun sitio. El acceso a la instancia es
#  por SSM Session Manager, que no necesita puertos de entrada.
# ============================================================

resource "aws_security_group" "instancia" {
  name        = "${local.nombre}-ec2"
  description = "Instancia de aplicacion: entrada solo al puerto de Tomcat"
  vpc_id      = aws_vpc.principal.id

  tags = { Name = "${local.nombre}-ec2" }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_vpc_security_group_ingress_rule" "instancia_tomcat" {
  count = length(var.cidr_acceso_web)

  security_group_id = aws_security_group.instancia.id
  description       = "Frontend en Tomcat 9"
  cidr_ipv4         = var.cidr_acceso_web[count.index]
  from_port         = 8080
  to_port           = 8080
  ip_protocol       = "tcp"
}

# El puerto 5000 del backend no se publica: el unico que lo consume es el
# frontend, que corre en esta misma instancia y lo alcanza por localhost.

resource "aws_vpc_security_group_egress_rule" "instancia_salida" {
  security_group_id = aws_security_group.instancia.id
  description       = "Salida a internet: SSM, S3, repositorios de paquetes"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_security_group" "base_datos" {
  name        = "${local.nombre}-rds"
  description = "RDS MySQL: entrada solo desde la instancia de aplicacion"
  vpc_id      = aws_vpc.principal.id

  tags = { Name = "${local.nombre}-rds" }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_vpc_security_group_ingress_rule" "base_datos_mysql" {
  security_group_id = aws_security_group.base_datos.id
  description       = "MySQL unicamente desde el grupo de seguridad de la instancia"
  from_port         = 3306
  to_port           = 3306
  ip_protocol       = "tcp"

  # Referencia entre grupos de seguridad, no un rango de direcciones.
  referenced_security_group_id = aws_security_group.instancia.id
}

# RDS no necesita ninguna regla de salida: solo responde conexiones.
