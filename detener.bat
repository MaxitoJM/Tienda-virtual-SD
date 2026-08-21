@echo off
REM ============================================================
REM  Tienda Generica Virtual - detencion del sistema completo
REM ============================================================
setlocal
cd /d "%~dp0"

echo.
echo Deteniendo el sistema...
echo.

REM ---------- Tomcat ----------
if exist "%CD%\.tools\tomcat9\bin\shutdown.bat" (
    set "CATALINA_HOME=%CD%\.tools\tomcat9"
    call "%CD%\.tools\tomcat9\bin\shutdown.bat" >nul 2>&1
    echo [1/3] Tomcat detenido.
) else (
    echo [1/3] No hay Tomcat portable que detener.
)

REM ---------- Backend ----------
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { $_.CommandLine -like '*appbackend.jar*' -or $_.CommandLine -like '*catalina*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }" >nul 2>&1
echo [2/3] Backend detenido.

REM ---------- MySQL ----------
if exist "%CD%\.tools\mysql8\bin\mysqladmin.exe" (
    "%CD%\.tools\mysql8\bin\mysqladmin.exe" -u root -padmin123 --host=127.0.0.1 --port=3306 shutdown >nul 2>&1
    echo [3/3] MySQL detenido.
) else (
    echo [3/3] MySQL del sistema: no se detiene automaticamente.
)

echo.
echo Sistema detenido.
echo.

