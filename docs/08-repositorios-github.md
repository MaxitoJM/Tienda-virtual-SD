# 8. Repositorios GitHub: ambientes de desarrollo y producción

El documento establece **dos repositorios**, uno por cada aplicación desplegable:

```
https://github.com/<NombreDelEquipo>/AppBackend
https://github.com/<NombreDelEquipo>/AppFrontend
```

El nombre de usuario de GitHub debe ser el nombre del equipo del proyecto.

## Opción A — Dos repositorios independientes (la del documento)

```bash
# Backend
cd AppBackend
git init
git add .
git commit -m "Sprint 1: modulo de login y gestion de usuarios"
git branch -M main
git remote add origin https://github.com/<NombreDelEquipo>/AppBackend.git
git push -u origin main
```

```bash
# Frontend
cd AppFrontend
git init
git add .
git commit -m "Sprint 1: interfaz de login y gestion de usuarios"
git branch -M main
git remote add origin https://github.com/<NombreDelEquipo>/AppFrontend.git
git push -u origin main
```

## Opción B — Repositorio único (el usado en esta entrega)

Este proyecto se entrega como un repositorio que contiene ambas aplicaciones, lo que
facilita la revisión conjunta y mantiene la trazabilidad de los sprints en un solo
historial. Para publicarlo:

```bash
git remote add origin https://github.com/<NombreDelEquipo>/TiendaGenerica.git
git push -u origin main --tags
```

Si el formador exige los dos repositorios separados, cada carpeta (`AppBackend/` y
`AppFrontend/`) es un proyecto Maven autónomo y puede publicarse tal cual con la Opción A.

## Ambientes de desarrollo y producción

Los ambientes se manejan mediante ramas:

| Rama | Ambiente | Uso |
|---|---|---|
| `develop` | Desarrollo | Integración del trabajo del sprint en curso |
| `main` | Producción | Solo código validado por QA y aceptado por el Product Owner |

Flujo de trabajo por historia de usuario:

```bash
git checkout develop
git checkout -b feature/HU-014-carga-productos
# ... desarrollo ...
git add .
git commit -m "HU-014: carga de productos desde archivo CSV"
git checkout develop
git merge feature/HU-014-carga-productos
```

Al cerrar un sprint, `develop` se fusiona en `main` y se etiqueta:

```bash
git checkout main
git merge develop
git tag -a sprint-3 -m "Sprint 3: modulo de gestion de productos"
git push origin main --tags
```

## Etiquetas de este proyecto

| Etiqueta | Contenido |
|---|---|
| `sprint-1` | Login y Gestión de Usuarios |
| `sprint-2` | Gestión de Clientes y Proveedores |
| `sprint-3` | Gestión de Productos (carga CSV) |
| `sprint-4` | Gestión de Ventas |
| `sprint-5` | Consultas y Reportes |

## Integración con Eclipse

1. *Window → Show View → Other → Git → Git Repositories*.
2. *Clone a Git repository* y pegar la URL del repositorio.
3. Autenticarse con el usuario de GitHub y un **token de acceso personal**
   (GitHub ya no acepta la contraseña de la cuenta para operaciones Git).
4. *Import existing Maven projects* sobre las carpetas `AppBackend` y `AppFrontend`.

## Archivos excluidos del control de versiones

`.gitignore` excluye `target/`, los artefactos `*.jar` y `*.war`, los archivos de
configuración de IDE y la carpeta `.tools/` con las herramientas locales (JDK y Maven
portables). Nunca se versionan credenciales: la configuración sensible se resuelve
con variables de entorno.
