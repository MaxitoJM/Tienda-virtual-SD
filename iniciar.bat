@echo off
REM ============================================================
REM  Tienda Generica Virtual - arranque del sistema completo
REM  Levanta MySQL, el backend (puerto 5000) y Tomcat (puerto 8080)
REM ============================================================
setlocal EnableDelayedExpansion
cd /d "%~dp0"
set "RAIZ=%CD%"

echo.
echo ===============================================
echo   TIENDA GENERICA VIRTUAL - Iniciando sistema
echo ===============================================
echo.

REM ---------- 1. Localizar el JDK ----------
if exist "%RAIZ%\.tools\jdk11\bin\java.exe" (
    set "JAVA_HOME=%RAIZ%\.tools\jdk11"
) else if defined JAVA_HOME (
    echo [i] Usando el JDK del sistema: %JAVA_HOME%
) else (
    echo [ERROR] No se encontro un JDK.
    echo         Instale Java 11 o defina la variable JAVA_HOME.
    pause
    exit /b 1
)
echo [1/4] JDK: !JAVA_HOME!

REM ---------- 2. Base de datos MySQL ----------
if exist "%RAIZ%\.tools\mysql8\bin\mysqld.exe" (
    tasklist /FI "IMAGENAME eq mysqld.exe" 2>nul | find /I "mysqld.exe" >nul
    if errorlevel 1 (
        echo [2/4] Iniciando MySQL 8.4 en el puerto 3306...
        start "MySQL" /min "%RAIZ%\.tools\mysql8\bin\mysqld.exe" --defaults-file="%RAIZ%\.tools\mysql8\my.ini"
        timeout /t 12 /nobreak >nul
    ) else (
        echo [2/4] MySQL ya se encuentra en ejecucion.
    )
) else (
    echo [2/4] No hay MySQL portable en .tools; se usara el MySQL del sistema.
)

REM ---------- 3. Backend ----------
if not exist "%RAIZ%\AppBackend\target\appbackend.jar" (
    echo       Compilando el backend, esto puede tardar unos minutos...
    pushd "%RAIZ%\AppBackend"
    if exist "%RAIZ%\.tools\maven\bin\mvn.cmd" (
        call "%RAIZ%\.tools\maven\bin\mvn.cmd" -q -B clean package -DskipTests
    ) else (
        call mvnw.cmd -q -B clean package -DskipTests
    )
    popd
)
if not exist "%RAIZ%\AppBackend\target\appbackend.jar" (
    echo [ERROR] No fue posible compilar el backend.
    pause
    exit /b 1
)
echo [3/4] Iniciando el backend en el puerto 5000...
start "Backend Tienda Generica" /min "!JAVA_HOME!\bin\java.exe" -jar "%RAIZ%\AppBackend\target\appbackend.jar"

REM ---------- 4. Frontend sobre Tomcat ----------
if not exist "%RAIZ%\AppFrontend\target\ciclo3demo.war" (
    echo       Compilando el frontend...
    pushd "%RAIZ%\AppFrontend"
    if exist "%RAIZ%\.tools\maven\bin\mvn.cmd" (
        call "%RAIZ%\.tools\maven\bin\mvn.cmd" -q -B clean package
    ) else (
        call mvnw.cmd -q -B clean package
    )
    popd
)
if exist "%RAIZ%\.tools\tomcat9\bin\startup.bat" (
    echo [4/4] Desplegando el frontend en Tomcat 9, puerto 8080...
    if exist "%RAIZ%\.tools\tomcat9\webapps\ciclo3demo" rmdir /s /q "%RAIZ%\.tools\tomcat9\webapps\ciclo3demo"
    copy /Y "%RAIZ%\AppFrontend\target\ciclo3demo.war" "%RAIZ%\.tools\tomcat9\webapps\" >nul
    set "CATALINA_HOME=%RAIZ%\.tools\tomcat9"
    start "Tomcat" /min "%RAIZ%\.tools\tomcat9\bin\startup.bat"
    timeout /t 18 /nobreak >nul
) else (
    echo [4/4] No hay Tomcat en .tools. Copie AppFrontend\target\ciclo3demo.war
    echo       a la carpeta webapps de su instalacion de Tomcat 9.
)

echo.
echo ===============================================
echo   SISTEMA EN EJECUCION
echo ===============================================
echo   Aplicacion : http://localhost:8080/ciclo3demo/inicio.jsp
echo   API        : http://localhost:5000
echo   Swagger    : http://localhost:5000/swagger-ui/
echo.
echo   Usuario inicial: admininicial / admin123456
echo.
echo   Para detener todo, ejecute detener.bat
echo ===============================================
echo.


