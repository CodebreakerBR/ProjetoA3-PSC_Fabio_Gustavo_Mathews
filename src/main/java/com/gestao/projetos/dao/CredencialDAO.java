package com.gestao.projetos.dao;

import com.gestao.projetos.model.Credencial;
import com.gestao.projetos.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operações relacionadas à entidade Credencial
 */
public class CredencialDAO implements BaseDAO<Credencial, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(CredencialDAO.class);
    
    // Queries SQL
    private static final String INSERT_SQL = 
        "INSERT INTO credencial (hash, salt, usuario_id, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE credencial SET hash = ?, salt = ?, atualizado_em = ? WHERE id = ?";
    
    private static final String UPDATE_HASH_BY_USER_SQL = 
        "UPDATE credencial SET hash = ?, atualizado_em = ? WHERE usuario_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM credencial WHERE id = ?";
    
    private static final String DELETE_BY_USER_SQL = 
        "DELETE FROM credencial WHERE usuario_id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT id, hash, salt, usuario_id, criado_em, atualizado_em FROM credencial WHERE id = ?";
    
    private static final String SELECT_BY_USER_ID_SQL = 
        "SELECT id, hash, salt, usuario_id, criado_em, atualizado_em FROM credencial WHERE usuario_id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT id, hash, salt, usuario_id, criado_em, atualizado_em FROM credencial ORDER BY id";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM credencial WHERE id = ?";
    
    private static final String EXISTS_BY_USER_SQL = 
        "SELECT 1 FROM credencial WHERE usuario_id = ?";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM credencial";

    @Override
    public Credencial save(Credencial credencial) throws SQLException {
        if (credencial == null || !credencial.isValid()) {
            throw new IllegalArgumentException("Credencial inválida para inserção");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            
            LocalDateTime now = LocalDateTime.now();
            credencial.setCriadoEm(now);
            credencial.setAtualizadoEm(now);
            
            statement.setString(1, credencial.getHash());
            statement.setString(2, credencial.getSalt());
            statement.setLong(3, credencial.getUsuarioId());
            statement.setTimestamp(4, Timestamp.valueOf(credencial.getCriadoEm()));
            statement.setTimestamp(5, Timestamp.valueOf(credencial.getAtualizadoEm()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir credencial, nenhuma linha afetada");
            }
            
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                credencial.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Falha ao inserir credencial, ID não gerado");
            }
            
            DatabaseUtil.commit(connection);
            logger.info("Credencial inserida com sucesso para usuário: {}", credencial.getUsuarioId());
            
            return credencial;
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao inserir credencial para usuário: {}", credencial.getUsuarioId(), e);
            throw e;
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Salva uma Credencial usando uma conexão específica (para transações)
     */
    public Credencial save(Credencial credencial, Connection connection) throws SQLException {
        if (credencial == null || !credencial.isValid()) {
            throw new IllegalArgumentException("Credencial inválida para inserção");
        }

        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            
            LocalDateTime now = LocalDateTime.now();
            credencial.setCriadoEm(now);
            credencial.setAtualizadoEm(now);
            
            statement.setString(1, credencial.getHash());
            statement.setString(2, credencial.getSalt());
            statement.setLong(3, credencial.getUsuarioId());
            statement.setTimestamp(4, Timestamp.valueOf(credencial.getCriadoEm()));
            statement.setTimestamp(5, Timestamp.valueOf(credencial.getAtualizadoEm()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir credencial, nenhuma linha afetada");
            }
            
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                credencial.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Falha ao inserir credencial, ID não gerado");
            }
            
            logger.info("Credencial inserida com sucesso para usuário: {} usando conexão da transação", credencial.getUsuarioId());
            
            return credencial;
            
        } catch (SQLException e) {
            logger.error("Erro ao inserir credencial na transação para usuário: {}", credencial.getUsuarioId(), e);
            throw e;
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (statement != null) statement.close();
            // NÃO fechar a conexão aqui pois ela é da transação
        }
    }

    @Override
    public Credencial update(Credencial credencial) throws SQLException {
        if (credencial == null || !credencial.isValid() || credencial.getId() == null) {
            throw new IllegalArgumentException("Credencial inválida para atualização");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(UPDATE_SQL);
            
            credencial.setAtualizadoEm(LocalDateTime.now());
            
            statement.setString(1, credencial.getHash());
            statement.setString(2, credencial.getSalt());
            statement.setTimestamp(3, Timestamp.valueOf(credencial.getAtualizadoEm()));
            statement.setLong(4, credencial.getId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Credencial não encontrada para atualização: " + credencial.getId());
            }
            
            DatabaseUtil.commit(connection);
            logger.info("Credencial atualizada com sucesso: {}", credencial.getId());
            
            return credencial;
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao atualizar credencial: {}", credencial.getId(), e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Atualiza apenas o hash da senha para um usuário
     */
    public boolean updateHashByUserId(Long usuarioId, String novoHash) throws SQLException {
        if (usuarioId == null || novoHash == null || novoHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Parâmetros inválidos para atualização de hash");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(UPDATE_HASH_BY_USER_SQL);
            
            statement.setString(1, novoHash);
            statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(3, usuarioId);
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Credencial não encontrada para o usuário: " + usuarioId);
            }
            
            DatabaseUtil.commit(connection);
            logger.info("Hash atualizado com sucesso para usuário: {}", usuarioId);
            
            return true;
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao atualizar hash para usuário: {}", usuarioId, e);
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
                throw new SQLException("Credencial não encontrada para exclusão: " + id);
            }
            
            DatabaseUtil.commit(connection);
            logger.info("Credencial excluída com sucesso: {}", id);
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao excluir credencial: {}", id, e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Exclui credencial por usuário
     */
    public void deleteByUserId(Long usuarioId) throws SQLException {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("ID de usuário inválido para exclusão");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(DELETE_BY_USER_SQL);
            statement.setLong(1, usuarioId);
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Credencial não encontrada para o usuário: " + usuarioId);
            }
            
            DatabaseUtil.commit(connection);
            logger.info("Credencial excluída com sucesso para usuário: {}", usuarioId);
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao excluir credencial do usuário: {}", usuarioId, e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public Optional<Credencial> findById(Long id) throws SQLException {
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
                return Optional.of(mapResultSetToCredencial(resultSet));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar credencial por ID: {}", id, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Busca credencial por ID do usuário
     */
    public Optional<Credencial> findByUserId(Long usuarioId) throws SQLException {
        if (usuarioId == null || usuarioId <= 0) {
            return Optional.empty();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_USER_ID_SQL);
            statement.setLong(1, usuarioId);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return Optional.of(mapResultSetToCredencial(resultSet));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar credencial por usuário: {}", usuarioId, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public List<Credencial> findAll() throws SQLException {
        return executeQuery(SELECT_ALL_SQL);
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
            logger.error("Erro ao verificar existência de credencial: {}", id, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Verifica se existe credencial para o usuário
     */
    public boolean existsByUserId(Long usuarioId) throws SQLException {
        if (usuarioId == null || usuarioId <= 0) {
            return false;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(EXISTS_BY_USER_SQL);
            statement.setLong(1, usuarioId);
            
            resultSet = statement.executeQuery();
            return resultSet.next();
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência de credencial para usuário: {}", usuarioId, e);
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
            logger.error("Erro ao contar credenciais", e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Método auxiliar para executar queries que retornam lista de credenciais
     */
    private List<Credencial> executeQuery(String sql, Object... parameters) throws SQLException {
        List<Credencial> credenciais = new ArrayList<>();
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
                credenciais.add(mapResultSetToCredencial(resultSet));
            }

            return credenciais;

        } catch (SQLException e) {
            logger.error("Erro ao executar query de credenciais", e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Mapeia ResultSet para objeto Credencial
     */
    private Credencial mapResultSetToCredencial(ResultSet rs) throws SQLException {
        Credencial credencial = new Credencial();
        credencial.setId(rs.getLong("id"));
        credencial.setHash(rs.getString("hash"));
        credencial.setSalt(rs.getString("salt"));
        credencial.setUsuarioId(rs.getLong("usuario_id"));
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            credencial.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) {
            credencial.setAtualizadoEm(atualizadoEm.toLocalDateTime());
        }
        
        return credencial;
    }
}