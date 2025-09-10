# Sistema de Gestão de Projetos e Equipes

Projeto desenvolvido para a disciplina PSC (Programação de Soluções Computacionais) utilizando Java com Swing e MySQL seguindo o padrão arquitetural MVC.

## 👥 Desenvolvedores
- **Fabio**
- **Gustavo** 
- **Mathews**

## 📋 Descrição

Sistema completo para gestão de projetos, equipes e tarefas com interface gráfica desenvolvida em Java Swing e banco de dados MySQL. O sistema permite o controle eficaz de projetos, gerenciamento de equipes, acompanhamento de tarefas e geração de relatórios de desempenho.

## 🏗️ Arquitetura

O projeto segue o padrão **MVC (Model-View-Controller)**:

- **Model**: Classes de entidade e regras de negócio
- **View**: Interface gráfica com Swing
- **Controller**: Controladores que fazem a comunicação entre View e Model
- **DAO**: Camada de acesso a dados
- **Service**: Camada de serviços com lógica de negócio
- **Util**: Classes utilitárias

## 🚀 Tecnologias Utilizadas

- **Java 11+**: Linguagem de programação
- **Swing**: Interface gráfica
- **MySQL 8.0+**: Banco de dados
- **Maven**: Gerenciamento de dependências
- **HikariCP**: Pool de conexões
- **Logback**: Sistema de logs
- **BCrypt**: Criptografia de senhas
- **JCalendar**: Componente de calendário

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/gestao/projetos/
│   │   ├── model/          # Entidades do sistema
│   │   ├── view/           # Interfaces gráficas (Swing)
│   │   ├── controller/     # Controladores MVC
│   │   ├── dao/            # Acesso a dados
│   │   ├── service/        # Serviços de negócio
│   │   ├── util/           # Utilitários
│   │   └── Main.java       # Classe principal
│   └── resources/
│       ├── database.properties  # Configurações do banco
│       └── logback.xml         # Configurações de log
├── database/
│   ├── database.sql        # Script de criação do banco
│   ├── classes.puml        # Diagrama de classes
│   └── der.puml           # Diagrama ER
├── logs/                  # Arquivos de log
├── pom.xml               # Configuração Maven
└── run.sh               # Script de execução

```

## 🎯 Funcionalidades

### ✅ Implementado
- **Gestão de Usuários**
  - Cadastro, edição e exclusão
  - Validação de dados
  - Pesquisa e filtros
  - Status ativo/inativo

### 🚧 Em Desenvolvimento
- **Gestão de Projetos**
  - CRUD completo
  - Controle de status
  - Associação com gerentes
  
- **Gestão de Equipes**
  - Formação de equipes
  - Gerenciamento de membros
  
- **Gestão de Tarefas**
  - Criação e acompanhamento
  - Priorização
  - Controle de prazos
  
- **Relatórios e Dashboard**
  - Indicadores de desempenho
  - Gráficos e estatísticas

## 💾 Banco de Dados

O sistema utiliza MySQL com as seguintes entidades principais:

- **usuario**: Dados dos usuários do sistema
- **projeto**: Informações dos projetos
- **equipe**: Dados das equipes
- **tarefa**: Tarefas dos projetos
- **credencial**: Autenticação de usuários
- **papel/permissao**: Sistema de controle de acesso

## 🔧 Configuração e Instalação

### Pré-requisitos
- Java 11 ou superior
- Maven 3.6 ou superior
- MySQL 8.0 ou superior

### Instalação Rápida

1. **Clone o repositório**
```bash
git clone https://github.com/CodebreakerBR/ProjetoA3-PSC_Fabio_Gustavo_Mathews.git
cd ProjetoA3-PSC_Fabio_Gustavo_Mathews
```

2. **Execute o script de configuração**
```bash
sudo apt update
sudo apt install default-jdk maven mysql-server

sudo mysql -e "SOURCE /home/gustavo/Documents/facul/ProjetoA3-PSC_Fabio_Gustavo_Mathews/database/database-mysql.sql;"
sudo mysql -e "USE gestao_projetos; SHOW TABLES;"
sudo mysql -e "CREATE USER 'gestao_user'@'localhost' IDENTIFIED BY 'gestao123'; GRANT ALL PRIVILEGES ON gestao_projetos.* TO 'gestao_user'@'localhost'; FLUSH PRIVILEGES;"

./compile-manual.sh
./run-manual.sh
```

3. **Siga as instruções do menu**
   - Configurar banco de dados
   - Compilar projeto
   - Executar aplicação

### Configurar conexão

Edite `src/main/resources/database.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/gestao_projetos?useSSL=false&serverTimezone=UTC
db.username=seu_usuario
db.password=sua_senha
```

## 🖥️ Interface do Sistema

### Tela Principal
- Menu completo com todas as funcionalidades
- Barra de ferramentas para acesso rápido
- Área de trabalho com janelas internas (MDI)
- Barra de status com informações do sistema

### Gestão de Usuários
- Lista completa de usuários
- Formulário de cadastro/edição
- Pesquisa por nome ou email
- Controle de status ativo/inativo

## 📊 Logs e Monitoramento

O sistema gera logs detalhados em:
- `logs/application.log`: Log geral da aplicação
- `logs/error.log`: Log específico de erros

Configuração personalizada no `logback.xml`.

## 📦 Build

Gerar JAR executável:
```bash
mvn package
java -jar target/projetos-1.0.0.jar
```

## 🐛 Troubleshooting

### Problemas de Conexão
- Verifique se o MySQL está rodando
- Confirme as credenciais em `database.properties`
- Teste conectividade: `mysql -u usuario -p`

### Problemas de Compilação
- Verifique versão do Java: `java -version`
- Limpe o projeto: `mvn clean`
- Reinstale dependências: `mvn dependency:resolve`

### Problemas de Interface
- Verifique se está usando Java 11+
- Teste diferentes Look and Feel
- Verifique logs em `logs/error.log`

## 📝 Próximas Implementações

1. **Sistema de Autenticação**: Login e controle de acesso
2. **Gestão Completa de Projetos**: CRUD, relatórios, dashboard
3. **Gestão de Equipes**: Formação, papéis, alocações
4. **Gestão de Tarefas**: Kanban, dependências, comentários
5. **Relatórios Avançados**: PDF, Excel, gráficos
6. **API REST**: Para integração externa
7. **Testes Automatizados**: Cobertura completa

---

**Última atualização**: Setembro 2025  
**Status**: Em desenvolvimento ativo
Repositório criado para entrega do trabalho de grupo da nota A3
