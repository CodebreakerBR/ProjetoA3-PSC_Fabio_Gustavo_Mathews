package com.gestao.projetos.dao;

import com.gestao.projetos.model.StatusTarefa;
import com.gestao.projetos.model.Tarefa;
import com.gestao.projetos.model.Projeto;
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

public class TarefaDAO implements BaseDAO<Tarefa, Long> {

    private static final Logger logger = LoggerFactory.getLogger(TarefaDAO.class);

    private static final String INSERT_SQL =
            "INSERT INTO tarefa (titulo, descricao, status, prioridade, estimativa_horas, horas_trabalhadas, data_inicio, " +
                    "data_fim_prevista, data_fim_real, projeto_id, responsavel_id, equipe_id, criador_id, criado_em, atualizado_em) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE tarefa SET titulo = ?, descricao = ?, status = ?, prioridade = ?, estimativa_horas = ?, horas_trabalhadas = ?, " +
                    "data_inicio = ?, data_fim_prevista = ?, data_fim_real = ?, projeto_id = ?, responsavel_id = ?, equipe_id = ?, atualizado_em = ? WHERE id = ?";

    private static final String DELETE_SQL = "DELETE FROM tarefa WHERE id = ?";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM tarefa WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM tarefa ORDER BY data_fim_prevista, prioridade DESC";
    private static final String EXISTS_SQL = "SELECT 1 FROM tarefa WHERE id = ?";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM tarefa";

