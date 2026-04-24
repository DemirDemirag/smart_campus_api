@echo off
setlocal

set "APP_DIR=%~dp0"
set "JAR_PATH=%APP_DIR%target\smart-campus-api-1.0.0.jar"

if not exist "%JAR_PATH%" (
    echo Build artifact not found: "%JAR_PATH%"
    echo Run "mvn clean package" first.
    exit /b 1
)

java -jar "%JAR_PATH%"
