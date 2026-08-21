# Despliegue en AWS Elastic Beanstalk

Esta carpeta recibe los dos artefactos listos para subir. Se generan ejecutando en la
raíz del proyecto:

```
empaquetar-aws.bat
```

El guion compila los dos proyectos, **ejecuta las 51 pruebas** y solo si todo pasa
genera:

| Artefacto | Entorno de destino | Contenido |
|---|---|---|
| `appbackend-eb.zip` | Plataforma **Java** | `appbackend.jar` + `.ebextensions/` |
| `ciclo3demo.war` | Plataforma **Tomcat** | Aplicación web + `.ebextensions/` |

Los artefactos no se versionan: se regeneran cuando se necesiten.

---

## Antes de subir: propiedades del entorno

La configuración incluida en `.ebextensions` cubre puertos, tipo de instancia y
comprobación de estado. Las **credenciales no se versionan**, así que hay que
definirlas en la consola de cada entorno, en *Configuración → Software → Propiedades
del entorno*.

### Entorno de backend (plataforma Java)

| Propiedad | Valor |
|---|---|
| `DB_HOST` | Punto de enlace de la instancia de RDS |
| `DB_USER` | Usuario maestro de la base de datos |
| `DB_PASSWORD` | Contraseña del usuario maestro |

`DB_PORT`, `DB_NAME` y `SERVER_PORT` ya vienen definidos en `.ebextensions`.

### Entorno de frontend (plataforma Tomcat)

| Propiedad | Valor |
|---|---|
| `BACKEND_URL` | Dirección del entorno de backend, sin barra final |

Ejemplo: `http://equipoXpruebasbackend-env.us-east-2.elasticbeanstalk.com`

---

## Pasos del despliegue

1. **Crear la instancia RDS** con MySQL 8.0.24, plantilla de capa gratuita y base de
   datos inicial `tiendagenerica`. Copiar el punto de enlace.
2. **Grupo de seguridad de RDS:** permitir el tráfico entrante en el puerto 3306
   desde el grupo de seguridad de las instancias de Elastic Beanstalk.
3. **Crear el entorno de backend** con plataforma Java (Corretto 11). Definir las tres
   propiedades de la tabla anterior y subir `appbackend-eb.zip`.
4. **Verificar el backend** abriendo `<direccion-del-entorno>/usuarios/listar`. Debe
   devolver un documento JSON con el usuario inicial.
5. **Crear el entorno de frontend** con plataforma Tomcat. Definir `BACKEND_URL` con la
   dirección obtenida en el paso anterior y subir `ciclo3demo.war`.
6. **Abrir la aplicación** en `<direccion-del-entorno>/ciclo3demo/inicio.jsp` e ingresar
   con `admininicial` / `admin123456`.

---

## Comprobación posterior

| N.º | Comprobación | Resultado esperado |
|---|---|---|
| 1 | Estado de ambos entornos en la consola | Correcto (verde) |
| 2 | `<backend>/usuarios/listar` | Documento JSON |
| 3 | `<backend>/swagger-ui/` | Documentación de la API |
| 4 | `<frontend>/ciclo3demo/inicio.jsp` | Pantalla de ingreso |
| 5 | Crear un cliente desde la aplicación | Se guarda en la base de datos de RDS |
| 6 | Cargar `datos/productos.csv` | 18 productos cargados |

---

## Si algo falla

| Síntoma | Causa habitual |
|---|---|
| El entorno de backend queda en estado degradado | `DB_HOST` incorrecto, o el grupo de seguridad de RDS no permite la conexión |
| El frontend muestra "No fue posible comunicarse con el backend" | `BACKEND_URL` mal definida, o con una barra final sobrante |
| Error 404 en la raíz del frontend | Es el comportamiento normal de Tomcat. La aplicación está en `/ciclo3demo/inicio.jsp` |
| La comprobación de estado falla | Revisar que el backend escuche en el puerto 5000 |

Los registros de cada entorno se descargan desde la consola, en *Registros → Solicitar
registros → Últimos 100 líneas*.
