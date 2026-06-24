#!/usr/bin/env pwsh

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -LiteralPath $ProjectRoot

# ─── 1. Copy .env if missing ──────────────────────────────
if (-not (Test-Path -LiteralPath ".env")) {
    if (Test-Path -LiteralPath ".env.example") {
        Copy-Item -LiteralPath ".env.example" -Destination ".env"
        Write-Host ".env criado a partir de .env.example"
    } else {
        Write-Host ".env.example nao encontrado. Crie um .env manualmente."
        exit 1
    }
} else {
    Write-Host ".env ja existe"
}

# ─── 2. Start Docker Compose ──────────────────────────────
Write-Host "Subindo dependencias (MySQL, Redis, RabbitMQ)..."
docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "Falha ao iniciar Docker Compose"
    exit 1
}

# ─── 3. Wait for services ─────────────────────────────────
Write-Host "Aguardando servicos ficarem saudaveis..."
$Containers = @("uolcompass-mysql", "uolcompass-redis", "uolcompass-rabbitmq")
$MaxRetries = 30
$RetryInterval = 5

foreach ($Container in $Containers) {
    $Retry = 0
    $Ready = $false
    do {
        $Status = docker inspect --format='{{.State.Status}}' $Container 2>$null
        $Health = docker inspect --format='{{.State.Health.Status}}' $Container 2>$null
        if ($Status -eq "running") {
            if ([string]::IsNullOrEmpty($Health) -or $Health -eq "healthy") {
                $Ready = $true
                break
            }
        }
        $Retry++
        if ($Retry -ge $MaxRetries) {
            Write-Host "Servico $Container nao ficou pronto apos $($MaxRetries * $RetryInterval)s"
            exit 1
        }
        Start-Sleep -Seconds $RetryInterval
    } while (-not $Ready)
    Write-Host "$Container pronto"
}

# ─── 4. Run application ───────────────────────────────────
Write-Host "Iniciando a aplicacao Spring Boot..."
./mvnw.cmd spring-boot:run
