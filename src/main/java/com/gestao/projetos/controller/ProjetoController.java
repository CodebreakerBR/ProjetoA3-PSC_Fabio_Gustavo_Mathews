package com.gestao.projetos.controller;

import com.gestao.projetos.model.Projeto;
import com.gestao.projetos.model.Equipe;
import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.model.StatusProjeto;
import com.gestao.projetos.service.ProjetoService;
import com.gestao.projetos.service.EquipeService;
import com.gestao.projetos.service.UsuarioService;
import com.gestao.projetos.view.ProjetoFrame;
import com.gestao.projetos.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para operações relacionadas a projetos
 */
public class ProjetoController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjetoController.class);
    private final ProjetoFrame view;
    private final ProjetoService projetoService;
    private final EquipeService equipeService;
    private final UsuarioService usuarioService;

    public ProjetoController(ProjetoFrame view) {
        this.view = view;
        this.projetoService = new ProjetoService();
        this.equipeService = new EquipeService();
        this.usuarioService = new UsuarioService();
    }

    /**
     * Carrega todos os projetos na tabela
     */
    public void carregarProjetos() {
        try {
            List<Projeto> projetos = projetoService.listarTodos();
            view.atualizarTabela(projetos);
        } catch (SQLException e) {
            logger.error("Erro ao carregar projetos", e);
            view.exibirMensagemErro("Erro ao carregar projetos: " + e.getMessage());
        }
    }

    /**
     * Pesquisa projetos por termo
     */
    public void pesquisarProjetos(String termo) {
        try {
            List<Projeto> projetos = projetoService.pesquisar(termo);
            view.atualizarTabela(projetos);
        } catch (SQLException e) {
            logger.error("Erro ao pesquisar projetos", e);
            view.exibirMensagemErro("Erro ao pesquisar projetos: " + e.getMessage());
        }
    }

    /**
     * Seleciona um projeto pelo ID
     */
    public void selecionarProjeto(Long id) {
        try {
            Optional<Projeto> projeto = projetoService.buscarPorId(id);
            if (projeto.isPresent()) {
                view.selecionarProjeto(projeto.get());
            } else {
                view.exibirMensagemAviso("Projeto não encontrado");
            }
        } catch (SQLException e) {
            logger.error("Erro ao selecionar projeto", e);
            view.exibirMensagemErro("Erro ao selecionar projeto: " + e.getMessage());
        }
    }

    /**
     * Cria um novo projeto
     */
    public void criarProjeto(String nome, String descricao, LocalDate dataInicio, 
                           LocalDate dataTerminoPrevista, String status, Long gerenteId) {
        try {
            if (nome == null || nome.trim().isEmpty()) {
                view.exibirMensagemAviso("Nome do projeto é obrigatório");
                return;
            }

            if (dataInicio != null && dataTerminoPrevista != null && 
                dataInicio.isAfter(dataTerminoPrevista)) {
                view.exibirMensagemAviso("Data de início não pode ser posterior à data de término");
                return;
            }

            Projeto projeto = new Projeto();
            projeto.setNome(nome);
            projeto.setDescricao(descricao);
            projeto.setDataInicio(dataInicio);
            projeto.setDataFimPrevista(dataTerminoPrevista);
            projeto.setStatus(status);
            projeto.setGerenteId(gerenteId);

            projeto = projetoService.criar(projeto);
            
            logger.info("Projeto criado com sucesso: ID={}, Nome={}", projeto.getId(), projeto.getNome());
            view.exibirMensagemSucesso("Projeto criado com sucesso!");
            view.finalizarEdicao();
            carregarProjetos();
            
        } catch (SQLException e) {
            logger.error("Erro ao criar projeto", e);
            view.exibirMensagemErro("Erro ao criar projeto: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar projeto", e);
            view.exibirMensagemErro("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Atualiza um projeto existente
     */
    public void atualizarProjeto(Long id, String nome, String descricao, LocalDate dataInicio, 
                               LocalDate dataTerminoPrevista, String status, Long gerenteId) {
        try {
            if (nome == null || nome.trim().isEmpty()) {
                view.exibirMensagemAviso("Nome do projeto é obrigatório");
                return;
            }

            if (dataInicio != null && dataTerminoPrevista != null && 
                dataInicio.isAfter(dataTerminoPrevista)) {
                view.exibirMensagemAviso("Data de início não pode ser posterior à data de término");
                return;
            }

            Optional<Projeto> projetoExistente = projetoService.buscarPorId(id);
            if (!projetoExistente.isPresent()) {
                view.exibirMensagemAviso("Projeto não encontrado");
                return;
            }

            Projeto projeto = projetoExistente.get();
            projeto.setNome(nome);
            projeto.setDescricao(descricao);
            projeto.setDataInicio(dataInicio);
            projeto.setDataFimPrevista(dataTerminoPrevista);
            projeto.setStatus(status);
            projeto.setGerenteId(gerenteId);

            projetoService.atualizar(projeto);
            
            logger.info("Projeto atualizado com sucesso: ID={}, Nome={}", projeto.getId(), projeto.getNome());
            view.exibirMensagemSucesso("Projeto atualizado com sucesso!");
            view.finalizarEdicao();
            carregarProjetos();
            
        } catch (SQLException e) {
            logger.error("Erro ao atualizar projeto", e);
            view.exibirMensagemErro("Erro ao atualizar projeto: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar projeto", e);
            view.exibirMensagemErro("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Exclui um projeto
     */
    public void excluirProjeto() {
        try {
            Projeto projetoSelecionado = view.getProjetoSelecionado();
            if (projetoSelecionado == null) {
                view.exibirMensagemAviso("Selecione um projeto para excluir");
                return;
            }

            int confirmacao = JOptionPane.showConfirmDialog(
                view,
                "Tem certeza que deseja excluir o projeto '" + projetoSelecionado.getNome() + "'?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (confirmacao == JOptionPane.YES_OPTION) {
                projetoService.excluir(projetoSelecionado.getId());
                
                logger.info("Projeto excluído com sucesso: ID={}, Nome={}", 
                           projetoSelecionado.getId(), projetoSelecionado.getNome());
                view.exibirMensagemSucesso("Projeto excluído com sucesso!");
                carregarProjetos();
                view.limparSelecao();
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao excluir projeto", e);
            view.exibirMensagemErro("Erro ao excluir projeto: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao excluir projeto", e);
            view.exibirMensagemErro("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Atribui uma equipe a um projeto
     */
    public void atribuirEquipe(Long projetoId, Long equipeId, String papelEquipe) {
        try {
            projetoService.atribuirEquipe(projetoId, equipeId, papelEquipe);
            
            logger.info("Equipe atribuída com sucesso: ProjetoID={}, EquipeID={}, Papel={}", 
                       projetoId, equipeId, papelEquipe);
            view.exibirMensagemSucesso("Equipe atribuída com sucesso!");
            
        } catch (SQLException e) {
            logger.error("Erro ao atribuir equipe ao projeto", e);
            view.exibirMensagemErro("Erro ao atribuir equipe: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao atribuir equipe", e);
            view.exibirMensagemErro("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Remove uma equipe de um projeto
     */
    public void removerEquipe(Long projetoId, Long equipeId) {
        try {
            projetoService.removerEquipe(projetoId, equipeId);
            
            logger.info("Equipe removida com sucesso: ProjetoID={}, EquipeID={}", 
                       projetoId, equipeId);
            view.exibirMensagemSucesso("Equipe removida com sucesso!");
            
        } catch (SQLException e) {
            logger.error("Erro ao remover equipe do projeto", e);
            view.exibirMensagemErro("Erro ao remover equipe: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao remover equipe", e);
            view.exibirMensagemErro("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Lista equipes atribuídas a um projeto
     */
    public List<Equipe> listarEquipesProjeto(Long projetoId) {
        try {
            return projetoService.listarEquipesProjeto(projetoId);
        } catch (SQLException e) {
            logger.error("Erro ao listar equipes do projeto", e);
            view.exibirMensagemErro("Erro ao listar equipes: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lista todas as equipes disponíveis
     */
    public List<Equipe> listarTodasEquipes() {
        try {
            return equipeService.listarTodas();
        } catch (SQLException e) {
            logger.error("Erro ao listar equipes", e);
            view.exibirMensagemErro("Erro ao listar equipes: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lista todos os usuários que podem ser gerentes
     */
    public List<Usuario> listarPossiveisGerentes() {
        try {
            return usuarioService.listarAtivos();
        } catch (SQLException e) {
            logger.error("Erro ao listar usuários", e);
            view.exibirMensagemErro("Erro ao listar usuários: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lista projetos por status
     */
    public void listarProjetosPorStatus(String status) {
        try {
            List<Projeto> projetos = projetoService.listarPorStatus(status);
            view.atualizarTabela(projetos);
        } catch (SQLException e) {
            logger.error("Erro ao listar projetos por status", e);
            view.exibirMensagemErro("Erro ao listar projetos: " + e.getMessage());
        }
    }

    /**
     * Lista projetos por gerente
     */
    public void listarProjetosPorGerente(Long gerenteId) {
        try {
            List<Projeto> projetos = projetoService.listarPorGerente(gerenteId);
            view.atualizarTabela(projetos);
        } catch (SQLException e) {
            logger.error("Erro ao listar projetos por gerente", e);
            view.exibirMensagemErro("Erro ao listar projetos: " + e.getMessage());
        }
    }

    /**
     * Lista projetos atrasados
     */
    public void listarProjetosAtrasados() {
        try {
            List<Projeto> projetos = projetoService.listarAtrasados();
            view.atualizarTabela(projetos);
        } catch (SQLException e) {
            logger.error("Erro ao listar projetos atrasados", e);
            view.exibirMensagemErro("Erro ao listar projetos atrasados: " + e.getMessage());
        }
    }

    /**
     * Obtém estatísticas de projetos
     */
    public void obterEstatisticas() {
        try {
            // Implementar quando necessário
            logger.info("Estatísticas solicitadas pelo usuário");
            
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas", e);
            view.exibirMensagemErro("Erro ao obter estatísticas: " + e.getMessage());
        }
    }

    /**
     * Valida se o usuário atual pode editar o projeto
     */
    public boolean podeEditarProjeto(Projeto projeto) {
        try {
            SessionManager sessionManager = SessionManager.getInstance();
            Usuario usuarioAtual = sessionManager.getCurrentUser();
            
            if (usuarioAtual == null) {
                return false;
            }

            // Administrador pode editar qualquer projeto
            if (sessionManager.isCurrentUserAdmin()) {
                return true;
            }

            // Gerente pode editar seus próprios projetos
            return projeto.getGerenteId() != null && 
                   projeto.getGerenteId().equals(usuarioAtual.getId());
                   
        } catch (Exception e) {
            logger.error("Erro ao verificar permissão de edição", e);
            return false;
        }
    }
}