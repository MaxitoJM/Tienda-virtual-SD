# Flujo de publicación: de la rama `prod` a la aplicación en marcha

Qué le pasa al código desde que está en `prod` hasta que responde en AWS, y qué
pieza hace cada cosa.

Este documento cubre el **ciclo de publicación**. El aprovisionamiento de la
infraestructura —crear la VPC, la instancia, RDS— está en
[`infra/terraform/README.md`](../infra/terraform/README.md), y el porqué de cada
decisión de arquitectura en
[`docs/07-despliegue-aws.md`](../docs/07-despliegue-aws.md).

## El recorrido completo

```
  rama prod  ──▶  compilación en el Mac  ──▶  bucket S3  ──▶  instancia EC2
   (GitHub)          (JDK 11 Corretto)        (versionado)      (systemd)
      │                     │                      │                 │
      │                     ├─ appbackend.jar ─────┤                 │
      │                     └─ ciclo3demo.war ─────┘                 │
      │                                                              │
      └─ la integración continua compila y prueba,                   │
         pero NO despliega: la subida es manual                      │
                                                                     ▼
                                        ┌────────────────────────────────────┐
                                        │ tienda-config.service  (al arrancar)│
                                        │   lee SSM ─▶ /etc/tienda/*.env      │
                                        ├────────────────────────────────────┤
                                        │ appbackend.service   :5000          │
                                        │   descarga el .jar de S3 y arranca  │
                                        ├────────────────────────────────────┤
                                        │ tomcat.service       :8080          │
                                        │   descarga el .war de S3 y arranca  │
                                        └───────────────┬────────────────────┘
                                                        │ JDBC · sslMode=REQUIRED
                                                        ▼
                                              RDS MySQL 8.4 (subred privada)
```

## 1. La rama `prod`

`prod` es la rama de despliegue. Salió de `main` y lleva la configuración
preparada para AWS: el conector `mysql-connector-j` 8.4.0, el perfil `aws` del
backend y la infraestructura en `infra/terraform/`.

La integración continua se ejecuta sobre `main`, `develop` y `prod`, y en cada
empujón compila ambos proyectos, ejecuta las pruebas y mide la cobertura, además
de una tanda extra del backend contra un MySQL 8.4 real.

**La integración continua no despliega.** Publica los artefactos como adjuntos
de la ejecución, pero la subida a S3 es un paso manual y deliberado: así se
decide cuándo entra una versión en el entorno desplegado.

## 2. Compilación en el Mac

Los artefactos se compilan con **JDK 11**, el mismo que los ejecuta en la
instancia (Amazon Corretto 11). Compilar con un JDK más nuevo produciría
bytecode válido —Maven usa `--release 11`— pero dejaría de coincidir el entorno
de compilación con el de ejecución.

### Conseguir el JDK 11

Dos formas. La primera necesita contraseña de administrador; la segunda no.

```bash
# a) instalación en el sistema
brew install --cask corretto@11

# b) portable dentro del proyecto, sin permisos de administrador
mkdir -p .tools && curl -fsSL -o /tmp/corretto11.tar.gz \
  https://corretto.aws/downloads/latest/amazon-corretto-11-aarch64-macos-jdk.tar.gz
mkdir -p .tools/jdk11 && tar -xzf /tmp/corretto11.tar.gz -C .tools/jdk11 --strip-components=1
```

> En un Mac con procesador Intel, cambie `aarch64` por `x64` en la dirección.

`.tools/` está en `.gitignore`, así que el JDK portable no entra al repositorio.

### Compilar

```bash
export JAVA_HOME="$PWD/.tools/jdk11/Contents/Home"   # omitir si se instaló con brew
export PATH="$JAVA_HOME/bin:$PATH"
java -version                                        # debe decir 11

cd AppBackend    && ./mvnw clean package
cd ../AppFrontend && ./mvnw clean package
```

Resultado:

| Artefacto | Ruta | Qué es |
|---|---|---|
| `appbackend.jar` | `AppBackend/target/` | JAR ejecutable de Spring Boot, con Tomcat incrustado y el conector MySQL dentro |
| `ciclo3demo.war` | `AppFrontend/target/` | Aplicación web JSP/Servlets, para desplegar en Tomcat |

`clean package` ya ejecuta las pruebas. Para exigir además los umbrales de
cobertura, use `clean verify`.

## 3. Subida al bucket de S3

```bash
BUCKET=$(cd infra/terraform && terraform output -raw bucket_artefactos)

aws s3 cp AppBackend/target/appbackend.jar   "s3://$BUCKET/appbackend.jar"
aws s3 cp AppFrontend/target/ciclo3demo.war  "s3://$BUCKET/ciclo3demo.war"
```

El bucket es privado, está cifrado y **tiene versionado**: cada subida conserva
la anterior. Volver atrás no exige recompilar, basta con restaurar la versión
previa del objeto y reiniciar el servicio.

Los nombres de los dos objetos son fijos. La instancia siempre busca
`appbackend.jar` y `ciclo3demo.war` en la raíz del bucket.

## 4. Cómo llegan los artefactos a la instancia

**No se copian a la instancia: la instancia los descarga.** Cada servicio lleva
un `ExecStartPre` que trae su artefacto de S3 antes de arrancar, autenticándose
con el rol de la instancia (sin claves de acceso).

