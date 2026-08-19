# 2. Organización del Equipo Scrum

El equipo se conforma por cinco (5) integrantes que aplican el marco de trabajo Scrum.

| N.º | Rol | Responsabilidad |
|---|---|---|
| 1 | Product Owner | Maneja la definición funcional del producto completo y es el punto de contacto con el cliente para validar la aceptación de cada incremento. |
| 2 | Scrum Master | Maneja, difunde y controla la ejecución correcta del marco de trabajo Scrum en todos los miembros del equipo. |
| 3 | Development Team: Arquitecto | Realiza el diseño de arquitectura del software y de la base de datos. |
| 4 | Development Team: Desarrollador | Realiza la codificación del software. |
| 5 | Development Team: Control de Calidad (QA) | Realiza las pruebas al software establecidas en el documento de especificación. |

Por la naturaleza del proyecto y el número de participantes, **todos los integrantes
tienen el rol de desarrolladores por defecto**; el equipo puede sumar más
desarrolladores conforme a los requerimientos y la ejecución del sprint.

## Ceremonias y seguimiento

- **Daily standup meeting:** reunión diaria de seguimiento convocada por el Scrum Master
  en coordinación con el formador.
- **Herramienta de seguimiento:** Trello, para el control del avance de los sprints.
- **Definición de Terminado (DoD)** aplicada en este proyecto:
  1. La historia de usuario está implementada en backend y frontend.
  2. Los casos de prueba QA del sprint están automatizados y en verde.
  3. El código está en el repositorio con su etiqueta de sprint.
  4. La documentación del sprint está actualizada.

## Trazabilidad de sprints en el repositorio

Cada sprint corresponde a una etiqueta de Git, de manera que el avance del proyecto
es verificable en el historial:

| Sprint | Etiqueta | Contenido |
|---|---|---|
| Sprint 1 | `sprint-1` | Login y Gestión de Usuarios |
| Sprint 2 | `sprint-2` | Gestión de Clientes y Proveedores |
| Sprint 3 | `sprint-3` | Gestión de Productos (carga CSV) |
| Sprint 4 | `sprint-4` | Gestión de Ventas |
| Sprint 5 | `sprint-5` | Consultas y Reportes |
