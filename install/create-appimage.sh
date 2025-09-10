#!/bin/bash

# =====================================================
# GERADOR DE APPIMAGE - Sistema de Gestão de Projetos
# Cria um AppImage portátil para qualquer distribuição Linux
# =====================================================

set -e

APP_NAME="gestao-projetos"
VERSION="1.0.0"
APP_DIR="AppDir"
APPIMAGE_NAME="Sistema_Gestao_Projetos-${VERSION}-x86_64.AppImage"

echo "🚀 Gerando AppImage para Sistema de Gestão de Projetos v$VERSION"

# Verificar dependências
if ! command -v wget &> /dev/null; then
    echo "❌ wget não encontrado. Instale com: sudo apt install wget"
    exit 1
fi

# Compilar se necessário
if [ ! -d "build/classes" ]; then
    echo "📦 Compilando aplicação..."
    ./compile-manual.sh
fi

# Limpar build anterior
rm -rf "$APP_DIR"
rm -f "$APPIMAGE_NAME"

# Criar estrutura AppDir
echo "📁 Criando estrutura AppImage..."
mkdir -p "$APP_DIR/usr/bin"
mkdir -p "$APP_DIR/usr/lib/gestao-projetos"
mkdir -p "$APP_DIR/usr/share/applications"
mkdir -p "$APP_DIR/usr/share/icons/hicolor/256x256/apps"
mkdir -p "$APP_DIR/usr/share/java"

