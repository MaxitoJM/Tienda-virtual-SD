# Colección de Postman

`TiendaGenerica.postman_collection.json` contiene las 38 peticiones de la API del
backend, organizadas en 8 carpetas que siguen el orden de los sprints del proyecto.

## Importar

1. Abrir Postman → **Import** → seleccionar el archivo `.json`.
2. En la colección, pestaña **Variables**, ajustar `base_url`:

| Entorno | Valor |
|---|---|
| Local | `http://localhost:5000` |
| AWS | `http://<entorno>backend-env.<region>.elasticbeanstalk.com` |

## Orden sugerido de ejecución

1. **01 - Login** → *Ingreso correcto* con `admininicial` / `admin123456`
2. **02 - Usuarios** → *Guardar* para crear un usuario del sistema
3. **04 - Proveedores** → *Guardar* (repetir con NIT 1 a 5)
4. **03 - Clientes** → *Guardar*
5. **05 - Productos** → *Cargar archivo CSV de productos* (seleccionar `datos/productos.csv`)
6. **06 - Ventas** → *Registrar venta completa (3 productos)*
7. **08 - Reportes** → los tres listados

> En la petición de carga del CSV, Postman exige volver a seleccionar el archivo
> desde el disco por seguridad: apunte a `datos/productos.csv` del repositorio.

## Variables disponibles

| Variable | Valor por defecto |
|---|---|
| `base_url` | `http://localhost:5000` |
| `cedula_cliente` | `52123456` |
| `cedula_usuario` | `1020304050` |
