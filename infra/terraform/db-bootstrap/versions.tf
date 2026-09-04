# ============================================================
#  Modulo db-bootstrap: crea dentro de MySQL la base de datos de la
#  aplicacion, su usuario y los privilegios minimos.
#
#  Va aparte del modulo raiz por dos razones:
#    - Solo puede ejecutarse cuando RDS ya existe.
#    - Necesita un tunel de SSM abierto, porque RDS no es accesible
#      desde internet. El proveedor apunta a 127.0.0.1, que el tunel
#      reenvia hasta el punto de enlace de la base de datos.
#
#  El comando exacto del tunel esta en ../README.md y tambien lo
#  imprime la salida comando_tunel_ssm del modulo raiz.
# ============================================================

terraform {
  required_version = ">= 1.10"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    mysql = {
      source  = "petoju/mysql"
      version = "~> 3.0"
    }
  }

  # Estado propio, en el mismo bucket pero con otra clave, para que
  # este modulo se pueda aplicar y destruir sin tocar la infraestructura.
  backend "s3" {
    use_lockfile = true
    encrypt      = true
  }
}

provider "aws" {
  region = var.region
}

# Credenciales del usuario maestro. Se leen de SSM en el momento de
# ejecutar: no se escriben en ningun archivo ni se exportan al entorno.
data "aws_ssm_parameter" "usuario_maestro" {
  name = "/${var.proyecto}/${var.entorno}/db/master-user"
}

data "aws_ssm_parameter" "password_maestro" {
  name            = "/${var.proyecto}/${var.entorno}/db/master-password"
  with_decryption = true
}

data "aws_ssm_parameter" "usuario_app" {
  name = "/${var.proyecto}/${var.entorno}/db/user"
}

data "aws_ssm_parameter" "password_app" {
  name            = "/${var.proyecto}/${var.entorno}/db/password"
  with_decryption = true
}

provider "mysql" {
  # Extremo local del tunel de SSM, no el punto de enlace de RDS.
  endpoint = var.extremo_local
  username = data.aws_ssm_parameter.usuario_maestro.value
  password = data.aws_ssm_parameter.password_maestro.value

  # El trafico va cifrado, pero el certificado que presenta RDS esta
  # emitido para su nombre de dominio y aqui se conecta a 127.0.0.1: la
  # comprobacion de identidad no puede cuadrar a traves de un reenvio de
  # puerto. skip-verify mantiene el cifrado y omite esa comprobacion.
  tls = "skip-verify"
}
