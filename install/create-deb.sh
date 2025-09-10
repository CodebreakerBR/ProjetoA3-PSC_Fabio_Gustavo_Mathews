#!/bin/bash

# =====================================================
# GERADOR DE PACOTE DEB - Sistema de Gestão de Projetos
# Cria um pacote .deb instalável para Ubuntu/Debian
# =====================================================

set -e

# Configurações do pacote
PACKAGE_NAME="gestao-projetos"
VERSION="1.0.0"
MAINTAINER="Gustavo <gustavo@gestao.com>"
DESCRIPTION="Sistema completo de gestão de projetos e equipes com MySQL"
ARCHITECTURE="all"

# Diretórios
BUILD_DIR="build-package"
DEB_DIR="$BUILD_DIR/DEBIAN"
APP_DIR="$BUILD_DIR/opt/gestao-projetos"
SERVICE_DIR="$BUILD_DIR/etc/systemd/system"
DESKTOP_DIR="$BUILD_DIR/usr/share/applications"
BIN_DIR="$BUILD_DIR/usr/local/bin"

echo "🚀 Gerando pacote DEB para $PACKAGE_NAME v$VERSION"

# Limpar build anterior
rm -rf "$BUILD_DIR"
rm -f "${PACKAGE_NAME}_${VERSION}_${ARCHITECTURE}.deb"

# Compilar aplicação se necessário
if [ ! -d "build/classes" ]; then
    echo "📦 Compilando aplicação..."
    ./compile-manual.sh
fi

# Criar estrutura do pacote
echo "📁 Criando estrutura do pacote..."
mkdir -p "$DEB_DIR"
mkdir -p "$APP_DIR"
mkdir -p "$SERVICE_DIR"
mkdir -p "$DESKTOP_DIR"
mkdir -p "$BIN_DIR"
mkdir -p "$APP_DIR/lib"
mkdir -p "$APP_DIR/config"
mkdir -p "$APP_DIR/scripts"

