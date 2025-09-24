package com.gestao.projetos.service;

import com.gestao.projetos.dao.EquipeDAO;
import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.dao.UsuarioPapelDAO;
import com.gestao.projetos.model.Equipe;
import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço para operações relacionadas a equipes
 */
public class EquipeService {
    
    private static final Logger logger = LoggerFactory.getLogger(EquipeService.class);
    private final EquipeDAO equipeDAO;
    private final UsuarioDAO usuarioDAO;
    private final UsuarioPapelDAO usuarioPapelDAO;
    private final AuthorizationService authorizationService;

    public EquipeService() {
        this.equipeDAO = new EquipeDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.usuarioPapelDAO = new UsuarioPapelDAO();
        this.authorizationService = new AuthorizationService();
    }
    
    public EquipeService(EquipeDAO equipeDAO) {
        this.equipeDAO = equipeDAO;
        this.usuarioDAO = new UsuarioDAO();
        this.usuarioPapelDAO = new UsuarioPapelDAO();
        this.authorizationService = new AuthorizationService();
    }

    /**
     * Cria uma nova equipe
     */
    public Equipe criarEquipe(String nome, String descricao, List<Long> membrosIds, Long gerenteId) throws SQLException {
        // Verificar permissão - apenas admin pode criar equipes
        if (!isUsuarioAdmin()) {
            throw new SecurityException("Apenas administradores podem criar equipes");
        }
        
        validateCreateEquipeParams(nome, membrosIds, gerenteId);
        
        // Verificar se nome já existe
        if (equipeDAO.existsByNome(nome)) {
            throw new IllegalArgumentException("Já existe uma equipe com este nome");
        }
        
        // Verificar se gerente não é admin
        if (isUsuarioAdministrador(gerenteId)) {
            throw new IllegalArgumentException("Administradores não podem ser membros de equipes");
        }
        
        // Verificar se todos os membros não são admins
        for (Long membroId : membrosIds) {
            if (isUsuarioAdministrador(membroId)) {
                throw new IllegalArgumentException("Administradores não podem ser membros de equipes");
            }
        }
        
        // Criar equipe
        Equipe equipe = new Equipe(nome, descricao);
        
        // Carregar membros
        List<Usuario> membros = carregarUsuarios(membrosIds);
        
        // Definir papéis - gerente tem papel GERENTE, outros são COLABORADOR
        for (Usuario membro : membros) {
            if (membro.getId().equals(gerenteId)) {
                membro.setCargo("GERENTE"); // Usando cargo temporariamente para papel na equipe
            } else {
                membro.setCargo("COLABORADOR");
            }
        }
        
        // Verificar se gerente está na lista de membros
        boolean gerenteNaLista = membros.stream().anyMatch(u -> u.getId().equals(gerenteId));
        if (!gerenteNaLista) {
            Optional<Usuario> gerenteOpt = usuarioDAO.findById(gerenteId);
            if (gerenteOpt.isPresent()) {
                Usuario gerente = gerenteOpt.get();
                gerente.setCargo("GERENTE");
                membros.add(gerente);
            }
        }
        
        equipe.setMembros(membros);
        
        Equipe equipeSalva = equipeDAO.save(equipe);
        logger.info("Equipe criada com sucesso: {} (ID: {})", equipeSalva.getNome(), equipeSalva.getId());
        
        return equipeSalva;
    }

