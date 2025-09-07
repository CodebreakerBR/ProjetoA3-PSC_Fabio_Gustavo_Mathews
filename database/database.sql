-- =====================================================
-- SCHEMA DDL - Sistema de Gestão de Projetos
-- Gerado a partir do DER transformado do UML de Classes
-- =====================================================

-- Configurações iniciais
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

-- =====================================================
-- TABELAS DE DOMÍNIO (ENUMS)
-- =====================================================

CREATE TABLE status_projeto (
    codigo VARCHAR(20) PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL
);

INSERT INTO status_projeto (codigo, descricao) VALUES
    ('PLANEJADO', 'Projeto em fase de planejamento'),
    ('EM_ANDAMENTO', 'Projeto em execução'),
    ('PAUSADO', 'Projeto temporariamente pausado'),
    ('CANCELADO', 'Projeto cancelado'),
    ('CONCLUIDO', 'Projeto finalizado com sucesso');

CREATE TABLE status_tarefa (
    codigo VARCHAR(20) PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL
);

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
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Credenciais de autenticação
CREATE TABLE credencial (
    id BIGSERIAL PRIMARY KEY,
    hash VARCHAR(255) NOT NULL,
    salt VARCHAR(100) NOT NULL,
    ultima_troca TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT UNIQUE NOT NULL,
    
    CONSTRAINT fk_credencial_usuario 
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) 
        ON DELETE CASCADE
);