    @Override
    public Tarefa save(Tarefa tarefa) throws SQLException {
        if (tarefa == null || !tarefa.isValid()) {
            throw new IllegalArgumentException("Dados da tarefa inválidos para inserção.");
        }

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            tarefa.setCriadoEm(now);
            tarefa.setAtualizadoEm(now);

            statement.setString(1, tarefa.getTitulo());
            statement.setString(2, tarefa.getDescricao());
            statement.setString(3, tarefa.getStatus().getCodigo());
            statement.setInt(4, tarefa.getPrioridade());
            statement.setObject(5, tarefa.getEstimativaHoras());
            statement.setObject(6, tarefa.getHorasTrabalhadas());
            statement.setObject(7, tarefa.getDataInicioPrevista());
            statement.setObject(8, tarefa.getDataFimPrevista());
            statement.setObject(9, tarefa.getDataFimReal());
            statement.setLong(10, tarefa.getProjetoId());
            statement.setObject(11, tarefa.getResponsavelId(), Types.BIGINT);
            statement.setObject(12, tarefa.getEquipeId(), Types.BIGINT);
            statement.setObject(13, tarefa.getResponsavelId(), Types.BIGINT); // Usando responsável como criador_id
            statement.setTimestamp(14, Timestamp.valueOf(tarefa.getCriadoEm()));
            statement.setTimestamp(15, Timestamp.valueOf(tarefa.getAtualizadoEm()));

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tarefa.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Erro ao inserir tarefa, ID não foi gerado.");
                }
            }
            DatabaseUtil.commit(connection);
            return tarefa;

        } catch (SQLException e) {
            logger.error("Erro ao inserir tarefa: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Tarefa update(Tarefa tarefa) throws SQLException {
        if (tarefa == null || !tarefa.isValid() || tarefa.getId() == null) {
            throw new IllegalArgumentException("Dados da tarefa inválidos para atualização.");
        }
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            tarefa.setAtualizadoEm(LocalDateTime.now());

            statement.setString(1, tarefa.getTitulo());
            statement.setString(2, tarefa.getDescricao());
            statement.setString(3, tarefa.getStatus().getCodigo());
            statement.setInt(4, tarefa.getPrioridade());
            statement.setObject(5, tarefa.getEstimativaHoras());
            statement.setObject(6, tarefa.getHorasTrabalhadas());
            statement.setObject(7, tarefa.getDataInicioPrevista());
            statement.setObject(8, tarefa.getDataFimPrevista());
            statement.setObject(9, tarefa.getDataFimReal());
            statement.setLong(10, tarefa.getProjetoId());
            statement.setObject(11, tarefa.getResponsavelId(), Types.BIGINT);
            statement.setObject(12, tarefa.getEquipeId(), Types.BIGINT);
            statement.setTimestamp(13, Timestamp.valueOf(tarefa.getAtualizadoEm()));
            statement.setLong(14, tarefa.getId()); // WHERE

            statement.executeUpdate();
            DatabaseUtil.commit(connection);
            return tarefa;
        } catch (SQLException e) {
            logger.error("Erro ao atualizar tarefa: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido para exclusão.");
        }
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, id);
            statement.executeUpdate();
            DatabaseUtil.commit(connection);
            logger.info("Tentativa de exclusão para tarefa ID: {}", id);
        } catch (SQLException e) {
            logger.error("Erro ao excluir tarefa: {}", id, e);
            throw e;
        }
    }

    @Override
    public Optional<Tarefa> findById(Long id) throws SQLException {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToTarefa(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar tarefa por ID: {}", id, e);
            throw e;
        }
        return Optional.empty();
    }

    @Override
    public List<Tarefa> findAll() throws SQLException {
        List<Tarefa> tarefas = new ArrayList<>();
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tarefas.add(mapResultSetToTarefa(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar todas as tarefas", e);
            throw e;
        }
        return tarefas;
    }

    @Override
    public boolean exists(Long id) throws SQLException {
        if (id == null || id <= 0) {
            return false;
        }
        return findById(id).isPresent();
    }

    @Override
    public long count() throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Erro ao contar tarefas", e);
            throw e;
        }
        return 0;
    }

    private Tarefa mapResultSetToTarefa(ResultSet rs) throws SQLException {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(rs.getLong("id"));
        tarefa.setTitulo(rs.getString("titulo"));
        tarefa.setDescricao(rs.getString("descricao"));
        tarefa.setStatus(StatusTarefa.fromCodigo(rs.getString("status")));
        tarefa.setPrioridade(rs.getInt("prioridade"));
        tarefa.setEstimativaHoras(rs.getDouble("estimativa_horas"));
        tarefa.setHorasTrabalhadas(rs.getDouble("horas_trabalhadas"));

        Date dataInicio = rs.getDate("data_inicio");
        if (dataInicio != null) {
            tarefa.setDataInicioPrevista(dataInicio.toLocalDate());
        }

        Date dataFimPrevista = rs.getDate("data_fim_prevista");
        if (dataFimPrevista != null) {
            tarefa.setDataFimPrevista(dataFimPrevista.toLocalDate());
        }

        Date dataFimReal = rs.getDate("data_fim_real");
        if (dataFimReal != null) {
            tarefa.setDataFimReal(dataFimReal.toLocalDate());
        }

        // Definir IDs
        tarefa.setProjetoId(rs.getLong("projeto_id"));
        tarefa.setResponsavelId((Long) rs.getObject("responsavel_id"));
        tarefa.setEquipeId((Long) rs.getObject("equipe_id"));

        // Carregar objetos relacionados
        carregarObjetosRelacionados(tarefa);

        tarefa.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        tarefa.setAtualizadoEm(rs.getTimestamp("atualizado_em").toLocalDateTime());

        return tarefa;
    }

    private void carregarObjetosRelacionados(Tarefa tarefa) {
        try {
            // Carregar projeto
            if (tarefa.getProjetoId() != null) {
                ProjetoDAO projetoDAO = new ProjetoDAO();
                projetoDAO.findById(tarefa.getProjetoId()).ifPresent(tarefa::setProjeto);
            }

            // Carregar responsável
            if (tarefa.getResponsavelId() != null) {
                UsuarioDAO usuarioDAO = new UsuarioDAO();
                usuarioDAO.findById(tarefa.getResponsavelId()).ifPresent(tarefa::setResponsavel);
            }

            // Carregar equipe
            if (tarefa.getEquipeId() != null) {
                EquipeDAO equipeDAO = new EquipeDAO();
                equipeDAO.findById(tarefa.getEquipeId()).ifPresent(tarefa::setEquipe);
            }
            
        } catch (SQLException e) {
            logger.warn("Erro ao carregar objetos relacionados para tarefa {}: {}", tarefa.getId(), e.getMessage());
        }
    }

    public List<Tarefa> pesquisarPorTitulo(String termo) throws SQLException {
        String sql = "SELECT * FROM tarefa WHERE lower(titulo) LIKE ?";
        List<Tarefa> tarefas = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + termo.toLowerCase() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tarefas.add(mapResultSetToTarefa(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao pesquisar tarefas por título", e);
            throw e;
        }
        return tarefas;
    }

    public List<Tarefa> findByProjectoId(Long projetoId) throws SQLException {
        if (projetoId == null || projetoId <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM tarefa WHERE projeto_id = ? ORDER BY data_fim_prevista, prioridade DESC";
        List<Tarefa> tarefas = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, projetoId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tarefas.add(mapResultSetToTarefa(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar tarefas por projeto ID: {}", projetoId, e);
            throw e;
        }
        return tarefas;
    }

    public List<Tarefa> findByEquipeId(Long equipeId) throws SQLException {
        if (equipeId == null || equipeId <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM tarefa WHERE equipe_id = ? ORDER BY data_fim_prevista, prioridade DESC";
        List<Tarefa> tarefas = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, equipeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tarefas.add(mapResultSetToTarefa(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar tarefas por equipe ID: {}", equipeId, e);
            throw e;
        }
        return tarefas;
    }

    public List<Tarefa> findByStatus(StatusTarefa status) throws SQLException {
        if (status == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM tarefa WHERE status = ? ORDER BY data_fim_prevista, prioridade DESC";
        List<Tarefa> tarefas = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.getCodigo());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tarefas.add(mapResultSetToTarefa(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar tarefas por status: {}", status, e);
            throw e;
        }
        return tarefas;
    }

    public List<Tarefa> findByResponsavelId(Long responsavelId) throws SQLException {
        if (responsavelId == null || responsavelId <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM tarefa WHERE responsavel_id = ? ORDER BY data_fim_prevista, prioridade DESC";
        List<Tarefa> tarefas = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, responsavelId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tarefas.add(mapResultSetToTarefa(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar tarefas por responsável ID: {}", responsavelId, e);
            throw e;
        }
        return tarefas;
    }

    public List<Tarefa> findTarefasAtrasadas() throws SQLException {
        String sql = "SELECT * FROM tarefa WHERE data_fim_prevista < CURDATE() AND status NOT IN ('CONCLUIDA', 'CANCELADA') ORDER BY data_fim_prevista, prioridade DESC";
        List<Tarefa> tarefas = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                tarefas.add(mapResultSetToTarefa(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar tarefas atrasadas", e);
            throw e;
        }
        return tarefas;
    }
}