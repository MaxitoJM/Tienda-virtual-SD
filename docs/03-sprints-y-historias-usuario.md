# 3. Organización de los Sprints e Historias de Usuario

## Sprint 1 — Módulo de Login y Módulo de Gestión de Usuarios

| ID | Rol | Característica / Funcionalidad | Razón / Resultado |
|---|---|---|---|
| HU-001 | Administrador | Ingresar al sistema con el usuario inicial `admininicial` y la contraseña `admin123456`. | Validar el funcionamiento del login, la interfaz gráfica y que el usuario inicial opere correctamente. |
| HU-002 | Administrador | Crear nuevos usuarios que entrarán al sistema. | Cargar los usuarios que harán las labores de ingreso de datos para los demás módulos. |
| HU-003 | Administrador | Consultar los datos del usuario por medio de la cédula. | Poder recuperar los datos de un usuario creado. |
| HU-004 | Administrador | Actualizar los datos de los usuarios del sistema. | Actualizar nombre completo, correo, usuario y contraseña, previa consulta por cédula. |
| HU-005 | Administrador | Borrar los datos de usuarios del sistema. | Borrar los usuarios que ya no deben tener ingreso, previa consulta por cédula. |

## Sprint 2 — Módulo de Gestión de Clientes y de Proveedores

| ID | Rol | Característica / Funcionalidad | Razón / Resultado |
|---|---|---|---|
| HU-006 | Administrador | Crear nuevos clientes. | Registrar a los clientes que realicen compras en la tienda. |
| HU-007 | Administrador | Consultar los datos del cliente por medio de la cédula. | Recuperar los datos de un cliente creado. |
| HU-008 | Administrador | Actualizar los datos de los clientes. | Actualizar nombre, dirección, teléfono y correo, previa consulta por cédula. |
| HU-009 | Administrador | Borrar los datos de clientes. | Retirar del sistema los clientes que ya no aplican, previa consulta por cédula. |
| HU-010 | Administrador | Crear nuevos proveedores. | Cargar los proveedores de la tienda. |
| HU-011 | Administrador | Consultar los datos del proveedor por medio del NIT. | Recuperar los datos de un proveedor creado. |
| HU-012 | Administrador | Actualizar los datos de los proveedores. | Actualizar nombre, dirección, teléfono y ciudad, previa consulta por NIT. |
| HU-013 | Administrador | Borrar los datos de un proveedor. | Retirar del sistema los proveedores que ya no aplican, previa consulta por NIT. |

## Sprint 3 — Módulo de Gestión de Productos

| ID | Rol | Característica / Funcionalidad | Razón / Resultado |
|---|---|---|---|
| HU-014 | Administrador | Subir al sistema un archivo de texto separado por comas (CSV) con los datos de productos, según el formato definido. | Cargar la tabla de productos reemplazando los productos previos, y poderlos utilizar para realizar ventas. |

## Sprint 4 — Módulo de Gestión de Ventas

| ID | Rol | Característica / Funcionalidad | Razón / Resultado |
|---|---|---|---|
| HU-015 | Usuario | Registrar las ventas de la tienda para un cliente. | Ingresar los datos de una nueva venta. |
| HU-016 | Usuario | Obtener los datos del cliente mediante consulta por cédula. | Cargarlos al formulario de ventas al registrar una venta. |
| HU-017 | Usuario | Obtener los datos de los productos mediante consulta por código de producto. | Cargarlos al formulario de ventas. |
| HU-018 | Usuario | Calcular el valor de venta por cada producto vendido mediante la cantidad, hasta un total de tres (3) productos. | Acumular el total de venta de los productos. |
| HU-019 | Usuario | Generar el valor de la venta total incluyendo IVA y total con IVA. | Presentar al cliente los valores totales de venta e IVA. |
| HU-020 | Usuario | Confirmar el valor de venta dada la aceptación del cliente. | Registrar la venta en la base de datos de la tienda. |

## Sprint 5 — Módulo de Consultas y Reportes

| ID | Rol | Característica / Funcionalidad | Razón / Resultado |
|---|---|---|---|
| HU-021 | Usuario | Consultar el listado de usuarios del sistema. | Verificar quiénes son los usuarios registrados. |
| HU-022 | Usuario | Consultar el listado de clientes. | Verificar cuáles clientes han comprado en la tienda. |
| HU-023 | Usuario | Consultar el total de ventas por cliente. | Contabilizar las ventas por cliente. |
