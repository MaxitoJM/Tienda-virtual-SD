#!/usr/bin/env bash
# ============================================================
#  Crea el bucket de S3 que guarda el estado de Terraform.
#
#  Es el unico paso que no se hace con Terraform, por el problema del
#  huevo y la gallina: el backend remoto no puede almacenarse a si
#  mismo. Se ejecuta una sola vez, antes del primer "terraform init".
#
#  El bloqueo del estado lo da el propio S3 (use_lockfile en
#  versions.tf), asi que no hace falta ninguna tabla de DynamoDB.
#
#  Uso:  ./crear-bucket-estado.sh [region]
# ============================================================
set -euo pipefail

REGION="${1:-us-east-1}"
PROYECTO="tienda-virtual"

echo "==> Comprobando la identidad de AWS"
IDENTIDAD=$(aws sts get-caller-identity --output json)
CUENTA=$(echo "$IDENTIDAD" | sed -n 's/.*"Account": *"\([0-9]*\)".*/\1/p')

if [[ -z "$CUENTA" ]]; then
  echo "ERROR: no se pudo determinar la cuenta. Revise sus credenciales." >&2
  exit 1
fi

echo "    Cuenta ...${CUENTA: -2}   Region $REGION"

# Salvaguarda: solo se opera sobre la cuenta acordada.
if [[ "${CUENTA: -2}" != "95" ]]; then
  echo "ERROR: la cuenta no termina en 95. Se detiene sin crear nada." >&2
  exit 1
fi

# Sufijo estable derivado de la cuenta: repetir el guion no crea un
# bucket nuevo, y el identificador de la cuenta no queda expuesto.
SUFIJO=$(printf '%s-%s' "$CUENTA" "$REGION" | shasum -a 256 | cut -c1-8)
BUCKET="${PROYECTO}-estado-${SUFIJO}"

echo "==> Bucket de estado: $BUCKET"

if aws s3api head-bucket --bucket "$BUCKET" 2>/dev/null; then
  echo "    Ya existe. No se vuelve a crear."
else
  echo "    Creandolo..."
  if [[ "$REGION" == "us-east-1" ]]; then
    aws s3api create-bucket --bucket "$BUCKET" --region "$REGION"
  else
    aws s3api create-bucket --bucket "$BUCKET" --region "$REGION" \
      --create-bucket-configuration "LocationConstraint=$REGION"
  fi
fi

echo "==> Versionado (permite recuperar un estado anterior)"
aws s3api put-bucket-versioning --bucket "$BUCKET" \
  --versioning-configuration Status=Enabled

echo "==> Cifrado en reposo"
aws s3api put-bucket-encryption --bucket "$BUCKET" \
  --server-side-encryption-configuration \
  '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

echo "==> Bloqueo de acceso publico"
aws s3api put-public-access-block --bucket "$BUCKET" \
  --public-access-block-configuration \
  'BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true'

cat <<FIN

============================================================
 Bucket de estado listo.

 Escriba infra/terraform/backend.hcl con este contenido
 (no se versiona) y despues ejecute el init:

bucket = "$BUCKET"
key    = "prod/infra.tfstate"
region = "$REGION"

     cd infra/terraform
     terraform init -backend-config=backend.hcl
============================================================
FIN
