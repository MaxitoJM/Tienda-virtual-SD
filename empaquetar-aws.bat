@echo off
REM ============================================================
REM  Tienda Generica Virtual
REM  Genera los dos artefactos listos para subir a Elastic Beanstalk:
REM    despliegue\appbackend-eb.zip   -> entorno de plataforma Java
REM    despliegue\ciclo3demo.war      -> entorno de plataforma Tomcat
REM ============================================================
setlocal EnableDelayedExpansion
cd /d "%~dp0"
set "RAIZ=%CD%"
set "SALIDA=%RAIZ%\despliegue"

echo.
echo ===================================================
echo   Empaquetado de artefactos para Elastic Beanstalk
echo ===================================================
echo.

REM ---------- JDK ----------
if exist "%RAIZ%\.tools\jdk11\bin\java.exe" (
    set "JAVA_HOME=%RAIZ%\.tools\jdk11"
) else if not defined JAVA_HOME (
    echo [ERROR] No se encontro un JDK. Instale Java 11 o defina JAVA_HOME.
    pause
    exit /b 1
)
echo [1/5] JDK: !JAVA_HOME!

if exist "%RAIZ%\.tools\maven\bin\mvn.cmd" (
    set "MVN=%RAIZ%\.tools\maven\bin\mvn.cmd"
) else (
    set "MVN="
)

if not exist "%SALIDA%" mkdir "%SALIDA%"

REM ---------- Backend ----------
echo [2/5] Compilando el backend y ejecutando las pruebas...
pushd "%RAIZ%\AppBackend"
if defined MVN ( call "!MVN!" -q -B clean package ) else ( call mvnw.cmd -q -B clean package )
set "FALLO=!ERRORLEVEL!"
popd
if not !FALLO!==0 (
    echo [ERROR] La compilacion del backend fallo. No se genera ningun artefacto.
    pause
    exit /b 1
)
if not exist "%RAIZ%\AppBackend\target\appbackend.jar" (
    echo [ERROR] No se encontro appbackend.jar.
    pause
    exit /b 1
)

echo [3/5] Empaquetando appbackend-eb.zip...
set "STAGE=%TEMP%\tg-backend-eb"
if exist "%STAGE%" rmdir /s /q "%STAGE%"
mkdir "%STAGE%"
copy /Y "%RAIZ%\AppBackend\target\appbackend.jar" "%STAGE%\" >nul
xcopy /E /I /Y /Q "%RAIZ%\AppBackend\.ebextensions" "%STAGE%\.ebextensions" >nul
if exist "%SALIDA%\appbackend-eb.zip" del /q "%SALIDA%\appbackend-eb.zip"
powershell -NoProfile -Command "Compress-Archive -Path '%STAGE%\*' -DestinationPath '%SALIDA%\appbackend-eb.zip' -Force"
rmdir /s /q "%STAGE%"

REM ---------- Frontend ----------
echo [4/5] Compilando el frontend...
pushd "%RAIZ%\AppFrontend"
if defined MVN ( call "!MVN!" -q -B clean package ) else ( call mvnw.cmd -q -B clean package )
set "FALLO=!ERRORLEVEL!"
popd
if not !FALLO!==0 (
    echo [ERROR] La compilacion del frontend fallo.
    pause
    exit /b 1
)

echo [5/5] Incorporando la configuracion al archivo WAR...
copy /Y "%RAIZ%\AppFrontend\target\ciclo3demo.war" "%SALIDA%\ciclo3demo.war" >nul
powershell -NoProfile -Command ^
  "Add-Type -AssemblyName System.IO.Compression.FileSystem;" ^
  "$war = [System.IO.Compression.ZipFile]::Open('%SALIDA%\ciclo3demo.war','Update');" ^
  "Get-ChildItem '%RAIZ%\AppFrontend\.ebextensions' -File | ForEach-Object {" ^
  "  [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($war, $_.FullName, '.ebextensions/' + $_.Name) | Out-Null };" ^
  "$war.Dispose()"

echo.
echo ===================================================
echo   ARTEFACTOS GENERADOS
echo ===================================================
dir /b "%SALIDA%"
echo.
echo   Suba appbackend-eb.zip al entorno de plataforma Java.
echo   Suba ciclo3demo.war    al entorno de plataforma Tomcat.
echo.
echo   AVISO: este es el camino historico por Elastic Beanstalk.
echo   El despliegue vigente usa Terraform y S3: vea despliegue\LEEME.md
echo   Las propiedades de cada entorno estan en docs\07-despliegue-aws.md
echo ===================================================
echo.
pause
