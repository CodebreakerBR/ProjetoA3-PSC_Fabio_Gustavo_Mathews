package com.gestao.projetos.service;

import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.dao.UsuarioPapelDAO;
import com.gestao.projetos.dao.PapelDAO;
import com.gestao.projetos.model.Papel;
import com.gestao.projetos.model.UsuarioPapel;
import com.gestao.projetos.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

/**
 * Serviço responsável pela autorização baseada em papéis
 */
public class AuthorizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);
    
    private final UsuarioPapelDAO usuarioPapelDAO;
    private final PapelDAO papelDAO;
    
    // Definir recursos do sistema
    public static final String RECURSO_USUARIOS = "usuarios";
    public static final String RECURSO_PROJETOS = "projetos";
    public static final String RECURSO_TAREFAS = "tarefas";
    public static final String RECURSO_EQUIPES = "equipes";
    public static final String RECURSO_DASHBOARD = "dashboard";
    public static final String RECURSO_RELATORIOS = "relatorios";
    
    // Definir papéis
    public static final String PAPEL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String PAPEL_GERENTE = "GERENTE";
    public static final String PAPEL_COLABORADOR = "COLABORADOR";
    
    public AuthorizationService() {
        this.usuarioPapelDAO = new UsuarioPapelDAO();
        this.papelDAO = new PapelDAO();
    }
    
    /**
     * Verifica se o usuário atual pode acessar um recurso
     */
    public boolean podeAcessar(String recurso) {
        Usuario usuarioAtual = SessionManager.getInstance().getCurrentUser();
        if (usuarioAtual == null) {
            logger.warn("Tentativa de verificar acesso sem usuário logado");
            return false;
        }
        
        return podeAcessar(usuarioAtual.getId(), recurso);
    }
    
    /**
     * Verifica se um usuário específico pode acessar um recurso
     */
    public boolean podeAcessar(Long usuarioId, String recurso) {
        if (usuarioId == null || recurso == null || recurso.trim().isEmpty()) {
            return false;
        }
        
        try {
            Set<String> papeisUsuario = obterPapeisUsuario(usuarioId);
            
            switch (recurso.toLowerCase()) {
                case RECURSO_USUARIOS:
                    return papeisUsuario.contains(PAPEL_ADMINISTRADOR);
                    
                case RECURSO_PROJETOS:
                    return papeisUsuario.contains(PAPEL_ADMINISTRADOR) || 
                           papeisUsuario.contains(PAPEL_GERENTE);
                    
                case RECURSO_TAREFAS:
                    return papeisUsuario.contains(PAPEL_ADMINISTRADOR) || 
                           papeisUsuario.contains(PAPEL_GERENTE) || 
                           papeisUsuario.contains(PAPEL_COLABORADOR);
                    
                case RECURSO_EQUIPES:
                    return papeisUsuario.contains(PAPEL_ADMINISTRADOR);
                    
                case RECURSO_DASHBOARD:
                    return papeisUsuario.contains(PAPEL_ADMINISTRADOR) || 
                           papeisUsuario.contains(PAPEL_GERENTE) || 
                           papeisUsuario.contains(PAPEL_COLABORADOR);
                    
                case RECURSO_RELATORIOS:
                    return papeisUsuario.contains(PAPEL_ADMINISTRADOR) || 
                           papeisUsuario.contains(PAPEL_GERENTE);
                    
                default:
                    logger.warn("Recurso desconhecido: {}", recurso);
                    return false;
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar acesso para usuário {} ao recurso {}", usuarioId, recurso, e);
            return false;
        }
    }
    
    /**
     * Obtém todos os papéis de um usuário
     */
    public Set<String> obterPapeisUsuario(Long usuarioId) throws SQLException {
        Set<String> papeis = new HashSet<>();
        
        if (usuarioId == null) {
            return papeis;
        }
        
        List<UsuarioPapel> usuarioPapeis = usuarioPapelDAO.findByUsuarioId(usuarioId);
        
        for (UsuarioPapel usuarioPapel : usuarioPapeis) {
            if (usuarioPapel.isAtivo() && !usuarioPapel.isExpirada()) {
                Optional<Papel> papelOpt = papelDAO.findById(usuarioPapel.getPapelId());
                if (papelOpt.isPresent()) {
                    papeis.add(papelOpt.get().getNome());
                }
            }
        }
        
        return papeis;
    }
    
    /**
     * Verifica se o usuário atual é administrador
     */
    public boolean isAdministrador() {
        return temPapel(PAPEL_ADMINISTRADOR);
    }
    
    /**
     * Verifica se o usuário atual é gerente
     */
    public boolean isGerente() {
        return temPapel(PAPEL_GERENTE);
    }
    
    /**
     * Verifica se o usuário atual é colaborador
     */
    public boolean isColaborador() {
        return temPapel(PAPEL_COLABORADOR);
    }
    
    /**
     * Verifica se o usuário atual tem um papel específico
     */
    public boolean temPapel(String nomePapel) {
        Usuario usuarioAtual = SessionManager.getInstance().getCurrentUser();
        if (usuarioAtual == null) {
            return false;
        }
        
        try {
            Set<String> papeis = obterPapeisUsuario(usuarioAtual.getId());
            return papeis.contains(nomePapel);
        } catch (SQLException e) {
            logger.error("Erro ao verificar papel {} para usuário {}", nomePapel, usuarioAtual.getId(), e);
            return false;
        }
    }
    
    /**
     * Obtém uma descrição dos privilégios do usuário atual
     */
    public String getDescricaoPrivilegios() {
        try {
            Usuario usuarioAtual = SessionManager.getInstance().getCurrentUser();
            if (usuarioAtual == null) {
                return "Nenhum usuário logado";
            }
            
            Set<String> papeis = obterPapeisUsuario(usuarioAtual.getId());
            if (papeis.isEmpty()) {
                return "Nenhum papel atribuído";
            }
            
            StringBuilder descricao = new StringBuilder();
            descricao.append("Papéis: ").append(String.join(", ", papeis)).append("\n");
            descricao.append("Pode acessar: ");
            
            if (podeAcessar(RECURSO_USUARIOS)) descricao.append("Usuários, ");
            if (podeAcessar(RECURSO_PROJETOS)) descricao.append("Projetos, ");
            if (podeAcessar(RECURSO_TAREFAS)) descricao.append("Tarefas, ");
            if (podeAcessar(RECURSO_DASHBOARD)) descricao.append("Dashboard, ");
            if (podeAcessar(RECURSO_RELATORIOS)) descricao.append("Relatórios, ");
            
            // Remove a última vírgula
            String resultado = descricao.toString();
            if (resultado.endsWith(", ")) {
                resultado = resultado.substring(0, resultado.length() - 2);
            }
            
            return resultado;
            
        } catch (SQLException e) {
            logger.error("Erro ao obter descrição de privilégios", e);
            return "Erro ao obter privilégios";
        }
    }
    
    /**
     * Obtém o nível de acesso mais alto do usuário atual
     */
    public String getNivelAcessoMaisAlto() {
        if (isAdministrador()) {
            return PAPEL_ADMINISTRADOR;
        } else if (isGerente()) {
            return PAPEL_GERENTE;
        } else if (isColaborador()) {
            return PAPEL_COLABORADOR;
        } else {
            return "SEM_PAPEL";
        }
    }
}