Esto tiene una consecuencia útil: si el artefacto todavía no existe, el servicio
falla y systemd lo reintenta cada 15 segundos. **En cuanto se sube, arranca
solo.** Así funciona el primer despliegue sobre una infraestructura recién
creada, sin tocar la máquina.

Para aplicar una versión nueva de inmediato en lugar de esperar:

```bash
aws ssm send-command --region us-east-1 --instance-ids <ID_DE_LA_INSTANCIA> \
  --document-name AWS-RunShellScript \
  --parameters 'commands=["systemctl restart appbackend.service tomcat.service"]'
```

## 5. De dónde sale la configuración

Ninguna configuración viaja dentro de los artefactos: **el mismo `.jar` sirve
para cualquier entorno.** Al arrancar la instancia, `tienda-config.service` lee
los parámetros de SSM Parameter Store y escribe dos archivos que systemd carga
como variables de entorno.

| Unidad | Archivo | Contenido |
|---|---|---|
| `appbackend.service` | `/etc/tienda/backend.env` | `SPRING_PROFILES_ACTIVE=aws`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT` |
| `tomcat.service` | `/etc/tienda/tomcat.env` | `BACKEND_URL`, `CATALINA_OPTS` |

Las contraseñas están en SSM como `SecureString` y se descifran al arrancar. No
hay credenciales en el repositorio ni en los artefactos.

Cambiar configuración es cambiar el parámetro y reiniciar; no hay que
recompilar ni volver a subir nada:

```bash
aws ssm put-parameter --region us-east-1 --overwrite \
  --name /tienda-virtual/prod/app/server-port --value 5000
```

## 6. Dónde acaba corriendo

| Pieza | Puerto | Alcance |
|---|---|---|
| Tomcat 9 con `ciclo3demo.war` | 8080 | Abierto a internet por la IP elástica |
| Backend Spring Boot | 5000 | **Solo `localhost`**, no está en ningún grupo de seguridad |
| RDS MySQL 8.4 | 3306 | Subredes privadas, solo desde el grupo de seguridad de la instancia |

Las dos aplicaciones comparten instancia y el frontend llama al backend por
`localhost:5000`, así que ese tráfico nunca sale de la máquina.

Cada una corre en su propia JVM con `-Xmx1g`, en unidades de systemd
independientes: reiniciar el frontend no toca la API.

La aplicación queda en:

```
http://<IP_ELÁSTICA>:8080/ciclo3demo/inicio.jsp
```

## Resumen del ciclo habitual

```bash
# 1. código en prod
git checkout prod && git pull

# 2. compilar con JDK 11
export JAVA_HOME="$PWD/.tools/jdk11/Contents/Home"
cd AppBackend && ./mvnw clean verify && cd ..
cd AppFrontend && ./mvnw clean verify && cd ..

# 3. subir
BUCKET=$(cd infra/terraform && terraform output -raw bucket_artefactos)
aws s3 cp AppBackend/target/appbackend.jar   "s3://$BUCKET/appbackend.jar"
aws s3 cp AppFrontend/target/ciclo3demo.war  "s3://$BUCKET/ciclo3demo.war"

# 4. aplicar
aws ssm send-command --region us-east-1 --instance-ids <ID_DE_LA_INSTANCIA> \
  --document-name AWS-RunShellScript \
  --parameters 'commands=["systemctl restart appbackend.service tomcat.service"]'
```

## Comprobar que salió bien

```bash
# consola en la instancia, sin SSH
aws ssm start-session --region us-east-1 --target <ID_DE_LA_INSTANCIA>

# ya dentro
systemctl is-active tienda-config.service appbackend.service tomcat.service
sudo journalctl -u appbackend.service -n 50 --no-pager
curl -s http://localhost:5000/usuarios/listar
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/ciclo3demo/inicio.jsp
```

## Si algo falla

| Síntoma | Causa habitual |
|---|---|
| El servicio reinicia en bucle nada más crear la infraestructura | El artefacto no está en el bucket todavía. Súbalo: arranca solo |
| `appbackend.service` no levanta y el registro habla de JDBC | RDS parada, o `db-bootstrap` no se ha ejecutado y no existen la base ni el usuario |
| El frontend responde pero no encuentra al backend | `BACKEND_URL` mal puesta en SSM; debe ser `http://localhost:5000`, sin barra final |
| Error 404 en la raíz del puerto 8080 | Es lo normal en Tomcat. La aplicación está en `/ciclo3demo/inicio.jsp` |
| Se subió una versión nueva y no cambia nada | Los servicios no se han reiniciado; reinícielos |
| Desde el navegador no carga, pero desde la instancia sí | Un filtro de red o cortafuegos local bloqueando el puerto 8080 |

## Sobre la carpeta `despliegue/`

Antes recogía los artefactos empaquetados para Elastic Beanstalk que generaba
`empaquetar-aws.bat`. **Ese camino ya no se usa**; se conserva documentado como
anexo histórico en
[`docs/07-despliegue-aws.md`](../docs/07-despliegue-aws.md#anexo-histórico-despliegue-manual-con-elastic-beanstalk).
Hoy los artefactos se publican en S3 y no se guarda ninguna copia aquí.
