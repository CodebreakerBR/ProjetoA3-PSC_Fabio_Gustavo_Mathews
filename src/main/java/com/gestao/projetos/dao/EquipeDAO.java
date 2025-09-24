package com.gestao.projetos.dao;

import com.gestao.projetos.model.Equipe;
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
 * DAO para operações relacionadas à entidade Equipe
 */
public class EquipeDAO implements BaseDAO<Equipe, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(EquipeDAO.class);
    
    // Queries SQL para equipe
    private static final String INSERT_EQUIPE_SQL = 
        "INSERT INTO equipe (nome, descricao, ativa, gerente_id, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_EQUIPE_SQL = 
        "UPDATE equipe SET nome = ?, descricao = ?, ativa = ?, gerente_id = ?, atualizado_em = ? WHERE id = ?";
    
    private static final String DELETE_EQUIPE_SQL = 
        "DELETE FROM equipe WHERE id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT e.id, e.nome, e.descricao, e.ativa, e.gerente_id, e.criado_em, e.atualizado_em, " +
        "u.nome as gerente_nome, u.email as gerente_email " +
        "FROM equipe e LEFT JOIN usuario u ON e.gerente_id = u.id WHERE e.id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT e.id, e.nome, e.descricao, e.ativa, e.gerente_id, e.criado_em, e.atualizado_em, " +
        "u.nome as gerente_nome, u.email as gerente_email " +
        "FROM equipe e LEFT JOIN usuario u ON e.gerente_id = u.id ORDER BY e.nome";
    
    private static final String SELECT_ACTIVE_SQL = 
        "SELECT e.id, e.nome, e.descricao, e.ativa, e.gerente_id, e.criado_em, e.atualizado_em, " +
        "u.nome as gerente_nome, u.email as gerente_email " +
        "FROM equipe e LEFT JOIN usuario u ON e.gerente_id = u.id WHERE e.ativa = true ORDER BY e.nome";
    
    private static final String EXISTS_SQL = 
        "SELECT 1 FROM equipe WHERE id = ?";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM equipe";
    
    private static final String EXISTS_NOME_SQL = 
        "SELECT 1 FROM equipe WHERE nome = ? AND id != ?";
    
    // Queries para membros da equipe
    private static final String INSERT_MEMBRO_SQL = 
        "INSERT INTO equipe_membro (equipe_id, usuario_id, papel_equipe, data_entrada, ativo, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String DELETE_MEMBRO_SQL = 
        "DELETE FROM equipe_membro WHERE equipe_id = ? AND usuario_id = ?";
    
    private static final String UPDATE_MEMBRO_PAPEL_SQL = 
        "UPDATE equipe_membro SET papel_equipe = ?, atualizado_em = ? WHERE equipe_id = ? AND usuario_id = ?";
    
    private static final String SELECT_MEMBROS_SQL = 
        "SELECT em.id, em.papel_equipe, em.data_entrada, em.ativo, " +
        "u.id as usuario_id, u.nome, u.email, u.cargo, u.ativo as usuario_ativo " +
        "FROM equipe_membro em " +
        "INNER JOIN usuario u ON em.usuario_id = u.id " +
        "WHERE em.equipe_id = ? AND em.ativo = true ORDER BY u.nome";
    
    private static final String COUNT_GERENTES_EQUIPE_SQL = 
        "SELECT COUNT(*) FROM equipe_membro WHERE equipe_id = ? AND papel_equipe = 'GERENTE' AND ativo = true";

    @Override
    public Equipe save(Equipe equipe) throws SQLException {
        if (equipe == null) {
            throw new IllegalArgumentException("Equipe não pode ser nula");
        }
        
        validateEquipe(equipe);
        
        logger.debug("Salvando nova equipe: {}", equipe.getNome());
        
        try (Connection connection = DatabaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                // Inserir a equipe
                try (PreparedStatement stmt = connection.prepareStatement(INSERT_EQUIPE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    LocalDateTime now = LocalDateTime.now();
                    
                    stmt.setString(1, equipe.getNome());
                    stmt.setString(2, equipe.getDescricao());
                    stmt.setBoolean(3, equipe.isAtiva());
                    if (equipe.getMembros().isEmpty()) {
                        stmt.setNull(4, Types.BIGINT);
                    } else {
                        // O gerente será definido através dos membros
                        stmt.setNull(4, Types.BIGINT);
                    }
                    stmt.setTimestamp(5, Timestamp.valueOf(now));
                    stmt.setTimestamp(6, Timestamp.valueOf(now));
                    
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Falha ao salvar equipe, nenhuma linha afetada");
                    }
                    
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            equipe.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Falha ao salvar equipe, ID não foi gerado");
                        }
                    }
                }
                
                // Salvar membros da equipe
                salvarMembros(equipe, connection);
                
                // Commit da transação
                connection.commit();
                logger.info("Equipe salva com sucesso: {} (ID: {})", equipe.getNome(), equipe.getId());
                return equipe;
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao salvar equipe: {}", equipe.getNome(), e);
            throw e;
        }
    }

    @Override
    public Equipe update(Equipe equipe) throws SQLException {
        if (equipe == null || equipe.getId() == null) {
            throw new IllegalArgumentException("Equipe e ID não podem ser nulos");
        }
        
        validateEquipe(equipe);
        
        logger.debug("Atualizando equipe: {} (ID: {})", equipe.getNome(), equipe.getId());
        
        try (Connection connection = DatabaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                // Atualizar dados da equipe
                try (PreparedStatement stmt = connection.prepareStatement(UPDATE_EQUIPE_SQL)) {
                    stmt.setString(1, equipe.getNome());
                    stmt.setString(2, equipe.getDescricao());
                    stmt.setBoolean(3, equipe.isAtiva());
                    
                    // Encontrar o gerente entre os membros
                    Usuario gerente = equipe.getMembros().stream()
                        .filter(u -> "GERENTE".equals(u.getCargo())) // Assumindo que cargo indica papel na equipe
                        .findFirst()
                        .orElse(null);
                    
                    if (gerente != null) {
                        stmt.setLong(4, gerente.getId());
                    } else {
                        stmt.setNull(4, Types.BIGINT);
                    }
                    
                    stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setLong(6, equipe.getId());
                    
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Equipe não encontrada para atualização");
                    }
                }
                
                // Remover membros existentes e adicionar novos
                try (PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM equipe_membro WHERE equipe_id = ?")) {
                    deleteStmt.setLong(1, equipe.getId());
                    deleteStmt.executeUpdate();
                }
                
                // Salvar novos membros
                salvarMembros(equipe, connection);
                
                connection.commit();
                logger.info("Equipe atualizada com sucesso: {} (ID: {})", equipe.getNome(), equipe.getId());
                return equipe;
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao atualizar equipe: {} (ID: {})", equipe.getNome(), equipe.getId(), e);
            throw e;
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        logger.debug("Excluindo equipe com ID: {}", id);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(DELETE_EQUIPE_SQL)) {
            
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Equipe não encontrada para exclusão");
            }
            
            logger.info("Equipe excluída com sucesso (ID: {})", id);
            
        } catch (SQLException e) {
            logger.error("Erro ao excluir equipe (ID: {})", id, e);
            throw e;
        }
    }

    @Override
    public Optional<Equipe> findById(Long id) throws SQLException {
        if (id == null) {
            return Optional.empty();
        }
        
        logger.debug("Buscando equipe por ID: {}", id);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Equipe equipe = mapResultSetToEquipe(rs);
                    carregarMembros(equipe, connection);
                    return Optional.of(equipe);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar equipe por ID: {}", id, e);
            throw e;
        }
        
        return Optional.empty();
    }

    @Override
    public List<Equipe> findAll() throws SQLException {
        return executeQuery(SELECT_ALL_SQL);
    }
    
    /**
     * Lista apenas equipes ativas
     */
    public List<Equipe> findAllActive() throws SQLException {
        return executeQuery(SELECT_ACTIVE_SQL);
    }

    @Override
    public boolean exists(Long id) throws SQLException {
        if (id == null) {
            return false;
        }
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(EXISTS_SQL)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência da equipe (ID: {})", id, e);
            throw e;
        }
    }

    @Override
    public long count() throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(COUNT_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
            
        } catch (SQLException e) {
            logger.error("Erro ao contar equipes", e);
            throw e;
        }
    }
    
    /**
     * Verifica se existe equipe com o nome informado
     */
    public boolean existsByNome(String nome) throws SQLException {
        return existsByNomeForOtherEquipe(nome, -1L);
    }
    
    /**
     * Verifica se existe outra equipe com o mesmo nome
     */
    public boolean existsByNomeForOtherEquipe(String nome, Long equipeId) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(EXISTS_NOME_SQL)) {
            
            stmt.setString(1, nome.trim());
            stmt.setLong(2, equipeId != null ? equipeId : -1L);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência do nome da equipe: {}", nome, e);
            throw e;
        }
    }

    /**
     * Método auxiliar para executar queries que retornam lista de equipes
     */
    private List<Equipe> executeQuery(String sql, Object... parameters) throws SQLException {
        List<Equipe> equipes = new ArrayList<>();
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Equipe equipe = mapResultSetToEquipe(rs);
                    carregarMembros(equipe, connection);
                    equipes.add(equipe);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao executar query de equipes: {}", sql, e);
            throw e;
        }
        
        return equipes;
    }

    /**
     * Mapeia ResultSet para objeto Equipe
     */
    private Equipe mapResultSetToEquipe(ResultSet rs) throws SQLException {
        Equipe equipe = new Equipe();
        
        equipe.setId(rs.getLong("id"));
        equipe.setNome(rs.getString("nome"));
        equipe.setDescricao(rs.getString("descricao"));
        equipe.setAtiva(rs.getBoolean("ativa"));
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            equipe.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) {
            equipe.setAtualizadoEm(atualizadoEm.toLocalDateTime());
        }
        
        return equipe;
    }
    
    /**
     * Carrega os membros de uma equipe
     */
    private void carregarMembros(Equipe equipe, Connection connection) throws SQLException {
        List<Usuario> membros = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_MEMBROS_SQL)) {
            stmt.setLong(1, equipe.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getLong("usuario_id"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setCargo(rs.getString("papel_equipe")); // Papel na equipe vai para cargo temporariamente
                    usuario.setAtivo(rs.getBoolean("usuario_ativo"));
                    
                    membros.add(usuario);
                }
            }
        }
        
        equipe.setMembros(membros);
    }
    
    /**
     * Salva os membros de uma equipe
     */
    private void salvarMembros(Equipe equipe, Connection connection) throws SQLException {
        if (equipe.getMembros() == null || equipe.getMembros().isEmpty()) {
            return;
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_MEMBRO_SQL)) {
            LocalDateTime now = LocalDateTime.now();
            
            for (Usuario membro : equipe.getMembros()) {
                stmt.setLong(1, equipe.getId());
                stmt.setLong(2, membro.getId());
                // Se o cargo do usuário for GERENTE, definir como GERENTE na equipe, senão COLABORADOR
                String papelEquipe = "GERENTE".equals(membro.getCargo()) ? "GERENTE" : "COLABORADOR";
                stmt.setString(3, papelEquipe);
                stmt.setDate(4, Date.valueOf(now.toLocalDate()));
                stmt.setBoolean(5, true);
                stmt.setTimestamp(6, Timestamp.valueOf(now));
                stmt.setTimestamp(7, Timestamp.valueOf(now));
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    /**
     * Valida os dados da equipe
     */
    private void validateEquipe(Equipe equipe) {
        if (equipe.getNome() == null || equipe.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da equipe é obrigatório");
        }
        
        if (equipe.getNome().length() > 100) {
            throw new IllegalArgumentException("Nome da equipe deve ter no máximo 100 caracteres");
        }
        
        if (equipe.getDescricao() != null && equipe.getDescricao().length() > 1000) {
            throw new IllegalArgumentException("Descrição deve ter no máximo 1000 caracteres");
        }
    }
}