# Copiar arquivos da aplicação
echo "📋 Copiando arquivos da aplicação..."
cp -r build/classes/* "$APP_DIR/"
cp -r src/main/resources/* "$APP_DIR/config/"
cp -r lib/* "$APP_DIR/lib/"
cp database/database-mysql.sql "$APP_DIR/scripts/"

# Criar script principal
cat > "$APP_DIR/gestao-projetos.sh" << 'EOF'
#!/bin/bash
APP_DIR="/opt/gestao-projetos"
LOG_DIR="/var/log/gestao-projetos"

mkdir -p "$LOG_DIR"

CLASSPATH="$APP_DIR:$APP_DIR/config"
for jar in $APP_DIR/lib/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

cd "$APP_DIR"
exec java -cp "$CLASSPATH" \
     -Djava.awt.headless=false \
     -Dfile.encoding=UTF-8 \
     com.gestao.projetos.Main "$@"
EOF

chmod +x "$APP_DIR/gestao-projetos.sh"

# Criar symlink para bin
ln -sf "/opt/gestao-projetos/gestao-projetos.sh" "$BIN_DIR/gestao-projetos"

# Criar serviço systemd
cat > "$SERVICE_DIR/gestao-projetos.service" << 'EOF'
[Unit]
Description=Sistema de Gestão de Projetos
After=mysql.service
Wants=mysql.service

[Service]
Type=simple
User=root
Group=root
WorkingDirectory=/opt/gestao-projetos
ExecStart=/opt/gestao-projetos/gestao-projetos.sh
Restart=on-failure
RestartSec=10
Environment=DISPLAY=:0

[Install]
WantedBy=multi-user.target
EOF

# Criar entrada desktop
cat > "$DESKTOP_DIR/gestao-projetos.desktop" << 'EOF'
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

# Criar arquivo de controle do DEB
cat > "$DEB_DIR/control" << EOF
Package: $PACKAGE_NAME
Version: $VERSION
Section: devel
Priority: optional
Architecture: $ARCHITECTURE
Depends: openjdk-11-jdk, mysql-server, mysql-client
Maintainer: $MAINTAINER
Description: $DESCRIPTION
 Sistema completo de gestão de projetos desenvolvido em Java com Swing.
 Inclui interface gráfica intuitiva, banco de dados MySQL integrado,
 e funcionalidades completas de CRUD para usuários, projetos e tarefas.
 .
 Características:
  - Interface gráfica nativa com Swing
  - Banco de dados MySQL local
  - Arquitetura MVC profissional
  - Sistema de logging robusto
  - Pool de conexões otimizado
  - Pronto para produção
EOF

# Criar script de pós-instalação
cat > "$DEB_DIR/postinst" << 'EOF'
#!/bin/bash
set -e

echo "Configurando Sistema de Gestão de Projetos..."

# Configurar MySQL se não estiver configurado
if ! systemctl is-active --quiet mysql; then
    systemctl start mysql
    systemctl enable mysql
fi

# Configurar banco de dados
MYSQL_ROOT_PASSWORD="gestao_root_2025"
MYSQL_APP_PASSWORD="gestao123"

# Tentar configurar MySQL
mysql -e "
    ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$MYSQL_ROOT_PASSWORD';
    CREATE DATABASE IF NOT EXISTS gestao_projetos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE USER IF NOT EXISTS 'gestao_user'@'localhost' IDENTIFIED BY '$MYSQL_APP_PASSWORD';
    GRANT ALL PRIVILEGES ON gestao_projetos.* TO 'gestao_user'@'localhost';
    FLUSH PRIVILEGES;
" 2>/dev/null || {
    # Se falhar, tentar sem senha (instalação fresca)
    mysql -e "
        SET PASSWORD FOR 'root'@'localhost' = PASSWORD('$MYSQL_ROOT_PASSWORD');
        CREATE DATABASE IF NOT EXISTS gestao_projetos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
        CREATE USER IF NOT EXISTS 'gestao_user'@'localhost' IDENTIFIED BY '$MYSQL_APP_PASSWORD';
        GRANT ALL PRIVILEGES ON gestao_projetos.* TO 'gestao_user'@'localhost';
        FLUSH PRIVILEGES;
    " 2>/dev/null || true
}

# Executar script SQL
if [ -f /opt/gestao-projetos/scripts/database-mysql.sql ]; then
    mysql -u root -p$MYSQL_ROOT_PASSWORD < /opt/gestao-projetos/scripts/database-mysql.sql 2>/dev/null || true
fi

# Criar diretórios de log
mkdir -p /var/log/gestao-projetos
chown root:root /var/log/gestao-projetos

# Habilitar e iniciar serviço
systemctl daemon-reload
systemctl enable gestao-projetos

echo "✅ Sistema de Gestão de Projetos instalado com sucesso!"
echo ""
echo "Para executar:"
echo "  - Via comando: gestao-projetos"
echo "  - Via menu: Aplicações → Desenvolvimento"
echo "  - Via serviço: sudo systemctl start gestao-projetos"
echo ""
echo "Credenciais MySQL:"
echo "  - Root password: $MYSQL_ROOT_PASSWORD"
echo "  - App user: gestao_user"
echo "  - App password: $MYSQL_APP_PASSWORD"
EOF

chmod +x "$DEB_DIR/postinst"

# Criar script de remoção
cat > "$DEB_DIR/prerm" << 'EOF'
#!/bin/bash
set -e

echo "Removendo Sistema de Gestão de Projetos..."

# Parar e desabilitar serviço
systemctl stop gestao-projetos 2>/dev/null || true
systemctl disable gestao-projetos 2>/dev/null || true

systemctl daemon-reload
EOF

chmod +x "$DEB_DIR/prerm"

# Criar script de pós-remoção
cat > "$DEB_DIR/postrm" << 'EOF'
#!/bin/bash
set -e

if [ "$1" = "purge" ]; then
    echo "Limpando dados do Sistema de Gestão de Projetos..."
    
    # Remover logs
    rm -rf /var/log/gestao-projetos
    
    echo "ℹ️  O banco de dados MySQL foi mantido para segurança"
    echo "   Para remover completamente:"
    echo "   mysql -u root -p -e 'DROP DATABASE gestao_projetos; DROP USER gestao_user@localhost;'"
fi
EOF

chmod +x "$DEB_DIR/postrm"

# Definir permissões corretas
echo "🔐 Configurando permissões..."
find "$BUILD_DIR" -type d -exec chmod 755 {} \;
find "$BUILD_DIR" -type f -exec chmod 644 {} \;
chmod +x "$APP_DIR/gestao-projetos.sh"
chmod +x "$BIN_DIR/gestao-projetos"
chmod +x "$DEB_DIR/postinst"
chmod +x "$DEB_DIR/prerm"
chmod +x "$DEB_DIR/postrm"

# Construir o pacote DEB
echo "🔨 Construindo pacote DEB..."
fakeroot dpkg-deb --build "$BUILD_DIR" "${PACKAGE_NAME}_${VERSION}_${ARCHITECTURE}.deb"

# Limpar diretório de build
rm -rf "$BUILD_DIR"

echo ""
echo "✅ Pacote DEB criado com sucesso!"
echo "📦 Arquivo: ${PACKAGE_NAME}_${VERSION}_${ARCHITECTURE}.deb"
echo ""
echo "🚀 Para instalar:"
echo "   sudo dpkg -i ${PACKAGE_NAME}_${VERSION}_${ARCHITECTURE}.deb"
echo "   sudo apt-get install -f  # Se houver dependências"
echo ""
echo "🗑️ Para remover:"
echo "   sudo apt remove $PACKAGE_NAME"
echo ""

# Mostrar informações do pacote
echo "📋 Informações do pacote:"
dpkg-deb -I "${PACKAGE_NAME}_${VERSION}_${ARCHITECTURE}.deb"
