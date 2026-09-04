# ============================================================
#  Instancia de aplicacion: una sola EC2 en subred publica con una
#  direccion IP elastica, que aloja el backend en el puerto 5000 y
#  Tomcat 9 en el 8080.
#
#  El frontend llama al backend por localhost, de modo que el trafico
#  entre ambos no sale de la instancia y el puerto 5000 no se publica.
#
#  No hay par de claves ni puerto 22: la administracion es por SSM
#  Session Manager, que sale desde la instancia y no necesita ninguna
#  regla de entrada.
# ============================================================

data "aws_ssm_parameter" "ami_al2023" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

resource "aws_instance" "aplicacion" {
  ami           = data.aws_ssm_parameter.ami_al2023.value
  instance_type = var.tipo_instancia

  subnet_id              = aws_subnet.publicas[0].id
  vpc_security_group_ids = [aws_security_group.instancia.id]
  iam_instance_profile   = aws_iam_instance_profile.instancia.name

  user_data = templatefile("${path.module}/plantillas/user-data.sh.tftpl", {
    region             = var.region
    prefijo_ssm        = local.prefijo_ssm
    bucket_artefactos  = aws_s3_bucket.artefactos.bucket
    version_tomcat     = var.version_tomcat
    memoria_maxima_jvm = var.memoria_maxima_jvm
  })

  # Cambiar el aprovisionamiento sustituye la instancia. Es seguro: no
  # guarda ningun estado, los datos viven en RDS y los artefactos en S3,
  # y la IP elastica se vuelve a asociar sola.
  user_data_replace_on_change = true

  root_block_device {
    volume_type = "gp3"
    volume_size = var.tamano_disco_gb
    encrypted   = true
  }

  # IMDSv2 obligatorio: cierra el acceso a las credenciales del rol
  # desde una peticion falsificada del lado del servidor.
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  tags = { Name = "${local.nombre}-app" }

  depends_on = [
    aws_ssm_parameter.bd_host,
    aws_ssm_parameter.bd_puerto,
    aws_ssm_parameter.bd_nombre,
    aws_ssm_parameter.bd_usuario,
    aws_ssm_parameter.bd_password,
    aws_ssm_parameter.backend_puerto,
    aws_ssm_parameter.backend_url,
    aws_ssm_parameter.bucket_artefactos,
  ]
}

# La IP elastica es un recurso aparte para que sobreviva a la
# sustitucion de la instancia y la direccion publica no cambie.
resource "aws_eip" "aplicacion" {
  domain = "vpc"

  tags = { Name = "${local.nombre}-app" }

  depends_on = [aws_internet_gateway.principal]
}

resource "aws_eip_association" "aplicacion" {
  instance_id   = aws_instance.aplicacion.id
  allocation_id = aws_eip.aplicacion.id
}
