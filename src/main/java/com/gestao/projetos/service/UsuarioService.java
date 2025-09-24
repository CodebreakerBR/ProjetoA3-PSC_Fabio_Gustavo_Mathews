package com.gestao.projetos.service;

import com.gestao.projetos.dao.*;
import com.gestao.projetos.model.*;
import com.gestao.projetos.util.DatabaseUtil;
import com.gestao.projetos.util.ValidationUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para operações relacionadas a usuários
 */
public class UsuarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioDAO usuarioDAO;
    private final CredencialDAO credencialDAO;
    private final PapelDAO papelDAO;
    private final UsuarioPapelDAO usuarioPapelDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
        this.credencialDAO = new CredencialDAO();
        this.papelDAO = new PapelDAO();
        this.usuarioPapelDAO = new UsuarioPapelDAO();
    }
    
    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
        this.credencialDAO = new CredencialDAO();
        this.papelDAO = new PapelDAO();
        this.usuarioPapelDAO = new UsuarioPapelDAO();
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
     * Verifica se um CPF já existe
     */
    public boolean cpfExiste(String cpf) throws SQLException {
        return ValidationUtil.isNotEmpty(cpf) && 
               usuarioDAO.existsByCpf(cpf.trim());
    }

    /**
     * Verifica se um CPF está em uso por outro usuário
     */
    public boolean cpfExisteParaOutroUsuario(String cpf, Long userId) throws SQLException {
        return ValidationUtil.isNotEmpty(cpf) && 
               usuarioDAO.existsByCpfForOtherUser(cpf.trim(), userId);
    }

    /**
     * Verifica se um login já existe
     */
    public boolean loginExiste(String login) throws SQLException {
        return ValidationUtil.isNotEmpty(login) && 
               usuarioDAO.existsByLogin(login.trim());
    }

    /**
     * Verifica se um login está em uso por outro usuário
     */
    public boolean loginExisteParaOutroUsuario(String login, Long userId) throws SQLException {
        return ValidationUtil.isNotEmpty(login) && 
               usuarioDAO.existsByLoginForOtherUser(login.trim(), userId);
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
     * Cria um usuário completo com credenciais e papéis
     */
    public Usuario criarUsuarioCompleto(String nome, String email, String senha, 
                                       List<String> nomesPapeis) throws SQLException {
        
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        
        if (nomesPapeis == null || nomesPapeis.isEmpty()) {
            throw new IllegalArgumentException("Pelo menos um papel deve ser informado");
        }

        Connection connection = null;
        try {
            connection = DatabaseUtil.getConnection();
            connection.setAutoCommit(false);
            
            // 1. Criar usuário
            Usuario usuario = new Usuario(nome.trim(), email.trim().toLowerCase());
            usuario.setAtivo(true);
            
            // Validar antes de salvar
            validarUsuario(usuario);
            
            // Verificar se email já existe
            if (emailExiste(usuario.getEmail())) {
                throw new IllegalArgumentException("Email já está em uso: " + usuario.getEmail());
            }
            
            usuario = usuarioDAO.save(usuario);
            logger.info("Usuário criado: {} (ID: {})", usuario.getNome(), usuario.getId());
            
            // 2. Criar credencial
            String salt = BCrypt.gensalt();
            String hash = BCrypt.hashpw(senha, salt);
            
            Credencial credencial = new Credencial();
            credencial.setHash(hash);
            credencial.setSalt(salt);
            credencial.setUsuarioId(usuario.getId());
            
            credencialDAO.save(credencial);
            logger.info("Credencial criada para usuário ID: {}", usuario.getId());
            
            // 3. Atribuir papéis
            for (String nomePapel : nomesPapeis) {
                Optional<Papel> papelOpt = papelDAO.findByNome(nomePapel.trim());
                
                if (papelOpt.isEmpty()) {
                    logger.warn("Papel '{}' não encontrado, ignorando", nomePapel);
                    continue;
                }
                
                UsuarioPapel usuarioPapel = new UsuarioPapel();
                usuarioPapel.setUsuarioId(usuario.getId());
                usuarioPapel.setPapelId(papelOpt.get().getId());
                usuarioPapel.setAtivo(true);
                
                usuarioPapelDAO.save(usuarioPapel);
                logger.info("Papel '{}' atribuído ao usuário ID: {}", nomePapel, usuario.getId());
            }
            
            connection.commit();
            logger.info("Usuário completo criado com sucesso: {} ({})", usuario.getNome(), usuario.getEmail());
            
            return usuario;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.error("Rollback executado devido a erro na criação do usuário", e);
                } catch (SQLException rollbackEx) {
                    logger.error("Erro durante rollback", rollbackEx);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    logger.error("Erro ao fechar conexão", closeEx);
                }
            }
        }
    }

    /**
     * Cria um usuário completo com todos os campos, credenciais e papéis
     */
    public Usuario criarUsuarioCompleto(String nome, String cpf, String email, String cargo, String login,
                                       String senha, List<String> nomesPapeis) throws SQLException {
        
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Login é obrigatório");
        }
        
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        
        if (nomesPapeis == null || nomesPapeis.isEmpty()) {
            throw new IllegalArgumentException("Pelo menos um papel deve ser informado");
        }

        Connection connection = null;
        try {
            connection = DatabaseUtil.getConnection();
            connection.setAutoCommit(false);
            
            // 1. Criar usuário com todos os campos
            Usuario usuario = new Usuario(nome.trim(), cpf.trim(), email.trim().toLowerCase(), 
                                         cargo != null ? cargo.trim() : null, login.trim());
            usuario.setAtivo(true);
            
            // Validar antes de salvar
            validarUsuario(usuario);
            
            // Verificar duplicatas
            if (emailExiste(usuario.getEmail())) {
                throw new IllegalArgumentException("Email já está em uso: " + usuario.getEmail());
            }
            
            if (cpfExiste(usuario.getCpf())) {
                throw new IllegalArgumentException("CPF já está em uso: " + usuario.getCpf());
            }
            
            if (loginExiste(usuario.getLogin())) {
                throw new IllegalArgumentException("Login já está em uso: " + usuario.getLogin());
            }
            
            usuario = usuarioDAO.save(usuario, connection);
            logger.info("Usuário completo criado: {} (ID: {})", usuario.getNome(), usuario.getId());
            
            // 2. Criar credencial
            String salt = BCrypt.gensalt();
            String hash = BCrypt.hashpw(senha, salt);
            
            Credencial credencial = new Credencial();
            credencial.setHash(hash);
            credencial.setSalt(salt);
            credencial.setUsuarioId(usuario.getId());
            
            credencialDAO.save(credencial, connection);
            logger.info("Credencial criada para usuário ID: {}", usuario.getId());
            
            // 3. Atribuir papéis
            for (String nomePapel : nomesPapeis) {
                Optional<Papel> papelOpt = papelDAO.findByNome(nomePapel.trim());
                
                if (papelOpt.isEmpty()) {
                    logger.warn("Papel '{}' não encontrado, ignorando", nomePapel);
                    continue;
                }
                
                UsuarioPapel usuarioPapel = new UsuarioPapel();
                usuarioPapel.setUsuarioId(usuario.getId());
                usuarioPapel.setPapelId(papelOpt.get().getId());
                usuarioPapel.setAtivo(true);
                
                usuarioPapelDAO.save(usuarioPapel, connection);
                logger.info("Papel '{}' atribuído ao usuário ID: {}", nomePapel, usuario.getId());
            }
            
            connection.commit();
            logger.info("Usuário completo criado com sucesso: {} ({})", usuario.getNome(), usuario.getEmail());
            
            return usuario;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.error("Rollback executado devido a erro na criação do usuário", e);
                } catch (SQLException rollbackEx) {
                    logger.error("Erro durante rollback", rollbackEx);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    logger.error("Erro ao fechar conexão", closeEx);
                }
            }
        }
    }

    /**
     * Atribui um papel a um usuário existente
     */
    public void atribuirPapel(Long usuarioId, String nomePapel) throws SQLException {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido");
        }
        
        if (nomePapel == null || nomePapel.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do papel é obrigatório");
        }

        // Verificar se usuário existe
        Optional<Usuario> usuarioOpt = usuarioDAO.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuarioId);
        }

        // Verificar se papel existe
        Optional<Papel> papelOpt = papelDAO.findByNome(nomePapel.trim());
        if (papelOpt.isEmpty()) {
            throw new IllegalArgumentException("Papel não encontrado: " + nomePapel);
        }

        // Verificar se usuário já possui esse papel
        Optional<UsuarioPapel> existente = usuarioPapelDAO.findByUsuarioAndPapel(
            usuarioId, papelOpt.get().getId());
        
        if (existente.isPresent() && existente.get().isAtivo()) {
            throw new IllegalArgumentException("Usuário já possui esse papel: " + nomePapel);
        }

        // Criar associação
        UsuarioPapel usuarioPapel = new UsuarioPapel();
        usuarioPapel.setUsuarioId(usuarioId);
        usuarioPapel.setPapelId(papelOpt.get().getId());
        usuarioPapel.setAtivo(true);
        
        usuarioPapelDAO.save(usuarioPapel);
        
        logger.info("Papel '{}' atribuído ao usuário ID: {}", nomePapel, usuarioId);
    }

    /**
     * Remove um papel de um usuário
     */
    public void removerPapel(Long usuarioId, String nomePapel) throws SQLException {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido");
        }
        
        if (nomePapel == null || nomePapel.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do papel é obrigatório");
        }

        // Verificar se papel existe
        Optional<Papel> papelOpt = papelDAO.findByNome(nomePapel.trim());
        if (papelOpt.isEmpty()) {
            throw new IllegalArgumentException("Papel não encontrado: " + nomePapel);
        }

        // Buscar associação
        Optional<UsuarioPapel> usuarioPapelOpt = usuarioPapelDAO.findByUsuarioAndPapel(
            usuarioId, papelOpt.get().getId());
        
        if (usuarioPapelOpt.isEmpty() || !usuarioPapelOpt.get().isAtivo()) {
            throw new IllegalArgumentException("Usuário não possui esse papel: " + nomePapel);
        }

        // Desativar associação
        UsuarioPapel usuarioPapel = usuarioPapelOpt.get();
        usuarioPapel.setAtivo(false);
        usuarioPapelDAO.update(usuarioPapel);
        
        logger.info("Papel '{}' removido do usuário ID: {}", nomePapel, usuarioId);
    }

    /**
     * Lista os papéis de um usuário
     */
    public List<String> listarPapeisUsuario(Long usuarioId) throws SQLException {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido");
        }

        List<UsuarioPapel> usuarioPapeis = usuarioPapelDAO.findByUsuarioId(usuarioId);
        
        return usuarioPapeis.stream()
            .filter(up -> up.isAtivo() && !up.isExpirada())
            .map(up -> {
                try {
                    Optional<Papel> papelOpt = papelDAO.findById(up.getPapelId());
                    return papelOpt.map(Papel::getNome).orElse("Papel não encontrado");
                } catch (SQLException e) {
                    logger.error("Erro ao buscar papel ID: {}", up.getPapelId(), e);
                    return "Erro ao buscar papel";
                }
            })
            .toList();
    }

    /**
     * Verifica se um usuário tem um papel específico
     */
    public boolean usuarioTemPapel(Long usuarioId, String nomePapel) throws SQLException {
        if (usuarioId == null || usuarioId <= 0 || nomePapel == null || nomePapel.trim().isEmpty()) {
            return false;
        }

        Optional<Papel> papelOpt = papelDAO.findByNome(nomePapel.trim());
        if (papelOpt.isEmpty()) {
            return false;
        }

        Optional<UsuarioPapel> usuarioPapelOpt = usuarioPapelDAO.findByUsuarioAndPapel(
            usuarioId, papelOpt.get().getId());
        
        return usuarioPapelOpt.isPresent() && usuarioPapelOpt.get().isAtivo() && 
               !usuarioPapelOpt.get().isExpirada();
    }

    /**
     * Lista todos os papéis disponíveis
     */
    public List<Papel> listarTodosPapeis() throws SQLException {
        return papelDAO.findAll();
    }

    /**
     * Altera a senha de um usuário
     */
    public boolean alterarSenha(Long usuarioId, String senhaAtual, String novaSenha) throws SQLException {
        if (usuarioId == null || senhaAtual == null || novaSenha == null || 
            senhaAtual.trim().isEmpty() || novaSenha.trim().isEmpty()) {
            throw new IllegalArgumentException("Parâmetros inválidos para alteração de senha");
        }

        // Buscar credencial atual
        Optional<Credencial> credencialOpt = credencialDAO.findByUserId(usuarioId);
        
        if (credencialOpt.isEmpty()) {
            throw new IllegalArgumentException("Credencial não encontrada para o usuário: " + usuarioId);
        }
        
        Credencial credencial = credencialOpt.get();
        
        // Verificar senha atual
        if (!BCrypt.checkpw(senhaAtual, credencial.getHash())) {
            logger.warn("Tentativa de alteração de senha com senha atual incorreta para usuário ID: {}", usuarioId);
            return false;
        }
        
        // Gerar novo hash
        String salt = BCrypt.gensalt();
        String novoHash = BCrypt.hashpw(novaSenha, salt);
        
        // Atualizar credencial
        credencial.setHash(novoHash);
        credencial.setSalt(salt);
        credencialDAO.update(credencial);
        
        logger.info("Senha alterada com sucesso para usuário ID: {}", usuarioId);
        return true;
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
