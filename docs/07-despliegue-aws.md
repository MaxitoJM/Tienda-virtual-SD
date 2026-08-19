# 7. Despliegue en AWS

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

## 1. Asignación de la cuenta IAM

El líder de soporte asigna al grupo una cuenta IAM de AWS. Las instrucciones de ingreso
llegan por correo electrónico al integrante designado para el uso y custodia de la cuenta.

## 2. Creación de la instancia EC2

Crear la instancia dentro de los parámetros de la **capa gratuita**:

| Paso | Configuración |
|---|---|
| 1. AMI | Amazon Linux 2 (elegible para capa gratuita) |
| 2. Tipo de instancia | `t2.micro` |
| 3. Par de claves | Crear un par nuevo y **descargar el archivo `.pem`** |
| 4. Grupo de seguridad | Abrir los puertos 22 (SSH), 80 (HTTP) y 5000 (backend) |

Al lanzarla, la instancia debe aparecer en estado *running* en el panel de EC2.

## 3. Creación de la instancia RDS (MySQL)

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

## 4. Entorno Elastic Beanstalk para el backend

| Parámetro | Valor |
|---|---|
| Tipo de entorno | Entorno de servidor web |
| Nombre de la aplicación | `EquipoXpruebas` |
| Nombre del entorno | `EquipoXpruebasbackend-env` |
| Plataforma | **Java** (Corretto 11) |
| Código de la aplicación | Se carga en el paso 7 |

## 5. Entorno Elastic Beanstalk para el frontend

Igual al anterior, cambiando dos valores:

| Parámetro | Valor |
|---|---|
| Nombre del entorno | `EquipoXpruebasfrontend-env` |
| Plataforma | **Tomcat** (necesaria para desplegar el archivo `.WAR`) |

## 6. Configuración del backend contra RDS

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

## 7. Generación y carga del backend (.JAR)

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

## 8. Generación y carga del frontend (.WAR)

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

## 9. Verificación

La raíz del entorno responde con un error 404 de Tomcat (comportamiento esperado:
indica que Tomcat está en funcionamiento). La aplicación se abre en:

```
http://equipoXpruebasfrontend-env.xxxxxxxx.elasticbeanstalk.com/ciclo3demo/inicio.jsp
```

Ingresar con `admininicial` / `admin123456`, crear un usuario y consultar el listado
de usuarios para comprobar que la interacción frontend–backend es correcta.

## 10. Lista de verificación

- [ ] Instancia RDS MySQL 8.0.24 creada y accesible.
- [ ] Grupo de seguridad de RDS permite conexiones desde los entornos de Beanstalk.
- [ ] Entorno de backend con plataforma Java y `SERVER_PORT=5000`.
- [ ] Entorno de frontend con plataforma Tomcat y `BACKEND_URL` configurada.
- [ ] `/usuarios/listar` del backend responde JSON.
- [ ] `inicio.jsp` del frontend permite iniciar sesión.
- [ ] Carga del CSV de productos exitosa contra la base de datos de RDS.
