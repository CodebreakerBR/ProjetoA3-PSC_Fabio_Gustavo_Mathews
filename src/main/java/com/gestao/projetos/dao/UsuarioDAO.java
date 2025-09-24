package com.gestao.projetos.dao;

import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operações relacionadas à entidade Usuario
 */
public class UsuarioDAO implements BaseDAO<Usuario, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAO.class);
    
    // Queries SQL
    private static final String INSERT_SQL = 
        "INSERT INTO usuario (nome, cpf, email, cargo, login, ativo, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE usuario SET nome = ?, cpf = ?, email = ?, cargo = ?, login = ?, ativo = ?, atualizado_em = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM usuario WHERE id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT id, nome, cpf, email, cargo, login, ativo, criado_em, atualizado_em FROM usuario WHERE id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT id, nome, cpf, email, cargo, login, ativo, criado_em, atualizado_em FROM usuario ORDER BY nome";
    
    private static final String SELECT_BY_EMAIL_SQL = 
        "SELECT id, nome, cpf, email, cargo, login, ativo, criado_em, atualizado_em FROM usuario WHERE email = ?";
    
    private static final String SELECT_BY_LOGIN_SQL = 
        "SELECT id, nome, cpf, email, cargo, login, ativo, criado_em, atualizado_em FROM usuario WHERE login = ?";
    
    private static final String SELECT_BY_CPF_SQL = 
        "SELECT id, nome, cpf, email, cargo, login, ativo, criado_em, atualizado_em FROM usuario WHERE cpf = ?";
    
    private static final String SELECT_ACTIVE_SQL = 
        "SELECT id, nome, cpf, email, cargo, login, ativo, criado_em, atualizado_em FROM usuario WHERE ativo = true ORDER BY nome";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM usuario WHERE id = ?";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM usuario";
    
    private static final String EXISTS_EMAIL_SQL = 
        "SELECT 1 FROM usuario WHERE email = ? AND id != ?";
    
    private static final String EXISTS_LOGIN_SQL = 
        "SELECT 1 FROM usuario WHERE login = ? AND id != ?";
    
    private static final String EXISTS_CPF_SQL = 
        "SELECT 1 FROM usuario WHERE cpf = ? AND id != ?";

    @Override
    public Usuario save(Usuario usuario) throws SQLException {
        if (usuario == null || !usuario.isValid()) {
            throw new IllegalArgumentException("Usuário inválido para inserção");
        }

        // Verifica se email já existe
        if (existsByEmail(usuario.getEmail())) {
            throw new SQLException("Email já está em uso: " + usuario.getEmail());
        }
        
        // Verifica se login já existe
        if (existsByLogin(usuario.getLogin())) {
            throw new SQLException("Login já está em uso: " + usuario.getLogin());
        }
        
        // Verifica se CPF já existe
        if (existsByCpf(usuario.getCpf())) {
            throw new SQLException("CPF já está em uso: " + usuario.getCpf());
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            
            LocalDateTime now = LocalDateTime.now();
            usuario.setCriadoEm(now);
            usuario.setAtualizadoEm(now);
            
            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getCpf());
            statement.setString(3, usuario.getEmail());
            statement.setString(4, usuario.getCargo());
            statement.setString(5, usuario.getLogin());
            statement.setBoolean(6, usuario.isAtivo());
            statement.setTimestamp(7, Timestamp.valueOf(usuario.getCriadoEm()));
            statement.setTimestamp(8, Timestamp.valueOf(usuario.getAtualizadoEm()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Erro ao inserir usuário, nenhuma linha afetada");
            }
            
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                usuario.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Erro ao inserir usuário, ID não foi gerado");
            }
            
            DatabaseUtil.commit(connection);
            logger.info("Usuário inserido com sucesso: {}", usuario.getEmail());
            
            return usuario;
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao inserir usuário: {}", usuario.getEmail(), e);
            throw e;
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Salva um Usuario usando uma conexão específica (para transações)
     */
    public Usuario save(Usuario usuario, Connection connection) throws SQLException {
        if (usuario == null || !usuario.isValid()) {
            throw new IllegalArgumentException("Usuário inválido para inserção");
        }

        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            
            LocalDateTime now = LocalDateTime.now();
            usuario.setCriadoEm(now);
            usuario.setAtualizadoEm(now);
            
            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getCpf());
            statement.setString(3, usuario.getEmail());
            statement.setString(4, usuario.getCargo());
            statement.setString(5, usuario.getLogin());
            statement.setBoolean(6, usuario.isAtivo());
            statement.setTimestamp(7, Timestamp.valueOf(usuario.getCriadoEm()));
            statement.setTimestamp(8, Timestamp.valueOf(usuario.getAtualizadoEm()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir usuário, nenhuma linha afetada");
            }
            
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                usuario.setId(generatedKeys.getLong(1));
                logger.debug("Usuário inserido com ID: {} usando conexão da transação", usuario.getId());
            } else {
                throw new SQLException("Falha ao inserir usuário, ID não gerado");
            }
            
            return usuario;
            
        } catch (SQLException e) {
            logger.error("Erro ao inserir usuário na transação: {}", usuario.getEmail(), e);
            throw e;
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (statement != null) statement.close();
            // NÃO fechar a conexão aqui pois ela é da transação
        }
    }
    public List<Usuario> searchByNomeOuEmail(String termo) throws SQLException {
        String sql = "SELECT id, nome, cpf, email, cargo, login, ativo, criado_em, atualizado_em " +
                "FROM usuario WHERE LOWER(nome) LIKE ? OR LOWER(email) LIKE ? OR LOWER(login) LIKE ? OR cpf LIKE ? ORDER BY nome";
        String filtro = "%" + termo.toLowerCase().trim() + "%";
        return executeQuery(sql, filtro, filtro, filtro, filtro);
    }


    @Override
    public Usuario update(Usuario usuario) throws SQLException {
        if (usuario == null || !usuario.isValid() || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuário inválido para atualização");
        }

        // Verifica se email já existe para outro usuário
        if (existsByEmailForOtherUser(usuario.getEmail(), usuario.getId())) {
            throw new SQLException("Email já está em uso por outro usuário: " + usuario.getEmail());
        }
        
        // Verifica se login já existe para outro usuário
        if (existsByLoginForOtherUser(usuario.getLogin(), usuario.getId())) {
            throw new SQLException("Login já está em uso por outro usuário: " + usuario.getLogin());
        }
        
        // Verifica se CPF já existe para outro usuário
        if (existsByCpfForOtherUser(usuario.getCpf(), usuario.getId())) {
            throw new SQLException("CPF já está em uso por outro usuário: " + usuario.getCpf());
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(UPDATE_SQL);
            
            usuario.setAtualizadoEm(LocalDateTime.now());
            
            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getCpf());
            statement.setString(3, usuario.getEmail());
            statement.setString(4, usuario.getCargo());
            statement.setString(5, usuario.getLogin());
            statement.setBoolean(6, usuario.isAtivo());
            statement.setTimestamp(7, Timestamp.valueOf(usuario.getAtualizadoEm()));
            statement.setLong(8, usuario.getId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Usuário não encontrado para atualização: " + usuario.getId());
            }
            
            DatabaseUtil.commit(connection);
            logger.info("Usuário atualizado com sucesso: {}", usuario.getEmail());
            
            return usuario;
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao atualizar usuário: {}", usuario.getId(), e);
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
                throw new SQLException("Usuário não encontrado para exclusão: " + id);
            }
            
            DatabaseUtil.commit(connection);
            logger.info("Usuário excluído com sucesso: {}", id);
            
        } catch (SQLException e) {
            DatabaseUtil.rollback(connection);
            logger.error("Erro ao excluir usuário: {}", id, e);
            throw e;
        } finally {
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public Optional<Usuario> findById(Long id) throws SQLException {
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
                return Optional.of(mapResultSetToUsuario(resultSet));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por ID: {}", id, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public List<Usuario> findAll() throws SQLException {
        return executeQuery(SELECT_ALL_SQL);
    }
    
    /**
     * Busca usuário por email
     */
    public Optional<Usuario> findByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_EMAIL_SQL);
            statement.setString(1, email);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return Optional.of(mapResultSetToUsuario(resultSet));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por email: {}", email, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }
    
    /**
     * Lista apenas usuários ativos
     */
    public List<Usuario> findAllActive() throws SQLException {
        return executeQuery(SELECT_ACTIVE_SQL);
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
            logger.error("Erro ao verificar existência do usuário: {}", id, e);
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
            logger.error("Erro ao contar usuários", e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }
    
    /**
     * Verifica se existe usuário com o email informado
     */
    public boolean existsByEmail(String email) throws SQLException {
        return findByEmail(email).isPresent();
    }
    
    /**
     * Verifica se existe outro usuário com o mesmo email
     */
    public boolean existsByEmailForOtherUser(String email, Long userId) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(EXISTS_EMAIL_SQL);
            statement.setString(1, email);
            statement.setLong(2, userId != null ? userId : 0);
            
            resultSet = statement.executeQuery();
            return resultSet.next();
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar email duplicado: {}", email, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Método auxiliar para executar queries que retornam lista de usuários
     */
    private List<Usuario> executeQuery(String sql, Object... parameters) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
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
                usuarios.add(mapResultSetToUsuario(resultSet));
            }
            
            return usuarios;
            
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
     * Mapeia ResultSet para objeto Usuario
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setEmail(rs.getString("email"));
        usuario.setCargo(rs.getString("cargo"));
        usuario.setLogin(rs.getString("login"));
        usuario.setAtivo(rs.getBoolean("ativo"));
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            usuario.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) {
            usuario.setAtualizadoEm(atualizadoEm.toLocalDateTime());
        }
        
        return usuario;
    }

    /**
     * Busca usuário por login
     */
    public Optional<Usuario> findByLogin(String login) throws SQLException {
        if (login == null || login.trim().isEmpty()) {
            return Optional.empty();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_LOGIN_SQL);
            statement.setString(1, login.trim());
            
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToUsuario(resultSet));
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por login: {}", login, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Busca usuário por CPF
     */
    public Optional<Usuario> findByCpf(String cpf) throws SQLException {
        if (cpf == null || cpf.trim().isEmpty()) {
            return Optional.empty();
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_CPF_SQL);
            statement.setString(1, cpf.trim());
            
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToUsuario(resultSet));
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por CPF: {}", cpf, e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Verifica se existe usuário com o login informado
     */
    public boolean existsByLogin(String login) throws SQLException {
        return findByLogin(login).isPresent();
    }

    /**
     * Verifica se existe outro usuário com o mesmo login
     */
    public boolean existsByLoginForOtherUser(String login, Long userId) throws SQLException {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(EXISTS_LOGIN_SQL);
            statement.setString(1, login.trim());
            statement.setLong(2, userId);
            
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            logger.error("Erro ao verificar login para outro usuário", e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Verifica se existe usuário com o CPF informado
     */
    public boolean existsByCpf(String cpf) throws SQLException {
        return findByCpf(cpf).isPresent();
    }

    /**
     * Verifica se existe outro usuário com o mesmo CPF
     */
    public boolean existsByCpfForOtherUser(String cpf, Long userId) throws SQLException {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement(EXISTS_CPF_SQL);
            statement.setString(1, cpf.trim());
            statement.setLong(2, userId);
            
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            logger.error("Erro ao verificar CPF para outro usuário", e);
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            DatabaseUtil.closeConnection(connection);
        }
    }
}
