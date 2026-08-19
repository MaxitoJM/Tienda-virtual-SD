# 6. Especificación de la API REST

La API conecta la aplicación de frontend con el backend. Documentación interactiva
(Swagger UI) disponible en:

```
http://localhost:5000/swagger-ui/
```

Todas las peticiones y respuestas usan `application/json` con nombres de campo en
`snake_case`, idénticos a los del modelo entidad-relación.

## Convención de operaciones

Cada recurso expone el mismo conjunto de rutas:

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/{recurso}/listar` | Lista todos los registros |
| `GET` | `/{recurso}/consultar/{id}` | Consulta un registro por su identificador |
| `POST` | `/{recurso}/guardar` | Crea un registro (201 Created) |
| `PUT` | `/{recurso}/actualizar` | Actualiza un registro existente |
| `DELETE` | `/{recurso}/eliminar/{id}` | Elimina un registro |

Recursos disponibles: `/usuarios`, `/clientes`, `/proveedores`, `/productos`,
`/Ventas`, `/detalleventas`.

> La ruta de ventas conserva la **V mayúscula** (`/Ventas`) tal como aparece en la
> especificación de la API del documento del proyecto.

## Cuerpos JSON por recurso

**`/usuarios`**
```json
{ "cedula_usuario": 0, "email_usuario": "string", "nombre_usuario": "string",
  "password": "string", "usuario": "string" }
```

**`/clientes`**
```json
{ "cedula_cliente": 0, "direccion_cliente": "string", "email_cliente": "string",
  "nombre_cliente": "string", "telefono_cliente": "string" }
```

**`/proveedores`**
```json
{ "ciudad_proveedor": "string", "direccion_proveedor": "string", "nitproveedor": 0,
  "nombre_proveedor": "string", "telefono_proveedor": "string" }
```

**`/productos`**
```json
{ "codigo_producto": 0, "ivacompra": 0, "nitproveedor": 0,
  "nombre_producto": "string", "precio_compra": 0, "precio_venta": 0 }
```

**`/Ventas`**
```json
{ "cedula_cliente": 0, "cedula_usuario": 0, "codigo_venta": 0,
  "ivaventa": 0, "total_venta": 0, "valor_venta": 0 }
```

**`/detalleventas`**
```json
{ "cantidad_producto": 0, "codigo_detalle_venta": 0, "codigo_producto": 0,
  "codigo_venta": 0, "valor_total": 0, "valor_venta": 0, "valoriva": 0 }
```

## Operaciones específicas de cada módulo

### Login — `POST /usuarios/login`
```json
{ "usuario": "admininicial", "password": "admin123456" }
```
Respuesta `200 OK`:
```json
{ "autenticado": true, "mensaje": "Ingreso correcto al sistema",
  "cedula_usuario": 1, "nombre_usuario": "Administrador Inicial", "usuario": "admininicial" }
```
Respuesta `401 Unauthorized`:
```json
{ "autenticado": false, "mensaje": "Usuario y/o contrasena errados, intente de nuevo" }
```

### Estado del usuario inicial — `GET /usuarios/usuario-inicial-activo`
```json
{ "exitoso": false, "mensaje": "El usuario inicial fue desactivado" }
```

### Carga de productos — `POST /productos/cargar`
`multipart/form-data`, campo `archivo` con el CSV.

```json
{ "exitoso": true, "mensaje": "Archivo Cargado correctamente",
  "registros_leidos": 18, "registros_cargados": 18, "errores": [] }
```
Si el archivo tiene errores de datos, `400 Bad Request` con el detalle línea por línea:
```json
{ "exitoso": false, "mensaje": "Error en los datos leidos del archivo. No se cargo ningun registro",
  "registros_leidos": 2, "registros_cargados": 0,
  "errores": ["Linea 3: el campo codigo_producto debe ser un numero entero y se recibio: abc"] }
```

### Registro de una venta — `POST /Ventas/registrar`
```json
{ "cedula_cliente": 52123456, "cedula_usuario": 1020304050,
  "productos": [ { "codigo_producto": 1, "cantidad_producto": 2 },
                 { "codigo_producto": 10, "cantidad_producto": 3 },
                 { "codigo_producto": 15, "cantidad_producto": 1 } ] }
```
Respuesta `201 Created`:
```json
{ "codigo_venta": 1, "cedula_cliente": 52123456, "nombre_cliente": "Maria Lopez Diaz",
  "valor_venta": 153989.0, "ivaventa": 29257.91, "total_venta": 183246.91,
  "detalles": [ { "codigo_detalle_venta": 1, "codigo_venta": 1, "codigo_producto": 1,
                  "cantidad_producto": 2, "valor_venta": 30351.0,
                  "valoriva": 11533.38, "valor_total": 60702.0 } ],
  "mensaje": "Venta registrada correctamente con el consecutivo 1" }
```
Registra la cabecera y el detalle dentro de una única transacción.

### Detalle por venta — `GET /detalleventas/venta/{codigoVenta}`
Devuelve las líneas de detalle de una venta concreta.

### Reportes
| Método | Ruta | Historia |
|---|---|---|
| `GET` | `/reportes/usuarios` | HU-021 |
| `GET` | `/reportes/clientes` | HU-022 |
| `GET` | `/reportes/ventasporcliente` | HU-023 |

`GET /reportes/ventasporcliente`:
```json
{ "clientes": [ { "cedula_cliente": 52123456, "nombre_cliente": "Maria Lopez Diaz",
                  "valor_total_ventas": 183246.91 } ],
  "total_general_ventas": 183246.91, "mensaje": null }
```

## Códigos de estado

| Código | Significado |
|---|---|
| `200 OK` | Operación realizada |
| `201 Created` | Registro creado |
| `400 Bad Request` | Datos incompletos o inválidos |
| `401 Unauthorized` | Credenciales incorrectas |
| `404 Not Found` | El registro consultado no existe |
| `500 Internal Server Error` | Error interno del sistema |

## Formato de error

Todos los errores comparten la misma estructura:

```json
{ "exitoso": false, "mensaje": "La cedula 999 no se encuentra registrada en la base de datos" }
```

## Nota sobre las contraseñas

Las contraseñas se almacenan cifradas con **BCrypt** y se devuelven **enmascaradas**
(`********`) en todas las respuestas de la API. Para reproducir literalmente el
pantallazo del documento, en el que el listado de usuarios muestra la contraseña,
cambie en `application.properties`:

```properties
tienda.reportes.enmascarar-password=false
```

No se recomienda hacerlo fuera de un entorno de demostración.
