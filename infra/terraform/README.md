# Despliegue en AWS con Terraform

Infraestructura de la Tienda Genérica Virtual en la región **us-east-1**, descrita
por completo en código. Este es el camino oficial de despliegue; la ruta anterior
por Elastic Beanstalk se conserva como anexo histórico en
[`docs/07-despliegue-aws.md`](../../docs/07-despliegue-aws.md).

## Arquitectura

```
                    Internet
                        │  :8080
        ┌───────────────▼──────────────────────────┐
        │  Subred pública (AZ a)                   │
        │  ┌────────────────────────────────────┐  │
        │  │ EC2 t3.medium · Amazon Linux 2023  │  │
        │  │   tomcat.service      :8080  ─┐    │  │   ← IP elástica
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

Decisiones que conviene conocer antes de tocar nada:

- **Una sola instancia** aloja el backend y Tomcat. El frontend llama al backend
  por `localhost:5000`, de modo que ese tráfico no sale de la máquina y el puerto
  5000 no se publica en el grupo de seguridad.
- **Sin NAT Gateway.** Nada en las subredes privadas necesita salir a internet.
  Una NAT costaría más que el resto de la infraestructura junta.
- **Sin puerto 22 y sin par de claves.** La administración es por SSM Session
  Manager, que sale desde la instancia y no necesita ninguna regla de entrada.
- **Las dos subredes privadas son obligatorias** aunque RDS sea de zona única: el
  grupo de subredes de RDS exige un mínimo de dos zonas de disponibilidad.
- **Ninguna contraseña está en el código.** Se generan con `random_password` y se
  guardan cifradas en SSM Parameter Store.

## Prerrequisitos

| Herramienta | Versión mínima | Para qué |
|---|---|---|
| Terraform | 1.10 | `use_lockfile` (bloqueo nativo de S3) exige 1.10 o superior |
| AWS CLI | 2.x | Aprovisionamiento y lectura de parámetros |
| `session-manager-plugin` | cualquiera | Túnel y consola sin SSH |
| JDK 11 | 11.0.x | Compilar los artefactos con el mismo JDK que los ejecuta |

Instalación del complemento de Session Manager en macOS:

```bash
brew install --cask session-manager-plugin
```

Credenciales de AWS configuradas y apuntando a la cuenta correcta:

```bash
aws sts get-caller-identity
```

## Orden de ejecución

Los cinco pasos van en este orden. El 3 y el 4 solo hacen falta la primera vez
o cuando se recrea la base de datos.

### 1. Bucket del estado de Terraform

Es el único paso que no se hace con Terraform, por el problema del huevo y la
gallina: el backend remoto no puede almacenarse a sí mismo. Se ejecuta una vez.

```bash
cd infra/terraform
./bootstrap/crear-bucket-estado.sh us-east-1
```

El guion comprueba la identidad de AWS y **se detiene si la cuenta no es la
esperada**. Crea el bucket con versionado, cifrado y acceso público bloqueado, y
al terminar imprime el contenido de `backend.hcl`. Cópielo tal cual:

```bash
cat > backend.hcl <<'FIN'
bucket = "tienda-virtual-estado-XXXXXXXX"
key    = "prod/infra.tfstate"
region = "us-east-1"
FIN
```

`backend.hcl` **no se versiona**: el nombre del bucket depende de la cuenta.

### 2. Infraestructura

```bash
terraform init -backend-config=backend.hcl
terraform plan -out=plan.tfplan
terraform apply plan.tfplan
```

Tarda entre 8 y 12 minutos; casi todo es la creación de RDS. Al terminar:

```bash
terraform output
```

De las salidas se usan `ip_publica`, `bucket_artefactos`, `id_instancia` y
`comando_tunel_ssm`.

> En el primer arranque, `appbackend.service` y `tomcat.service` entran en bucle
> de reintento porque el bucket de artefactos todavía está vacío. Es el
> comportamiento previsto: descargan su artefacto en cada intento y arrancan
> solos en cuanto existan, sin necesidad de tocar la instancia.

### 3. Túnel de SSM hacia RDS

RDS no es accesible desde internet, así que el módulo `db-bootstrap` se ejecuta
a través de un reenvío de puerto contra la instancia. **Deje esta terminal
abierta** mientras dure el paso 4.

```bash
aws ssm start-session --region us-east-1 --target <ID_DE_LA_INSTANCIA> \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters '{"host":["<PUNTO_DE_ENLACE_DE_RDS>"],"portNumber":["3306"],"localPortNumber":["3306"]}'
```

El comando exacto, con los valores ya sustituidos, lo imprime:

```bash
terraform output -raw comando_tunel_ssm
```

Debe responder `Port 3306 opened for sessionId ... Waiting for connections...`

### 4. Base de datos y usuario de la aplicación

En **otra terminal**, con el túnel abierto:

```bash
cd infra/terraform/db-bootstrap
cat > backend.hcl <<'FIN'
bucket = "tienda-virtual-estado-XXXXXXXX"
key    = "prod/db-bootstrap.tfstate"
region = "us-east-1"
FIN
terraform init -backend-config=backend.hcl
terraform apply
```

Crea la base `tiendagenerica` y el usuario `tienda_app` con
`caching_sha2_password` y privilegios acotados a esa única base. El módulo lee
las credenciales del usuario maestro directamente de SSM: no hay que exportar
ninguna contraseña al entorno del intérprete de órdenes.

Para comprobarlo:

```sql
SELECT user, host, plugin FROM mysql.user WHERE user = 'tienda_app';
SHOW GRANTS FOR 'tienda_app'@'10.20.%';
```

### 5. Compilación y subida de los artefactos

Compile con **JDK 11**, el mismo que ejecuta las aplicaciones en la instancia:

```bash
cd AppBackend   && ./mvnw clean package
cd ../AppFrontend && ./mvnw clean package
```

```bash
BUCKET=$(cd infra/terraform && terraform output -raw bucket_artefactos)
aws s3 cp AppBackend/target/appbackend.jar   "s3://$BUCKET/appbackend.jar"
aws s3 cp AppFrontend/target/ciclo3demo.war  "s3://$BUCKET/ciclo3demo.war"
```

Los servicios los recogen en el siguiente intento de arranque, como máximo 15
segundos después. Para forzarlo:

```bash
aws ssm send-command --region us-east-1 --instance-ids <ID_DE_LA_INSTANCIA> \
  --document-name AWS-RunShellScript \
  --parameters 'commands=["systemctl restart appbackend.service tomcat.service"]'