# Copiar aplicação
echo "📋 Copiando arquivos da aplicação..."
cp -r build/classes/* "$APP_DIR/usr/lib/gestao-projetos/"
cp -r src/main/resources/* "$APP_DIR/usr/lib/gestao-projetos/"
cp -r lib/* "$APP_DIR/usr/lib/gestao-projetos/"
cp database/database-mysql.sql "$APP_DIR/usr/lib/gestao-projetos/"

# Baixar OpenJDK portátil se necessário
if [ ! -d "jdk-portable" ]; then
    echo "☕ Baixando OpenJDK portátil..."
    wget -q "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.21%2B9/OpenJDK11U-jdk_x64_linux_hotspot_11.0.21_9.tar.gz" -O jdk.tar.gz
    mkdir -p jdk-portable
    tar -xzf jdk.tar.gz -C jdk-portable --strip-components=1
    rm jdk.tar.gz
fi

# Copiar JDK portátil
cp -r jdk-portable "$APP_DIR/usr/lib/java"

# Criar script principal
cat > "$APP_DIR/usr/bin/gestao-projetos" << 'EOF'
#!/bin/bash

# Script principal do AppImage
APPDIR="$(dirname "$(readlink -f "$0")")/.."
JAVA_HOME="$APPDIR/usr/lib/java"
APP_HOME="$APPDIR/usr/lib/gestao-projetos"

# Configurar ambiente
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"
export CLASSPATH="$APP_HOME:$APP_HOME/*"

# Verificar se MySQL está instalado
if ! command -v mysql &> /dev/null; then
    zenity --error --text="MySQL não está instalado!\n\nPor favor, instale o MySQL:\nsudo apt install mysql-server mysql-client\n\nOu execute o instalador completo." 2>/dev/null || {
        echo "❌ ERRO: MySQL não está instalado!"
        echo "   Instale com: sudo apt install mysql-server mysql-client"
        echo "   Ou use o instalador completo: ./install.sh"
        exit 1
    }
fi

# Verificar se o banco está configurado
if ! mysql -u gestao_user -pgestao123 -e "USE gestao_projetos;" 2>/dev/null; then
    SETUP_MSG="Banco de dados não configurado!\n\nDeseja configurar automaticamente?\n\n(Requer senha de administrador)"
    
    if zenity --question --text="$SETUP_MSG" 2>/dev/null || read -p "Configurar banco? (y/n): " -n 1 -r && [[ $REPLY =~ ^[Yy]$ ]]; then
        echo
        echo "Configurando banco de dados..."
        
        # Tentar configurar banco
        mysql -e "
            CREATE DATABASE IF NOT EXISTS gestao_projetos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
            CREATE USER IF NOT EXISTS 'gestao_user'@'localhost' IDENTIFIED BY 'gestao123';
            GRANT ALL PRIVILEGES ON gestao_projetos.* TO 'gestao_user'@'localhost';
            FLUSH PRIVILEGES;
        " 2>/dev/null && {
            # Executar script SQL
            mysql -u gestao_user -pgestao123 < "$APP_HOME/database-mysql.sql" 2>/dev/null
            echo "✅ Banco configurado com sucesso!"
        } || {
            echo "❌ Falha na configuração. Execute manualmente:"
            echo "   sudo mysql < $APP_HOME/database-mysql.sql"
        }
    fi
fi

# Executar aplicação
cd "$APP_HOME"
exec "$JAVA_HOME/bin/java" \
    -cp "$CLASSPATH" \
    -Djava.awt.headless=false \
    -Dfile.encoding=UTF-8 \
    -Duser.dir="$APP_HOME" \
    com.gestao.projetos.Main "$@"
EOF

chmod +x "$APP_DIR/usr/bin/gestao-projetos"

# Criar AppRun
cat > "$APP_DIR/AppRun" << 'EOF'
#!/bin/bash
exec "$APPDIR/usr/bin/gestao-projetos" "$@"
EOF

chmod +x "$APP_DIR/AppRun"

# Criar arquivo .desktop
cat > "$APP_DIR/gestao-projetos.desktop" << 'EOF'
[Desktop Entry]
Name=Sistema de Gestão de Projetos
Comment=Sistema completo de gestão de projetos e equipes
Exec=gestao-projetos
Icon=gestao-projetos
Terminal=false
Type=Application
Categories=Development;Office;ProjectManagement;
StartupNotify=true
EOF

# Copiar desktop file para local padrão
cp "$APP_DIR/gestao-projetos.desktop" "$APP_DIR/usr/share/applications/"

# Criar ícone simples (pode ser substituído por um ícone real)
cat > "$APP_DIR/gestao-projetos.svg" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<svg width="256" height="256" viewBox="0 0 256 256" xmlns="http://www.w3.org/2000/svg">
  <rect width="256" height="256" fill="#2196F3" rx="32"/>
  <rect x="32" y="32" width="192" height="192" fill="#1976D2" rx="16"/>
  <rect x="64" y="64" width="128" height="16" fill="#FFFFFF" rx="8"/>
  <rect x="64" y="96" width="96" height="16" fill="#FFFFFF" rx="8"/>
  <rect x="64" y="128" width="112" height="16" fill="#FFFFFF" rx="8"/>
  <rect x="64" y="160" width="80" height="16" fill="#FFFFFF" rx="8"/>
  <circle cx="200" cy="72" r="16" fill="#4CAF50"/>
  <circle cx="200" cy="104" r="16" fill="#FF9800"/>
  <circle cx="200" cy="136" r="16" fill="#F44336"/>
</svg>
EOF

# Converter SVG para PNG (se disponível)
if command -v convert &> /dev/null; then
    convert "$APP_DIR/gestao-projetos.svg" "$APP_DIR/usr/share/icons/hicolor/256x256/apps/gestao-projetos.png"
    cp "$APP_DIR/usr/share/icons/hicolor/256x256/apps/gestao-projetos.png" "$APP_DIR/gestao-projetos.png"
else
    # Usar SVG como fallback
    cp "$APP_DIR/gestao-projetos.svg" "$APP_DIR/gestao-projetos.png"
fi

# Baixar appimagetool se necessário
if [ ! -f "appimagetool-x86_64.AppImage" ]; then
    echo "🔧 Baixando appimagetool..."
    wget -q "https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage"
    chmod +x appimagetool-x86_64.AppImage
fi

# Gerar AppImage
echo "🔨 Gerando AppImage..."
./appimagetool-x86_64.AppImage "$APP_DIR" "$APPIMAGE_NAME"

# Limpar arquivos temporários
rm -rf "$APP_DIR"

echo ""
echo "✅ AppImage criado com sucesso!"
echo "📦 Arquivo: $APPIMAGE_NAME"
echo ""
echo "🚀 Para executar:"
echo "   chmod +x $APPIMAGE_NAME"
echo "   ./$APPIMAGE_NAME"
echo ""
echo "📋 Características do AppImage:"
echo "   • Portátil - não requer instalação"
echo "   • Inclui Java 11 integrado"
echo "   • Funciona em qualquer distribuição Linux"
echo "   • Configura banco automaticamente (se MySQL estiver instalado)"
echo ""
echo "⚠️  Pré-requisitos:"
echo "   • MySQL Server deve estar instalado no sistema"
echo "   • Ambiente gráfico (X11 ou Wayland)"
echo ""

# Mostrar tamanho do arquivo
echo "📊 Tamanho do AppImage: $(du -h "$APPIMAGE_NAME" | cut -f1)"

# Tornar executável
chmod +x "$APPIMAGE_NAME"

echo ""
echo "🎯 AppImage pronto para distribuição!"
