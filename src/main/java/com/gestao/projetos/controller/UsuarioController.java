package com.gestao.projetos.controller;

import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.service.UsuarioService;
import com.gestao.projetos.view.UsuarioFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para operações relacionadas a usuários
 */
public class UsuarioController {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private final UsuarioFrame view;
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioFrame view) {
        this.view = view;
        this.usuarioService = new UsuarioService();
    }

    /**
     * Carrega todos os usuários na tabela
     */
    public void carregarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.listarTodos();
            view.atualizarTabela(usuarios);
            logger.debug("Carregados {} usuários", usuarios.size());
            
        } catch (SQLException e) {
            logger.error("Erro ao carregar usuários", e);
            view.showError("Erro ao carregar usuários: " + e.getMessage());
        }
    }

    /**
     * Pesquisa usuários por termo
     */
    public void pesquisarUsuarios(String termo) {
        try {
            List<Usuario> usuarios = usuarioService.pesquisar(termo);
            view.atualizarTabela(usuarios);
            logger.debug("Encontrados {} usuários para o termo '{}'", usuarios.size(), termo);
            
        } catch (SQLException e) {
            logger.error("Erro ao pesquisar usuários", e);
            view.showError("Erro ao pesquisar usuários: " + e.getMessage());
        }
    }

    /**
     * Seleciona um usuário pelo ID
     */
    public void selecionarUsuario(Long id) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            if (usuarioOpt.isPresent()) {
                view.selecionarUsuario(usuarioOpt.get());
                logger.debug("Usuário selecionado: {}", usuarioOpt.get().getEmail());
            } else {
                view.selecionarUsuario(null);
                logger.warn("Usuário não encontrado: {}", id);
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao selecionar usuário", e);
            view.showError("Erro ao selecionar usuário: " + e.getMessage());
        }
    }

    /**
     * Cria um novo usuário (versão antiga - mantida para compatibilidade)
     */
    public void criarUsuario(String nome, String email, boolean ativo) {
        try {
            // Verifica se email já existe
            if (usuarioService.emailExiste(email)) {
                view.showError("Email já está em uso: " + email);
                return;
            }
            
            Usuario usuario = new Usuario(nome, email);
            usuario.setAtivo(ativo);
            
            Usuario usuarioSalvo = usuarioService.salvar(usuario);
            
            view.showSuccess("Usuário criado com sucesso!");
            view.finalizarEdicao();
            carregarUsuarios();
            
            logger.info("Usuário criado: {} ({})", usuarioSalvo.getNome(), usuarioSalvo.getEmail());
            
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos para criação de usuário: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao criar usuário", e);
            view.showError("Erro ao criar usuário: " + e.getMessage());
        }
    }

    /**
     * Cria um novo usuário com todos os campos
     */
    public void criarUsuario(String nome, String cpf, String email, String cargo, String login, boolean ativo) {
        try {
            // Verificações de duplicatas
            if (usuarioService.emailExiste(email)) {
                view.showError("Email já está em uso: " + email);
                return;
            }
            
            if (usuarioService.cpfExiste(cpf)) {
                view.showError("CPF já está em uso: " + cpf);
                return;
            }
            
            if (usuarioService.loginExiste(login)) {
                view.showError("Login já está em uso: " + login);
                return;
            }
            
            Usuario usuario = new Usuario(nome, cpf, email, cargo, login);
            usuario.setAtivo(ativo);
            
            Usuario usuarioSalvo = usuarioService.salvar(usuario);
            
            view.showSuccess("Usuário criado com sucesso!");
            view.finalizarEdicao();
            carregarUsuarios();
            
            logger.info("Usuário criado: {} ({})", usuarioSalvo.getNome(), usuarioSalvo.getEmail());
            
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos para criação de usuário: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao criar usuário", e);
            view.showError("Erro ao criar usuário: " + e.getMessage());
        }
    }

    /**
     * Cria um usuário completo com credenciais e papéis (versão antiga)
     */
    public void criarUsuarioCompleto(String nome, String email, String senha, 
                                   List<String> papeis, boolean ativo) {
        try {
            Usuario usuarioSalvo = usuarioService.criarUsuarioCompleto(nome, email, senha, papeis);
            
            // Ajustar status ativo se necessário
            if (!ativo) {
                usuarioService.alterarStatus(usuarioSalvo.getId(), false);
            }
            
            view.showSuccess("Usuário completo criado com sucesso!");
            view.finalizarEdicao();
            carregarUsuarios();
            
            logger.info("Usuário completo criado: {} ({})", usuarioSalvo.getNome(), usuarioSalvo.getEmail());
            
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos para criação de usuário completo: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao criar usuário completo", e);
            view.showError("Erro ao criar usuário completo: " + e.getMessage());
        }
    }

    /**
     * Cria um usuário completo com todos os campos, credenciais e papéis
     */
    public void criarUsuarioCompleto(String nome, String cpf, String email, String cargo, String login, 
                                   String senha, List<String> papeis, boolean ativo) {
        try {
            // Verificações de duplicatas
            if (usuarioService.emailExiste(email)) {
                view.showError("Email já está em uso: " + email);
                return;
            }
            
            if (usuarioService.cpfExiste(cpf)) {
                view.showError("CPF já está em uso: " + cpf);
                return;
            }
            
            if (usuarioService.loginExiste(login)) {
                view.showError("Login já está em uso: " + login);
                return;
            }
            
            Usuario usuarioSalvo = usuarioService.criarUsuarioCompleto(nome, cpf, email, cargo, login, senha, papeis);
            
            // Ajustar status ativo se necessário
            if (!ativo) {
                usuarioService.alterarStatus(usuarioSalvo.getId(), false);
            }
            
            view.showSuccess("Usuário completo criado com sucesso!");
            view.finalizarEdicao();
            carregarUsuarios();
            
            logger.info("Usuário completo criado: {} ({}) com papéis: {}", 
                       usuarioSalvo.getNome(), usuarioSalvo.getEmail(), papeis);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos para criação de usuário completo: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao criar usuário completo", e);
            view.showError("Erro ao criar usuário completo: " + e.getMessage());
        }
    }

    /**
     * Atualiza um usuário existente (versão antiga)
     */
    public void atualizarUsuario(Long id, String nome, String email, boolean ativo) {
        try {
            // Verifica se email já existe para outro usuário
            if (usuarioService.emailExisteParaOutroUsuario(email, id)) {
                view.showError("Email já está em uso por outro usuário: " + email);
                return;
            }
            
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                view.showError("Usuário não encontrado");
                return;
            }
            
            Usuario usuario = usuarioOpt.get();
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setAtivo(ativo);
            
            Usuario usuarioAtualizado = usuarioService.atualizar(usuario);
            
            view.showSuccess("Usuário atualizado com sucesso!");
            view.finalizarEdicao();
            carregarUsuarios();
            
            logger.info("Usuário atualizado: {} ({})", usuarioAtualizado.getNome(), usuarioAtualizado.getEmail());
            
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos para atualização de usuário: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao atualizar usuário", e);
            view.showError("Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    /**
     * Atualiza um usuário existente com todos os campos
     */
    public void atualizarUsuario(Long id, String nome, String cpf, String email, String cargo, String login, boolean ativo) {
        try {
            // Verificações de duplicatas para outro usuário
            if (usuarioService.emailExisteParaOutroUsuario(email, id)) {
                view.showError("Email já está em uso por outro usuário: " + email);
                return;
            }
            
            if (usuarioService.cpfExisteParaOutroUsuario(cpf, id)) {
                view.showError("CPF já está em uso por outro usuário: " + cpf);
                return;
            }
            
            if (usuarioService.loginExisteParaOutroUsuario(login, id)) {
                view.showError("Login já está em uso por outro usuário: " + login);
                return;
            }
            
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                view.showError("Usuário não encontrado");
                return;
            }
            
            Usuario usuario = usuarioOpt.get();
            usuario.setNome(nome);
            usuario.setCpf(cpf);
            usuario.setEmail(email);
            usuario.setCargo(cargo);
            usuario.setLogin(login);
            usuario.setAtivo(ativo);
            
            Usuario usuarioAtualizado = usuarioService.atualizar(usuario);
            
            view.showSuccess("Usuário atualizado com sucesso!");
            view.finalizarEdicao();
            carregarUsuarios();
            
            logger.info("Usuário atualizado: {} ({})", usuarioAtualizado.getNome(), usuarioAtualizado.getEmail());
            
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos para atualização de usuário: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao atualizar usuário", e);
            view.showError("Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    /**
     * Exclui um usuário
     */
    public void excluirUsuario() {
        try {
            // Solicita confirmação
            if (!view.confirmarAcao("Deseja realmente excluir este usuário?")) {
                return;
            }
            
            // Busca o usuário selecionado através da view
            // Por simplicidade, vamos assumir que a view tem uma referência ao usuário selecionado
            // Em uma implementação mais robusta, seria melhor passar o ID como parâmetro
            
            // Por ora, vamos implementar uma versão genérica
            view.showError("Para excluir um usuário, selecione-o na tabela e tente novamente");
            
        } catch (Exception e) {
            logger.error("Erro ao excluir usuário", e);
            view.showError("Erro ao excluir usuário: " + e.getMessage());
        }
    }

    /**
     * Exclui um usuário por ID
     */
    public void excluirUsuario(Long id) {
        try {
            if (!view.confirmarAcao("Deseja realmente excluir este usuário?")) {
                return;
            }
            
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                view.showError("Usuário não encontrado");
                return;
            }
            
            Usuario usuario = usuarioOpt.get();
            
            usuarioService.remover(id);
            
            view.showSuccess("Usuário excluído com sucesso!");
            carregarUsuarios();
            
            logger.info("Usuário excluído: {} ({})", usuario.getNome(), usuario.getEmail());
            
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao excluir usuário: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao excluir usuário", e);
            
            // Verifica se é erro de integridade referencial
            if (e.getMessage().toLowerCase().contains("foreign key") || 
                e.getMessage().toLowerCase().contains("constraint")) {
                view.showError("Não é possível excluir este usuário pois ele está sendo usado em outros registros");
            } else {
                view.showError("Erro ao excluir usuário: " + e.getMessage());
            }
        }
    }

    /**
     * Altera o status de um usuário (ativo/inativo)
     */
    public void alterarStatusUsuario(Long id, boolean ativo) {
        try {
            Usuario usuarioAtualizado = usuarioService.alterarStatus(id, ativo);
            
            String status = ativo ? "ativado" : "desativado";
            view.showSuccess("Usuário " + status + " com sucesso!");
            carregarUsuarios();
            
            logger.info("Status do usuário alterado: {} - {}", usuarioAtualizado.getEmail(), status);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao alterar status: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao alterar status do usuário", e);
            view.showError("Erro ao alterar status do usuário: " + e.getMessage());
        }
    }

    /**
     * Atribui um papel a um usuário
     */
    public void atribuirPapel(Long usuarioId, String nomePapel) {
        try {
            usuarioService.atribuirPapel(usuarioId, nomePapel);
            view.showSuccess("Papel '" + nomePapel + "' atribuído com sucesso!");
            
            logger.info("Papel '{}' atribuído ao usuário ID: {}", nomePapel, usuarioId);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Erro ao atribuir papel: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao atribuir papel", e);
            view.showError("Erro ao atribuir papel: " + e.getMessage());
        }
    }

    /**
     * Remove um papel de um usuário
     */
    public void removerPapel(Long usuarioId, String nomePapel) {
        try {
            usuarioService.removerPapel(usuarioId, nomePapel);
            view.showSuccess("Papel '" + nomePapel + "' removido com sucesso!");
            
            logger.info("Papel '{}' removido do usuário ID: {}", nomePapel, usuarioId);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Erro ao remover papel: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao remover papel", e);
            view.showError("Erro ao remover papel: " + e.getMessage());
        }
    }

    /**
     * Lista os papéis de um usuário
     */
    public List<String> listarPapeisUsuario(Long usuarioId) {
        try {
            return usuarioService.listarPapeisUsuario(usuarioId);
            
        } catch (SQLException e) {
            logger.error("Erro ao listar papéis do usuário", e);
            view.showError("Erro ao listar papéis: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lista todos os papéis disponíveis
     */
    public List<com.gestao.projetos.model.Papel> listarTodosPapeis() {
        try {
            return usuarioService.listarTodosPapeis();
            
        } catch (SQLException e) {
            logger.error("Erro ao listar papéis", e);
            view.showError("Erro ao listar papéis: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Altera a senha de um usuário
     */
    public void alterarSenha(Long usuarioId, String senhaAtual, String novaSenha) {
        try {
            boolean sucesso = usuarioService.alterarSenha(usuarioId, senhaAtual, novaSenha);
            
            if (sucesso) {
                view.showSuccess("Senha alterada com sucesso!");
            } else {
                view.showError("Senha atual incorreta!");
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Erro ao alterar senha: {}", e.getMessage());
            view.showError(e.getMessage());
            
        } catch (SQLException e) {
            logger.error("Erro ao alterar senha", e);
            view.showError("Erro ao alterar senha: " + e.getMessage());
        }
    }

    /**
     * Corrige usuários sem papel atribuindo um papel padrão
     */
    public void corrigirUsuariosSemPapel() {
        try {
            List<Usuario> usuarios = usuarioService.listarTodos();
            int corrigidos = 0;
            
            for (Usuario usuario : usuarios) {
                List<String> papeis = usuarioService.listarPapeisUsuario(usuario.getId());
                
                if (papeis.isEmpty()) {
                    // Usuário sem papel - atribuir COLABORADOR como padrão
                    usuarioService.atribuirPapel(usuario.getId(), "COLABORADOR");
                    logger.info("Papel COLABORADOR atribuído ao usuário: {} ({})", 
                               usuario.getNome(), usuario.getEmail());
                    corrigidos++;
                }
            }
            
            if (corrigidos > 0) {
                view.showSuccess(String.format("Corrigidos %d usuários sem papel. Papel COLABORADOR foi atribuído automaticamente.", corrigidos));
                carregarUsuarios(); // Recarregar a tabela
            } else {
                view.showSuccess("Todos os usuários já possuem papéis atribuídos.");
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao corrigir usuários sem papel", e);
            view.showError("Erro ao corrigir usuários: " + e.getMessage());
        }
    }

    /**
     * Obtém estatísticas de usuários
     */
    public void obterEstatisticas() {
        try {
            long totalUsuarios = usuarioService.contarUsuarios();
            List<Usuario> usuariosAtivos = usuarioService.listarAtivos();
            long usuariosAtivosCount = usuariosAtivos.size();
            long usuariosInativosCount = totalUsuarios - usuariosAtivosCount;
            
            String estatisticas = String.format(
                "Estatísticas de Usuários:\n\n" +
                "Total: %d\n" +
                "Ativos: %d\n" +
                "Inativos: %d",
                totalUsuarios, usuariosAtivosCount, usuariosInativosCount
            );
            
            // Aqui você poderia mostrar em uma janela específica ou no status
            logger.info("Estatísticas: Total={}, Ativos={}, Inativos={}", 
                       totalUsuarios, usuariosAtivosCount, usuariosInativosCount);
            
        } catch (SQLException e) {
            logger.error("Erro ao obter estatísticas", e);
            view.showError("Erro ao obter estatísticas: " + e.getMessage());
        }
    }
}
