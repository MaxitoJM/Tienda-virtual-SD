# Tienda Genérica Virtual

[![Integracion Continua](https://github.com/MaxitoJM/Tienda-virtual-SD/actions/workflows/ci.yml/badge.svg)](https://github.com/MaxitoJM/Tienda-virtual-SD/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-11-007396)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.4.5-6DB33F)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0.24-4479A1)](https://www.mysql.com/)
[![Pruebas](https://img.shields.io/badge/pruebas-51%20QA-success)](docs/04-plan-de-pruebas.md)

Software para gestionar las transacciones comerciales de una tienda de propósito
general: usuarios, clientes, proveedores, productos, ventas y reportes.

Proyecto del Ciclo 3 — Desarrollo de Software, implementado conforme al documento
*"Proyecto de Software para Gestionar Transacciones Comerciales de una Tienda Genérica"*
(Ing. Ricardo D. Camargo L., versión 4).

## Arquitectura

Dos aplicaciones desplegables por separado, comunicadas por HTTP/REST:

```
  Navegador  ──▶  AppFrontend (JSP + Servlets, WAR sobre Tomcat 9, :8080)
                        │  REST / JSON
                        ▼
                  AppBackend (Spring Boot, JAR ejecutable, :5000)
                        │  JDBC
                        ▼
                  MySQL 8.0.24  ·  base de datos "tiendagenerica"
```

## Stack tecnológico

Conforme a la Parte 4 del documento de especificación:

| Componente | Versión |
|---|---|
| Java | JDK 11 |
| Framework | Spring Boot 2.4.5 |
| Gestor de dependencias | Maven 3.6+ (incluido vía Maven Wrapper) |
| Servidor de aplicaciones | Apache Tomcat 9 |
| Base de datos | MySQL 8.0.24 |
| Documentación de API | Swagger UI (springfox 3.0.0) |
| Pruebas | JUnit 5 + Spring MockMvc + H2 |

## Estructura del proyecto

```
├── AppBackend/          Backend REST (Spring Boot) → appbackend.jar
├── AppFrontend/         Frontend web (JSP/Servlets) → ciclo3demo.war
├── datos/productos.csv  Archivo de muestra con los 18 productos
├── docs/                Documentación del proyecto (8 documentos)
└── docker-compose.yml   Entorno local completo
```

## Puesta en marcha

### Opción 0 — Scripts de arranque en Windows (la más simple)

Si las herramientas portables están en la carpeta `.tools/`, basta con:

```
iniciar.bat     levanta MySQL, el backend y Tomcat, y abre el navegador
detener.bat     detiene los tres servicios
```

Los scripts compilan el proyecto la primera vez si aún no existen los artefactos.
Si no hay herramientas portables, usan el `JAVA_HOME` y el MySQL del sistema.

### Opción 1 — Docker (la más rápida)

```bash
docker compose up --build
```

Levanta MySQL 8.0.24, el backend y Tomcat 9 con el frontend ya desplegado.

> **Requisito en Windows.** Docker Desktop necesita el subsistema WSL 2 con una
> distribución instalada. Si `docker compose up` no responde, ejecute en una terminal
> de PowerShell **como administrador** `wsl --install`, reinicie el equipo y vuelva a
> abrir Docker Desktop. Mientras tanto, use la Opción 2 o la Opción 3.

### Opción 2 — Ejecución manual

**1. Base de datos.** MySQL 8 en `localhost:3306` con una base `tiendagenerica`.
Ajuste usuario y contraseña mediante variables de entorno (`DB_USER`, `DB_PASSWORD`)
o edite `AppBackend/src/main/resources/application.properties`.

**2. Backend.**

```bash
cd AppBackend && ./mvnw spring-boot:run
```

**3. Frontend.** Genere el WAR y despliéguelo en Tomcat 9:

```bash
cd AppFrontend && ./mvnw clean package
```

Copie `target/ciclo3demo.war` a la carpeta `webapps` de Tomcat.

### Opción 3 — Sin MySQL (perfil `local`)

Para probar el sistema completo sin instalar MySQL, el backend puede ejecutarse con
una base de datos H2 en archivo:

```bash
cd AppBackend && ./mvnw clean package
java -jar target/appbackend.jar --spring.profiles.active=local
```

## Direcciones del sistema

| Recurso | URL |
|---|---|
| Aplicación web | http://localhost:8080/ciclo3demo/inicio.jsp |
| API del backend | http://localhost:5000 |
| Documentación de la API | http://localhost:5000/swagger-ui/ |

**Usuario inicial:** `admininicial` / `admin123456`
Se desactiva automáticamente al crear el primer usuario desde el módulo de usuarios.

## Pruebas

```bash
cd AppBackend && ./mvnw test
```

51 pruebas automatizadas que cubren los 43 casos del conjunto de pruebas QA del
documento. Cada método de prueba lleva el identificador de su caso
(`sp1QA3_creacionDeUsuarioCorrecta`, `sp4QA9_validacionDelTotalConIva`, …).
Por defecto usan H2 en modo de compatibilidad MySQL, así que no requieren MySQL.

El mismo conjunto puede ejecutarse contra una instancia real de MySQL 8:

```bash
cd AppBackend && ./mvnw test -DargLine="-Dspring.profiles.active=mysql"
```

Usa la base de datos `tiendagenerica_test`, independiente de la de trabajo.
Verificado sobre MySQL 8.0.24: 51 pruebas, 0 fallos.

## Primer recorrido por el sistema

1. Ingresar con `admininicial` / `admin123456`.
2. **Usuarios** → crear un usuario propio y volver a ingresar con él.
3. **Proveedores** → crear los proveedores con NIT 1 a 5.
4. **Clientes** → registrar al menos un cliente.
5. **Productos** → cargar `datos/productos.csv` (18 productos).
6. **Ventas** → consultar el cliente por cédula, agregar hasta 3 productos y confirmar.
7. **Reportes** → revisar los tres listados.

## Documentación

| Documento | Contenido |
|---|---|
| [01 · Especificación funcional](docs/01-especificacion-funcional.md) | Módulos y reglas de negocio |
| [02 · Equipo Scrum](docs/02-equipo-scrum.md) | Roles, responsabilidades y ceremonias |
| [03 · Sprints e historias de usuario](docs/03-sprints-y-historias-usuario.md) | Las 23 historias de usuario |
| [04 · Plan de pruebas](docs/04-plan-de-pruebas.md) | Casos QA y su prueba automatizada |
| [05 · Modelo de datos](docs/05-modelo-de-datos.md) | Modelo entidad-relación y DDL |
| [06 · API REST](docs/06-api-rest.md) | Contrato completo de la API |
| [07 · Despliegue en AWS](docs/07-despliegue-aws.md) | EC2, RDS y Elastic Beanstalk |
| [08 · Repositorios GitHub](docs/08-repositorios-github.md) | Ambientes de desarrollo y producción |

### Documento de entrega

El documento formal del proyecto, con la especificación completa, las historias de
usuario, el plan de pruebas con resultados, el modelo de datos, la especificación de
la API y los anexos con las evidencias de ejecución:

- [Tienda-Generica-Virtual-Documento-de-Entrega.pdf](docs/Tienda-Generica-Virtual-Documento-de-Entrega.pdf)
- [Tienda-Generica-Virtual-Documento-de-Entrega.docx](docs/Tienda-Generica-Virtual-Documento-de-Entrega.docx)

Y la presentación de sustentación del proyecto:

- [Tienda-Generica-Virtual-Sustentacion.pptx](docs/Tienda-Generica-Virtual-Sustentacion.pptx)

### Otros recursos

| Recurso | Contenido |
|---|---|
| [Colección de Postman](postman/) | 38 peticiones de la API organizadas por sprint |
| [Diagramas](docs/diagramas/) | Arquitectura, modelo entidad-relación, flujo de venta y diagrama de clases |
| [Evidencias](docs/evidencias/) | Capturas del sistema en ejecución |

## Decisiones de implementación

- **Contraseñas cifradas con BCrypt** y enmascaradas en las respuestas de la API.
  Es la única desviación respecto del documento, que muestra la contraseña en el
  listado de usuarios; se revierte poniendo
  `tienda.reportes.enmascarar-password=false` en `application.properties`.
- **Rutas de la API literales al documento**, incluida `/Ventas` con V mayúscula.
- **Carga de productos todo o nada**: la tabla solo se reemplaza si el archivo
  completo es válido.
- **Venta transaccional**: la cabecera y su detalle se guardan o fallan en conjunto.
- **Sin credenciales en el repositorio**: la configuración sensible se resuelve con
  variables de entorno y valores por defecto de desarrollo.
