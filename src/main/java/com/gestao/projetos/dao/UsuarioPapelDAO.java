package com.gestao.projetos.dao;

import com.gestao.projetos.model.UsuarioPapel;
import com.gestao.projetos.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operações relacionadas à entidade UsuarioPapel
 */
public class UsuarioPapelDAO implements BaseDAO<UsuarioPapel, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioPapelDAO.class);
    
    // Queries SQL
    private static final String INSERT_SQL = 
        "INSERT INTO usuario_papel (usuario_id, papel_id, atribuido_em, expira_em, ativo) VALUES (?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE usuario_papel SET expira_em = ?, ativo = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM usuario_papel WHERE id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT id, usuario_id, papel_id, atribuido_em, expira_em, ativo FROM usuario_papel WHERE id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT id, usuario_id, papel_id, atribuido_em, expira_em, ativo FROM usuario_papel ORDER BY id";
    
    private static final String SELECT_BY_USER_SQL = 
        "SELECT id, usuario_id, papel_id, atribuido_em, expira_em, ativo FROM usuario_papel WHERE usuario_id = ? AND ativo = TRUE";
    
    private static final String SELECT_BY_USER_AND_ROLE_SQL = 
        "SELECT id, usuario_id, papel_id, atribuido_em, expira_em, ativo FROM usuario_papel WHERE usuario_id = ? AND papel_id = ? AND ativo = TRUE";
    
    private static final String DEACTIVATE_USER_ROLES_SQL = 
        "UPDATE usuario_papel SET ativo = FALSE WHERE usuario_id = ? AND ativo = TRUE";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM usuario_papel WHERE id = ?";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM usuario_papel";

    @Override
    public UsuarioPapel save(UsuarioPapel usuarioPapel) throws SQLException {
        if (usuarioPapel == null || !usuarioPapel.isValid()) {
            throw new IllegalArgumentException("UsuarioPapel inválido para inserção");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            
            if (usuarioPapel.getAtribuidoEm() == null) {
                usuarioPapel.setAtribuidoEm(LocalDateTime.now());
            }
            
            statement.setLong(1, usuarioPapel.getUsuarioId());
            statement.setLong(2, usuarioPapel.getPapelId());
            statement.setTimestamp(3, Timestamp.valueOf(usuarioPapel.getAtribuidoEm()));
            
            if (usuarioPapel.getExpiraEm() != null) {
                statement.setTimestamp(4, Timestamp.valueOf(usuarioPapel.getExpiraEm()));
            } else {
                statement.setNull(4, Types.TIMESTAMP);
            }
            
            statement.setBoolean(5, usuarioPapel.isAtivo());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir usuario_papel, nenhuma linha afetada");
            }

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                usuarioPapel.setId(generatedKeys.getLong(1));
                logger.debug("UsuarioPapel inserido com ID: {}", usuarioPapel.getId());
                return usuarioPapel;
            } else {
                throw new SQLException("Falha ao inserir usuario_papel, ID não gerado");
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao inserir usuario_papel: usuarioId={}, papelId={}", 
                        usuarioPapel.getUsuarioId(), usuarioPapel.getPapelId(), e);
            throw e;
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    /**
     * Salva um UsuarioPapel usando uma conexão específica (para transações)
     */
    public UsuarioPapel save(UsuarioPapel usuarioPapel, Connection connection) throws SQLException {
        if (usuarioPapel == null || !usuarioPapel.isValid()) {
            throw new IllegalArgumentException("UsuarioPapel inválido para inserção");
        }

        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            
            if (usuarioPapel.getAtribuidoEm() == null) {
                usuarioPapel.setAtribuidoEm(LocalDateTime.now());
            }
            
            statement.setLong(1, usuarioPapel.getUsuarioId());
            statement.setLong(2, usuarioPapel.getPapelId());
            statement.setTimestamp(3, Timestamp.valueOf(usuarioPapel.getAtribuidoEm()));
            
            if (usuarioPapel.getExpiraEm() != null) {
                statement.setTimestamp(4, Timestamp.valueOf(usuarioPapel.getExpiraEm()));
            } else {
                statement.setNull(4, Types.TIMESTAMP);
            }
            
            statement.setBoolean(5, usuarioPapel.isAtivo());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir usuario_papel, nenhuma linha afetada");
            }

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                usuarioPapel.setId(generatedKeys.getLong(1));
                logger.debug("UsuarioPapel inserido com ID: {} usando conexão da transação", usuarioPapel.getId());
                return usuarioPapel;
            } else {
                throw new SQLException("Falha ao inserir usuario_papel, ID não gerado");
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao inserir usuario_papel na transação: usuarioId={}, papelId={}", 
                        usuarioPapel.getUsuarioId(), usuarioPapel.getPapelId(), e);
            throw e;
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (statement != null) statement.close();
            // NÃO fechar a conexão aqui pois ela é da transação
        }
    }

    @Override
    public UsuarioPapel update(UsuarioPapel usuarioPapel) throws SQLException {
        if (usuarioPapel == null || usuarioPapel.getId() == null) {
            throw new IllegalArgumentException("UsuarioPapel inválido para atualização");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(UPDATE_SQL);
            
            if (usuarioPapel.getExpiraEm() != null) {
                statement.setTimestamp(1, Timestamp.valueOf(usuarioPapel.getExpiraEm()));
            } else {
                statement.setNull(1, Types.TIMESTAMP);
            }
            
            statement.setBoolean(2, usuarioPapel.isAtivo());
            statement.setLong(3, usuarioPapel.getId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao atualizar usuario_papel, nenhuma linha afetada");
            }
            
            logger.debug("UsuarioPapel atualizado: {}", usuarioPapel.getId());
            return usuarioPapel;
            
        } catch (SQLException e) {
            logger.error("Erro ao atualizar usuario_papel: {}", usuarioPapel.getId(), e);
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
                throw new SQLException("Falha ao excluir usuario_papel, nenhuma linha afetada");
            }
            
            logger.debug("UsuarioPapel excluído: {}", id);
            
        } catch (SQLException e) {
            logger.error("Erro ao excluir usuario_papel: {}", id, e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    @Override
    public Optional<UsuarioPapel> findById(Long id) throws SQLException {
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
                return Optional.of(mapResultSetToUsuarioPapel(resultSet));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuario_papel por ID: {}", id, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    @Override
    public List<UsuarioPapel> findAll() throws SQLException {
        return executeQuery(SELECT_ALL_SQL);
    }
    
    /**
     * Busca papéis ativos de um usuário
     */
    public List<UsuarioPapel> findByUsuarioId(Long usuarioId) throws SQLException {
        if (usuarioId == null || usuarioId <= 0) {
            return new ArrayList<>();
        }
        
        return executeQuery(SELECT_BY_USER_SQL, usuarioId);
    }
    
    /**
     * Verifica se um usuário tem um papel específico
     */
    public Optional<UsuarioPapel> findByUsuarioAndPapel(Long usuarioId, Long papelId) throws SQLException {
        if (usuarioId == null || usuarioId <= 0 || papelId == null || papelId <= 0) {
            return Optional.empty();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_USER_AND_ROLE_SQL);
            statement.setLong(1, usuarioId);
            statement.setLong(2, papelId);
            
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToUsuarioPapel(resultSet));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuario_papel: usuarioId={}, papelId={}", usuarioId, papelId, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }
    
    /**
     * Busca os papéis de um usuário
     */
    public List<String> findPapeisUsuario(Long usuarioId) throws SQLException {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido");
        }

        List<String> papeis = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            String sql = "SELECT p.nome FROM usuario_papel up " +
                        "INNER JOIN papel p ON up.papel_id = p.id " +
                        "WHERE up.usuario_id = ? AND up.ativo = TRUE";
            
            statement = connection.prepareStatement(sql);
            statement.setLong(1, usuarioId);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                papeis.add(resultSet.getString("nome"));
            }
            
            logger.debug("Encontrados {} papéis para o usuário: {}", papeis.size(), usuarioId);
            return papeis;
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar papéis do usuário: {}", usuarioId, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    /**
     * Desativa todos os papéis de um usuário
     */
    public void deactivateUserRoles(Long usuarioId) throws SQLException {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(DEACTIVATE_USER_ROLES_SQL);
            statement.setLong(1, usuarioId);
            
            int affectedRows = statement.executeUpdate();
            logger.debug("Desativados {} papéis do usuário: {}", affectedRows, usuarioId);
            
        } catch (SQLException e) {
            logger.error("Erro ao desativar papéis do usuário: {}", usuarioId, e);
            throw e;
        } finally {
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
            logger.error("Erro ao verificar existência do usuario_papel: {}", id, e);
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
            logger.error("Erro ao contar usuario_papel", e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    /**
     * Método auxiliar para executar queries que retornam lista de UsuarioPapel
     */
    private List<UsuarioPapel> executeQuery(String sql, Object... parameters) throws SQLException {
        List<UsuarioPapel> usuarioPapeis = new ArrayList<>();
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
                usuarioPapeis.add(mapResultSetToUsuarioPapel(resultSet));
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao executar query de usuario_papel", e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
        
        return usuarioPapeis;
    }

    /**
     * Mapeia ResultSet para objeto UsuarioPapel
     */
    private UsuarioPapel mapResultSetToUsuarioPapel(ResultSet rs) throws SQLException {
        UsuarioPapel usuarioPapel = new UsuarioPapel();
        usuarioPapel.setId(rs.getLong("id"));
        usuarioPapel.setUsuarioId(rs.getLong("usuario_id"));
        usuarioPapel.setPapelId(rs.getLong("papel_id"));
        usuarioPapel.setAtivo(rs.getBoolean("ativo"));
        
        Timestamp atribuidoEm = rs.getTimestamp("atribuido_em");
        if (atribuidoEm != null) {
            usuarioPapel.setAtribuidoEm(atribuidoEm.toLocalDateTime());
        }
        
        Timestamp expiraEm = rs.getTimestamp("expira_em");
        if (expiraEm != null) {
            usuarioPapel.setExpiraEm(expiraEm.toLocalDateTime());
        }
        
        return usuarioPapel;
    }
}