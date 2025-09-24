package com.gestao.projetos.dao;

import com.gestao.projetos.model.Projeto;
import com.gestao.projetos.model.StatusProjeto;
import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.model.Equipe;
import com.gestao.projetos.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operações relacionadas à entidade Projeto
 */
public class ProjetoDAO implements BaseDAO<Projeto, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjetoDAO.class);
    private final UsuarioDAO usuarioDAO;
    private Long id;
    
    // Queries SQL
    private static final String INSERT_SQL = 
        "INSERT INTO projeto (nome, descricao, data_inicio, data_fim_prevista, " +
        "data_fim_real, status, gerente_id, criado_em, atualizado_em) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE projeto SET nome = ?, descricao = ?, data_inicio = ?, " +
        "data_fim_prevista = ?, data_fim_real = ?, status = ?, " +
        "gerente_id = ?, atualizado_em = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM projeto WHERE id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT p.id, p.nome, p.descricao, p.data_inicio, p.data_fim_prevista, " +
        "p.data_fim_real, p.status, p.gerente_id, p.criado_em, p.atualizado_em, " +
        "u.nome as gerente_nome, u.email as gerente_email, u.ativo as gerente_ativo " +
        "FROM projeto p " +
        "LEFT JOIN usuario u ON p.gerente_id = u.id " +
        "WHERE p.id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT p.id, p.nome, p.descricao, p.data_inicio, p.data_fim_prevista, " +
        "p.data_fim_real, p.status, p.gerente_id, p.criado_em, p.atualizado_em, " +
        "u.nome as gerente_nome, u.email as gerente_email, u.ativo as gerente_ativo " +
        "FROM projeto p " +
        "LEFT JOIN usuario u ON p.gerente_id = u.id " +
        "ORDER BY p.nome";
    
    private static final String SELECT_BY_GERENTE_SQL = 
        "SELECT p.id, p.nome, p.descricao, p.data_inicio, p.data_fim_prevista, " +
        "p.data_fim_real, p.status, p.gerente_id, p.criado_em, p.atualizado_em, " +
        "u.nome as gerente_nome, u.email as gerente_email, u.ativo as gerente_ativo " +
        "FROM projeto p " +
        "LEFT JOIN usuario u ON p.gerente_id = u.id " +
        "WHERE p.gerente_id = ? " +
        "ORDER BY p.nome";
    
    private static final String SELECT_BY_STATUS_SQL = 
        "SELECT p.id, p.nome, p.descricao, p.data_inicio, p.data_fim_prevista, " +
        "p.data_fim_real, p.status, p.gerente_id, p.criado_em, p.atualizado_em, " +
        "u.nome as gerente_nome, u.email as gerente_email, u.ativo as gerente_ativo " +
        "FROM projeto p " +
        "LEFT JOIN usuario u ON p.gerente_id = u.id " +
        "WHERE p.status = ? " +
        "ORDER BY p.nome";
    
    private static final String SELECT_ATRASADOS_SQL = 
        "SELECT p.id, p.nome, p.descricao, p.data_inicio, p.data_fim_prevista, " +
        "p.data_fim_real, p.status, p.gerente_id, p.criado_em, p.atualizado_em, " +
        "u.nome as gerente_nome, u.email as gerente_email, u.ativo as gerente_ativo " +
        "FROM projeto p " +
        "LEFT JOIN usuario u ON p.gerente_id = u.id " +
        "WHERE p.data_fim_prevista < CURRENT_DATE " +
        "AND p.status NOT IN ('CONCLUIDO', 'CANCELADO') " +
        "ORDER BY p.data_fim_prevista";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM projeto WHERE id = ?";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM projeto";

    public ProjetoDAO() {
        this.usuarioDAO = new UsuarioDAO();
    }

    @Override
    public Projeto save(Projeto projeto) throws SQLException {
        if (projeto == null || !projeto.isValid()) {
            throw new IllegalArgumentException("Projeto inválido para inserção");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            
            LocalDateTime now = LocalDateTime.now();
            projeto.setCriadoEm(now);
            projeto.setCriadoEm(now);
            
            statement.setString(1, projeto.getNome());
            statement.setString(2, projeto.getDescricao());
            statement.setDate(3, projeto.getDataInicio() != null ? 
                Date.valueOf(projeto.getDataInicio()) : null);
            statement.setDate(4, projeto.getDataFimPrevista() != null ? 
                Date.valueOf(projeto.getDataFimPrevista()) : null);
            statement.setDate(5, projeto.getDataFimReal() != null ? 
                Date.valueOf(projeto.getDataFimReal()) : null);
            statement.setString(6, projeto.getStatus());
            if (projeto.getGerenteId() != null) {
                statement.setLong(7, projeto.getGerenteId());
            } else {
                statement.setNull(7, java.sql.Types.BIGINT);
            }
            statement.setTimestamp(8, Timestamp.valueOf(projeto.getCriadoEm()));
            statement.setTimestamp(9, Timestamp.valueOf(projeto.getAtualizadoEm()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Erro ao inserir projeto, nenhuma linha afetada");
            }
            
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                projeto.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Erro ao inserir projeto, ID não foi gerado");
            }
            
            DatabaseUtil.commit(connection);
            logger.info("Projeto inserido com sucesso: {}", projeto.getNome());
            
            return projeto;
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao inserir projeto: {}", projeto.getNome(), e);
            throw e;
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public Projeto update(Projeto projeto) throws SQLException {

        if (!projeto.isValid() || projeto.getId() == null || projeto.getId() <= 0) {
            throw new IllegalArgumentException("Projeto inválido para atualização");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(UPDATE_SQL);

            projeto.setAtualizadoEm(LocalDateTime.now());

            statement.setString(1, projeto.getNome());
            statement.setString(2, projeto.getDescricao());
            statement.setDate(3, projeto.getDataInicio() != null ?
                Date.valueOf(projeto.getDataInicio()) : null);
            statement.setDate(4, projeto.getDataFimPrevista() != null ?
                Date.valueOf(projeto.getDataFimPrevista()) : null);
            statement.setDate(5, projeto.getDataFimReal() != null ?
                Date.valueOf(projeto.getDataFimReal()) : null);
            statement.setString(6, projeto.getStatus());
            if (projeto.getGerenteId() != null) {
                statement.setLong(7, projeto.getGerenteId());
            } else {
                statement.setNull(7, java.sql.Types.BIGINT);
            }
            statement.setTimestamp(8, Timestamp.valueOf(projeto.getAtualizadoEm()));
            statement.setLong(9, projeto.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Projeto não encontrado para atualização: " + projeto.getId());
            }

            DatabaseUtil.commit(connection);
            logger.info("Projeto atualizado com sucesso: {}", projeto.getNome());
            
            return projeto;
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao atualizar projeto: {}", projeto.getId(), e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido para exclusão");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(DELETE_SQL);
            statement.setLong(1, id);
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Projeto não encontrado para exclusão: " + id);
            }
            
            DatabaseUtil.commit(connection);
            logger.info("Projeto excluído com sucesso: {}", id);
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao excluir projeto: {}", id, e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public Optional<Projeto> findById(Long id) throws SQLException {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_ID_SQL);
            statement.setLong(1, id);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return Optional.of(mapResultSetToProjeto(resultSet));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar projeto por ID: {}", id, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public List<Projeto> findAll() throws SQLException {
        return executeQuery(SELECT_ALL_SQL);
    }
    
    /**
     * Busca projetos por gerente
     */
    public List<Projeto> findByGerente(Long gerenteId) throws SQLException {
        if (gerenteId == null || gerenteId <= 0) {
            return new ArrayList<>();
        }
        return executeQuery(SELECT_BY_GERENTE_SQL, gerenteId);
    }
    
    /**
     * Busca projetos por status
     */
    public List<Projeto> findByStatus(StatusProjeto status) throws SQLException {
        if (status == null) {
            return new ArrayList<>();
        }
        return executeQuery(SELECT_BY_STATUS_SQL, status.getCodigo());
    }
    
    /**
     * Busca projetos atrasados
     */
    public List<Projeto> findAtrasados() throws SQLException {
        return executeQuery(SELECT_ATRASADOS_SQL);
    }

    @Override
    public boolean exists(Long id) throws SQLException {
        if (id == null || id <= 0) {
            return false;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(EXISTS_SQL);
            statement.setLong(1, id);
            
            resultSet = statement.executeQuery();
            return resultSet.next();
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência do projeto: {}", id, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public long count() throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(COUNT_SQL);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            logger.error("Erro ao contar projetos", e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Método auxiliar para executar queries que retornam lista de projetos
     */
    private List<Projeto> executeQuery(String sql, Object... parameters) throws SQLException {
        List<Projeto> projetos = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(sql);
            
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                projetos.add(mapResultSetToProjeto(resultSet));
            }
            
            return projetos;
            
        } catch (SQLException e) {
            logger.error("Erro ao executar query: {}", sql, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Mapeia ResultSet para objeto Projeto
     */
    private Projeto mapResultSetToProjeto(ResultSet rs) throws SQLException {
        Projeto projeto = new Projeto();
        projeto.setId(rs.getLong("id"));
        projeto.setNome(rs.getString("nome"));
        projeto.setDescricao(rs.getString("descricao"));
        
        Date dataInicio = rs.getDate("data_inicio");
        if (dataInicio != null) {
            projeto.setDataInicio(dataInicio.toLocalDate());
        }
        
        Date dataFimPrevista = rs.getDate("data_fim_prevista");
        if (dataFimPrevista != null) {
            projeto.setDataFimPrevista(dataFimPrevista.toLocalDate());
        }
        
        Date dataFimReal = rs.getDate("data_fim_real");
        if (dataFimReal != null) {
            projeto.setDataFimReal(dataFimReal.toLocalDate());
        }
        
        String status = rs.getString("status");
        if (status != null) {
            projeto.setStatus(status);
        }
        
        long gerenteId = rs.getLong("gerente_id");
        if (!rs.wasNull()) {
            projeto.setGerenteId(gerenteId);
        }
        
        // Mapeamento do gerente se existir
        String gerenteNome = rs.getString("gerente_nome");
        if (gerenteNome != null) {
            Usuario gerente = new Usuario();
            gerente.setId(projeto.getGerenteId());
            gerente.setNome(gerenteNome);
            gerente.setEmail(rs.getString("gerente_email"));
            gerente.setAtivo(rs.getBoolean("gerente_ativo"));
            projeto.setGerenteId(gerente.getId());
        }
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            projeto.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) {
            projeto.setAtualizadoEm(atualizadoEm.toLocalDateTime());
        }
        
        return projeto;
    }

    /**
     * Sobrecarga para aceitar String status
     */
    public List<Projeto> findByStatus(String status) throws SQLException {
        if (status == null || status.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return executeQuery(SELECT_BY_STATUS_SQL, status.trim().toUpperCase());
    }

    /**
     * Atribui uma equipe a um projeto
     */
    public void atribuirEquipe(Long projetoId, Long equipeId, String papelEquipe) throws SQLException {
        if (projetoId == null || equipeId == null) {
            throw new IllegalArgumentException("IDs do projeto e equipe não podem ser nulos");
        }

        String sql = "INSERT INTO projeto_equipe (projeto_id, equipe_id, papel_equipe, alocado_em) VALUES (?, ?, ?, ?)";
        
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(sql);
            
            statement.setLong(1, projetoId);
            statement.setLong(2, equipeId);
            statement.setString(3, papelEquipe != null ? papelEquipe : "EQUIPE_PRINCIPAL");
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            statement.executeUpdate();
            DatabaseUtil.commit(connection);
            
            logger.info("Equipe {} atribuída ao projeto {} com papel {}", equipeId, projetoId, papelEquipe);
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao atribuir equipe {} ao projeto {}", equipeId, projetoId, e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Remove uma equipe de um projeto
     */
    public void removerEquipe(Long projetoId, Long equipeId) throws SQLException {
        if (projetoId == null || equipeId == null) {
            throw new IllegalArgumentException("IDs do projeto e equipe não podem ser nulos");
        }

        String sql = "DELETE FROM projeto_equipe WHERE projeto_id = ? AND equipe_id = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(sql);
            
            statement.setLong(1, projetoId);
            statement.setLong(2, equipeId);
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Equipe não está atribuída ao projeto ou não foi encontrada");
            }
            
            DatabaseUtil.commit(connection);
            
            logger.info("Equipe {} removida do projeto {}", equipeId, projetoId);
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao remover equipe {} do projeto {}", equipeId, projetoId, e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Lista equipes atribuídas a um projeto
     */
    public List<Equipe> findEquipesByProjeto(Long projetoId) throws SQLException {
        if (projetoId == null) {
            return new ArrayList<>();
        }

        String sql = "SELECT e.id, e.nome, e.descricao, e.ativa, e.criado_em, e.atualizado_em, " +
                    "pe.papel_equipe, pe.alocado_em " +
                    "FROM equipe e " +
                    "INNER JOIN projeto_equipe pe ON e.id = pe.equipe_id " +
                    "WHERE pe.projeto_id = ? AND e.ativa = true " +
                    "ORDER BY e.nome";

        List<Equipe> equipes = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setLong(1, projetoId);
            
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                Equipe equipe = new Equipe();
                equipe.setId(resultSet.getLong("id"));
                equipe.setNome(resultSet.getString("nome"));
                equipe.setDescricao(resultSet.getString("descricao"));
                equipe.setAtiva(resultSet.getBoolean("ativa"));
                
                Timestamp criadoEm = resultSet.getTimestamp("criado_em");
                if (criadoEm != null) {
                    equipe.setCriadoEm(criadoEm.toLocalDateTime());
                }
                
                Timestamp atualizadoEm = resultSet.getTimestamp("atualizado_em");
                if (atualizadoEm != null) {
                    equipe.setAtualizadoEm(atualizadoEm.toLocalDateTime());
                }
                
                equipes.add(equipe);
            }
            
            return equipes;
            
        } catch (SQLException e) {
            logger.error("Erro ao listar equipes do projeto {}", projetoId, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }
}
