#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configurações do MySQL (compatível com database.properties)
MYSQL_CONTAINER="gestao-mysql"
MYSQL_PORT="3306"
MYSQL_DATABASE="gestao_projetos"
MYSQL_USER="gestao_user"
MYSQL_PASSWORD="gestao123"
MYSQL_ROOT_PASSWORD="root123"
MYSQL_VOLUME="gestao-mysql-data"
MYSQL_DATA_DIR="$HOME/gestao-projetos-data"

check_container() {
    docker ps --format "table {{.Names}}" | grep -q "^${MYSQL_CONTAINER}$"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker não encontrado. Instale o Docker primeiro.${NC}"
        echo -e "${YELLOW}💡 Ubuntu/Debian: sudo apt install docker.io${NC}"
        return 1
    fi

    if ! docker info >/dev/null 2>&1; then
        echo -e "${RED}❌ Docker daemon não está rodando.${NC}"
        echo -e "${YELLOW}💡 Inicie o Docker com: sudo systemctl start docker${NC}"
        echo -e "${YELLOW}💡 Ou adicione seu usuário ao grupo docker: sudo usermod -aG docker $USER${NC}"
        return 1
    fi

    return 0
}

create_mysql_volume() {
    echo -e "${YELLOW}📁 Configurando volume persistente para MySQL...${NC}"
    
    # Criar diretório local para dados (método bind mount)
    if [ ! -d "$MYSQL_DATA_DIR" ]; then
        echo -e "${YELLOW}📂 Criando diretório para dados: $MYSQL_DATA_DIR${NC}"
        mkdir -p "$MYSQL_DATA_DIR"
        chmod 755 "$MYSQL_DATA_DIR"
    fi
    
    # Verificar se o volume Docker existe, se não, criar
    if ! docker volume ls | grep -q "$MYSQL_VOLUME"; then
        echo -e "${YELLOW}📦 Criando volume Docker: $MYSQL_VOLUME${NC}"
        docker volume create $MYSQL_VOLUME >/dev/null 2>&1
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Volume Docker criado com sucesso${NC}"
        else
            echo -e "${YELLOW}⚠️ Usando bind mount como fallback${NC}"
        fi
    else
        echo -e "${GREEN}✅ Volume Docker já existe${NC}"
    fi
}

start_mysql() {
    echo -e "${YELLOW}🐳 Iniciando MySQL via Docker...${NC}"

    if ! check_docker; then
        return 1
    fi

    # Criar volume antes de inicializar o container
    create_mysql_volume

    if docker ps -a --format "table {{.Names}}" | grep -q "^${MYSQL_CONTAINER}$"; then
        if ! check_container; then
            echo -e "${YELLOW}⚠️ Removendo container MySQL parado...${NC}"
            docker rm ${MYSQL_CONTAINER} >/dev/null 2>&1
        fi
    fi

    if ! check_container; then
        echo -e "${YELLOW}📥 Baixando imagem MySQL (pode demorar na primeira vez)...${NC}"

        if ! docker images mysql:8.0 --format "table {{.Repository}}" | grep -q "mysql"; then
            docker pull mysql:8.0
        fi

        echo -e "${YELLOW}🚀 Criando container MySQL com volume persistente...${NC}"

        # Tentar usar volume Docker primeiro, fallback para bind mount
        if docker volume ls | grep -q "$MYSQL_VOLUME"; then
            VOLUME_OPTION="-v ${MYSQL_VOLUME}:/var/lib/mysql"
            echo -e "${BLUE}📦 Usando volume Docker: $MYSQL_VOLUME${NC}"
        else
            VOLUME_OPTION="-v ${MYSQL_DATA_DIR}:/var/lib/mysql"
            echo -e "${BLUE}📁 Usando bind mount: $MYSQL_DATA_DIR${NC}"
        fi

        docker run -d \
            --name ${MYSQL_CONTAINER} \
            --network=host \
            $VOLUME_OPTION \
            -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \
            -e MYSQL_DATABASE=${MYSQL_DATABASE} \
            -e MYSQL_USER=${MYSQL_USER} \
            -e MYSQL_PASSWORD=${MYSQL_PASSWORD} \
            --restart=unless-stopped \
            mysql:8.0 \
            --character-set-server=utf8mb4 \
            --collation-server=utf8mb4_unicode_ci \
            --default-authentication-plugin=mysql_native_password \
            --bind-address=0.0.0.0

        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Container MySQL criado com volume persistente${NC}"
            echo -e "${BLUE}💾 Dados serão salvos em: ${MYSQL_DATA_DIR}${NC}"
            return 0
        else
            echo -e "${RED}❌ Erro ao criar container MySQL${NC}"
            echo -e "${YELLOW}💡 Verifique se a porta 3306 não está em uso:${NC}"
            echo "sudo netstat -tlnp | grep :3306"
            return 1
        fi
    else
        echo -e "${GREEN}✅ MySQL já está rodando${NC}"
        return 0
    fi
}

