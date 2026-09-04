# 7. Despliegue en AWS

El despliegue oficial del proyecto es **infraestructura como código con
Terraform**. La guía operativa completa —prerrequisitos, orden de ejecución,
túnel de SSM y destrucción— está en
[`infra/terraform/README.md`](../infra/terraform/README.md); este documento
explica la arquitectura y por qué es así.

La ruta anterior por Elastic Beanstalk se conserva al final como
[anexo histórico](#anexo-histórico-despliegue-manual-con-elastic-beanstalk).

## Arquitectura

```
                    Internet
                        │  :8080
        ┌───────────────▼──────────────────────────┐
        │  Subred pública (AZ a)                   │
        │  ┌────────────────────────────────────┐  │
        │  │ EC2 t3.medium · Amazon Linux 2023  │  │   ← IP elástica
        │  │   tomcat.service      :8080  ─┐    │  │
        │  │   appbackend.service  :5000  ◄┘    │  │     (localhost)
        │  └──────────────┬─────────────────────┘  │
        └─────────────────┼────────────────────────┘
                          │  3306 (solo entre grupos de seguridad)
        ┌─────────────────▼────────────────────────┐
        │  Subredes privadas (AZ a + AZ b)         │
        │  ┌────────────────────────────────────┐  │
        │  │ RDS MySQL 8.4 · db.t4g.micro       │  │
        │  │ sin acceso público · cifrada       │  │
        │  └────────────────────────────────────┘  │
        └──────────────────────────────────────────┘
```

| Componente | Elección | Motivo |
|---|---|---|
| Cómputo | 1 × EC2 `t3.medium`, Amazon Linux 2023 | Los 4 GB alojan las dos JVM con `-Xmx1g` cada una y dejan margen al sistema |
| Aplicaciones | Dos unidades de systemd independientes | Se reinician por separado; un fallo del frontend no tumba la API |
| Base de datos | RDS MySQL 8.4, `db.t4g.micro`, 20 GB gp3 | Ver [nota sobre la versión](#por-qué-mysql-84-y-no-8024) |
| Red | VPC propia, 2 subredes públicas y 2 privadas | RDS exige dos zonas en su grupo de subredes aunque sea de zona única |
| Salida a internet | Solo pasarela de internet, **sin NAT** | Nada en las subredes privadas necesita salir; una NAT costaría más que todo lo demás junto |
| Administración | SSM Session Manager | Sin puerto 22, sin par de claves, sin regla de entrada |
| Secretos | SSM Parameter Store (`SecureString`) | Generados con `random_password`; no hay contraseñas en el repositorio |
| Artefactos | Bucket S3 privado con versionado | Permite volver a una versión anterior sin recompilar |
| Estado de Terraform | S3 con `use_lockfile` | Bloqueo nativo de S3 desde Terraform 1.10; no hace falta DynamoDB |

## Por qué el backend no se publica

El frontend y el backend comparten instancia y el frontend llama a la API por
`localhost:5000`. Ese tráfico no sale de la máquina, así que **el puerto 5000 no
aparece en ningún grupo de seguridad**: la API no es alcanzable desde internet.
El único puerto abierto es el 8080 de Tomcat.

## Cómo se protege la base de datos

El grupo de seguridad de RDS admite el puerto 3306 **referenciando el grupo de
seguridad de la instancia**, no un rango de direcciones. Si la instancia se
sustituye y cambia de IP, la regla sigue siendo correcta, y en ningún momento
hay un rango de red con acceso al motor.

Como RDS no es accesible desde internet, administrarla desde un puesto de
trabajo exige un túnel de reenvío de puerto por SSM. El comando exacto lo
imprime la salida `comando_tunel_ssm` de Terraform.

## Por qué MySQL 8.4 y no 8.0.24

El documento de especificación indica MySQL 8.0.24. El despliegue usa **MySQL
8.4** porque el soporte estándar de MySQL 8.0 en Amazon RDS terminó el **31 de
julio de 2026**, y desde el **1 de agosto de 2026** las instancias que sigan en
8.0 generan cargos de *Extended Support*, facturados por vCPU-hora.

Consecuencias en el proyecto:

- El conector pasa a `com.mysql:mysql-connector-j` 8.4.0, con `<version>`
  explícita: Spring Boot 2.4.5 solo gestiona la coordenada antigua
  `mysql:mysql-connector-java`, ya descontinuada.
- `docker-compose.yml` y la integración continua usan `mysql:8.4`, para que el
  entorno local y el desplegado no difieran en el motor.
- Desaparece `--default-authentication-plugin=mysql_native_password`: MySQL 8.4
  eliminó ese complemento y esa opción. El usuario de la aplicación se crea con
  `caching_sha2_password`.

## Configuración de las aplicaciones

Ninguna configuración viaja dentro de los artefactos. Al arrancar la instancia,
`tienda-config.service` lee los parámetros de SSM y escribe dos
`EnvironmentFile` que consumen las otras dos unidades:

| Unidad | Archivo | Variables |
|---|---|---|
| `appbackend.service` | `/etc/tienda/backend.env` | `SPRING_PROFILES_ACTIVE=aws`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT` |
| `tomcat.service` | `/etc/tienda/tomcat.env` | `BACKEND_URL`, `CATALINA_OPTS` |

Cambiar un parámetro en SSM y reiniciar la instancia basta para aplicarlo: no
hay que recompilar ni volver a subir nada.

El backend arranca con el perfil `aws`, que exige `DB_HOST`, no crea la base de
datos —el usuario de la aplicación no tiene ese privilegio— y fuerza el cifrado
de la conexión con `sslMode=REQUIRED`.

## Privilegios del usuario de la aplicación

El módulo `db-bootstrap` crea `tienda_app` con permisos acotados a la base
`tiendagenerica`, sin `GRANT ALL` y sin privilegios globales:

```sql
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON `tiendagenerica`.* TO `tienda_app`@`10.20.%`
```

Los cuatro últimos son de definición de esquema y hacen falta porque el backend
arranca con `spring.jpa.hibernate.ddl-auto=update`, es decir, crea y ajusta sus
propias tablas. **`DROP` no se concede**: `update` nunca elimina tablas ni
columnas. El día que el proyecto incorpore migraciones versionadas (Flyway o
Liquibase) podrán retirarse y dejar solo los cuatro primeros.

El patrón de origen `10.20.%` limita la cuenta al rango de la VPC: no sirve
desde fuera de la red aunque alguien conociera su contraseña.

## Lista de verificación del despliegue

- [ ] `aws sts get-caller-identity` apunta a la cuenta correcta.
- [ ] Bucket del estado creado y `backend.hcl` escrito.
- [ ] `terraform apply` completado sin errores.
- [ ] `db-bootstrap` aplicado a través del túnel de SSM.
- [ ] `appbackend.jar` y `ciclo3demo.war` subidos al bucket de artefactos.
- [ ] Los tres servicios responden `active` en la instancia.
- [ ] `inicio.jsp` permite iniciar sesión con `admininicial`.
- [ ] El puerto 5000 **no** responde desde internet.
- [ ] `SELECT ... FROM mysql.user` confirma `caching_sha2_password`.

---

# Anexo histórico: despliegue manual con Elastic Beanstalk

> **Esta sección ya no es el camino de despliegue.** Se conserva porque
> documenta el procedimiento seguido durante el ciclo académico, conforme a la
> sección de especificaciones de AWS del documento original. Describe MySQL
> 8.0.24, instancias `t2.micro` y acceso por SSH en el puerto 22, todo ello
> sustituido por la arquitectura de arriba.


Arquitectura de despliegue, conforme a la sección de especificaciones de AWS del documento:

```
                 ┌──────────────────────────────┐
   Navegador ──▶ │ Elastic Beanstalk (Tomcat 9) │  ciclo3demo.war   :80
                 │        AppFrontend           │
                 └───────────────┬──────────────┘
                                 │  HTTP / REST
                 ┌───────────────▼──────────────┐
                 │ Elastic Beanstalk (Java SE)  │  appbackend.jar   :5000
                 │        AppBackend            │
                 └───────────────┬──────────────┘
                                 │  JDBC
                 ┌───────────────▼──────────────┐
                 │      RDS - MySQL 8.0.24      │  tiendagenerica   :3306
                 └──────────────────────────────┘
```

### 1. Asignación de la cuenta IAM

El líder de soporte asigna al grupo una cuenta IAM de AWS. Las instrucciones de ingreso
llegan por correo electrónico al integrante designado para el uso y custodia de la cuenta.

### 2. Creación de la instancia EC2

Crear la instancia dentro de los parámetros de la **capa gratuita**:

| Paso | Configuración |
|---|---|
| 1. AMI | Amazon Linux 2 (elegible para capa gratuita) |
| 2. Tipo de instancia | `t2.micro` |
| 3. Par de claves | Crear un par nuevo y **descargar el archivo `.pem`** |
| 4. Grupo de seguridad | Abrir los puertos 22 (SSH), 80 (HTTP) y 5000 (backend) |

Al lanzarla, la instancia debe aparecer en estado *running* en el panel de EC2.

### 3. Creación de la instancia RDS (MySQL)

| Parámetro | Valor |
|---|---|
| Motor | MySQL 8.0.24 |
| Plantilla | Capa gratuita |
| Identificador | `equipoXpruebas` |
| Usuario maestro | `admin` |
| Contraseña maestra | `admin123` |
| Nombre de la base de datos inicial | `tiendagenerica` |
| Acceso público | Sí (para las pruebas del ciclo) |

Al terminar, copiar el **punto de enlace (endpoint)** de la instancia; se necesita en el
paso 6.

### 4. Entorno Elastic Beanstalk para el backend

| Parámetro | Valor |
|---|---|
| Tipo de entorno | Entorno de servidor web |
| Nombre de la aplicación | `EquipoXpruebas` |
| Nombre del entorno | `EquipoXpruebasbackend-env` |
| Plataforma | **Java** (Corretto 11) |
| Código de la aplicación | Se carga en el paso 7 |

### 5. Entorno Elastic Beanstalk para el frontend

Igual al anterior, cambiando dos valores:

| Parámetro | Valor |
|---|---|
| Nombre del entorno | `EquipoXpruebasfrontend-env` |
| Plataforma | **Tomcat** (necesaria para desplegar el archivo `.WAR`) |

### 6. Configuración del backend contra RDS

Editar `AppBackend/src/main/resources/application.properties` (o mejor: definir las
variables de entorno del entorno de Elastic Beanstalk, que este proyecto ya soporta).

**Opción A — variables de entorno (recomendada).**
En *Configuración → Software → Propiedades del entorno*:

| Propiedad | Valor |
|---|---|
| `DB_HOST` | `equipoXpruebas.xxxxxxxx.us-east-2.rds.amazonaws.com` |
| `DB_PORT` | `3306` |
| `DB_NAME` | `tiendagenerica` |
| `DB_USER` | `admin` |
| `DB_PASSWORD` | `admin123` |
| `SERVER_PORT` | `5000` |

No requiere recompilar y evita dejar credenciales en el repositorio.

**Opción B — archivo de propiedades literal**, tal como aparece en el documento:

```properties
spring.jpa.database = MYSQL
spring.jpa.show-sql= true
spring.jpa.hibernate.ddl-auto=update
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://equipoXpruebas.xxxxxxxx.us-east-2.rds.amazonaws.com:3306/tiendagenerica?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC
spring.datasource.username=admin
spring.datasource.password=admin123
server.port=5000
```

> `server.port=5000` es obligatorio: es el puerto por el que Elastic Beanstalk
> enruta las peticiones hacia una aplicación Java SE.

### 7. Generación de los artefactos

El proyecto incluye un guion que compila las dos aplicaciones, ejecuta el conjunto de
pruebas y deja los artefactos listos para subir, con la configuración de
`.ebextensions` ya incorporada:

```
empaquetar-aws.bat
```

Genera en la carpeta `despliegue/`:

| Artefacto | Entorno |
|---|---|
| `appbackend-eb.zip` | Plataforma Java |
| `ciclo3demo.war` | Plataforma Tomcat |

El detalle de las propiedades que había que definir en cada entorno estaba en
`despliegue/LEEME.md`; ese archivo describe hoy el flujo de publicación vigente,
así que las propiedades de Elastic Beanstalk quedan recogidas en el apartado 6
de este mismo anexo.

Quien prefiera hacerlo manualmente puede seguir los pasos siguientes.

### 7.1. Generación y carga del backend (.JAR)

```bash
cd AppBackend
./mvnw clean package
# genera target/appbackend.jar
```

En Eclipse: *Run As → Maven build…* con el objetivo `clean package`.

En la consola de Elastic Beanstalk, entorno `EquipoXpruebasbackend-env` →
**Cargar e implementar** → seleccionar `appbackend.jar` → *Implementar*.

Verificar con el enlace del entorno:

```
http://equipoXpruebasbackend-env.xxxxxxxx.elasticbeanstalk.com/usuarios/listar
```

Debe devolver un JSON (inicialmente solo con el usuario `admininicial`).

### 8. Generación y carga del frontend (.WAR)

Antes de compilar, apuntar el frontend al backend desplegado. La URL se resuelve así:

1. Variable de entorno `BACKEND_URL`
2. Propiedad de sistema `backend.url`
3. Valor por defecto `http://localhost:5000`

En el entorno de Elastic Beanstalk del frontend, definir la propiedad:

| Propiedad | Valor |
|---|---|
| `BACKEND_URL` | `http://equipoXpruebasbackend-env.xxxxxxxx.elasticbeanstalk.com` |

Generar el WAR:

```bash
cd AppFrontend
./mvnw clean package
# genera target/ciclo3demo.war
```

En Eclipse: *Export → Web → WAR file*, con nombre `ciclo3demo.war`.

Cargar el WAR en el entorno `EquipoXpruebasfrontend-env` → *Implementar*.

### 9. Verificación

La raíz del entorno responde con un error 404 de Tomcat (comportamiento esperado:
indica que Tomcat está en funcionamiento). La aplicación se abre en:

```
http://equipoXpruebasfrontend-env.xxxxxxxx.elasticbeanstalk.com/ciclo3demo/inicio.jsp
```

Ingresar con `admininicial` / `admin123456`, crear un usuario y consultar el listado
de usuarios para comprobar que la interacción frontend–backend es correcta.

### 10. Lista de verificación

- [ ] Instancia RDS MySQL 8.0.24 creada y accesible.
- [ ] Grupo de seguridad de RDS permite conexiones desde los entornos de Beanstalk.
- [ ] Entorno de backend con plataforma Java y `SERVER_PORT=5000`.
- [ ] Entorno de frontend con plataforma Tomcat y `BACKEND_URL` configurada.
- [ ] `/usuarios/listar` del backend responde JSON.
- [ ] `inicio.jsp` del frontend permite iniciar sesión.
- [ ] Carga del CSV de productos exitosa contra la base de datos de RDS.
