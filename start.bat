@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

:: ─── 1. Copy .env if missing ──────────────────────────────
if not exist ".env" (
    if exist ".env.example" (
        copy ".env.example" ".env" >nul
        echo .env criado a partir de .env.example
    ) else (
        echo .env.example nao encontrado. Crie um .env manualmente.
        exit /b 1
    )
) else (
    echo .env ja existe
)

:: ─── 2. Start Docker Compose ──────────────────────────────
echo Subindo dependencias (MySQL, Redis, RabbitMQ)...
docker compose up -d
if %ERRORLEVEL% neq 0 (
    echo Falha ao iniciar Docker Compose
    exit /b 1
)

:: ─── 3. Wait for services ─────────────────────────────────
echo Aguardando servicos ficarem saudaveis...

call :wait_for_container uolcompass-mysql
if %ERRORLEVEL% neq 0 exit /b 1

call :wait_for_container uolcompass-redis
if %ERRORLEVEL% neq 0 exit /b 1

call :wait_for_container uolcompass-rabbitmq
if %ERRORLEVEL% neq 0 exit /b 1

:: ─── 4. Run application ───────────────────────────────────
echo Iniciando a aplicacao Spring Boot...
call mvnw.cmd spring-boot:run
exit /b %ERRORLEVEL%

:: ─── Subroutine: wait_for_container ───────────────────────
:wait_for_container
set CONTAINER=%~1
set MAX_RETRIES=30
set RETRY_INTERVAL=5
set RETRY=0

:wait_loop
for /f "delims=" %%S in ('docker inspect --format={{.State.Status}} %CONTAINER% 2^>nul') do set STATUS=%%S
for /f "delims=" %%H in ('docker inspect --format={{.State.Health.Status}} %CONTAINER% 2^>nul') do set HEALTH=%%H

if "!STATUS!"=="running" (
    if "!HEALTH!"=="" (
        echo %CONTAINER% pronto
        exit /b 0
    )
    if "!HEALTH!"=="healthy" (
        echo %CONTAINER% pronto
        exit /b 0
    )
)

set /a RETRY+=1
if !RETRY! geq %MAX_RETRIES% (
    echo Servico %CONTAINER% nao ficou pronto apos %MAX_RETRIES% tentativas
    exit /b 1
)
timeout /t %RETRY_INTERVAL% >nul
goto wait_loop
