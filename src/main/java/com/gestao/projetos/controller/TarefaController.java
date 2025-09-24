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
    private TarefaFrame tarefaFrame;
    private final TarefaService tarefaService;

    public TarefaController(TarefaFrame view) {
        this.tarefaFrame = view;
        this.tarefaService = new TarefaService();
    }

    public void carregarTarefas() {
        try {
            List<Tarefa> tarefas = tarefaService.listarTodas();
            atualizarView(tarefas);
            logger.debug("Carregadas {} tarefas", tarefas.size());
        } catch (SQLException e) {
            logger.error("Erro ao carregar tarefas", e);
            showError("Erro ao carregar tarefas: " + e.getMessage());
        }
    }

    public void pesquisarTarefasOld(String termo) {
        try {
            List<Tarefa> tarefas = tarefaService.pesquisar(termo);
            atualizarView(tarefas);
            logger.debug("Encontradas {} tarefas para o termo '{}'", tarefas.size(), termo);
        } catch (SQLException e) {
            logger.error("Erro ao pesquisar tarefas", e);
            showError("Erro ao pesquisar tarefas: " + e.getMessage());
        }
    }

    public void selecionarTarefa(Long id) {
        try {
            Optional<Tarefa> tarefaOpt = tarefaService.buscarPorId(id);
            // A nova TarefaFrame gerencia sua própria seleção
            carregarTarefas();
        } catch (SQLException e) {
            logger.error("Erro ao selecionar tarefa", e);
            showError("Erro ao selecionar tarefa: " + e.getMessage());
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
                showSuccess("Tarefa criada com sucesso!");
            } else {
                tarefaService.atualizar(tarefa);
                showSuccess("Tarefa atualizada com sucesso!");
            }

            // A nova TarefaFrame gerencia sua própria edição
            carregarTarefas();

        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos para salvar tarefa: {}", e.getMessage());
            showError(e.getMessage());
        } catch (SQLException e) {
            logger.error("Erro de banco de dados ao salvar tarefa", e);
            showError("Erro ao salvar tarefa: " + e.getMessage());
        }
    }

    public void excluirTarefa(Long id) {
        boolean confirmar = false;
        if (tarefaFrame != null) {
            confirmar = tarefaFrame.confirmarAcao("Deseja realmente excluir esta tarefa?");
        }
        
        if (!confirmar) {
            return;
        }
        try {
            tarefaService.remover(id);
            showSuccess("Tarefa excluída com sucesso!");
            carregarTarefas();
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao excluir tarefa: {}", e.getMessage());
            showError(e.getMessage());
        } catch (SQLException e) {
            logger.error("Erro de banco de dados ao excluir tarefa", e);
            showError("Erro ao excluir tarefa: " + e.getMessage());
        }
    }

    // Novos métodos para a interface melhorada
    public Tarefa buscarTarefaPorId(Long id) throws SQLException {
        Optional<Tarefa> tarefa = tarefaService.buscarPorId(id);
        return tarefa.orElse(null);
    }

    public void criarTarefa(Tarefa tarefa) throws SQLException {
        tarefaService.salvar(tarefa);
    }

    public void atualizarTarefa(Tarefa tarefa) throws SQLException {
        tarefaService.atualizar(tarefa);
    }

    public void removerTarefa(Long id) throws SQLException {
        tarefaService.remover(id);
    }

    public void pesquisarTarefas(String termo) throws SQLException {
        List<Tarefa> tarefas = tarefaService.pesquisar(termo);
        atualizarView(tarefas);
    }

    public void filtrarPorProjeto(Long projetoId) throws SQLException {
        List<Tarefa> tarefas = tarefaService.listarPorProjeto(projetoId);
        atualizarView(tarefas);
    }

    public void filtrarPorEquipe(Long equipeId) throws SQLException {
        List<Tarefa> tarefas = tarefaService.listarPorEquipe(equipeId);
        atualizarView(tarefas);
    }

    public void filtrarPorStatus(StatusTarefa status) throws SQLException {
        List<Tarefa> tarefas = tarefaService.listarPorStatus(status);
        atualizarView(tarefas);
    }

    public void carregarTarefasAtrasadas() throws SQLException {
        List<Tarefa> tarefas = tarefaService.listarTarefasAtrasadas();
        atualizarView(tarefas);
    }

    private void atualizarView(List<Tarefa> tarefas) {
        if (tarefaFrame != null) {
            tarefaFrame.atualizarTabela(tarefas);
        }
    }

    private void showError(String message) {
        if (tarefaFrame != null) {
            tarefaFrame.showError(message);
        }
    }

    private void showSuccess(String message) {
        if (tarefaFrame != null) {
            tarefaFrame.showSuccess(message);
        }
    }
}
