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
docker-compose up -d
if %ERRORLEVEL% neq 0 (
    echo Falha ao iniciar Docker Compose
    exit /b 1
)

:: ─── 3. Wait for services ─────────────────────────────────
echo Aguardando servicos ficarem saudaveis...
set CONTAINERS=uolcompass-mysql uolcompass-redis uolcompass-rabbitmq
set MAX_RETRIES=30
set RETRY_INTERVAL=5

for %%C in (%CONTAINERS%) do (
    set RETRY=0
    :wait_%%C
    for /f "delims=" %%S in ('docker inspect --format={{.State.Status}} %%C 2^>nul') do set STATUS=%%S
    for /f "delims=" %%H in ('docker inspect --format={{.State.Health.Status}} %%C 2^>nul') do set HEALTH=%%H
    if "!STATUS!"=="running" (
        if "!HEALTH!"=="" goto ready_%%C
        if "!HEALTH!"=="healthy" goto ready_%%C
    )
    set /a RETRY+=1
    if !RETRY! geq %MAX_RETRIES% (
        echo Servico %%C nao ficou pronto apos %MAX_RETRIES% tentativas
        exit /b 1
    )
    timeout /t %RETRY_INTERVAL% >nul
    goto wait_%%C
    :ready_%%C
    echo %%C pronto
)

:: ─── 4. Run application ───────────────────────────────────
echo Iniciando a aplicacao Spring Boot...
mvnw.cmd spring-boot:run
