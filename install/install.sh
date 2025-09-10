#!/bin/bash

# =====================================================
# INSTALADOR - Sistema de Gestão de Projetos
# Instala aplicação + MySQL local automaticamente
# =====================================================

set -e  # Parar em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurações
APP_NAME="Sistema de Gestão de Projetos"
APP_DIR="/opt/gestao-projetos"
DATA_DIR="/var/lib/gestao-projetos"
LOG_DIR="/var/log/gestao-projetos"
SERVICE_NAME="gestao-projetos"
MYSQL_ROOT_PASSWORD="gestao_root_2025"
MYSQL_APP_PASSWORD="gestao123"
DESKTOP_FILE="/usr/share/applications/gestao-projetos.desktop"

echo -e "${BLUE}======================================================${NC}"
echo -e "${BLUE}    INSTALADOR - $APP_NAME${NC}"
echo -e "${BLUE}======================================================${NC}"
echo

# Verificar se está rodando como root
if [[ $EUID -ne 0 ]]; then
   echo -e "${RED}❌ Este script deve ser executado como root (sudo)${NC}" 
   exit 1
fi

# Função para log
log() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Detectar distribuição Linux
detect_distro() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$NAME
        VER=$VERSION_ID
    else
        error "Não foi possível detectar a distribuição Linux"
        exit 1
    fi
    log "Distribuição detectada: $OS $VER"
}

# Instalar dependências
install_dependencies() {
    log "Instalando dependências do sistema..."
    
    if [[ "$OS" == *"Ubuntu"* ]] || [[ "$OS" == *"Debian"* ]]; then
        apt update
        apt install -y openjdk-11-jdk mysql-server mysql-client curl wget unzip
    elif [[ "$OS" == *"CentOS"* ]] || [[ "$OS" == *"Red Hat"* ]] || [[ "$OS" == *"Fedora"* ]]; then
        if command -v dnf &> /dev/null; then
            dnf install -y java-11-openjdk java-11-openjdk-devel mysql-server mysql curl wget unzip
        else
            yum install -y java-11-openjdk java-11-openjdk-devel mysql-server mysql curl wget unzip
        fi
    else
        error "Distribuição não suportada: $OS"
        exit 1
    fi
    
    log "✅ Dependências instaladas com sucesso"
}

# Configurar MySQL
setup_mysql() {
    log "Configurando MySQL..."
    
    # Iniciar MySQL
    systemctl start mysql || systemctl start mysqld
    systemctl enable mysql || systemctl enable mysqld
    
    # Configurar senha root do MySQL (se ainda não configurada)
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$MYSQL_ROOT_PASSWORD';" 2>/dev/null || true
    
    # Criar banco e usuário
    mysql -u root -p$MYSQL_ROOT_PASSWORD -e "
        CREATE DATABASE IF NOT EXISTS gestao_projetos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
        CREATE USER IF NOT EXISTS 'gestao_user'@'localhost' IDENTIFIED BY '$MYSQL_APP_PASSWORD';
        GRANT ALL PRIVILEGES ON gestao_projetos.* TO 'gestao_user'@'localhost';
        FLUSH PRIVILEGES;
    " 2>/dev/null || {
        warn "Configuração automática do MySQL falhou. Tentando configuração manual..."
        
        # Tentar sem senha primeiro (instalação fresca)
        mysql -e "
            ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$MYSQL_ROOT_PASSWORD';
            CREATE DATABASE IF NOT EXISTS gestao_projetos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
            CREATE USER IF NOT EXISTS 'gestao_user'@'localhost' IDENTIFIED BY '$MYSQL_APP_PASSWORD';
            GRANT ALL PRIVILEGES ON gestao_projetos.* TO 'gestao_user'@'localhost';
            FLUSH PRIVILEGES;
        " 2>/dev/null || {
            error "❌ Falha na configuração do MySQL. Execute manualmente:"
            echo "sudo mysql"
            echo "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$MYSQL_ROOT_PASSWORD';"
            echo "CREATE DATABASE gestao_projetos;"
            echo "CREATE USER 'gestao_user'@'localhost' IDENTIFIED BY '$MYSQL_APP_PASSWORD';"
            echo "GRANT ALL PRIVILEGES ON gestao_projetos.* TO 'gestao_user'@'localhost';"
            exit 1
        }
    }
    
    log "✅ MySQL configurado com sucesso"
}