wait_for_mysql() {
    echo -e "${YELLOW}⏳ Aguardando MySQL estar pronto...${NC}"

    for i in {1..30}; do
        if docker exec ${MYSQL_CONTAINER} mysqladmin ping -h localhost --silent >/dev/null 2>&1; then
            echo -e "${GREEN}✅ MySQL está pronto!${NC}"
            return 0
        fi
        sleep 2
    done

    echo -e "${RED}❌ Timeout aguardando MySQL${NC}"
    return 1
}

setup_database() {
    echo -e "${YELLOW}🗄️ Configurando banco de dados...${NC}"

    echo -e "${YELLOW}🔍 Testando conexão com MySQL...${NC}"

    for i in {1..10}; do
        if docker exec ${MYSQL_CONTAINER} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} -e "SELECT 1;" >/dev/null 2>&1; then
            echo -e "${GREEN}✅ Conexão com MySQL OK${NC}"
            break
        else
            echo -e "${YELLOW}⏳ Tentativa $i/10 - Aguardando MySQL aceitar conexões...${NC}"
            sleep 3
        fi

        if [ $i -eq 10 ]; then
            echo -e "${RED}❌ Falha ao conectar com MySQL após 10 tentativas${NC}"
            return 1
        fi
    done

    echo -e "${YELLOW}🔐 Configurando permissões do usuário...${NC}"
    docker exec ${MYSQL_CONTAINER} mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "
        -- Configuração simples para network=host
        CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY '${MYSQL_PASSWORD}';
        ALTER USER '${MYSQL_USER}'@'%' IDENTIFIED WITH mysql_native_password BY '${MYSQL_PASSWORD}';
        GRANT ALL PRIVILEGES ON ${MYSQL_DATABASE}.* TO '${MYSQL_USER}'@'%';
        GRANT ALL PRIVILEGES ON ${MYSQL_DATABASE}.* TO '${MYSQL_USER}'@'localhost';
        GRANT ALL PRIVILEGES ON ${MYSQL_DATABASE}.* TO '${MYSQL_USER}'@'127.0.0.1';
        FLUSH PRIVILEGES;
    " >/dev/null 2>&1

    echo -e "${GREEN}✅ Permissões configuradas${NC}"

    echo -e "${YELLOW}🗄️ Verificando database ${MYSQL_DATABASE}...${NC}"
    DB_EXISTS=$(docker exec ${MYSQL_CONTAINER} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} -e "SHOW DATABASES LIKE '${MYSQL_DATABASE}';" -s -N 2>/dev/null | wc -l)

    if [ "$DB_EXISTS" -eq "0" ]; then
        echo -e "${YELLOW}📝 Criando database ${MYSQL_DATABASE}...${NC}"
        docker exec ${MYSQL_CONTAINER} mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" >/dev/null 2>&1
    else
        echo -e "${GREEN}✅ Database ${MYSQL_DATABASE} já existe${NC}"
    fi

    TABLE_COUNT=$(docker exec ${MYSQL_CONTAINER} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${MYSQL_DATABASE}';" -s -N 2>/dev/null || echo "0")

    if [ "$TABLE_COUNT" -eq "0" ] 2>/dev/null || [ "$TABLE_COUNT" -lt "5" ] 2>/dev/null; then
        echo -e "${YELLOW}📝 Criando schema do banco...${NC}"

        if [ -f "database/database-mysql.sql" ]; then
            echo -e "${YELLOW}📄 Executando database-mysql.sql...${NC}"
            docker exec -i ${MYSQL_CONTAINER} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} < database/database-mysql.sql 2>/dev/null

            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✅ Schema criado com sucesso!${NC}"

                NEW_TABLE_COUNT=$(docker exec ${MYSQL_CONTAINER} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${MYSQL_DATABASE}';" -s -N 2>/dev/null)
                echo -e "${GREEN}📊 Total de tabelas criadas: ${NEW_TABLE_COUNT}${NC}"
            else
                echo -e "${RED}❌ Erro ao executar database-mysql.sql${NC}"
                return 1
            fi
        else
            echo -e "${YELLOW}⚠️ Arquivo database/database-mysql.sql não encontrado${NC}"
            echo -e "${YELLOW}💡 Criando estrutura básica...${NC}"
            docker exec ${MYSQL_CONTAINER} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} -e "
                CREATE TABLE IF NOT EXISTS usuario (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    nome VARCHAR(100) NOT NULL,
                    email VARCHAR(150) UNIQUE NOT NULL,
                    ativo BOOLEAN DEFAULT TRUE,
                    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

                INSERT IGNORE INTO usuario (nome, email) VALUES ('Admin', 'admin@gestao.com');
            " >/dev/null 2>&1
            echo -e "${GREEN}✅ Estrutura básica criada${NC}"
        fi
    else
        echo -e "${GREEN}✅ Schema já existe (${TABLE_COUNT} tabelas encontradas)${NC}"
    fi

    return 0
}

stop_mysql() {
    echo -e "${YELLOW}🛑 Parando MySQL...${NC}"
    if check_container; then
        docker stop ${MYSQL_CONTAINER} >/dev/null 2>&1
        echo -e "${GREEN}✅ MySQL parado (container mantido para preservar dados)${NC}"
        echo -e "${BLUE}💡 Para remover completamente: $0 remove${NC}"
    else
        echo -e "${YELLOW}⚠️ MySQL não estava rodando${NC}"
    fi
}

