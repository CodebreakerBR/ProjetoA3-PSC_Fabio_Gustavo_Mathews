package com.gestao.projetos.util;

import com.gestao.projetos.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Gerenciador de sessão de usuários
 * Controla o usuário logado e suas permissões
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static SessionManager instance;
    
    private Usuario currentUser;
    private LocalDateTime loginTime;
    private final Map<String, Object> sessionAttributes;
    
    private SessionManager() {
        this.sessionAttributes = new ConcurrentHashMap<>();
    }
    
    /**
     * Obtém a instância singleton do SessionManager
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Inicia uma sessão para o usuário
     * 
     * @param user Usuário logado
     */
    public void startSession(Usuario user) {
        if (user == null) {
            throw new IllegalArgumentException("Usuário não pode ser null");
        }
        
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
        this.sessionAttributes.clear();
        
        logger.info("Sessão iniciada para usuário: {} (ID: {})", user.getEmail(), user.getId());
        
        // Atributos padrão da sessão
        setAttribute("login_time", loginTime);
        setAttribute("user_id", user.getId());
        setAttribute("user_email", user.getEmail());
        setAttribute("user_name", user.getNome());
    }
    
    /**
     * Encerra a sessão atual
     */
    public void endSession() {
        if (currentUser != null) {
            logger.info("Encerrando sessão para usuário: {} (ID: {})", 
                       currentUser.getEmail(), currentUser.getId());
        }
        
        this.currentUser = null;
        this.loginTime = null;
        this.sessionAttributes.clear();
        
        logger.info("Sessão encerrada");
    }
    
    /**
     * Verifica se existe uma sessão ativa
     * 
     * @return true se existe usuário logado
     */
    public boolean isSessionActive() {
        return currentUser != null && currentUser.isAtivo();
    }
    
    /**
     * Obtém o usuário da sessão atual
     * 
     * @return Usuario logado ou null se não há sessão ativa
     */
    public Usuario getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Obtém o ID do usuário da sessão atual
     * 
     * @return ID do usuário ou null se não há sessão ativa
     */
    public Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
    
    /**
     * Obtém o email do usuário da sessão atual
     * 
     * @return Email do usuário ou null se não há sessão ativa
     */
    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }
    
    /**
     * Obtém o nome do usuário da sessão atual
     * 
     * @return Nome do usuário ou null se não há sessão ativa
     */
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getNome() : null;
    }
    
    /**
     * Obtém o tempo de login da sessão atual
     * 
     * @return Tempo de login ou null se não há sessão ativa
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    /**
     * Calcula o tempo de sessão ativa
     * 
     * @return Duração da sessão em minutos ou 0 se não há sessão ativa
     */
    public long getSessionDurationMinutes() {
        if (loginTime == null) {
            return 0;
        }
        
        return java.time.Duration.between(loginTime, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * Define um atributo da sessão
     * 
     * @param key Chave do atributo
     * @param value Valor do atributo
     */
    public void setAttribute(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Chave do atributo não pode ser nula ou vazia");
        }
        
        if (value != null) {
            sessionAttributes.put(key, value);
        } else {
            sessionAttributes.remove(key);
        }
    }
    
    /**
     * Obtém um atributo da sessão
     * 
     * @param key Chave do atributo
     * @return Valor do atributo ou null se não existe
     */
    public Object getAttribute(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        
        return sessionAttributes.get(key);
    }
    
    /**
     * Obtém um atributo da sessão com tipo específico
     * 
     * @param key Chave do atributo
     * @param type Classe do tipo esperado
     * @return Valor do atributo no tipo especificado ou null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = getAttribute(key);
        
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        
        return null;
    }
    
    /**
     * Remove um atributo da sessão
     * 
     * @param key Chave do atributo
     * @return Valor anterior do atributo ou null
     */
    public Object removeAttribute(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        
        return sessionAttributes.remove(key);
    }
    
    /**
     * Verifica se um atributo existe na sessão
     * 
     * @param key Chave do atributo
     * @return true se o atributo existe
     */
    public boolean hasAttribute(String key) {
        return getAttribute(key) != null;
    }
    
    /**
     * Obtém todos os atributos da sessão
     * 
     * @return Mapa com todos os atributos (cópia defensiva)
     */
    public Map<String, Object> getAllAttributes() {
        return new ConcurrentHashMap<>(sessionAttributes);
    }
    
    /**
     * Limpa todos os atributos da sessão (exceto os padrão)
     */
    public void clearAttributes() {
        // Manter atributos essenciais
        Object loginTime = getAttribute("login_time");
        Object userId = getAttribute("user_id");
        Object userEmail = getAttribute("user_email");
        Object userName = getAttribute("user_name");
        
        sessionAttributes.clear();
        
        // Restaurar atributos essenciais
        if (loginTime != null) setAttribute("login_time", loginTime);
        if (userId != null) setAttribute("user_id", userId);
        if (userEmail != null) setAttribute("user_email", userEmail);
        if (userName != null) setAttribute("user_name", userName);
    }
    
    /**
     * Valida se a sessão ainda é válida
     * Pode ser expandido para incluir timeout, verificações de segurança, etc.
     * 
     * @return true se sessão é válida
     */
    public boolean isSessionValid() {
        if (!isSessionActive()) {
            return false;
        }
        
        // Verificar timeout (por exemplo, 8 horas)
        long maxSessionMinutes = 8 * 60; // 8 horas
        if (getSessionDurationMinutes() > maxSessionMinutes) {
            logger.warn("Sessão expirou por timeout para usuário: {}", getCurrentUserEmail());
            return false;
        }
        
        // Verificar se usuário ainda está ativo no banco (pode ser implementado futuramente)
        // ...
        
        return true;
    }
    
    /**
     * Renova a sessão (atualiza tempo de login)
     */
    public void renewSession() {
        if (isSessionActive()) {
            this.loginTime = LocalDateTime.now();
            setAttribute("login_time", loginTime);
            logger.debug("Sessão renovada para usuário: {}", getCurrentUserEmail());
        }
    }
    
    /**
     * Obtém informações resumidas da sessão
     * 
     * @return String com informações da sessão
     */
    public String getSessionInfo() {
        if (!isSessionActive()) {
            return "Nenhuma sessão ativa";
        }
        
        return String.format(
            "Usuário: %s (%s) | Login: %s | Duração: %d minutos | Atributos: %d",
            getCurrentUserName(),
            getCurrentUserEmail(),
            loginTime.toString(),
            getSessionDurationMinutes(),
            sessionAttributes.size()
        );
    }
    
    /**
     * Força o encerramento da sessão por motivos de segurança
     * 
     * @param reason Motivo do encerramento forçado
     */
    public void forceEndSession(String reason) {
        if (currentUser != null) {
            logger.warn("Sessão encerrada forçadamente para usuário: {} - Motivo: {}", 
                       currentUser.getEmail(), reason);
        }
        
        endSession();
    }
    
    // Métodos de conveniência para verificações de permissão (para expansão futura)
    
    /**
     * Verifica se o usuário atual pode acessar um recurso
     * (Implementação básica - pode ser expandida com sistema de permissões)
     * 
     * @param resource Recurso a ser acessado
     * @return true se tem permissão
     */
    public boolean hasPermission(String resource) {
        if (!isSessionActive()) {
            return false;
        }
        
        // Por enquanto, usuários logados têm acesso a tudo
        // Pode ser expandido para verificar papéis e permissões no banco
        return true;
    }
    
    /**
     * Verifica se o usuário atual é administrador
     * (Implementação básica baseada no email)
     * 
     * @return true se é administrador
     */
    public boolean isCurrentUserAdmin() {
        if (!isSessionActive()) {
            return false;
        }
        
        // Verificação simples baseada no email
        String email = getCurrentUserEmail();
        return email != null && email.contains("admin");
    }
}