# Criar estrutura de diretórios
create_directories() {
    log "Criando estrutura de diretórios..."
    
    mkdir -p "$APP_DIR"
    mkdir -p "$DATA_DIR"
    mkdir -p "$LOG_DIR"
    mkdir -p "$APP_DIR/lib"
    mkdir -p "$APP_DIR/config"
    mkdir -p "$APP_DIR/scripts"
    
    log "✅ Diretórios criados"
}

# Copiar arquivos da aplicação
install_application() {
    log "Instalando aplicação..."
    
    # Copiar JARs compilados
    if [ -d "build/classes" ]; then
        cp -r build/classes/* "$APP_DIR/"
    else
        error "❌ Aplicação não compilada. Execute './compile-manual.sh' primeiro"
        exit 1
    fi
    
    # Copiar recursos
    if [ -d "src/main/resources" ]; then
        cp -r src/main/resources/* "$APP_DIR/config/"
    fi
    
    # Copiar dependências
    if [ -d "lib" ]; then
        cp -r lib/* "$APP_DIR/lib/"
    fi
    
    # Copiar script SQL
    if [ -f "database/database-mysql.sql" ]; then
        cp database/database-mysql.sql "$APP_DIR/scripts/"
    fi
    
    log "✅ Aplicação instalada"
}

# Configurar banco de dados
setup_database() {
    log "Configurando banco de dados..."
    
    # Executar script SQL
    if [ -f "$APP_DIR/scripts/database-mysql.sql" ]; then
        mysql -u root -p$MYSQL_ROOT_PASSWORD < "$APP_DIR/scripts/database-mysql.sql"
        log "✅ Schema do banco criado"
    else
        warn "Script SQL não encontrado"
    fi
}

# Criar script de execução
create_launcher() {
    log "Criando launcher..."
    
    cat > "$APP_DIR/gestao-projetos.sh" << 'EOF'
#!/bin/bash

# Script de execução do Sistema de Gestão de Projetos
APP_DIR="/opt/gestao-projetos"
LOG_DIR="/var/log/gestao-projetos"

# Criar diretório de logs se não existir
mkdir -p "$LOG_DIR"

# Configurar classpath
CLASSPATH="$APP_DIR:$APP_DIR/config"
for jar in $APP_DIR/lib/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Executar aplicação
cd "$APP_DIR"
java -cp "$CLASSPATH" \
     -Djava.library.path=/usr/lib/jvm/java-11-openjdk-amd64/lib \
     -Djava.awt.headless=false \
     -Dfile.encoding=UTF-8 \
     com.gestao.projetos.Main 2>&1 | tee "$LOG_DIR/application.log"
EOF

    chmod +x "$APP_DIR/gestao-projetos.sh"
    
    # Criar symlink para executar de qualquer lugar
    ln -sf "$APP_DIR/gestao-projetos.sh" "/usr/local/bin/gestao-projetos"
    
    log "✅ Launcher criado"
}

# Criar serviço systemd
create_service() {
    log "Criando serviço systemd..."
    
    cat > "/etc/systemd/system/$SERVICE_NAME.service" << EOF
[Unit]
Description=$APP_NAME
After=mysql.service
Requires=mysql.service
StartLimitIntervalSec=60
StartLimitBurst=3

[Service]
Type=simple
User=root
Group=root
WorkingDirectory=$APP_DIR
ExecStart=$APP_DIR/gestao-projetos.sh
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
Environment=DISPLAY=:0

[Install]
WantedBy=multi-user.target
EOF

    systemctl daemon-reload
    systemctl enable $SERVICE_NAME
    
    log "✅ Serviço systemd criado"
}

# Criar entrada no menu
create_desktop_entry() {
    log "Criando entrada no menu..."
    
    cat > "$DESKTOP_FILE" << EOF
[Desktop Entry]
Name=Sistema de Gestão de Projetos
Comment=Sistema completo de gestão de projetos e equipes
Exec=/opt/gestao-projetos/gestao-projetos.sh
Icon=applications-development
Terminal=false
Type=Application
Categories=Development;Office;ProjectManagement;
StartupNotify=true
EOF

    chmod +x "$DESKTOP_FILE"
    
    log "✅ Entrada no menu criada"
}

# Configurar permissões
set_permissions() {
    log "Configurando permissões..."
    
    chown -R root:root "$APP_DIR"
    chown -R root:root "$DATA_DIR"
    chown -R root:root "$LOG_DIR"
    
    chmod -R 755 "$APP_DIR"
    chmod -R 755 "$DATA_DIR"
    chmod -R 755 "$LOG_DIR"
    
    log "✅ Permissões configuradas"
}

# Criar script de desinstalação
create_uninstaller() {
    log "Criando desinstalador..."
    
    cat > "$APP_DIR/uninstall.sh" << 'EOF'
#!/bin/bash

echo "Desinstalando Sistema de Gestão de Projetos..."

# Parar serviço
systemctl stop gestao-projetos 2>/dev/null || true
systemctl disable gestao-projetos 2>/dev/null || true

# Remover arquivos
rm -rf /opt/gestao-projetos
rm -rf /var/lib/gestao-projetos
rm -rf /var/log/gestao-projetos
rm -f /etc/systemd/system/gestao-projetos.service
rm -f /usr/share/applications/gestao-projetos.desktop
rm -f /usr/local/bin/gestao-projetos

# Reload systemd
systemctl daemon-reload

echo "✅ Desinstalação concluída"
echo "ℹ️  O banco de dados MySQL não foi removido"
EOF

    chmod +x "$APP_DIR/uninstall.sh"
    
    log "✅ Desinstalador criado em $APP_DIR/uninstall.sh"
}

# Função principal
main() {
    echo -e "${BLUE}🚀 Iniciando instalação...${NC}"
    echo
    
    detect_distro
    install_dependencies
    setup_mysql
    create_directories
    install_application
    setup_database
    create_launcher
    create_service
    create_desktop_entry
    set_permissions
    create_uninstaller
    
    echo
    echo -e "${GREEN}======================================================${NC}"
    echo -e "${GREEN}    ✅ INSTALAÇÃO CONCLUÍDA COM SUCESSO!${NC}"
    echo -e "${GREEN}======================================================${NC}"
    echo
    echo -e "${BLUE}📋 Informações da Instalação:${NC}"
    echo -e "   • Aplicação: $APP_DIR"
    echo -e "   • Logs: $LOG_DIR"
    echo -e "   • Banco: gestao_projetos (MySQL local)"
    echo -e "   • Usuário DB: gestao_user"
    echo -e "   • Senha DB: $MYSQL_APP_PASSWORD"
    echo
    echo -e "${BLUE}🚀 Como executar:${NC}"
    echo -e "   • Via comando: ${GREEN}gestao-projetos${NC}"
    echo -e "   • Via menu: Aplicações → Desenvolvimento → Sistema de Gestão de Projetos"
    echo -e "   • Via serviço: ${GREEN}sudo systemctl start gestao-projetos${NC}"
    echo
    echo -e "${BLUE}🔧 Gerenciamento:${NC}"
    echo -e "   • Iniciar serviço: ${GREEN}sudo systemctl start gestao-projetos${NC}"
    echo -e "   • Parar serviço: ${GREEN}sudo systemctl stop gestao-projetos${NC}"
    echo -e "   • Status: ${GREEN}sudo systemctl status gestao-projetos${NC}"
    echo -e "   • Logs: ${GREEN}journalctl -u gestao-projetos -f${NC}"
    echo
    echo -e "${BLUE}🗑️ Para desinstalar:${NC}"
    echo -e "   • Execute: ${GREEN}sudo $APP_DIR/uninstall.sh${NC}"
    echo
    echo -e "${YELLOW}⚠️  Importante:${NC}"
    echo -e "   • MySQL Root Password: ${GREEN}$MYSQL_ROOT_PASSWORD${NC}"
    echo -e "   • Guarde essas informações em local seguro!"
    echo
}

# Executar instalação
main "$@"
