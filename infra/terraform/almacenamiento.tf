# ============================================================
#  Bucket privado con los artefactos desplegables:
#  appbackend.jar y ciclo3demo.war.
#
#  Tiene versionado para poder volver a una version anterior sin
#  recompilar: basta con restaurar el objeto y reiniciar el servicio.
# ============================================================

resource "random_id" "sufijo" {
  byte_length = 4
}

resource "aws_s3_bucket" "artefactos" {
  bucket = "${local.nombre}-artefactos-${random_id.sufijo.hex}"

  tags = { Name = "${local.nombre}-artefactos" }
}

resource "aws_s3_bucket_versioning" "artefactos" {
  bucket = aws_s3_bucket.artefactos.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_public_access_block" "artefactos" {
  bucket = aws_s3_bucket.artefactos.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "artefactos" {
  bucket = aws_s3_bucket.artefactos.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_ownership_controls" "artefactos" {
  bucket = aws_s3_bucket.artefactos.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}
