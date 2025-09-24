package com.gestao.projetos.util;

import com.gestao.projetos.service.AuthorizationService;
import com.gestao.projetos.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Utilitário para validação de acesso a recursos
 */
public class AccessValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(AccessValidator.class);
    private static final AuthorizationService authService = new AuthorizationService();
    
    /**
     * Verifica se o usuário atual pode acessar um recurso
     * Se não puder, exibe uma mensagem de erro
     */
    public static boolean validateAccess(String recurso, Component parentComponent) {
        if (!authService.podeAcessar(recurso)) {
            showAccessDeniedMessage(recurso, parentComponent);
            return false;
        }
        return true;
    }
    
    /**
     * Verifica se o usuário atual pode acessar um recurso
     * Versão silenciosa (sem mensagem de erro)
     */
    public static boolean hasAccess(String recurso) {
        return authService.podeAcessar(recurso);
    }
    
    /**
     * Exibe mensagem de acesso negado
     */
    private static void showAccessDeniedMessage(String recurso, Component parentComponent) {
        Usuario currentUser = SessionManager.getInstance().getCurrentUser();
        String userName = currentUser != null ? currentUser.getNome() : "Usuário não identificado";
        String userRole = authService.getNivelAcessoMaisAlto();
        
        String message = String.format(
            "Acesso negado!\n\n" +
            "Usuário: %s\n" +
            "Papel atual: %s\n" +
            "Recurso solicitado: %s\n\n" +
            "Você não possui permissão para acessar este recurso.\n" +
            "Entre em contato com o administrador do sistema para solicitar acesso.",
            userName, userRole, getRecursoDisplayName(recurso)
        );
        
        JOptionPane.showMessageDialog(
            parentComponent,
            message,
            "Acesso Negado",
            JOptionPane.WARNING_MESSAGE
        );
        
        logger.warn("Acesso negado para usuário {} (papel: {}) ao recurso: {}", 
                   userName, userRole, recurso);
    }
    
    /**
     * Converte o nome do recurso interno para um nome amigável
     */
    private static String getRecursoDisplayName(String recurso) {
        switch (recurso.toLowerCase()) {
            case AuthorizationService.RECURSO_USUARIOS:
                return "Gestão de Usuários";
            case AuthorizationService.RECURSO_PROJETOS:
                return "Gestão de Projetos";
            case AuthorizationService.RECURSO_TAREFAS:
                return "Gestão de Tarefas";
            case AuthorizationService.RECURSO_DASHBOARD:
                return "Dashboard";
            case AuthorizationService.RECURSO_RELATORIOS:
                return "Relatórios";
            default:
                return recurso;
        }
    }
    
    /**
     * Verifica acesso e executa uma ação se permitido
     */
    public static void executeWithAccess(String recurso, Component parentComponent, Runnable action) {
        if (validateAccess(recurso, parentComponent)) {
            try {
                action.run();
            } catch (Exception e) {
                logger.error("Erro ao executar ação para recurso: " + recurso, e);
                JOptionPane.showMessageDialog(
                    parentComponent,
                    "Erro interno do sistema: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Configura a visibilidade de um componente baseado no acesso
     */
    public static void setComponentVisibility(JComponent component, String recurso) {
        boolean hasAccess = hasAccess(recurso);
        component.setVisible(hasAccess);
        component.setEnabled(hasAccess);
    }
    
    /**
     * Cria um painel com informações sobre as permissões do usuário atual
     */
    public static JPanel createPermissionInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Informações de Acesso"));
        
        Usuario currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            panel.add(new JLabel("Nenhum usuário logado"), BorderLayout.CENTER);
            return panel;
        }
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(panel.getBackground());
        textArea.setText(authService.getDescricaoPrivilegios());
        
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        
        return panel;
    }
}