"""Imprime un resumen de cobertura en formato Markdown a partir del CSV de JaCoCo.

Uso:  python resumen-cobertura.py <ruta-al-jacoco.csv> <nombre-del-proyecto>

La salida se redirige al resumen de la ejecucion de la integracion continua.
Si el informe no existe, el script termina sin escribir nada.
"""
import csv
import os
import sys

METRICAS = [
    ("Instrucciones", "INSTRUCTION"),
    ("Ramas", "BRANCH"),
    ("Lineas", "LINE"),
    ("Metodos", "METHOD"),
    ("Complejidad", "COMPLEXITY"),
]


def main():
    if len(sys.argv) < 3:
        print("Uso: resumen-cobertura.py <jacoco.csv> <proyecto>", file=sys.stderr)
        return 1

    ruta, proyecto = sys.argv[1], sys.argv[2]
    if not os.path.exists(ruta):
        print("No se encontro el informe de cobertura en " + ruta, file=sys.stderr)
        return 0

    with open(ruta, encoding="utf-8") as archivo:
        filas = list(csv.DictReader(archivo))

    def porcentaje(prefijo):
        cubierto = sum(int(f[prefijo + "_COVERED"]) for f in filas)
        perdido = sum(int(f[prefijo + "_MISSED"]) for f in filas)
        total = cubierto + perdido
        return (cubierto / total * 100) if total else 100.0

    print("## Cobertura de " + proyecto)
    print()
    print("| Metrica | Cobertura |")
    print("|---|---|")
    for nombre, prefijo in METRICAS:
        print("| %s | %.1f %% |" % (nombre, porcentaje(prefijo)))
    print()
    print("Clases analizadas: %d" % len(filas))
    return 0


if __name__ == "__main__":
    sys.exit(main())
