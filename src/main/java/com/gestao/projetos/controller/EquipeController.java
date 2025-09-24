package com.gestao.projetos.controller;

import com.gestao.projetos.model.Equipe;
import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.service.EquipeService;
import com.gestao.projetos.view.EquipeFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para operações relacionadas a equipes
 */
public class EquipeController {
    
    private static final Logger logger = LoggerFactory.getLogger(EquipeController.class);
    private final EquipeFrame view;
    private final EquipeService equipeService;

    public EquipeController(EquipeFrame view) {
        this.view = view;
        this.equipeService = new EquipeService();
    }

    /**
     * Carrega todas as equipes na tabela
     */
    public void carregarEquipes() {
        try {
            List<Equipe> equipes = equipeService.listarTodas();
            view.atualizarTabela(equipes);
        } catch (SecurityException e) {
            logger.warn("Acesso negado ao carregar equipes: {}", e.getMessage());
            view.showError("Você não tem permissão para visualizar equipes");
        } catch (SQLException e) {
            logger.error("Erro ao carregar equipes", e);
            view.showError("Erro ao carregar equipes: " + e.getMessage());
        }
    }

    /**
     * Carrega apenas equipes ativas na tabela
     */
    public void carregarEquipesAtivas() {
        try {
            List<Equipe> equipes = equipeService.listarAtivas();
            view.atualizarTabela(equipes);
        } catch (SecurityException e) {
            logger.warn("Acesso negado ao carregar equipes ativas: {}", e.getMessage());
            view.showError("Você não tem permissão para visualizar equipes");
        } catch (SQLException e) {
            logger.error("Erro ao carregar equipes ativas", e);
            view.showError("Erro ao carregar equipes: " + e.getMessage());
        }
    }

    /**
     * Seleciona uma equipe pelo ID
     */
    public void selecionarEquipe(Long id) {
        try {
            Optional<Equipe> equipeOpt = equipeService.buscarPorId(id);
            if (equipeOpt.isPresent()) {
                view.selecionarEquipe(equipeOpt.get());
            } else {
                view.showError("Equipe não encontrada");
            }
        } catch (SecurityException e) {
            logger.warn("Acesso negado ao selecionar equipe: {}", e.getMessage());
            view.showError("Você não tem permissão para visualizar equipes");
        } catch (SQLException e) {
            logger.error("Erro ao selecionar equipe (ID: {})", id, e);
            view.showError("Erro ao carregar equipe: " + e.getMessage());
        }
    }

    /**
     * Cria uma nova equipe
     */
    public void criarEquipe(String nome, String descricao, List<Long> membrosIds, Long gerenteId) {
        try {
            // Validações básicas
            if (nome == null || nome.trim().isEmpty()) {
                view.showError("Nome da equipe é obrigatório");
                return;
            }
            
            if (membrosIds == null || membrosIds.isEmpty()) {
                view.showError("A equipe deve ter pelo menos um membro");
                return;
            }
            
            if (gerenteId == null) {
                view.showError("É necessário definir um gerente para a equipe");
                return;
            }
            
            Equipe equipe = equipeService.criarEquipe(nome.trim(), 
                                                     descricao != null ? descricao.trim() : null, 
                                                     membrosIds, 
                                                     gerenteId);
            
            view.showSuccess("Equipe criada com sucesso: " + equipe.getNome());
            view.finalizarEdicao();
            carregarEquipes();
            
        } catch (SecurityException e) {
            logger.warn("Acesso negado ao criar equipe: {}", e.getMessage());
            view.showError("Você não tem permissão para criar equipes");
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao criar equipe: {}", e.getMessage());
            view.showError(e.getMessage());
        } catch (SQLException e) {
            logger.error("Erro ao criar equipe: {}", nome, e);
            view.showError("Erro ao criar equipe: " + e.getMessage());
        }
    }

    /**
     * Atualiza uma equipe existente
     */
    public void atualizarEquipe(Long id, String nome, String descricao, List<Long> membrosIds, Long gerenteId) {
        try {
            // Validações básicas
            if (id == null) {
                view.showError("ID da equipe não pode ser nulo");
                return;
            }
            
            if (nome == null || nome.trim().isEmpty()) {
                view.showError("Nome da equipe é obrigatório");
                return;
            }
            
            if (membrosIds == null || membrosIds.isEmpty()) {
                view.showError("A equipe deve ter pelo menos um membro");
                return;
            }
            
            if (gerenteId == null) {
                view.showError("É necessário definir um gerente para a equipe");
                return;
            }
            
            Equipe equipe = equipeService.atualizarEquipe(id, 
                                                         nome.trim(), 
                                                         descricao != null ? descricao.trim() : null, 
                                                         membrosIds, 
                                                         gerenteId);
            
            view.showSuccess("Equipe atualizada com sucesso: " + equipe.getNome());
            view.finalizarEdicao();
            carregarEquipes();
            
        } catch (SecurityException e) {
            logger.warn("Acesso negado ao atualizar equipe: {}", e.getMessage());
            view.showError("Você não tem permissão para atualizar equipes");
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao atualizar equipe: {}", e.getMessage());
            view.showError(e.getMessage());
        } catch (SQLException e) {
            logger.error("Erro ao atualizar equipe (ID: {}): {}", id, nome, e);
            view.showError("Erro ao atualizar equipe: " + e.getMessage());
        }
    }