```

La aplicación queda en:

```
http://<IP_ELASTICA>:8080/ciclo3demo/inicio.jsp
```

Se ingresa con `admininicial` / `admin123456`.

## Operación

### Consola en la instancia, sin SSH

```bash
aws ssm start-session --region us-east-1 --target <ID_DE_LA_INSTANCIA>
```

### Registros

```bash
sudo journalctl -u appbackend.service -n 100 --no-pager
sudo journalctl -u tomcat.service     -n 100 --no-pager
sudo cat /var/log/aprovisionamiento.log     # salida de user_data
```

### Conectarse a la base de datos desde el puesto de trabajo

Con el túnel del paso 3 abierto:

```bash
MYSQL_PWD=$(aws ssm get-parameter --region us-east-1 \
  --name /tienda-virtual/prod/db/master-password \
  --with-decryption --query Parameter.Value --output text) \
mysql -h 127.0.0.1 -P 3306 -u admin tiendagenerica
```

Desde un cliente gráfico como DBeaver: conexión MySQL corriente a
`127.0.0.1:3306`, **con la pestaña SSH desactivada** (el túnel ya está hecho por
fuera) y con la propiedad de controlador `sslMode` puesta en `PREFERRED`. Sin
ese ajuste, la conexión sin cifrar puede fallar con *Public Key Retrieval is not
allowed* en cuanto RDS vacíe su caché de credenciales.

### Ver la configuración publicada

```bash
aws ssm get-parameters-by-path --region us-east-1 \
  --path /tienda-virtual/prod --recursive \
  --query 'Parameters[].{Nombre:Name,Tipo:Type}' --output table