-- Papéis do sistema (RBAC)
CREATE TABLE papel (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) UNIQUE NOT NULL,
    descricao VARCHAR(200),
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissões específicas
CREATE TABLE permissao (
    id BIGSERIAL PRIMARY KEY,
    chave VARCHAR(100) UNIQUE NOT NULL,
    descricao VARCHAR(200),
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Associação N:N Usuario-Papel
CREATE TABLE usuario_papel (
    usuario_id BIGINT NOT NULL,
    papel_id BIGINT NOT NULL,
    atribuido_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (usuario_id, papel_id),
    
    CONSTRAINT fk_usuario_papel_usuario 
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_usuario_papel_papel 
        FOREIGN KEY (papel_id) REFERENCES papel(id) 
        ON DELETE CASCADE
);

-- Associação N:N Papel-Permissao
CREATE TABLE papel_permissao (
    papel_id BIGINT NOT NULL,
    permissao_id BIGINT NOT NULL,
    atribuido_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (papel_id, permissao_id),
    
    CONSTRAINT fk_papel_permissao_papel 
        FOREIGN KEY (papel_id) REFERENCES papel(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_papel_permissao_permissao 
        FOREIGN KEY (permissao_id) REFERENCES permissao(id) 
        ON DELETE CASCADE
);

-- Equipes de trabalho
CREATE TABLE equipe (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    ativa BOOLEAN DEFAULT TRUE,
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Membros das equipes (N:N Usuario-Equipe)
CREATE TABLE membro_equipe (
    id BIGSERIAL PRIMARY KEY,
    papel VARCHAR(50) NOT NULL, -- Dev, QA, PO, etc.
    desde DATE DEFAULT CURRENT_DATE,
    ativo BOOLEAN DEFAULT TRUE,
    usuario_id BIGINT NOT NULL,
    equipe_id BIGINT NOT NULL,
    
    CONSTRAINT fk_membro_equipe_usuario 
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_membro_equipe_equipe 
        FOREIGN KEY (equipe_id) REFERENCES equipe(id) 
        ON DELETE CASCADE,
        
    -- Evita duplicação de membro ativo na mesma equipe
    UNIQUE (usuario_id, equipe_id, ativo) 
        DEFERRABLE INITIALLY DEFERRED
);

-- Projetos
CREATE TABLE projeto (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    data_inicio_prevista DATE,
    data_fim_prevista DATE,
    data_inicio_real DATE,
    data_fim_real DATE,
    status VARCHAR(20) DEFAULT 'PLANEJADO',
    gerente_id BIGINT NOT NULL,
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_projeto_gerente 
        FOREIGN KEY (gerente_id) REFERENCES usuario(id),
    CONSTRAINT fk_projeto_status 
        FOREIGN KEY (status) REFERENCES status_projeto(codigo),
        
    -- Validações de data
    CONSTRAINT ck_projeto_datas_previstas 
        CHECK (data_fim_prevista IS NULL OR data_inicio_prevista IS NULL OR data_fim_prevista >= data_inicio_prevista),
    CONSTRAINT ck_projeto_datas_reais 
        CHECK (data_fim_real IS NULL OR data_inicio_real IS NULL OR data_fim_real >= data_inicio_real)
);

-- Alocação de equipes em projetos (N:N)
CREATE TABLE projeto_equipe (
    id BIGSERIAL PRIMARY KEY,
    papel_equipe VARCHAR(100), -- Equipe Líder, Apoio, etc.
    projeto_id BIGINT NOT NULL,
    equipe_id BIGINT NOT NULL,
    alocado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_projeto_equipe_projeto 
        FOREIGN KEY (projeto_id) REFERENCES projeto(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_projeto_equipe_equipe 
        FOREIGN KEY (equipe_id) REFERENCES equipe(id) 
        ON DELETE CASCADE,
        
    -- Evita duplicação de equipe no mesmo projeto
    UNIQUE (projeto_id, equipe_id)
);

-- Tarefas dos projetos
CREATE TABLE tarefa (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    status VARCHAR(20) DEFAULT 'NOVA',
    prioridade INTEGER DEFAULT 3 CHECK (prioridade BETWEEN 1 AND 5),
    data_inicio_prevista DATE,
    data_fim_prevista DATE,
    data_inicio_real DATE,
    data_fim_real DATE,
    projeto_id BIGINT NOT NULL,
    responsavel_id BIGINT,
    
    -- Auditoria
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_tarefa_projeto 
        FOREIGN KEY (projeto_id) REFERENCES projeto(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_tarefa_responsavel 
        FOREIGN KEY (responsavel_id) REFERENCES usuario(id),
    CONSTRAINT fk_tarefa_status 
        FOREIGN KEY (status) REFERENCES status_tarefa(codigo),
        
    -- Validações de data
    CONSTRAINT ck_tarefa_datas_previstas 
        CHECK (data_fim_prevista IS NULL OR data_inicio_prevista IS NULL OR data_fim_prevista >= data_inicio_prevista),
    CONSTRAINT ck_tarefa_datas_reais 
        CHECK (data_fim_real IS NULL OR data_inicio_real IS NULL OR data_fim_real >= data_inicio_real)
);

-- =====================================================
-- TABELAS DE AUDITORIA
-- =====================================================

-- Histórico de alterações das tarefas
CREATE TABLE historico_tarefa (
    id BIGSERIAL PRIMARY KEY,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    campo_alterado VARCHAR(100) NOT NULL,
    valor_anterior TEXT,
    valor_novo TEXT,
    motivo VARCHAR(500),
    tarefa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    
    CONSTRAINT fk_historico_tarefa_tarefa 
        FOREIGN KEY (tarefa_id) REFERENCES tarefa(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_historico_tarefa_usuario 
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- Log de acessos (autenticação)
CREATE TABLE log_acesso (
    id BIGSERIAL PRIMARY KEY,
    acao VARCHAR(50) NOT NULL, -- LOGIN, LOGOUT, FALHA_LOGIN
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip VARCHAR(45), -- Suporta IPv4 e IPv6
    sucesso BOOLEAN NOT NULL,
    usuario_id BIGINT,
    
    CONSTRAINT fk_log_acesso_usuario 
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) 
        ON DELETE SET NULL
);

-- Log de atividades do sistema
CREATE TABLE log_atividade (
    id BIGSERIAL PRIMARY KEY,
    entidade VARCHAR(100) NOT NULL, -- Usuario, Projeto, Tarefa, etc.
    entidade_id BIGINT,
    acao VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    detalhes TEXT,
    usuario_id BIGINT,
    
    CONSTRAINT fk_log_atividade_usuario 
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) 
        ON DELETE SET NULL
);

-- =====================================================
-- ÍNDICES PARA PERFORMANCE
-- =====================================================

-- Usuários
CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_usuario_ativo ON usuario(ativo);

-- Projetos
CREATE INDEX idx_projeto_gerente ON projeto(gerente_id);
CREATE INDEX idx_projeto_status ON projeto(status);
CREATE INDEX idx_projeto_datas ON projeto(data_inicio_prevista, data_fim_prevista);

-- Tarefas
CREATE INDEX idx_tarefa_projeto ON tarefa(projeto_id);
CREATE INDEX idx_tarefa_responsavel ON tarefa(responsavel_id);
CREATE INDEX idx_tarefa_status ON tarefa(status);
CREATE INDEX idx_tarefa_prioridade ON tarefa(prioridade);

-- Logs (particionamento recomendado em produção)
CREATE INDEX idx_log_acesso_usuario_data ON log_acesso(usuario_id, data_hora);
CREATE INDEX idx_log_atividade_entidade ON log_atividade(entidade, entidade_id);
CREATE INDEX idx_historico_tarefa_data ON historico_tarefa(tarefa_id, data_hora);

-- =====================================================
-- TRIGGERS PARA AUDITORIA AUTOMÁTICA
-- =====================================================

-- Função para atualizar timestamp de modificação
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger em tabelas relevantes
CREATE TRIGGER set_timestamp_usuario
    BEFORE UPDATE ON usuario
    FOR EACH ROW
    EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_projeto
    BEFORE UPDATE ON projeto
    FOR EACH ROW
    EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_tarefa
    BEFORE UPDATE ON tarefa
    FOR EACH ROW
    EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_equipe
    BEFORE UPDATE ON equipe
    FOR EACH ROW
    EXECUTE FUNCTION trigger_set_timestamp();

-- =====================================================
-- DADOS INICIAIS (SEEDS)
-- =====================================================

-- Papéis padrão
INSERT INTO papel (nome, descricao) VALUES
    ('ADMIN', 'Administrador do sistema'),
    ('GERENTE', 'Gerente de projetos'),
    ('COLABORADOR', 'Colaborador padrão');

-- Permissões básicas
INSERT INTO permissao (chave, descricao) VALUES
    ('USUARIO_CRUD', 'Gerenciar usuários'),
    ('PROJETO_CRUD', 'Gerenciar projetos'),
    ('TAREFA_CRUD', 'Gerenciar tarefas'),
    ('EQUIPE_CRUD', 'Gerenciar equipes'),
    ('RELATORIO_VIEW', 'Visualizar relatórios');

-- Associações papel-permissão
INSERT INTO papel_permissao (papel_id, permissao_id) 
SELECT p.id, pe.id 
FROM papel p, permissao pe 
WHERE p.nome = 'ADMIN'; -- Admin tem todas as permissões

INSERT INTO papel_permissao (papel_id, permissao_id) 
SELECT p.id, pe.id 
FROM papel p, permissao pe 
WHERE p.nome = 'GERENTE' 
  AND pe.chave IN ('PROJETO_CRUD', 'TAREFA_CRUD', 'RELATORIO_VIEW');

-- =====================================================
-- COMENTÁRIOS PARA DOCUMENTAÇÃO
-- =====================================================

COMMENT ON TABLE usuario IS 'Usuários do sistema de gestão de projetos';
COMMENT ON TABLE projeto IS 'Projetos gerenciados no sistema';
COMMENT ON TABLE tarefa IS 'Tarefas pertencentes aos projetos';
COMMENT ON TABLE historico_tarefa IS 'Auditoria imutável de alterações nas tarefas';
COMMENT ON TABLE log_acesso IS 'Log de tentativas de acesso ao sistema';
COMMENT ON TABLE log_atividade IS 'Log de atividades realizadas pelos usuários';

-- =====================================================
-- CONSTRAINTS DE REGRAS DE NEGÓCIO
-- =====================================================

-- Impede criação de tarefas em projetos cancelados
CREATE OR REPLACE FUNCTION check_projeto_ativo_para_tarefa()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM projeto 
        WHERE id = NEW.projeto_id 
          AND status = 'CANCELADO'
    ) THEN
        RAISE EXCEPTION 'Não é possível criar/editar tarefas em projeto cancelado';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_projeto_ativo
    BEFORE INSERT OR UPDATE ON tarefa
    FOR EACH ROW
    EXECUTE FUNCTION check_projeto_ativo_para_tarefa();

-- =====================================================
-- VIEWS ÚTEIS PARA CONSULTAS
-- =====================================================

-- View para listar usuários com seus papéis
CREATE VIEW v_usuario_papeis AS
SELECT 
    u.id,
    u.nome,
    u.email,
    u.ativo,
    STRING_AGG(p.nome, ', ') as papeis
FROM usuario u
LEFT JOIN usuario_papel up ON u.id = up.usuario_id
LEFT JOIN papel p ON up.papel_id = p.id
GROUP BY u.id, u.nome, u.email, u.ativo;

-- View para dashboard de projetos
CREATE VIEW v_dashboard_projetos AS
SELECT 
    p.id,
    p.nome,
    p.status,
    u.nome as gerente,
    COUNT(t.id) as total_tarefas,
    COUNT(CASE WHEN t.status = 'CONCLUIDA' THEN 1 END) as tarefas_concluidas,
    ROUND(
        COUNT(CASE WHEN t.status = 'CONCLUIDA' THEN 1 END) * 100.0 / 
        NULLIF(COUNT(t.id), 0), 2
    ) as percentual_conclusao
FROM projeto p
JOIN usuario u ON p.gerente_id = u.id
LEFT JOIN tarefa t ON p.id = t.projeto_id
GROUP BY p.id, p.nome, p.status, u.nome;

-- =====================================================
-- FUNÇÕES AUXILIARES
-- =====================================================

-- Função para calcular dias úteis entre datas
CREATE OR REPLACE FUNCTION dias_uteis(data_inicio DATE, data_fim DATE)
RETURNS INTEGER AS $$
DECLARE
    dias INTEGER := 0;
    data_atual DATE := data_inicio;
BEGIN
    WHILE data_atual <= data_fim LOOP
        -- Ignora sábado (6) e domingo (0)
        IF EXTRACT(DOW FROM data_atual) NOT IN (0, 6) THEN
            dias := dias + 1;
        END IF;
        data_atual := data_atual + 1;
    END LOOP;
    
    RETURN dias;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- FINALIZAÇÕES
-- =====================================================

-- Análise das tabelas criadas
ANALYZE;

-- Mensagem de sucesso
DO $$
BEGIN
    RAISE NOTICE 'Schema do Sistema de Gestão de Projetos criado com sucesso!';
    RAISE NOTICE 'Tabelas: %, Views: %, Triggers: %', 
        (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'),
        (SELECT COUNT(*) FROM information_schema.views WHERE table_schema = 'public'),
        (SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema = 'public');
END;
$$;
