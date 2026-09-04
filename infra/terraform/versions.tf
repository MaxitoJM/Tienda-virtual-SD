# ============================================================
#  Version de Terraform, proveedores y almacenamiento del estado
# ============================================================

terraform {
  required_version = ">= 1.10"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # Estado remoto en S3 con bloqueo nativo del propio bucket
  # (use_lockfile), disponible desde Terraform 1.10: no hace falta
  # ninguna tabla de DynamoDB.
  #
  # La configuracion es parcial a proposito: el nombre del bucket depende
  # de la cuenta y no se versiona. Se aporta al inicializar:
  #
  #     terraform init -backend-config=backend.hcl
  #
  # El bucket lo crea antes el guion bootstrap/crear-bucket-estado.sh
  backend "s3" {
    use_lockfile = true
    encrypt      = true
  }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Proyecto  = var.proyecto
      Entorno   = var.entorno
      Gestion   = "terraform"
      Repositor = "Tienda-virtual-SD"
    }
  }
}