```

### Rotar la contraseña del usuario maestro

```bash
cd infra/terraform
terraform taint random_password.maestro_bd
terraform apply
```

El nuevo valor llega a SSM en el mismo `apply`, y a la instancia al reiniciarla.

### Cambiar el aprovisionamiento

`user_data_replace_on_change = true`: modificar la plantilla **sustituye la
instancia**. Es seguro, porque no guarda estado —los datos están en RDS y los
artefactos en S3— y la IP elástica se vuelve a asociar sola. Implica unos
minutos de corte.

## Apagar sin destruir

Para dejar de pagar las horas de cómputo conservando los datos:

```bash
aws ec2 stop-instances  --region us-east-1 --instance-ids <ID_DE_LA_INSTANCIA>
aws rds stop-db-instance --region us-east-1 --db-instance-identifier tienda-virtual-prod-mysql
```

> **RDS solo permanece parada 7 días.** Pasado ese plazo AWS la arranca
> automáticamente. Para una pausa más larga, destruya la infraestructura.

Al volver, **arranque RDS primero** y espere a que esté `available`; si no, el
backend entrará en su bucle de reintento hasta que la base responda.

```bash
aws rds start-db-instance --region us-east-1 --db-instance-identifier tienda-virtual-prod-mysql
aws ec2 start-instances   --region us-east-1 --instance-ids <ID_DE_LA_INSTANCIA>
```

No hay nada más que hacer: `tienda-config.service` vuelve a leer los parámetros
de SSM en cada arranque y los dos servicios se levantan solos.

Parar las instancias por fuera de Terraform **no genera deriva**: ni
`aws_instance` ni `aws_db_instance` gestionan el estado de encendido.

Aun paradas siguen costando el almacenamiento de ambas, las copias de seguridad
de RDS y la IP elástica, que AWS cobra igual con la instancia detenida.

## Destruir todo

En orden inverso. El módulo `db-bootstrap` se destruye primero y **necesita el
túnel abierto**, porque habla con MySQL:

```bash
# Terminal 1: túnel abierto
terraform -chdir=infra/terraform output -raw comando_tunel_ssm   # y ejecutarlo

# Terminal 2
cd infra/terraform/db-bootstrap && terraform destroy
cd ..                            && terraform destroy
```

Con los valores por defecto (`omitir_instantanea_final = true`) **el `destroy` no
deja instantánea de la base de datos: los datos se pierden**. Para conservarlos,
antes de destruir:

```hcl
# terraform.tfvars
omitir_instantanea_final = false
```

El bucket de artefactos tiene versionado, así que si contiene objetos habrá que
vaciarlo antes:

```bash
aws s3 rm "s3://$(terraform output -raw bucket_artefactos)" --recursive
```

El bucket del estado **no lo gestiona Terraform** y sobrevive al `destroy`. Se
elimina a mano cuando ya no haga falta:

```bash
aws s3 rb "s3://tienda-virtual-estado-XXXXXXXX" --force
```

## Archivos que no se versionan

`backend.hcl`, `*.tfvars`, `*.tfstate*`, `.terraform/`, `*.tfplan` y `apply.log`
están en `.gitignore`. **`.terraform.lock.hcl` sí se versiona**: fija las
versiones exactas de los proveedores para que todo el equipo resuelva las
mismas.

## Comprobaciones antes de subir cambios

```bash
terraform fmt -recursive
terraform validate                      # en la raíz
cd db-bootstrap && terraform validate   # y en el módulo
```
