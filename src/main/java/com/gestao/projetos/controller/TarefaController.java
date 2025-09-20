package com.gestao.projetos.controller;

import com.gestao.projetos.model.StatusTarefa;
import com.gestao.projetos.model.Tarefa;
import com.gestao.projetos.service.TarefaService;
import com.gestao.projetos.view.TarefaFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TarefaController {

    private static final Logger logger = LoggerFactory.getLogger(TarefaController.class);
    private final TarefaFrame view;
    private final TarefaService tarefaService;

    public TarefaController(TarefaFrame view) {
        this.view = view;
        this.tarefaService = new TarefaService();
    }

    public void carregarTarefas() {
        try {
            List<Tarefa> tarefas = tarefaService.listarTodas();
            view.atualizarTabela(tarefas);
            logger.debug("Carregadas {} tarefas", tarefas.size());
        } catch (SQLException e) {
            logger.error("Erro ao carregar tarefas", e);
            view.showError("Erro ao carregar tarefas: " + e.getMessage());
        }
    }

    public void pesquisarTarefas(String termo) {
        try {
            List<Tarefa> tarefas = tarefaService.pesquisar(termo);
            view.atualizarTabela(tarefas);
            logger.debug("Encontradas {} tarefas para o termo '{}'", tarefas.size(), termo);
        } catch (SQLException e) {
            logger.error("Erro ao pesquisar tarefas", e);
            view.showError("Erro ao pesquisar tarefas: " + e.getMessage());
        }
    }

    public void selecionarTarefa(Long id) {
        try {
            Optional<Tarefa> tarefaOpt = tarefaService.buscarPorId(id);
            view.selecionarTarefa(tarefaOpt.orElse(null));
        } catch (SQLException e) {
            logger.error("Erro ao selecionar tarefa", e);
            view.showError("Erro ao selecionar tarefa: " + e.getMessage());
        }
    }

    public void salvarTarefa(String titulo, String descricao, StatusTarefa status, int prioridade, Double estimativaHoras,
                             Double horasTrabalhadas, LocalDate dataFimPrevista, Long projetoId, Long responsavelId, Tarefa tarefaExistente) {
        try {
            Tarefa tarefa;
            if (tarefaExistente == null) {
                tarefa = new Tarefa();
            } else {
                tarefa = tarefaExistente;
            }

            tarefa.setTitulo(titulo);
            tarefa.setDescricao(descricao);
            tarefa.setStatus(status);
            tarefa.setPrioridade(prioridade);
            tarefa.setEstimativaHoras(estimativaHoras);
            tarefa.setHorasTrabalhadas(horasTrabalhadas);
            tarefa.setDataFimPrevista(dataFimPrevista);
            tarefa.setProjetoId(projetoId);
            tarefa.setResponsavelId(responsavelId);

            if (tarefa.getId() == null) {
                tarefaService.salvar(tarefa);
                view.showSuccess("Tarefa criada com sucesso!");
            } else {
                tarefaService.atualizar(tarefa);
                view.showSuccess("Tarefa atualizada com sucesso!");
            }

            view.finalizarEdicao();
            carregarTarefas();

        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos para salvar tarefa: {}", e.getMessage());
            view.showError(e.getMessage());
        } catch (SQLException e) {
            logger.error("Erro de banco de dados ao salvar tarefa", e);
            view.showError("Erro ao salvar tarefa: " + e.getMessage());
        }
    }

    public void excluirTarefa(Long id) {
        if (!view.confirmarAcao("Deseja realmente excluir esta tarefa?")) {
            return;
        }
        try {
            tarefaService.remover(id);
            view.showSuccess("Tarefa excluída com sucesso!");
            view.finalizarEdicao();
            carregarTarefas();
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao excluir tarefa: {}", e.getMessage());
            view.showError(e.getMessage());
        } catch (SQLException e) {
            logger.error("Erro de banco de dados ao excluir tarefa", e);
            view.showError("Erro ao excluir tarefa: " + e.getMessage());
        }
    }
}
