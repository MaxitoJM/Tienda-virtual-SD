variable "region" {
  description = "Region de AWS donde estan los parametros de SSM y la instancia de RDS."
  type        = string
  default     = "us-east-1"
}

variable "proyecto" {
  description = "Debe coincidir con la variable homonima del modulo raiz."
  type        = string
  default     = "tienda-virtual"
}

variable "entorno" {
  description = "Debe coincidir con la variable homonima del modulo raiz."
  type        = string
  default     = "prod"
}

variable "extremo_local" {
  description = <<-TEXTO
    Extremo local del tunel de SSM. Es donde escucha el reenvio de
    puerto, no el punto de enlace de RDS.
  TEXTO
  type        = string
  default     = "127.0.0.1:3306"
}

variable "nombre_bd" {
  description = "Nombre de la base de datos de la aplicacion."
  type        = string
  default     = "tiendagenerica"
}

variable "usuario_app" {
  description = <<-TEXTO
    Usuario de la aplicacion. Debe coincidir con la variable usuario_app_bd
    del modulo raiz; una precondicion lo comprueba contra el parametro
    /db/user de SSM antes de crear nada.
  TEXTO
  type        = string
  default     = "tienda_app"
}

variable "host_usuario_app" {
  description = <<-TEXTO
    Patron de origen del usuario de la aplicacion en MySQL. Por defecto
    se limita al rango de la VPC, de modo que la cuenta no sirve desde
    fuera de la red aunque alguien conociera su contrasena.
  TEXTO
  type        = string
  default     = "10.20.%"
}
