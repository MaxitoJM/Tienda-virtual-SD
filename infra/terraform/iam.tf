# ============================================================
#  Rol y perfil de instancia de la EC2.
#
#  Tres permisos y ninguno mas:
#    - AmazonSSMManagedInstanceCore, que habilita Session Manager y
#      sustituye por completo al acceso por SSH.
#    - Lectura de los parametros del proyecto, acotada por prefijo.
#    - Lectura del bucket de artefactos, acotada a ese bucket.
# ============================================================

data "aws_caller_identity" "actual" {}

data "aws_region" "actual" {}

# Clave gestionada de AWS con la que SSM cifra los SecureString.
data "aws_kms_alias" "ssm" {
  name = "alias/aws/ssm"
}

data "aws_iam_policy_document" "asumir_ec2" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "instancia" {
  name               = "${local.nombre}-ec2"
  description        = "Rol de la instancia de aplicacion de ${local.nombre}"
  assume_role_policy = data.aws_iam_policy_document.asumir_ec2.json
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.instancia.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

data "aws_iam_policy_document" "instancia" {
  statement {
    sid    = "LeerParametrosDelProyecto"
    effect = "Allow"

    actions = [
      "ssm:GetParameter",
      "ssm:GetParameters",
      "ssm:GetParametersByPath",
    ]

    resources = [
      "arn:aws:ssm:${data.aws_region.actual.region}:${data.aws_caller_identity.actual.account_id}:parameter${local.prefijo_ssm}",
      "arn:aws:ssm:${data.aws_region.actual.region}:${data.aws_caller_identity.actual.account_id}:parameter${local.prefijo_ssm}/*",
    ]
  }

  statement {
    sid       = "DescifrarParametrosSeguros"
    effect    = "Allow"
    actions   = ["kms:Decrypt"]
    resources = [data.aws_kms_alias.ssm.target_key_arn]

    condition {
      test     = "StringEquals"
      variable = "kms:ViaService"
      values   = ["ssm.${data.aws_region.actual.region}.amazonaws.com"]
    }
  }

  statement {
    sid     = "DescargarArtefactos"
    effect  = "Allow"
    actions = ["s3:GetObject", "s3:GetObjectVersion"]

    resources = ["${aws_s3_bucket.artefactos.arn}/*"]
  }

  statement {
    sid       = "ListarBucketDeArtefactos"
    effect    = "Allow"
    actions   = ["s3:ListBucket"]
    resources = [aws_s3_bucket.artefactos.arn]
  }
}

resource "aws_iam_role_policy" "instancia" {
  name   = "${local.nombre}-ec2"
  role   = aws_iam_role.instancia.id
  policy = data.aws_iam_policy_document.instancia.json
}

resource "aws_iam_instance_profile" "instancia" {
  name = "${local.nombre}-ec2"
  role = aws_iam_role.instancia.name
}