    /**
     * Exclui uma equipe
     */
    public void excluirEquipe(Long id) {
        try {
            if (id == null) {
                view.showError("ID da equipe não pode ser nulo");
                return;
            }
            
            // Confirmar exclusão
            if (!view.confirmarAcao("Tem certeza que deseja excluir esta equipe?\nEsta ação não pode ser desfeita.")) {
                return;
            }
            
            equipeService.removerEquipe(id);
            
            view.showSuccess("Equipe excluída com sucesso");
            view.finalizarEdicao();
            carregarEquipes();
            
        } catch (SecurityException e) {
            logger.warn("Acesso negado ao excluir equipe: {}", e.getMessage());
            view.showError("Você não tem permissão para excluir equipes");
        } catch (IllegalArgumentException e) {
            logger.warn("Erro ao excluir equipe: {}", e.getMessage());
            view.showError(e.getMessage());
        } catch (SQLException e) {
            logger.error("Erro ao excluir equipe (ID: {})", id, e);
            view.showError("Erro ao excluir equipe: " + e.getMessage());
        }
    }

    /**
     * Altera o status de uma equipe (ativa/inativa)
     */
    public void alterarStatusEquipe(Long id, boolean ativa) {
        try {
            if (id == null) {
                view.showError("ID da equipe não pode ser nulo");
                return;
            }
            
            Equipe equipe = equipeService.alterarStatus(id, ativa);
            
            String status = ativa ? "ativada" : "desativada";
            view.showSuccess("Equipe " + status + " com sucesso: " + equipe.getNome());
            carregarEquipes();
            
        } catch (SecurityException e) {
            logger.warn("Acesso negado ao alterar status da equipe: {}", e.getMessage());
            view.showError("Você não tem permissão para alterar status de equipes");
        } catch (IllegalArgumentException e) {
            logger.warn("Erro ao alterar status da equipe: {}", e.getMessage());
            view.showError(e.getMessage());
        } catch (SQLException e) {
            logger.error("Erro ao alterar status da equipe (ID: {})", id, e);
            view.showError("Erro ao alterar status da equipe: " + e.getMessage());
        }
    }

    /**
     * Carrega usuários disponíveis para serem membros de equipes
     */
    public void carregarUsuariosDisponiveis() {
        try {
            List<Usuario> usuarios = equipeService.listarUsuariosDisponiveis();
            view.atualizarUsuariosDisponiveis(usuarios);
        } catch (SecurityException e) {
            logger.warn("Acesso negado ao carregar usuários disponíveis: {}", e.getMessage());
            view.showError("Você não tem permissão para acessar esta funcionalidade");
        } catch (SQLException e) {
            logger.error("Erro ao carregar usuários disponíveis", e);
            view.showError("Erro ao carregar usuários: " + e.getMessage());
        }
    }

    /**
     * Obtém estatísticas de equipes
     */
    public void obterEstatisticas() {
        try {
            List<Equipe> todasEquipes = equipeService.listarTodas();
            List<Equipe> equipesAtivas = equipeService.listarAtivas();
            
            int totalEquipes = todasEquipes.size();
            int equipesAtivasCount = equipesAtivas.size();
            int equipesInativas = totalEquipes - equipesAtivasCount;
            
            // Calcular total de membros em equipes ativas
            int totalMembros = equipesAtivas.stream()
                    .mapToInt(equipe -> equipe.getMembros().size())
                    .sum();
            
            String estatisticas = String.format(
                "Estatísticas de Equipes:\n" +
                "• Total de equipes: %d\n" +
                "• Equipes ativas: %d\n" +
                "• Equipes inativas: %d\n" +
                "• Total de membros em equipes ativas: %d",
                totalEquipes, equipesAtivasCount, equipesInativas, totalMembros
            );
            
            view.mostrarEstatisticas(estatisticas);
            
        } catch (SecurityException e) {
            logger.warn("Acesso negado ao obter estatísticas: {}", e.getMessage());
            view.showError("Você não tem permissão para visualizar estatísticas");
        } catch (SQLException e) {
            logger.error("Erro ao obter estatísticas de equipes", e);
            view.showError("Erro ao carregar estatísticas: " + e.getMessage());
        }
    }
}