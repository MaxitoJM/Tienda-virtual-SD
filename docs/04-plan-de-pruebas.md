# 4. Plan de Pruebas

Cada caso de prueba del documento de especificación está automatizado como un método
de prueba JUnit 5. El nombre del método contiene el identificador del caso, de manera
que el conjunto de pruebas QA se autoverifica en cada compilación.

## Ejecución

### Contra H2 (por defecto)

```bash
cd AppBackend && ./mvnw test
```

Usa **H2 en modo de compatibilidad MySQL**, por lo que no requiere una instancia de
MySQL en ejecución. Resultado esperado: **51 pruebas, 0 fallos**.

### Contra MySQL 8 real

```bash
cd AppBackend && ./mvnw test -DargLine="-Dspring.profiles.active=mysql"
```

Ejecuta el mismo conjunto de pruebas contra una instancia real de MySQL 8, usando la
base de datos `tiendagenerica_test` (se crea y se destruye en cada ejecución, de modo
que no toca los datos de `tiendagenerica`). La configuración está en
`src/test/resources/application-mysql.properties` y admite las variables de entorno
`DB_HOST`, `DB_PORT`, `DB_USER` y `DB_PASSWORD`.

Resultado verificado sobre **MySQL 8.0.24**: **51 pruebas, 0 fallos**.

## Sprint 1 — Login y Usuarios

| ID | Caso de prueba | Prueba automatizada | Resultado |
|---|---|---|---|
| SP1-QA-1 | Ingreso correcto | `sp1QA1_ingresoCorrectoConUsuarioInicial`, `sp1QA1_ingresoCorrectoConUsuarioCreado` | ✅ |
| SP1-QA-2 | Ingreso incorrecto por usuario y/o contraseña | `sp1QA2_ingresoIncorrectoPorContrasenaErrada`, `sp1QA2_ingresoIncorrectoPorUsuarioInexistente`, `sp1QA2_ingresoIncorrectoPorDatosEnBlanco` | ✅ |
| SP1-QA-3 | Creación de usuario correcta | `sp1QA3_creacionDeUsuarioCorrecta` | ✅ |
| SP1-QA-4 | Creación de usuario con datos incompletos | `sp1QA4_creacionDeUsuarioConDatosIncompletos` | ✅ |
| SP1-QA-5 | Consulta de usuario existente | `sp1QA5_consultaDeUsuarioExistente` | ✅ |
| SP1-QA-6 | Consulta de usuario inexistente | `sp1QA6_consultaDeUsuarioInexistente` | ✅ |
| SP1-QA-7 | Actualización correcta | `sp1QA7_actualizacionCorrectaDeUsuario` | ✅ |
| SP1-QA-8 | Actualización con datos en blanco | `sp1QA8_actualizacionDeUsuarioConDatosEnBlanco` | ✅ |
| SP1-QA-9 | Borrado correcto | `sp1QA9_borradoCorrectoDeUsuario` | ✅ |
| SP1-QA-10 | Borrado con cédula alterada/inexistente | `sp1QA10_borradoDeUsuarioInexistente` | ✅ |
| — | Regla funcional: desactivación de `admininicial` | `usuarioInicialSeDesactivaAlCrearOtrosUsuarios` | ✅ |

## Sprint 2 — Clientes y Proveedores

| ID | Caso de prueba | Prueba automatizada | Resultado |
|---|---|---|---|
| SP2-QA-1 | Creación de cliente correcta | `sp2QA1_creacionDeClienteCorrecta` | ✅ |
| SP2-QA-2 | Creación de cliente con datos incompletos | `sp2QA2_creacionDeClienteConDatosIncompletos` | ✅ |
| SP2-QA-3 | Consulta de cliente existente | `sp2QA3_consultaDeClienteExistente` | ✅ |
| SP2-QA-4 | Consulta de cliente inexistente | `sp2QA4_consultaDeClienteInexistente` | ✅ |
| SP2-QA-5 | Actualización correcta de cliente | `sp2QA5_actualizacionCorrectaDeCliente` | ✅ |
| SP2-QA-6 | Actualización de cliente con datos en blanco | `sp2QA6_actualizacionDeClienteConDatosEnBlanco` | ✅ |
| SP2-QA-7 | Borrado correcto de cliente | `sp2QA7_borradoCorrectoDeCliente` | ✅ |
| SP2-QA-8 | Borrado de cliente con cédula alterada | `sp2QA8_borradoDeClienteInexistente` | ✅ |
| SP2-QA-9 | Creación de proveedor correcta | `sp2QA9_creacionDeProveedorCorrecta` | ✅ |
| SP2-QA-10 | Creación de proveedor con datos incompletos | `sp2QA10_creacionDeProveedorConDatosIncompletos` | ✅ |
| SP2-QA-11 | Consulta de proveedor existente | `sp2QA11_consultaDeProveedorExistente` | ✅ |
| SP2-QA-12 | Consulta de proveedor inexistente | `sp2QA12_consultaDeProveedorInexistente` | ✅ |
| SP2-QA-13 | Actualización correcta de proveedor | `sp2QA13_actualizacionCorrectaDeProveedor` | ✅ |
| SP2-QA-14 | Actualización de proveedor con datos en blanco | `sp2QA14_actualizacionDeProveedorConDatosEnBlanco` | ✅ |
| SP2-QA-15 | Borrado correcto de proveedor | `sp2QA15_borradoCorrectoDeProveedor` | ✅ |
| SP2-QA-16 | Borrado de proveedor con NIT alterado | `sp2QA16_borradoDeProveedorInexistente` | ✅ |

