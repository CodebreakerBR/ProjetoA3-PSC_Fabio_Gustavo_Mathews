#!/bin/bash

# Script para executar a aplicação compilada manualmente

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Verificar se foi compilado
if [ ! -d "build/classes" ]; then
    echo "❌ Projeto não compilado. Execute primeiro:"
    echo "./compile-manual.sh"
    exit 1
fi

# Montar classpath
CLASSPATH="build/classes:src/main/resources"
for jar in lib/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Criar diretório de logs
mkdir -p logs

echo "=== Executando Sistema de Gestão de Projetos ==="
echo "Pressione Ctrl+C para parar"
echo ""

# Executar com env limpo para evitar conflitos de snap
env -i \
    PATH="/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/bin:/bin" \
    HOME="$HOME" \
    USER="$USER" \
    DISPLAY="$DISPLAY" \
    XAUTHORITY="$XAUTHORITY" \
    /usr/lib/jvm/java-21-openjdk-amd64/bin/java \
    -cp "$CLASSPATH" \
    com.gestao.projetos.Main