remove_mysql() {
    echo -e "${YELLOW}🗑️ Removendo container MySQL...${NC}"
    
    # Parar o container se estiver rodando
    if check_container; then
        docker stop ${MYSQL_CONTAINER} >/dev/null 2>&1
    fi
    
    # Remover o container
    if docker ps -a --format "table {{.Names}}" | grep -q "^${MYSQL_CONTAINER}$"; then
        docker rm ${MYSQL_CONTAINER} >/dev/null 2>&1
        echo -e "${GREEN}✅ Container MySQL removido${NC}"
    fi
    
    echo -e "${BLUE}💾 Dados preservados em: $MYSQL_DATA_DIR${NC}"
    echo -e "${YELLOW}⚠️ Para remover os dados permanentemente: rm -rf $MYSQL_DATA_DIR${NC}"
}

status_mysql() {
    if check_container; then
        echo -e "${GREEN}✅ MySQL está rodando (container: ${MYSQL_CONTAINER})${NC}"
        echo -e "${BLUE}📊 Informações do container:${NC}"
        docker ps --filter "name=${MYSQL_CONTAINER}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        
        echo -e "${BLUE}💾 Informações de armazenamento:${NC}"
        if docker volume ls | grep -q "$MYSQL_VOLUME"; then
            echo -e "  Volume Docker: $MYSQL_VOLUME"
            docker volume inspect $MYSQL_VOLUME --format "  Localização: {{.Mountpoint}}" 2>/dev/null
        fi
        echo -e "  Diretório local: $MYSQL_DATA_DIR"
        if [ -d "$MYSQL_DATA_DIR" ]; then
            echo -e "  Tamanho dos dados: $(du -sh "$MYSQL_DATA_DIR" 2>/dev/null | cut -f1)"
        fi
        
        echo -e "${BLUE}🗄️ Informações do banco:${NC}"
        TABLES=$(docker exec ${MYSQL_CONTAINER} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${MYSQL_DATABASE}';" -s -N 2>/dev/null || echo "0")
        echo -e "  Tabelas: $TABLES"
        
    else
        echo -e "${RED}❌ MySQL não está rodando${NC}"
        
        # Mostrar informações mesmo com container parado
        if [ -d "$MYSQL_DATA_DIR" ]; then
            echo -e "${BLUE}💾 Dados preservados em: $MYSQL_DATA_DIR${NC}"
            echo -e "  Tamanho: $(du -sh "$MYSQL_DATA_DIR" 2>/dev/null | cut -f1)"
        fi
    fi
}

logs_mysql() {
    if check_container; then
        echo -e "${BLUE}📋 Logs do MySQL:${NC}"
        docker logs ${MYSQL_CONTAINER} --tail 20
    else
        echo -e "${RED}❌ MySQL não está rodando${NC}"
    fi
}

connect_mysql() {
    if check_container; then
        echo -e "${BLUE}🔗 Conectando ao MySQL...${NC}"
        docker exec -it ${MYSQL_CONTAINER} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE}
    else
        echo -e "${RED}❌ MySQL não está rodando${NC}"
    fi
}

setup_complete_database() {
    echo -e "${BLUE}=== Configuração Completa do MySQL ===${NC}"

    if start_mysql; then
        if wait_for_mysql; then
            if setup_database; then
                echo -e "${GREEN}🎉 MySQL configurado com sucesso!${NC}"
                return 0
            fi
        fi
    fi

    echo -e "${RED}❌ Falha na configuração do MySQL${NC}"
    return 1
}

if [ "${BASH_SOURCE[0]}" == "${0}" ]; then
    case "${1:-setup}" in
        "start")
            start_mysql
            ;;
        "stop")
            stop_mysql
            ;;
        "remove")
            remove_mysql
            ;;
        "status")
            status_mysql
            ;;
        "logs")
            logs_mysql
            ;;
        "connect")
            connect_mysql
            ;;
        "setup")
            setup_complete_database
            ;;
        *)
            echo -e "${BLUE}Uso: $0 {start|stop|remove|status|logs|connect|setup}${NC}"
            echo ""
            echo -e "${YELLOW}Comandos disponíveis:${NC}"
            echo -e "  start    - Iniciar MySQL com volume persistente"
            echo -e "  stop     - Parar MySQL (preserva dados)"
            echo -e "  remove   - Remover container (preserva dados)"
            echo -e "  status   - Ver status e informações de armazenamento"
            echo -e "  logs     - Ver logs do MySQL"
            echo -e "  connect  - Conectar ao MySQL"
            echo -e "  setup    - Configuração completa (padrão)"
            echo ""
            echo -e "${BLUE}💾 Persistência de dados:${NC}"
            echo -e "  Os dados são salvos em: $MYSQL_DATA_DIR"
            echo -e "  Volume Docker: $MYSQL_VOLUME"
            echo -e "  Os dados persistem mesmo após remover o container"
            ;;
    esac
fi
