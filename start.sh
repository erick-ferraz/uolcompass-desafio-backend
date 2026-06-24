#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# ─── 1. Copy .env if missing ──────────────────────────────
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        cp .env.example .env
        echo ".env criado a partir de .env.example"
    else
        echo ".env.example nao encontrado. Crie um .env manualmente."
        exit 1
    fi
else
    echo ".env ja existe"
fi

# ─── 2. Start Docker Compose ──────────────────────────────
echo "Subindo dependencias (MySQL, Redis, RabbitMQ)..."
docker-compose up -d

# ─── 3. Wait for services ─────────────────────────────────
echo "Aguardando servicos ficarem saudaveis..."
MAX_RETRIES=30
RETRY_INTERVAL=5

for CONTAINER in uolcompass-mysql uolcompass-redis uolcompass-rabbitmq; do
    RETRY=0
    until [ "$RETRY" -ge "$MAX_RETRIES" ]; do
        STATUS=$(docker inspect --format='{{.State.Status}}' "$CONTAINER" 2>/dev/null || echo "missing")
        HEALTH=$(docker inspect --format='{{.State.Health.Status}}' "$CONTAINER" 2>/dev/null || echo "")
        if [ "$STATUS" = "running" ] && { [ -z "$HEALTH" ] || [ "$HEALTH" = "healthy" ]; }; then
            break
        fi
        RETRY=$((RETRY + 1))
        sleep "$RETRY_INTERVAL"
    done
    if [ "$RETRY" -ge "$MAX_RETRIES" ]; then
        echo "Servico $CONTAINER nao ficou pronto apos $((MAX_RETRIES * RETRY_INTERVAL))s"
        exit 1
    fi
    echo "$CONTAINER pronto"
done

# ─── 4. Run application ───────────────────────────────────
echo "Iniciando a aplicacao Spring Boot..."
./mvnw spring-boot:run
