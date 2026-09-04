# ============================================================
#  Variables del modulo raiz
#
#  Ninguna contiene credenciales ni identificadores de cuenta.
# ============================================================

variable "region" {
  description = "Region de AWS donde se despliega toda la infraestructura."
  type        = string
  default     = "us-east-1"
}

variable "proyecto" {
  description = "Nombre corto del proyecto. Prefija el nombre de los recursos."
  type        = string
  default     = "tienda-virtual"
}

variable "entorno" {
  description = "Nombre del entorno. Forma parte de los nombres y de las etiquetas."
  type        = string
  default     = "prod"
}

# ---------- Red ----------

variable "vpc_cidr" {
  description = "Rango de direcciones de la VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "subredes_publicas_cidr" {
  description = "Rangos de las dos subredes publicas, una por zona de disponibilidad."
  type        = list(string)
  default     = ["10.20.0.0/24", "10.20.1.0/24"]

  validation {
    condition     = length(var.subredes_publicas_cidr) == 2
    error_message = "Se necesitan exactamente dos subredes publicas, en zonas distintas."
  }
}

variable "subredes_privadas_cidr" {
  description = <<-TEXTO
    Rangos de las dos subredes privadas, una por zona de disponibilidad.
    RDS exige un grupo de subredes con al menos dos zonas aunque la
    instancia sea de zona unica.
  TEXTO
  type        = list(string)
  default     = ["10.20.10.0/24", "10.20.11.0/24"]

  validation {
    condition     = length(var.subredes_privadas_cidr) == 2
    error_message = "Se necesitan exactamente dos subredes privadas, en zonas distintas."
  }
}

variable "cidr_acceso_web" {
  description = <<-TEXTO
    Origenes autorizados a alcanzar el puerto 8080 de la instancia.
    Por defecto, todo internet: es una aplicacion web publica. Se puede
    restringir a la red del equipo durante las pruebas.
  TEXTO
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

# ---------- Instancia de computo ----------

variable "tipo_instancia" {
  description = "Tipo de la instancia EC2 que aloja el backend y Tomcat."
  type        = string
  default     = "t3.medium"
}

variable "tamano_disco_gb" {
  description = "Tamano del volumen raiz de la instancia EC2, en GB."
  type        = number
  default     = 30
}

variable "version_tomcat" {
  description = "Version exacta de Apache Tomcat 9 que instala user_data."
  type        = string
  default     = "9.0.121"

  validation {
    condition     = can(regex("^9\\.0\\.[0-9]+$", var.version_tomcat))
    error_message = "El frontend es una aplicacion Servlet 4.0: exige la serie Tomcat 9.0.x."
  }
}

variable "memoria_maxima_jvm" {
  description = <<-TEXTO
    Valor de -Xmx de cada una de las dos maquinas virtuales de Java.
    Con 1g cada una, las dos caben holgadamente en los 4 GB de una
    t3.medium dejando margen para el sistema operativo.
  TEXTO
  type        = string
  default     = "1g"
}

# ---------- Base de datos ----------

variable "version_motor_bd" {
  description = <<-TEXTO
    Version mayor del motor MySQL en RDS. Se fija 8.4 y no 8.0 porque el
    soporte estandar de MySQL 8.0 en RDS termino el 31 de julio de 2026 y
    desde el 1 de agosto genera cargos de Extended Support por vCPU-hora.
  TEXTO
  type        = string
  default     = "8.4"
}

variable "clase_instancia_bd" {
  description = "Clase de la instancia de RDS."
  type        = string
  default     = "db.t4g.micro"
}

variable "almacenamiento_bd_gb" {
  description = "Almacenamiento asignado a RDS, en GB. gp3 admite 20 GB como minimo."
  type        = number
  default     = 20
}

variable "retencion_copias_dias" {
  description = "Dias de retencion de las copias de seguridad automaticas de RDS."
  type        = number
  default     = 7
}

variable "nombre_bd" {
  description = "Nombre de la base de datos de la aplicacion."
  type        = string
  default     = "tiendagenerica"
}

variable "usuario_maestro_bd" {
  description = "Usuario maestro de la instancia de RDS. Su contrasena se genera y se guarda en SSM."
  type        = string
  default     = "admin"
}

variable "usuario_app_bd" {
  description = <<-TEXTO
    Usuario con el que la aplicacion se conecta a la base de datos. Lo crea
    el modulo db-bootstrap con privilegios acotados a una sola base.
  TEXTO
  type        = string
  default     = "tienda_app"
}

variable "proteccion_borrado_bd" {
  description = "Impide eliminar la instancia de RDS. Conviene activarlo si los datos importan."
  type        = bool
  default     = false
}

variable "omitir_instantanea_final" {
  description = <<-TEXTO
    Si es true, destruir la infraestructura no deja instantanea final de la
    base de datos. Es comodo para un entorno academico que se crea y se
    destruye; ponerlo en false si los datos deben sobrevivir al destroy.
  TEXTO
  type        = bool
  default     = true
}