    /**
     * Atualiza uma equipe existente
     */
    public Equipe atualizarEquipe(Long id, String nome, String descricao, List<Long> membrosIds, Long gerenteId) throws SQLException {
        // Verificar permissão - apenas admin pode atualizar equipes
        if (!isUsuarioAdmin()) {
            throw new SecurityException("Apenas administradores podem atualizar equipes");
        }
        
        validateUpdateEquipeParams(id, nome, membrosIds, gerenteId);
        
        // Verificar se equipe existe
        Optional<Equipe> equipeExistente = equipeDAO.findById(id);
        if (!equipeExistente.isPresent()) {
            throw new IllegalArgumentException("Equipe não encontrada");
        }
        
        // Verificar se nome já existe para outra equipe
        if (equipeDAO.existsByNomeForOtherEquipe(nome, id)) {
            throw new IllegalArgumentException("Já existe outra equipe com este nome");
        }
        
        // Verificar se gerente não é admin
        if (isUsuarioAdministrador(gerenteId)) {
            throw new IllegalArgumentException("Administradores não podem ser membros de equipes");
        }
        
        // Verificar se todos os membros não são admins
        for (Long membroId : membrosIds) {
            if (isUsuarioAdministrador(membroId)) {
                throw new IllegalArgumentException("Administradores não podem ser membros de equipes");
            }
        }
        
        Equipe equipe = equipeExistente.get();
        equipe.setNome(nome);
        equipe.setDescricao(descricao);
        
        // Carregar novos membros
        List<Usuario> membros = carregarUsuarios(membrosIds);
        
        // Definir papéis
        for (Usuario membro : membros) {
            if (membro.getId().equals(gerenteId)) {
                membro.setCargo("GERENTE");
            } else {
                membro.setCargo("COLABORADOR");
            }
        }
        
        // Verificar se gerente está na lista de membros
        boolean gerenteNaLista = membros.stream().anyMatch(u -> u.getId().equals(gerenteId));
        if (!gerenteNaLista) {
            Optional<Usuario> gerenteOpt = usuarioDAO.findById(gerenteId);
            if (gerenteOpt.isPresent()) {
                Usuario gerente = gerenteOpt.get();
                gerente.setCargo("GERENTE");
                membros.add(gerente);
            }
        }
        
        equipe.setMembros(membros);
        
        Equipe equipeAtualizada = equipeDAO.update(equipe);
        logger.info("Equipe atualizada com sucesso: {} (ID: {})", equipeAtualizada.getNome(), equipeAtualizada.getId());
        
        return equipeAtualizada;
    }

    /**
     * Remove uma equipe
     */
    public void removerEquipe(Long id) throws SQLException {
        // Verificar permissão - apenas admin pode remover equipes
        if (!isUsuarioAdmin()) {
            throw new SecurityException("Apenas administradores podem remover equipes");
        }
        
        if (id == null) {
            throw new IllegalArgumentException("ID da equipe não pode ser nulo");
        }
        
        // Verificar se equipe existe
        if (!equipeDAO.exists(id)) {
            throw new IllegalArgumentException("Equipe não encontrada");
        }
        
        equipeDAO.delete(id);
        logger.info("Equipe removida com sucesso (ID: {})", id);
    }

    /**
     * Busca equipe por ID
     */
    public Optional<Equipe> buscarPorId(Long id) throws SQLException {
        // Verificar permissão - apenas admin pode visualizar equipes
        if (!isUsuarioAdmin()) {
            throw new SecurityException("Apenas administradores podem visualizar equipes");
        }
        
        if (id == null) {
            return Optional.empty();
        }
        
        return equipeDAO.findById(id);
    }

    /**
     * Lista todas as equipes
     */
    public List<Equipe> listarTodas() throws SQLException {
        // Verificar permissão - apenas admin pode visualizar equipes
        if (!isUsuarioAdmin()) {
            throw new SecurityException("Apenas administradores podem visualizar equipes");
        }
        
        return equipeDAO.findAll();
    }

    /**
     * Lista apenas equipes ativas
     */
    public List<Equipe> listarAtivas() throws SQLException {
        // Verificar permissão - apenas admin pode visualizar equipes
        if (!isUsuarioAdmin()) {
            throw new SecurityException("Apenas administradores podem visualizar equipes");
        }
        
        return equipeDAO.findAllActive();
    }

