package com.gestao.projetos.service;

import com.gestao.projetos.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Serviço para coleta de dados estatísticos do dashboard
 */
public class DashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    // ========== ESTATÍSTICAS DE PROJETOS ==========
    
    /**
     * Conta o total de projetos
     */
    public long contarTotalProjetos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM projeto";
        return executarContagem(sql);
    }
    
    /**
     * Conta projetos em andamento
     */
    public long contarProjetosEmAndamento() throws SQLException {
        String sql = "SELECT COUNT(*) FROM projeto WHERE status = 'EM_ANDAMENTO'";
        return executarContagem(sql);
    }
    
    /**
     * Conta projetos concluídos
     */
    public long contarProjetosConcluidos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM projeto WHERE status = 'CONCLUIDO'";
        return executarContagem(sql);
    }
    
    /**
     * Conta projetos atrasados (data fim prevista < data atual e status != concluído)
     */
    public long contarProjetosAtrasados() throws SQLException {
        String sql = "SELECT COUNT(*) FROM projeto WHERE data_fim_prevista < CURRENT_DATE() AND status NOT IN ('CONCLUIDO', 'CANCELADO')";
        return executarContagem(sql);
    }
    
    /**
     * Conta projetos planejados
     */
    public long contarProjetosPlanejados() throws SQLException {
        String sql = "SELECT COUNT(*) FROM projeto WHERE status = 'PLANEJADO'";
        return executarContagem(sql);
    }
    
    /**
     * Conta projetos pausados
     */
    public long contarProjetosPausados() throws SQLException {
        String sql = "SELECT COUNT(*) FROM projeto WHERE status = 'PAUSADO'";
        return executarContagem(sql);
    }
    
    /**
     * Conta projetos cancelados
     */
    public long contarProjetosCancelados() throws SQLException {
        String sql = "SELECT COUNT(*) FROM projeto WHERE status = 'CANCELADO'";
        return executarContagem(sql);
    }
    
    // ========== ESTATÍSTICAS DE TAREFAS ==========
    
    /**
     * Conta o total de tarefas
     */
    public long contarTotalTarefas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tarefa";
        return executarContagem(sql);
    }
    
    /**
     * Conta tarefas em andamento
     */
    public long contarTarefasEmAndamento() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tarefa WHERE status = 'EM_ANDAMENTO'";
        return executarContagem(sql);
    }
    
    /**
     * Conta tarefas concluídas
     */
    public long contarTarefasConcluidas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tarefa WHERE status = 'CONCLUIDA'";
        return executarContagem(sql);
    }
    
    /**
     * Conta tarefas atrasadas (data fim prevista < data atual e status != concluída)
     */
    public long contarTarefasAtrasadas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tarefa WHERE data_fim_prevista < CURRENT_DATE() AND status NOT IN ('CONCLUIDA', 'CANCELADA')";
        return executarContagem(sql);
    }
    
    /**
     * Conta tarefas novas
     */
    public long contarTarefasNovas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tarefa WHERE status = 'NOVA'";
        return executarContagem(sql);
    }
    
    /**
     * Conta tarefas bloqueadas
     */
    public long contarTarefasBloqueadas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tarefa WHERE status = 'BLOQUEADA'";
        return executarContagem(sql);
    }
    
    /**
     * Conta tarefas canceladas
     */
    public long contarTarefasCanceladas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tarefa WHERE status = 'CANCELADA'";
        return executarContagem(sql);
    }
    
    // ========== ESTATÍSTICAS DE USUÁRIOS ==========
    
    /**
     * Conta o total de usuários
     */
    public long contarTotalUsuarios() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario";
        return executarContagem(sql);
    }
    
    /**
     * Conta usuários ativos
     */
    public long contarUsuariosAtivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE ativo = true";
        return executarContagem(sql);
    }
    
    /**
     * Conta usuários inativos
     */
    public long contarUsuariosInativos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE ativo = false";
        return executarContagem(sql);
    }
    
    /**
     * Conta administradores
     */
    public long contarAdministradores() throws SQLException {
        String sql = """
            SELECT COUNT(DISTINCT u.id) FROM usuario u 
            INNER JOIN usuario_papel up ON u.id = up.usuario_id 
            INNER JOIN papel p ON up.papel_id = p.id 
            WHERE p.nome = 'ADMINISTRADOR' AND u.ativo = true
            """;
        return executarContagem(sql);
    }
    
    /**
     * Conta gerentes
     */
    public long contarGerentes() throws SQLException {
        String sql = """
            SELECT COUNT(DISTINCT u.id) FROM usuario u 
            INNER JOIN usuario_papel up ON u.id = up.usuario_id 
            INNER JOIN papel p ON up.papel_id = p.id 
            WHERE p.nome = 'GERENTE' AND u.ativo = true
            """;
        return executarContagem(sql);
    }
    
    /**
     * Conta colaboradores
     */
    public long contarColaboradores() throws SQLException {
        String sql = """
            SELECT COUNT(DISTINCT u.id) FROM usuario u 
            INNER JOIN usuario_papel up ON u.id = up.usuario_id 
            INNER JOIN papel p ON up.papel_id = p.id 
            WHERE p.nome = 'COLABORADOR' AND u.ativo = true
            """;
        return executarContagem(sql);
    }
    
    // ========== ESTATÍSTICAS DE EQUIPES ==========
    
    /**
     * Conta o total de equipes
     */
    public long contarTotalEquipes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM equipe";
        return executarContagem(sql);
    }
    
    /**
     * Conta equipes ativas (com pelo menos um membro ativo)
     */
    public long contarEquipesAtivas() throws SQLException {
        String sql = """
            SELECT COUNT(DISTINCT e.id) FROM equipe e 
            INNER JOIN equipe_membro em ON e.id = em.equipe_id 
            INNER JOIN usuario u ON em.usuario_id = u.id 
            WHERE u.ativo = true AND em.ativo = true
            """;
        return executarContagem(sql);
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    /**
     * Executa uma query de contagem
     */
    private long executarContagem(String sql) throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 0L;
            
        } catch (SQLException e) {
            logger.error("Erro ao executar contagem: {}", sql, e);
            throw e;
        }
    }
    
    /**
     * Executa uma query de contagem com parâmetro
     */
    private long executarContagem(String sql, Object... params) throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Definir parâmetros
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
                return 0L;
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao executar contagem parametrizada: {}", sql, e);
            throw e;
        }
    }
    
    // ========== ESTATÍSTICAS ADICIONAIS ==========
    
    /**
     * Obtém a média de horas trabalhadas por tarefa
     */
    public double obterMediaHorasTrabalhadasPorTarefa() throws SQLException {
        String sql = "SELECT COALESCE(AVG(horas_trabalhadas), 0) FROM tarefa WHERE horas_trabalhadas IS NOT NULL";
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
            return 0.0;
            
        } catch (SQLException e) {
            logger.error("Erro ao obter média de horas trabalhadas", e);
            throw e;
        }
    }
    
    /**
     * Obtém a média de tarefas por projeto
     */
    public double obterMediaTarefasPorProjeto() throws SQLException {
        String sql = """
            SELECT COALESCE(AVG(t.quantidade), 0) FROM (
                SELECT COUNT(*) as quantidade FROM tarefa GROUP BY projeto_id
            ) t
            """;
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
            return 0.0;
            
        } catch (SQLException e) {
            logger.error("Erro ao obter média de tarefas por projeto", e);
            throw e;
        }
    }
    
    /**
     * Obtém o número de projetos que vão atrasar (com base na data atual)
     */
    public long contarProjetosComRiscoAtraso() throws SQLException {
        // Projetos que vão terminar em menos de 7 dias e ainda não estão concluídos
        String sql = """
            SELECT COUNT(*) FROM projeto 
            WHERE data_fim_prevista BETWEEN CURRENT_DATE() AND DATE_ADD(CURRENT_DATE(), INTERVAL 7 DAY)
            AND status NOT IN ('CONCLUIDO', 'CANCELADO')
            """;
        return executarContagem(sql);
    }
    
    /**
     * Conta usuários por cargo
     */
    public long contarUsuariosPorCargo(String cargo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE cargo = ? AND ativo = true";
        return executarContagem(sql, cargo);
    }
    
    /**
     * Obtém estatísticas de produtividade por usuário
     */
    public double obterTaxaConclusaoTarefasPorUsuario(Long usuarioId) throws SQLException {
        String sql = """
            SELECT 
                CASE 
                    WHEN COUNT(*) = 0 THEN 0 
                    ELSE (COUNT(CASE WHEN status = 'CONCLUIDA' THEN 1 END) * 100.0 / COUNT(*))
                END as taxa
            FROM tarefa 
            WHERE responsavel_id = ?
            """;
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, usuarioId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("taxa");
                }
                return 0.0;
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao obter taxa de conclusão para usuário: {}", usuarioId, e);
            throw e;
        }
    }
    
    /**
     * Verifica se existem dados no sistema
     */
    public boolean possuiDados() throws SQLException {
        return contarTotalProjetos() > 0 || contarTotalTarefas() > 0 || contarTotalUsuarios() > 0;
    }
    
    /**
     * Obtém data da última atualização (baseada no timestamp mais recente)
     */
    public String obterDataUltimaAtualizacao() throws SQLException {
        String sql = """
            SELECT MAX(ultima_atualizacao) as ultima_data FROM (
                SELECT MAX(atualizado_em) as ultima_atualizacao FROM projeto
                UNION ALL
                SELECT MAX(atualizado_em) as ultima_atualizacao FROM tarefa
                UNION ALL
                SELECT MAX(atualizado_em) as ultima_atualizacao FROM usuario
            ) t
            """;
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                java.sql.Timestamp timestamp = resultSet.getTimestamp("ultima_data");
                if (timestamp != null) {
                    return timestamp.toString();
                }
            }
            return "Dados não disponíveis";
            
        } catch (SQLException e) {
            logger.error("Erro ao obter data da última atualização", e);
            return "Erro ao obter data";
        }
    }
}