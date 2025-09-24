#!/bin/bash

# Script para executar a aplicação com MySQL via Docker

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Verificar se foi compilado
if [ ! -d "build/classes" ]; then
    echo -e "${RED}❌ Projeto não compilado. Execute primeiro:${NC}"
    echo "./compile-manual.sh"
    exit 1
fi

# Importar funções do database
if [ ! -f "database.sh" ]; then
    echo -e "${RED}❌ Arquivo database.sh não encontrado${NC}"
    exit 1
fi

source database.sh

echo -e "${BLUE}=== Sistema de Gestão de Projetos com MySQL ===${NC}"

# As funções MySQL agora vêm do database.sh via source

# Função para parar MySQL ao sair - usa função do database.sh
cleanup() {
    echo -e "\n${YELLOW}🛑 Finalizando aplicação...${NC}"
    
    # Perguntar se deve parar o MySQL (padrão é manter rodando)
    echo -e "${BLUE}💡 Deseja parar o MySQL? [s/N] (padrão: manter rodando)${NC}"
    read -t 5 -n 1 resposta
    echo ""
    
    if [[ $resposta =~ ^[Ss]$ ]]; then
        stop_mysql
    else
        echo -e "${GREEN}✅ MySQL mantido rodando (container: ${MYSQL_CONTAINER})${NC}"
        echo -e "${YELLOW}💡 Para parar depois: ./database.sh stop${NC}"
        echo -e "${YELLOW}💡 Para ver status: ./database.sh status${NC}"
    fi
    
    exit 0
}

# Capturar Ctrl+C para cleanup
trap cleanup SIGINT SIGTERM

# Iniciar MySQL usando database.sh
echo -e "${BLUE}🐳 Inicializando MySQL via database.sh...${NC}"
if ! start_mysql; then
    echo -e "${RED}❌ Falha ao iniciar MySQL${NC}"
    exit 1
fi

# Aguardar MySQL estar pronto usando função do database.sh
if ! wait_for_mysql; then
    echo -e "${RED}❌ MySQL não ficou pronto${NC}"
    exit 1
fi

# Configurar banco usando função do database.sh
if ! setup_database; then
    echo -e "${RED}❌ Falha ao configurar banco de dados${NC}"
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

echo -e "${BLUE}🚀 Iniciando aplicação...${NC}"
echo -e "${YELLOW}Pressione Ctrl+C para parar${NC}"
echo ""

# Executar aplicação com configurações MySQL via Docker
env -i \
    PATH="/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/bin:/bin" \
    HOME="$HOME" \
    USER="$USER" \
    DISPLAY="$DISPLAY" \
    XAUTHORITY="$XAUTHORITY" \
    /usr/lib/jvm/java-21-openjdk-amd64/bin/java \
    -cp "$CLASSPATH" \
    com.gestao.projetos.Main

# Cleanup automático ao finalizar
cleanup
