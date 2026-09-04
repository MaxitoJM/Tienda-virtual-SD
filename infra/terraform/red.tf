# ============================================================
#  Red: VPC propia, dos subredes publicas y dos privadas en zonas
#  de disponibilidad distintas, pasarela de internet y tabla de
#  rutas publica.
#
#  No hay NAT Gateway a proposito: nada en las subredes privadas
#  necesita salir a internet (RDS no lo requiere) y una NAT costaria
#  mas que el resto de la infraestructura junta.
# ============================================================

data "aws_availability_zones" "disponibles" {
  state = "available"

  filter {
    name   = "opt-in-status"
    values = ["opt-in-not-required"]
  }
}

locals {
  nombre = "${var.proyecto}-${var.entorno}"

  # Dos zonas: es el minimo que exige el grupo de subredes de RDS.
  zonas = slice(data.aws_availability_zones.disponibles.names, 0, 2)
}

resource "aws_vpc" "principal" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = { Name = local.nombre }
}

resource "aws_internet_gateway" "principal" {
  vpc_id = aws_vpc.principal.id

  tags = { Name = local.nombre }
}

resource "aws_subnet" "publicas" {
  count = length(var.subredes_publicas_cidr)

  vpc_id                  = aws_vpc.principal.id
  cidr_block              = var.subredes_publicas_cidr[count.index]
  availability_zone       = local.zonas[count.index]
  map_public_ip_on_launch = true

  tags = { Name = "${local.nombre}-publica-${local.zonas[count.index]}" }
}

resource "aws_subnet" "privadas" {
  count = length(var.subredes_privadas_cidr)

  vpc_id            = aws_vpc.principal.id
  cidr_block        = var.subredes_privadas_cidr[count.index]
  availability_zone = local.zonas[count.index]

  tags = { Name = "${local.nombre}-privada-${local.zonas[count.index]}" }
}

resource "aws_route_table" "publica" {
  vpc_id = aws_vpc.principal.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.principal.id
  }

  tags = { Name = "${local.nombre}-publica" }
}

resource "aws_route_table_association" "publicas" {
  count = length(aws_subnet.publicas)

  subnet_id      = aws_subnet.publicas[count.index].id
  route_table_id = aws_route_table.publica.id
}

# Las subredes privadas se quedan con la tabla de rutas principal de la
# VPC, que solo enruta trafico local. Sin ruta a la pasarela de internet
# y sin NAT, no tienen ninguna salida.
