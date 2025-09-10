-- =====================================================
-- SCHEMA DDL - Sistema de Gestão de Projetos (MySQL)
-- Compatível com MySQL 8.0+
-- =====================================================

-- Criar database se não existir
CREATE DATABASE IF NOT EXISTS gestao_projetos 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE gestao_projetos;

-- =====================================================
-- TABELAS DE DOMÍNIO (ENUMS)
-- =====================================================

CREATE TABLE status_projeto (
    codigo VARCHAR(20) PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO status_projeto (codigo, descricao) VALUES
    ('PLANEJADO', 'Projeto em fase de planejamento'),
    ('EM_ANDAMENTO', 'Projeto em execução'),
    ('PAUSADO', 'Projeto temporariamente pausado'),
    ('CANCELADO', 'Projeto cancelado'),
    ('CONCLUIDO', 'Projeto finalizado com sucesso');

CREATE TABLE status_tarefa (
    codigo VARCHAR(20) PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO status_tarefa (codigo, descricao) VALUES
    ('NOVA', 'Tarefa criada, aguardando início'),
    ('EM_ANDAMENTO', 'Tarefa sendo executada'),
    ('BLOQUEADA', 'Tarefa impedida por dependências'),
    ('CONCLUIDA', 'Tarefa finalizada'),
    ('CANCELADA', 'Tarefa cancelada');

-- =====================================================
-- ENTIDADES PRINCIPAIS
-- =====================================================

-- Usuários do sistema
CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_usuario_email (email),
    INDEX idx_usuario_ativo (ativo)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Credenciais de autenticação
CREATE TABLE credencial (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hash VARCHAR(255) NOT NULL,
    salt VARCHAR(100) NOT NULL,
    usuario_id BIGINT NOT NULL,
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    INDEX idx_credencial_usuario (usuario_id)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Papéis de usuário no sistema
CREATE TABLE papel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) UNIQUE NOT NULL,
    descricao VARCHAR(200),
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_papel_nome (nome)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Permissões do sistema
CREATE TABLE permissao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chave VARCHAR(100) UNIQUE NOT NULL,
    descricao VARCHAR(200),
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_permissao_chave (chave)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Associação entre papéis e permissões
CREATE TABLE papel_permissao (
    papel_id BIGINT NOT NULL,
    permissao_id BIGINT NOT NULL,
    
    PRIMARY KEY (papel_id, permissao_id),
    FOREIGN KEY (papel_id) REFERENCES papel(id) ON DELETE CASCADE,
    FOREIGN KEY (permissao_id) REFERENCES permissao(id) ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Projetos
CREATE TABLE projeto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANEJADO',
    
    -- Datas
    data_inicio DATE,
    data_fim_prevista DATE,
    data_fim_real DATE,
    
    -- Relacionamentos
    gerente_id BIGINT,
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (status) REFERENCES status_projeto(codigo),
    FOREIGN KEY (gerente_id) REFERENCES usuario(id) ON DELETE SET NULL,
    
    INDEX idx_projeto_status (status),
    INDEX idx_projeto_gerente (gerente_id),
    INDEX idx_projeto_nome (nome)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Equipes de projetos
CREATE TABLE equipe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    papel_equipe VARCHAR(100), 
    projeto_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (projeto_id) REFERENCES projeto(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_equipe_projeto_usuario (projeto_id, usuario_id),
    INDEX idx_equipe_projeto (projeto_id),
    INDEX idx_equipe_usuario (usuario_id)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tarefas
CREATE TABLE tarefa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'NOVA',
    prioridade INT DEFAULT 1,
    
    -- Estimativas e tempo
    estimativa_horas DECIMAL(8,2),
    horas_trabalhadas DECIMAL(8,2) DEFAULT 0.00,
    
    -- Datas
    data_inicio DATE,
    data_fim_prevista DATE,
    data_fim_real DATE,
    
    -- Relacionamentos
    projeto_id BIGINT NOT NULL,
    responsavel_id BIGINT,
    criador_id BIGINT NOT NULL,
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (status) REFERENCES status_tarefa(codigo),
    FOREIGN KEY (projeto_id) REFERENCES projeto(id) ON DELETE CASCADE,
    FOREIGN KEY (responsavel_id) REFERENCES usuario(id) ON DELETE SET NULL,
    FOREIGN KEY (criador_id) REFERENCES usuario(id) ON DELETE RESTRICT,
    
    INDEX idx_tarefa_projeto (projeto_id),
    INDEX idx_tarefa_responsavel (responsavel_id),
    INDEX idx_tarefa_status (status),
    INDEX idx_tarefa_prioridade (prioridade)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- TABELAS DE AUDITORIA
-- =====================================================

-- Histórico imutável de alterações em tarefas
CREATE TABLE historico_tarefa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    campo VARCHAR(50) NOT NULL,
    valor_anterior TEXT,
    valor_novo TEXT,
    
    -- Relacionamentos
    tarefa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    
    FOREIGN KEY (tarefa_id) REFERENCES tarefa(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE RESTRICT,
    
    INDEX idx_historico_tarefa (tarefa_id),
    INDEX idx_historico_usuario (usuario_id),
    INDEX idx_historico_data (data_hora)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Log de tentativas de acesso
CREATE TABLE log_acesso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    acao VARCHAR(50) NOT NULL, 
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_origem VARCHAR(45),
    user_agent TEXT,
    sucesso BOOLEAN DEFAULT FALSE,
    
    -- Relacionamentos
    usuario_id BIGINT,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL,
    
    INDEX idx_log_acesso_usuario (usuario_id),
    INDEX idx_log_acesso_data (data_hora),
    INDEX idx_log_acesso_sucesso (sucesso)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Log de atividades dos usuários
CREATE TABLE log_atividade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entidade VARCHAR(100) NOT NULL, 
    entidade_id BIGINT NOT NULL,
    acao VARCHAR(50) NOT NULL,
    detalhes JSON,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    usuario_id BIGINT NOT NULL,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE RESTRICT,
    
    INDEX idx_log_atividade_usuario (usuario_id),
    INDEX idx_log_atividade_entidade (entidade, entidade_id),
    INDEX idx_log_atividade_data (data_hora)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- VIEWS DE CONSULTA
-- =====================================================

-- View para relatórios de projetos com métricas
CREATE VIEW view_projeto_metricas AS
SELECT 
    p.id,
    p.nome,
    p.descricao,
    p.status,
    p.data_inicio,
    p.data_fim_prevista,
    p.data_fim_real,
    u.nome AS gerente_nome,
    u.email AS gerente_email,
    
    -- Métricas de tarefas
    COUNT(t.id) AS total_tarefas,
    COUNT(CASE WHEN t.status = 'CONCLUIDA' THEN 1 END) AS tarefas_concluidas,
    COUNT(CASE WHEN t.status = 'EM_ANDAMENTO' THEN 1 END) AS tarefas_em_andamento,
    COUNT(CASE WHEN t.status = 'NOVA' THEN 1 END) AS tarefas_novas,
    
    -- Cálculo de progresso
    CASE 
        WHEN COUNT(t.id) > 0 
        THEN ROUND((COUNT(CASE WHEN t.status = 'CONCLUIDA' THEN 1 END) * 100.0 / COUNT(t.id)), 2)
        ELSE 0 
    END AS progresso_percentual,
    
    -- Métricas de equipe
    COUNT(DISTINCT e.usuario_id) AS total_membros_equipe,
    
    p.criado_em,
    p.atualizado_em
FROM projeto p
LEFT JOIN usuario u ON p.gerente_id = u.id
LEFT JOIN tarefa t ON p.id = t.projeto_id
LEFT JOIN equipe e ON p.id = e.projeto_id
GROUP BY p.id, p.nome, p.descricao, p.status, p.data_inicio, p.data_fim_prevista, 
         p.data_fim_real, u.nome, u.email, p.criado_em, p.atualizado_em;

-- View para tarefas com informações completas
CREATE VIEW view_tarefa_detalhada AS
SELECT 
    t.id,
    t.titulo,
    t.descricao,
    t.status,
    t.prioridade,
    t.estimativa_horas,
    t.horas_trabalhadas,
    t.data_inicio,
    t.data_fim_prevista,
    t.data_fim_real,
    
    -- Informações do projeto
    p.nome AS projeto_nome,
    p.status AS projeto_status,
    
    -- Informações do responsável
    r.nome AS responsavel_nome,
    r.email AS responsavel_email,
    
    -- Informações do criador
    c.nome AS criador_nome,
    c.email AS criador_email,
    
    -- Indicadores
    CASE 
        WHEN t.data_fim_prevista IS NOT NULL AND t.data_fim_prevista < CURDATE() AND t.status != 'CONCLUIDA' 
        THEN TRUE 
        ELSE FALSE 
    END AS atrasada,
    
    CASE 
        WHEN t.estimativa_horas IS NOT NULL AND t.estimativa_horas > 0 
        THEN ROUND((t.horas_trabalhadas * 100.0 / t.estimativa_horas), 2)
        ELSE 0 
    END AS progresso_horas_percentual,
    
    t.criado_em,
    t.atualizado_em
FROM tarefa t
INNER JOIN projeto p ON t.projeto_id = p.id
LEFT JOIN usuario r ON t.responsavel_id = r.id
INNER JOIN usuario c ON t.criador_id = c.id;

-- View para dashboard de usuários
CREATE VIEW view_usuario_dashboard AS
SELECT 
    u.id,
    u.nome,
    u.email,
    u.ativo,
    
    -- Contadores de projetos
    COUNT(DISTINCT p_gerente.id) AS projetos_gerenciados,
    COUNT(DISTINCT e.projeto_id) AS projetos_participando,
    
    -- Contadores de tarefas
    COUNT(DISTINCT t_responsavel.id) AS tarefas_responsavel,
    COUNT(DISTINCT t_criador.id) AS tarefas_criadas,
    COUNT(DISTINCT CASE WHEN t_responsavel.status = 'EM_ANDAMENTO' THEN t_responsavel.id END) AS tarefas_em_andamento,
    COUNT(DISTINCT CASE WHEN t_responsavel.status = 'CONCLUIDA' THEN t_responsavel.id END) AS tarefas_concluidas,
    
    -- Horas trabalhadas
    COALESCE(SUM(t_responsavel.horas_trabalhadas), 0) AS total_horas_trabalhadas,
    
    u.criado_em
FROM usuario u
LEFT JOIN projeto p_gerente ON u.id = p_gerente.gerente_id
LEFT JOIN equipe e ON u.id = e.usuario_id
LEFT JOIN tarefa t_responsavel ON u.id = t_responsavel.responsavel_id
LEFT JOIN tarefa t_criador ON u.id = t_criador.criador_id
WHERE u.ativo = TRUE
GROUP BY u.id, u.nome, u.email, u.ativo, u.criado_em;

-- =====================================================
-- DADOS INICIAIS
-- =====================================================

-- Papéis básicos do sistema
INSERT INTO papel (nome, descricao) VALUES
    ('ADMIN', 'Administrador do sistema com acesso total'),
    ('GERENTE', 'Gerente de projetos com acesso a gestão'),
    ('DESENVOLVEDOR', 'Desenvolvedor com acesso a tarefas'),
    ('ANALISTA', 'Analista com acesso a relatórios'),
    ('USUARIO', 'Usuário básico do sistema');

-- Permissões básicas
INSERT INTO permissao (chave, descricao) VALUES
    ('usuarios.criar', 'Criar novos usuários'),
    ('usuarios.editar', 'Editar usuários existentes'),
    ('usuarios.excluir', 'Excluir usuários'),
    ('usuarios.visualizar', 'Visualizar usuários'),
    ('projetos.criar', 'Criar novos projetos'),
    ('projetos.editar', 'Editar projetos'),
    ('projetos.excluir', 'Excluir projetos'),
    ('projetos.visualizar', 'Visualizar projetos'),
    ('tarefas.criar', 'Criar novas tarefas'),
    ('tarefas.editar', 'Editar tarefas'),
    ('tarefas.excluir', 'Excluir tarefas'),
    ('tarefas.visualizar', 'Visualizar tarefas'),
    ('relatorios.visualizar', 'Visualizar relatórios'),
    ('sistema.administrar', 'Administrar o sistema');

-- Associações papel-permissão para ADMIN
INSERT INTO papel_permissao (papel_id, permissao_id)
SELECT p.id, pe.id 
FROM papel p, permissao pe 
WHERE p.nome = 'ADMIN';

-- Associações papel-permissão para GERENTE
INSERT INTO papel_permissao (papel_id, permissao_id)
SELECT p.id, pe.id 
FROM papel p, permissao pe 
WHERE p.nome = 'GERENTE' 
AND pe.chave IN (
    'usuarios.visualizar', 'projetos.criar', 'projetos.editar', 'projetos.visualizar',
    'tarefas.criar', 'tarefas.editar', 'tarefas.visualizar', 'relatorios.visualizar'
);

-- Associações papel-permissão para DESENVOLVEDOR
INSERT INTO papel_permissao (papel_id, permissao_id)
SELECT p.id, pe.id 
FROM papel p, permissao pe 
WHERE p.nome = 'DESENVOLVEDOR' 
AND pe.chave IN (
    'projetos.visualizar', 'tarefas.criar', 'tarefas.editar', 'tarefas.visualizar'
);

-- Usuário administrador padrão (senha: admin123)
INSERT INTO usuario (nome, email) VALUES 
('Administrador', 'admin@gestao.com');

-- Credencial para o usuário admin (hash BCrypt para 'admin123')
INSERT INTO credencial (hash, salt, usuario_id) VALUES 
('$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'salt123', 1);

-- =====================================================
-- TRIGGERS (MySQL não suporta triggers PostgreSQL)
-- =====================================================

-- Trigger para auditoria automática de tarefas será implementado na aplicação Java

-- =====================================================
-- ÍNDICES ADICIONAIS PARA PERFORMANCE
-- =====================================================

-- Índices compostos para consultas frequentes
CREATE INDEX idx_tarefa_projeto_status ON tarefa(projeto_id, status);
CREATE INDEX idx_tarefa_responsavel_status ON tarefa(responsavel_id, status);
CREATE INDEX idx_equipe_projeto_papel ON equipe(projeto_id, papel_equipe);
CREATE INDEX idx_historico_tarefa_data ON historico_tarefa(tarefa_id, data_hora);

-- =====================================================
-- SUCESSO
-- =====================================================

SELECT 'Schema do Sistema de Gestão de Projetos criado com sucesso!' AS status;
SELECT 
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'gestao_projetos') AS tabelas_criadas,
    (SELECT COUNT(*) FROM information_schema.views WHERE table_schema = 'gestao_projetos') AS views_criadas;
