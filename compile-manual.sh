#!/bin/bash

# Script de compilação manual (sem Maven)
# Para usar quando Maven não estiver disponível

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "=== Compilação Manual - Sistema de Gestão de Projetos ==="

# Criar diretórios
mkdir -p build/classes
mkdir -p build/lib
mkdir -p logs

# Baixar dependências (MySQL Connector)
MYSQL_JAR="mysql-connector-j-8.1.0.jar"
MYSQL_URL="https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.1.0/mysql-connector-j-8.1.0.jar"

if [ ! -f "lib/$MYSQL_JAR" ]; then
    echo "Baixando MySQL Connector..."
    wget -O "lib/$MYSQL_JAR" "$MYSQL_URL" 2>/dev/null || curl -o "lib/$MYSQL_JAR" "$MYSQL_URL"
    
    if [ $? -eq 0 ]; then
        echo "✅ MySQL Connector baixado"
    else
        echo "❌ Erro ao baixar MySQL Connector"
        echo "Baixe manualmente de: $MYSQL_URL"
        echo "E coloque em: lib/$MYSQL_JAR"
        exit 1
    fi
fi

# HikariCP
HIKARI_JAR="HikariCP-5.0.1.jar"
HIKARI_URL="https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar"

if [ ! -f "lib/$HIKARI_JAR" ]; then
    echo "Baixando HikariCP..."
    wget -O "lib/$HIKARI_JAR" "$HIKARI_URL" 2>/dev/null || curl -o "lib/$HIKARI_JAR" "$HIKARI_URL"
    
    if [ $? -eq 0 ]; then
        echo "✅ HikariCP baixado"
    else
        echo "❌ Erro ao baixar HikariCP"
    fi
fi

# SLF4J API
SLF4J_JAR="slf4j-api-2.0.7.jar"
SLF4J_URL="https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar"

if [ ! -f "lib/$SLF4J_JAR" ]; then
    echo "Baixando SLF4J API..."
    wget -O "lib/$SLF4J_JAR" "$SLF4J_URL" 2>/dev/null || curl -o "lib/$SLF4J_JAR" "$SLF4J_URL"
fi

# Logback Classic
LOGBACK_JAR="logback-classic-1.4.6.jar"
LOGBACK_URL="https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.4.6/logback-classic-1.4.6.jar"

if [ ! -f "lib/$LOGBACK_JAR" ]; then
    echo "Baixando Logback Classic..."
    wget -O "lib/$LOGBACK_JAR" "$LOGBACK_URL" 2>/dev/null || curl -o "lib/$LOGBACK_JAR" "$LOGBACK_URL"
fi

# Logback Core
LOGBACK_CORE_JAR="logback-core-1.4.6.jar"
LOGBACK_CORE_URL="https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.4.6/logback-core-1.4.6.jar"

if [ ! -f "lib/$LOGBACK_CORE_JAR" ]; then
    echo "Baixando Logback Core..."
    wget -O "lib/$LOGBACK_CORE_JAR" "$LOGBACK_CORE_URL" 2>/dev/null || curl -o "lib/$LOGBACK_CORE_JAR" "$LOGBACK_CORE_URL"
fi

# BCrypt
BCRYPT_JAR="jbcrypt-0.4.jar"
BCRYPT_URL="https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar"

if [ ! -f "lib/$BCRYPT_JAR" ]; then
    echo "Baixando BCrypt..."
    wget -O "lib/$BCRYPT_JAR" "$BCRYPT_URL" 2>/dev/null || curl -o "lib/$BCRYPT_JAR" "$BCRYPT_URL"
    
    if [ $? -eq 0 ]; then
        echo "✅ BCrypt baixado"
    else
        echo "❌ Erro ao baixar BCrypt"
    fi
fi

# Montar classpath
CLASSPATH="build/classes:src/main/resources"
for jar in lib/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

echo "Classpath: $CLASSPATH"

# Compilar código Java
echo "Compilando código Java..."
find src/main/java -name "*.java" > sources.txt

javac -cp "$CLASSPATH" -d build/classes @sources.txt

if [ $? -eq 0 ]; then
    echo "✅ Compilação concluída com sucesso"
    rm sources.txt
    
    echo ""
    echo "Para executar a aplicação:"
    echo "java -cp \"$CLASSPATH\" com.gestao.projetos.Main"
    echo ""
    echo "Ou use:"
    echo "./run-manual.sh execute"
    
else
    echo "❌ Erro na compilação"
    rm sources.txt
    exit 1
fi

# Executar se solicitado
if [ "$1" = "execute" ]; then
    echo ""
    echo "=== Executando Aplicação ==="
    java -cp "$CLASSPATH" com.gestao.projetos.Main
fi