    /**
     * Ativa ou desativa uma equipe
     */
    public Equipe alterarStatus(Long id, boolean ativa) throws SQLException {
        // Verificar permissão - apenas admin pode alterar status de equipes
        if (!isUsuarioAdmin()) {
            throw new SecurityException("Apenas administradores podem alterar status de equipes");
        }
        
        Optional<Equipe> equipeOpt = equipeDAO.findById(id);
        if (!equipeOpt.isPresent()) {
            throw new IllegalArgumentException("Equipe não encontrada");
        }
        
        Equipe equipe = equipeOpt.get();
        equipe.setAtiva(ativa);
        
        Equipe equipeAtualizada = equipeDAO.update(equipe);
        logger.info("Status da equipe alterado: {} -> {} (ID: {})", 
                   equipe.getNome(), ativa ? "ATIVA" : "INATIVA", id);
        
        return equipeAtualizada;
    }

    /**
     * Lista usuários disponíveis para fazer parte de equipes (não-admins)
     */
    public List<Usuario> listarUsuariosDisponiveis() throws SQLException {
        // Verificar permissão - apenas admin pode acessar esta funcionalidade
        if (!isUsuarioAdmin()) {
            throw new SecurityException("Apenas administradores podem acessar esta funcionalidade");
        }
        
        List<Usuario> todosUsuarios = usuarioDAO.findAllActive();
        
        // Filtrar apenas usuários que não são administradores
        return todosUsuarios.stream()
                .filter(usuario -> {
                    try {
                        return !isUsuarioAdministrador(usuario.getId());
                    } catch (SQLException e) {
                        logger.error("Erro ao verificar se usuário é admin: {}", usuario.getId(), e);
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Verifica se o usuário atual é administrador
     */
    private boolean isUsuarioAdmin() {
        Usuario usuarioAtual = SessionManager.getInstance().getCurrentUser();
        if (usuarioAtual == null) {
            return false;
        }
        
        try {
            return authorizationService.podeAcessar(usuarioAtual.getId(), "equipes");
        } catch (Exception e) {
            logger.error("Erro ao verificar permissões do usuário", e);
            return false;
        }
    }

    /**
     * Verifica se um usuário específico é administrador
     */
    private boolean isUsuarioAdministrador(Long usuarioId) throws SQLException {
        if (usuarioId == null) {
            return false;
        }
        
        List<String> papeis = usuarioPapelDAO.findPapeisUsuario(usuarioId);
        return papeis.contains("ADMINISTRADOR");
    }

    /**
     * Carrega usuários pelos IDs
     */
    private List<Usuario> carregarUsuarios(List<Long> usuarioIds) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        
        for (Long usuarioId : usuarioIds) {
            Optional<Usuario> usuarioOpt = usuarioDAO.findById(usuarioId);
            if (usuarioOpt.isPresent()) {
                usuarios.add(usuarioOpt.get());
            } else {
                throw new IllegalArgumentException("Usuário não encontrado: " + usuarioId);
            }
        }
        
        return usuarios;
    }

    /**
     * Valida parâmetros para criação de equipe
     */
    private void validateCreateEquipeParams(String nome, List<Long> membrosIds, Long gerenteId) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da equipe é obrigatório");
        }
        
        if (nome.length() > 100) {
            throw new IllegalArgumentException("Nome da equipe deve ter no máximo 100 caracteres");
        }
        
        if (membrosIds == null || membrosIds.isEmpty()) {
            throw new IllegalArgumentException("Equipe deve ter pelo menos um membro");
        }
        
        if (gerenteId == null) {
            throw new IllegalArgumentException("Gerente da equipe é obrigatório");
        }
        
        // Verificar duplicatas na lista de membros
        long distinctCount = membrosIds.stream().distinct().count();
        if (distinctCount != membrosIds.size()) {
            throw new IllegalArgumentException("Lista de membros contém duplicatas");
        }
    }

    /**
     * Valida parâmetros para atualização de equipe
     */
    private void validateUpdateEquipeParams(Long id, String nome, List<Long> membrosIds, Long gerenteId) {
        if (id == null) {
            throw new IllegalArgumentException("ID da equipe não pode ser nulo");
        }
        
        validateCreateEquipeParams(nome, membrosIds, gerenteId);
    }
}