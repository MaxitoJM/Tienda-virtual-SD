# 1. Especificación Funcional

Sistema web para gestionar las transacciones comerciales de una tienda de propósito
general que maneja proveedores, clientes, compras, ventas y productos.

## Módulos

### 1. Módulo de Login del sistema
Permite el ingreso al sistema previa validación de nombre de usuario y contraseña.
Existe un usuario por defecto para el primer ingreso:

| Usuario | Contraseña |
|---|---|
| `admininicial` | `admin123456` |

El usuario inicial se **desactiva automáticamente** en cuanto existe al menos un
usuario creado desde el módulo de gestión de usuarios.

### 2. Módulo de Gestión de Usuarios
Crear, consultar, actualizar y borrar los usuarios que operan el sistema.
Tabla `usuarios`. Datos: cédula, nombre completo, correo electrónico, usuario y contraseña.

### 3. Módulo de Gestión de Clientes
Crear, leer, actualizar y borrar clientes. Tabla `clientes`.
Datos: cédula, nombre completo, dirección, teléfono y correo electrónico.

### 4. Módulo de Gestión de Proveedores
Crear, leer, actualizar y borrar proveedores. Tabla `proveedores`.
Datos: NIT, nombre proveedor, dirección, teléfono y ciudad.

### 5. Módulo de Gestión de Productos
Los productos se cargan desde un archivo plano separado por comas (CSV) con la
siguiente estructura:

| Nombre del dato | Tipo | Longitud |
|---|---|---|
| `codigo_producto` | BIGINT | 20 |
| `nombre_producto` | VARCHAR | 50 |
| `nitproveedor` | BIGINT | 20 |
| `precio_compra` | DOUBLE | |
| `ivacompra` | DOUBLE | |
| `precio_venta` | DOUBLE | |

Validaciones aplicadas:

1. El archivo debe ser CSV (separado por comas).
2. Cada registro debe tener exactamente 6 columnas.
3. Los tipos de dato de cada columna deben ser correctos.
4. El `nitproveedor` indicado debe existir en la tabla `proveedores`.
5. No se admiten códigos de producto repetidos dentro del archivo.

El proceso es **todo o nada**: solo cuando el archivo completo es válido se borra
el contenido anterior de la tabla `productos` y se insertan los registros leídos.

### 6. Módulo de Gestión de Ventas
1. El sistema busca los datos del cliente por cédula.
2. Se escribe el código del producto y se visualiza su nombre.
3. Se digita la cantidad y se genera el valor total de venta por producto.
4. La operación se repite hasta un máximo de **tres (3) productos**.
5. Al totalizar se calcula el IVA de cada producto según su porcentaje y el total con IVA.
6. Se registra la venta en `ventas` (con código consecutivo) y su detalle en `detalle_ventas`.
7. Se muestra el mensaje de confirmación de la transacción.

Fórmulas aplicadas:

```
valor_total(línea) = cantidad_producto × precio_venta
valoriva(línea)    = valor_total(línea) × ivacompra / 100
valor_venta(venta) = Σ valor_total(línea)          -> "Total Venta"
ivaventa(venta)    = Σ valoriva(línea)             -> "Total IVA"
total_venta(venta) = valor_venta + ivaventa        -> "Total con IVA"
```

### 7. Módulo de Consultas y Reportes
Consultas por pantalla:

- a) Listado de usuarios
- b) Listado de clientes
- c) Total de ventas por cliente, con el total consolidado al final del listado

## Archivo de muestra de productos

Se entrega en [`datos/productos.csv`](../datos/productos.csv) con los 18 productos
del documento de especificación.
