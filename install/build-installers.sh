#!/bin/bash

# =====================================================
# GERADOR UNIVERSAL DE INSTALAVEIS
# Sistema de Gestão de Projetos - Linux
# =====================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}======================================================${NC}"
echo -e "${BLUE}  GERADOR DE INSTALÁVEIS - Sistema de Gestão de Projetos${NC}"
echo -e "${BLUE}======================================================${NC}"
echo

# Verificar se aplicação está compilada
if [ ! -d "build/classes" ]; then
    echo -e "${YELLOW}📦 Aplicação não compilada. Compilando...${NC}"
    ./compile-manual.sh
fi

echo -e "${CYAN}Escolha o tipo de instalável a gerar:${NC}"
echo
echo -e "${GREEN}1)${NC} Instalador Completo (.sh)"
echo -e "   • Instala MySQL + aplicação automaticamente"
echo -e "   • Configura serviço systemd"
echo -e "   • Requer privilégios de root"
echo -e "   • Melhor para ambientes corporativos"
echo
echo -e "${GREEN}2)${NC} Pacote DEB (.deb)"
echo -e "   • Para Ubuntu/Debian"
echo -e "   • Instala via package manager"
echo -e "   • Configuração automática pós-instalação"
echo -e "   • Fácil remoção via apt"
echo
echo -e "${GREEN}3)${NC} AppImage (.AppImage)"
echo -e "   • Executável portátil"
echo -e "   • Inclui Java integrado"
echo -e "   • Não requer instalação"
echo -e "   • Funciona em qualquer distribuição"
echo
echo -e "${GREEN}4)${NC} Instalador Auto-extraível (.run)"
echo -e "   • Um único arquivo executável"
echo -e "   • Extrai e instala automaticamente"
echo -e "   • Funciona offline"
echo -e "   • Inclui todas as dependências"
echo
echo -e "${GREEN}5)${NC} Todos os tipos"
echo -e "   • Gera todos os instaláveis"
echo -e "   • Para distribuição completa"
echo
echo -e "${GREEN}0)${NC} Sair"
echo

read -p "Escolha uma opção (1-5, 0 para sair): " choice

case $choice in
    1)
        echo -e "${BLUE}🚀 Gerando instalador completo...${NC}"
        if [ -f "install.sh" ]; then
            chmod +x install.sh
            echo -e "${GREEN}✅ Instalador completo disponível: install.sh${NC}"
            echo -e "${YELLOW}💡 Para usar: sudo ./install.sh${NC}"
        else
            echo -e "${RED}❌ Arquivo install.sh não encontrado${NC}"
        fi
        ;;
    
    2)
        echo -e "${BLUE}🚀 Gerando pacote DEB...${NC}"
        if [ -f "create-deb.sh" ]; then
            chmod +x create-deb.sh
            ./create-deb.sh
        else
            echo -e "${RED}❌ Script create-deb.sh não encontrado${NC}"
        fi
        ;;
    
    3)
        echo -e "${BLUE}🚀 Gerando AppImage...${NC}"
        if [ -f "create-appimage.sh" ]; then
            chmod +x create-appimage.sh
            ./create-appimage.sh
        else
            echo -e "${RED}❌ Script create-appimage.sh não encontrado${NC}"
        fi
        ;;
    
    4)
        echo -e "${BLUE}🚀 Gerando instalador auto-extraível...${NC}"
        if [ -f "create-installer.sh" ]; then
            chmod +x create-installer.sh
            ./create-installer.sh
        else
            echo -e "${RED}❌ Script create-installer.sh não encontrado${NC}"
        fi
        ;;
    
    5)
        echo -e "${BLUE}🚀 Gerando todos os instaláveis...${NC}"
        echo
        
        echo -e "${CYAN}📦 Gerando pacote DEB...${NC}"
        if [ -f "create-deb.sh" ]; then
            chmod +x create-deb.sh
            ./create-deb.sh
        fi
        
        echo
        echo -e "${CYAN}📦 Gerando AppImage...${NC}"
        if [ -f "create-appimage.sh" ]; then
            chmod +x create-appimage.sh
            ./create-appimage.sh
        fi
        
        echo
        echo -e "${CYAN}📦 Gerando instalador auto-extraível...${NC}"
        if [ -f "create-installer.sh" ]; then
            chmod +x create-installer.sh
            ./create-installer.sh
        fi
        
        echo
        echo -e "${GREEN}======================================================${NC}"
        echo -e "${GREEN}    ✅ TODOS OS INSTALÁVEIS GERADOS!${NC}"
        echo -e "${GREEN}======================================================${NC}"
        echo
        echo -e "${BLUE}📦 Arquivos gerados:${NC}"
        ls -lh *.deb *.AppImage *.run 2>/dev/null || echo "   (Alguns arquivos podem não ter sido gerados)"
        echo
        echo -e "${BLUE}📋 Resumo de uso:${NC}"
        echo -e "${GREEN}   • DEB:${NC} sudo dpkg -i gestao-projetos_*.deb"
        echo -e "${GREEN}   • AppImage:${NC} chmod +x *.AppImage && ./Sistema_Gestao_Projetos-*.AppImage"
        echo -e "${GREEN}   • Instalador:${NC} sudo ./gestao-projetos-installer.run"
        echo -e "${GREEN}   • Manual:${NC} sudo ./install.sh"
        ;;
    
    0)
        echo -e "${YELLOW}Saindo...${NC}"
        exit 0
        ;;
    
    *)
        echo -e "${RED}❌ Opção inválida!${NC}"
        exit 1
        ;;
esac

echo
echo -e "${GREEN}🎯 Processo concluído!${NC}"
echo -e "${BLUE}ℹ️  Para mais informações, consulte PROJETO_COMPLETO.md${NC}"
