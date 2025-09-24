package com.gestao.projetos.dao;

import com.gestao.projetos.model.Papel;
import com.gestao.projetos.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operações relacionadas à entidade Papel
 */
public class PapelDAO implements BaseDAO<Papel, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(PapelDAO.class);
    
    // Queries SQL
    private static final String INSERT_SQL = 
        "INSERT INTO papel (nome, descricao, criado_em, atualizado_em) VALUES (?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE papel SET nome = ?, descricao = ?, atualizado_em = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM papel WHERE id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT id, nome, descricao, criado_em, atualizado_em FROM papel WHERE id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT id, nome, descricao, criado_em, atualizado_em FROM papel ORDER BY nome";
    
    private static final String SELECT_BY_NOME_SQL = 
        "SELECT id, nome, descricao, criado_em, atualizado_em FROM papel WHERE nome = ?";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM papel WHERE id = ?";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM papel";

    @Override
    public Papel save(Papel papel) throws SQLException {
        if (papel == null || !papel.isValid()) {
            throw new IllegalArgumentException("Papel inválido para inserção");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            
            LocalDateTime now = LocalDateTime.now();
            papel.setCriadoEm(now);
            papel.setAtualizadoEm(now);
            
            statement.setString(1, papel.getNome());
            statement.setString(2, papel.getDescricao());
            statement.setTimestamp(3, Timestamp.valueOf(papel.getCriadoEm()));
            statement.setTimestamp(4, Timestamp.valueOf(papel.getAtualizadoEm()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir papel, nenhuma linha afetada");
            }

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                papel.setId(generatedKeys.getLong(1));
                logger.debug("Papel inserido com ID: {}", papel.getId());
                return papel;
            } else {
                throw new SQLException("Falha ao inserir papel, ID não gerado");
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao inserir papel: {}", papel.getNome(), e);
            throw e;
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    @Override
    public Papel update(Papel papel) throws SQLException {
        if (papel == null || !papel.isValid() || papel.getId() == null) {
            throw new IllegalArgumentException("Papel inválido para atualização");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(UPDATE_SQL);
            
            papel.setAtualizadoEm(LocalDateTime.now());
            
            statement.setString(1, papel.getNome());
            statement.setString(2, papel.getDescricao());
            statement.setTimestamp(3, Timestamp.valueOf(papel.getAtualizadoEm()));
            statement.setLong(4, papel.getId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao atualizar papel, nenhuma linha afetada");
            }
            
            logger.debug("Papel atualizado: {}", papel.getId());
            return papel;
            
        } catch (SQLException e) {
            logger.error("Erro ao atualizar papel: {}", papel.getId(), e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
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
                throw new SQLException("Falha ao excluir papel, nenhuma linha afetada");
            }
            
            logger.debug("Papel excluído: {}", id);
            
        } catch (SQLException e) {
            logger.error("Erro ao excluir papel: {}", id, e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    @Override
    public Optional<Papel> findById(Long id) throws SQLException {
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
                return Optional.of(mapResultSetToPapel(resultSet));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar papel por ID: {}", id, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    @Override
    public List<Papel> findAll() throws SQLException {
        return executeQuery(SELECT_ALL_SQL);
    }
    
    /**
     * Busca papel por nome
     */
    public Optional<Papel> findByNome(String nome) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            return Optional.empty();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_NOME_SQL);
            statement.setString(1, nome.trim());
            
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToPapel(resultSet));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar papel por nome: {}", nome, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
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
            logger.error("Erro ao verificar existência do papel: {}", id, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
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
            logger.error("Erro ao contar papéis", e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    /**
     * Método auxiliar para executar queries que retornam lista de papéis
     */
    private List<Papel> executeQuery(String sql, Object... parameters) throws SQLException {
        List<Papel> papeis = new ArrayList<>();
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
                papeis.add(mapResultSetToPapel(resultSet));
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao executar query de papéis", e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
        
        return papeis;
    }

    /**
     * Mapeia ResultSet para objeto Papel
     */
    private Papel mapResultSetToPapel(ResultSet rs) throws SQLException {
        Papel papel = new Papel();
        papel.setId(rs.getLong("id"));
        papel.setNome(rs.getString("nome"));
        papel.setDescricao(rs.getString("descricao"));
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            papel.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) {
            papel.setAtualizadoEm(atualizadoEm.toLocalDateTime());
        }
        
        return papel;
    }
}