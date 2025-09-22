package com.gestao.projetos.service;

import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.util.ValidationUtil;
// import org.mindrot.jbcrypt.BCrypt; // Comentado temporariamente
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para operações relacionadas a usuários
 */
public class UsuarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }
    
    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Salva um novo usuário
     */
    public Usuario salvar(Usuario usuario) throws SQLException {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        
        validarUsuario(usuario);
        
        // Normaliza os dados
        usuario.setNome(ValidationUtil.capitalizeWords(usuario.getNome()));
        usuario.setEmail(usuario.getEmail().toLowerCase().trim());
        
        return usuarioDAO.save(usuario);
    }


    /**
     * Atualiza um usuário existente
     */
    public Usuario atualizar(Usuario usuario) throws SQLException {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuário ou ID não pode ser nulo");
        }
        
        validarUsuario(usuario);
        
        // Verifica se o usuário existe
        if (!usuarioDAO.exists(usuario.getId())) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuario.getId());
        }
        
        // Normaliza os dados
        usuario.setNome(ValidationUtil.capitalizeWords(usuario.getNome()));
        usuario.setEmail(usuario.getEmail().toLowerCase().trim());
        
        return usuarioDAO.update(usuario);
    }

    /**
     * Remove um usuário
     */
    public void remover(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }
        
        if (!usuarioDAO.exists(id)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + id);
        }
        
        usuarioDAO.delete(id);
    }

    /**
     * Busca usuário por ID
     */
    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        
        return usuarioDAO.findById(id);
    }

    /**
     * Busca usuário por email
     */
    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        if (!ValidationUtil.isNotEmpty(email)) {
            return Optional.empty();
        }
        
        return usuarioDAO.findByEmail(email.toLowerCase().trim());
    }

    /**
     * Lista todos os usuários
     */
    public List<Usuario> listarTodos() throws SQLException {
        return usuarioDAO.findAll();
    }

    /**
     * Lista apenas usuários ativos
     */
    public List<Usuario> listarAtivos() throws SQLException {
        return usuarioDAO.findAllActive();
    }

    /**
     * Ativa ou desativa um usuário
     */
    public Usuario alterarStatus(Long id, boolean ativo) throws SQLException {
        Optional<Usuario> usuarioOpt = buscarPorId(id);
        if (!usuarioOpt.isPresent()) {
            throw new IllegalArgumentException("Usuário não encontrado: " + id);
        }
        
        Usuario usuario = usuarioOpt.get();
        usuario.setAtivo(ativo);
        
        return usuarioDAO.update(usuario);
    }

    /**
     * Verifica se um email já está em uso
     */
    public boolean emailExiste(String email) throws SQLException {
        return ValidationUtil.isNotEmpty(email) && 
               usuarioDAO.existsByEmail(email.toLowerCase().trim());
    }

    /**
     * Verifica se um email está em uso por outro usuário
     */
    public boolean emailExisteParaOutroUsuario(String email, Long userId) throws SQLException {
        return ValidationUtil.isNotEmpty(email) && 
               usuarioDAO.existsByEmailForOtherUser(email.toLowerCase().trim(), userId);
    }

    /**
     * Conta o total de usuários
     */
    public long contarUsuarios() throws SQLException {
        return usuarioDAO.count();
    }

    /**
     * Busca usuários por termo de pesquisa (nome ou email)
     */
    public List<Usuario> pesquisar(String termo) throws SQLException {
        if (!ValidationUtil.isNotEmpty(termo)) {
            return listarTodos();
        }
        
        // Por simplicidade, vamos filtrar na aplicação
        // Em um sistema real, seria melhor fazer isso no banco
        List<Usuario> todos = listarTodos();
        String termoBusca = termo.toLowerCase().trim();
        
        return todos.stream()
                .filter(u -> u.getNome().toLowerCase().contains(termoBusca) ||
                           u.getEmail().toLowerCase().contains(termoBusca))
                .toList();
    }

    /**
     * Valida os dados do usuário
     */
    private void validarUsuario(Usuario usuario) {
        if (!ValidationUtil.isNotEmpty(usuario.getNome())) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        if (!ValidationUtil.hasMinLength(usuario.getNome(), 2)) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 2 caracteres");
        }
        
        if (!ValidationUtil.hasMaxLength(usuario.getNome(), 100)) {
            throw new IllegalArgumentException("Nome não pode ter mais de 100 caracteres");
        }
        
        if (!ValidationUtil.isValidEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Email inválido");
        }
        
        if (!ValidationUtil.hasMaxLength(usuario.getEmail(), 150)) {
            throw new IllegalArgumentException("Email não pode ter mais de 150 caracteres");
        }
    }
}