## Sprint 3 — Carga de Productos

| ID | Caso de prueba | Prueba automatizada | Resultado |
|---|---|---|---|
| SP3-QA-1 | Carga exitosa del archivo | `sp3QA1_cargaExitosaDelArchivo` | ✅ |
| SP3-QA-2 | Carga fallida por falta de nombre de archivo | `sp3QA2_cargaFallidaSinArchivo` | ✅ |
| SP3-QA-3 | Carga fallida por errores de formato | `sp3QA3_cargaFallidaPorFormatoInvalido` | ✅ |
| SP3-QA-4 | Carga fallida por errores de validación de datos | `sp3QA4_cargaFallidaPorTiposDeDatoInvalidos`, `sp3QA4_cargaFallidaPorNitDeProveedorInexistente` | ✅ |

## Sprint 4 — Ventas

| ID | Caso de prueba | Prueba automatizada | Resultado |
|---|---|---|---|
| SP4-QA-1 | Consulta exitosa de la cédula del cliente | `sp4QA1_consultaExitosaDeCliente` | ✅ |
| SP4-QA-2 | Consulta fallida de la cédula del cliente | `sp4QA2_consultaFallidaDeCliente` | ✅ |
| SP4-QA-3 | Consulta exitosa de producto | `sp4QA3_consultaExitosaDeProducto` | ✅ |
| SP4-QA-4 | Consulta fallida de producto | `sp4QA4_consultaFallidaDeProducto` | ✅ |
| SP4-QA-5 | Validación del campo cantidad | `sp4QA5_validacionDeCantidadIncorrecta` | ✅ |
| SP4-QA-6 | Validación del valor total por producto | `sp4QA6_validacionDelValorTotalPorProducto` | ✅ |
| SP4-QA-7 | Validación del campo Total Venta | `sp4QA7_validacionDelTotalVenta` | ✅ |
| SP4-QA-8 | Validación del campo Total IVA | `sp4QA8_validacionDelTotalIva` | ✅ |
| SP4-QA-9 | Validación del campo Total con IVA | `sp4QA9_validacionDelTotalConIva` | ✅ |
| SP4-QA-10 | Generación del consecutivo de venta | `sp4QA10_generacionDelConsecutivoYDetalleDeVenta` | ✅ |
| — | Regla funcional: máximo tres productos | `ventaConMasDeTresProductosEsRechazada` | ✅ |

## Sprint 5 — Consultas y Reportes

| ID | Caso de prueba | Prueba automatizada | Resultado |
|---|---|---|---|
| SP5-QA-1 | Listado de usuarios | `sp5QA1_listadoDeUsuarios` | ✅ |
| SP5-QA-2 | Listado de clientes | `sp5QA2_listadoDeClientes`, `sp5QA2_listadoDeClientesVacio` | ✅ |
| SP5-QA-3 | Total de ventas por cliente con consolidado | `sp5QA3_totalDeVentasPorCliente`, `sp5QA3_sinClientesGeneraMensaje` | ✅ |

## Verificación del cálculo de una venta

Escenario usado en `Sprint4VentasTest`:

| Producto | Precio venta | IVA % | Cantidad | Valor total | IVA línea |
|---|---|---|---|---|---|
| 1 | 1.000 | 19 | 2 | 2.000 | 380 |
| 2 | 2.000 | 19 | 3 | 6.000 | 1.140 |
| 3 | 500 | 5 | 4 | 2.000 | 100 |
| | | | **Totales** | **10.000** | **1.620** |

`Total Venta` = 10.000 · `Total IVA` = 1.620 · `Total con IVA` = **11.620**

## Pruebas de aceptación end-to-end

Recorrido manual verificado sobre el sistema desplegado (frontend en Tomcat 9 +
backend en el puerto 5000):

1. Ingresar en `http://localhost:8080/ciclo3demo/inicio.jsp` con `admininicial` / `admin123456`.
2. **Usuarios** → crear un usuario. Se muestra "Registro creado correctamente" y el formulario se limpia.
3. Cerrar sesión e ingresar con el usuario recién creado. Comprobar que `admininicial` ya no permite el ingreso.
4. **Proveedores** → crear los proveedores con NIT 1 a 5.
5. **Clientes** → crear al menos dos clientes.
6. **Productos** → pulsar *Cargar* sin archivo: aparece "No se selecciono archivo para cargar".
   Luego seleccionar `datos/productos.csv` y cargar: se muestran los 18 productos.
7. **Ventas** → escribir la cédula del cliente y pulsar *Consultar*: aparece su nombre.
   Escribir tres códigos de producto con sus cantidades y pulsar *Confirmar*.
   Se generan el consecutivo, los valores por producto, Total Venta, Total IVA y Total con IVA.
8. **Reportes** → verificar los tres listados; el de ventas por cliente muestra el total consolidado.
