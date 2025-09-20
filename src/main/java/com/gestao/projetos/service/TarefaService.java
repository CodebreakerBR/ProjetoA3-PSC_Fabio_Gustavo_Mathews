package com.gestao.projetos.service;

import com.gestao.projetos.dao.TarefaDAO;
import com.gestao.projetos.model.Tarefa;
import com.gestao.projetos.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TarefaService {

    private static final Logger logger = LoggerFactory.getLogger(TarefaService.class);
    private final TarefaDAO tarefaDAO;

    public TarefaService() {
        this.tarefaDAO = new TarefaDAO();
    }

    private void validarTarefa(Tarefa tarefa) {
        if (!tarefa.isValid()) {
            if (!ValidationUtil.isNotEmpty(tarefa.getTitulo())) {
                throw new IllegalArgumentException("O título da tarefa é obrigatório.");
            }
            if (tarefa.getProjetoId() == null) {
                throw new IllegalArgumentException("A tarefa deve estar associada a um projeto.");
            }
            throw new IllegalArgumentException("Dados da tarefa inválidos. Verifique os campos.");
        }
    }

    public Tarefa salvar(Tarefa tarefa) throws SQLException {
        if (tarefa == null) {
            throw new IllegalArgumentException("O objeto Tarefa não pode ser nulo.");
        }
        validarTarefa(tarefa);
        logger.info("Salvando nova tarefa: {}", tarefa.getTitulo());
        return tarefaDAO.save(tarefa);
    }

    public Tarefa atualizar(Tarefa tarefa) throws SQLException {
        if (tarefa == null || tarefa.getId() == null) {
            throw new IllegalArgumentException("Tarefa ou ID da tarefa não pode ser nulo para atualização.");
        }
        if (!tarefaDAO.exists(tarefa.getId())) {
            throw new IllegalArgumentException("Tarefa não encontrada para o ID: " + tarefa.getId());
        }
        validarTarefa(tarefa);
        logger.info("Atualizando tarefa ID {}: {}", tarefa.getId(), tarefa.getTitulo());
        return tarefaDAO.update(tarefa);
    }

    public void remover(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de tarefa inválido.");
        }
        if (!tarefaDAO.exists(id)) {
            throw new IllegalArgumentException("Tarefa não encontrada para o ID: " + id);
        }
        logger.info("Removendo tarefa ID {}", id);
        tarefaDAO.delete(id);
    }

    public Optional<Tarefa> buscarPorId(Long id) throws SQLException {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return tarefaDAO.findById(id);
    }

    public List<Tarefa> listarTodas() throws SQLException {
        return tarefaDAO.findAll();
    }

    public List<Tarefa> pesquisar(String termo) throws SQLException {
        if (!ValidationUtil.isNotEmpty(termo)) {
            return listarTodas();
        }

        List<Tarefa> todas = listarTodas();
        String termoBusca = termo.toLowerCase().trim();

        return todas.stream()
                .filter(t -> t.getTitulo().toLowerCase().contains(termoBusca) ||
                        (t.getDescricao() != null && t.getDescricao().toLowerCase().contains(termoBusca)))
                .collect(Collectors.toList());
    }
}