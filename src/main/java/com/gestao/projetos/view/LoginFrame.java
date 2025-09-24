package com.gestao.projetos.view;

import com.gestao.projetos.service.AuthenticationService;
import com.gestao.projetos.util.SessionManager;
import com.gestao.projetos.model.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Tela de login simples e funcional
 */
public class LoginFrame extends JDialog {
    
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel messageLabel;
    private boolean loginSuccessful = false;
    
    public LoginFrame() {
        super((Frame) null, "Sistema de Gestão de Projetos - Login", true); // Modal
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null);
        setResizable(false);
        
        createComponents();
        setupLayout();
        setupActions();
        
        // Não chamar setVisible(true) aqui - será chamado pelo Main
    }
    
    private void createComponents() {
        emailField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Entrar");
        cancelButton = new JButton("Cancelar");
        messageLabel = new JLabel(" ");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);
    }
    
    private void setupLayout() {
        // Layout simples com GridBagLayout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        add(emailField, gbc);
        
        // Senha
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Senha:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        add(passwordField, gbc);
        
        // Botões
        gbc.gridx = 0; gbc.gridy = 2;
        add(loginButton, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        add(cancelButton, gbc);
        
        // Mensagem
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(messageLabel, gbc);
    }
    
    private void setupActions() {
        loginButton.addActionListener(e -> performLogin());
        cancelButton.addActionListener(e -> {
            loginSuccessful = false;
            dispose();
        });
        passwordField.addActionListener(e -> performLogin());
    }
    
    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Preencha todos os campos", Color.RED);
            return;
        }
        
        loginButton.setEnabled(false);
        showMessage("Autenticando...", Color.BLUE);
        
        // Thread separada para não travar a UI
        new Thread(() -> {
            try {
                AuthenticationService authService = new AuthenticationService();
                Usuario usuario = authService.authenticate(email, password);
                
                SwingUtilities.invokeLater(() -> {
                    if (usuario != null) {
                        SessionManager.getInstance().startSession(usuario);
                        showMessage("Login realizado com sucesso!", Color.GREEN);
                        loginSuccessful = true;
                        
                        // Fechar automaticamente após um pequeno delay para mostrar a mensagem
                        Timer timer = new Timer(500, evt -> dispose());
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showMessage("Email ou senha inválidos", Color.RED);
                        loginButton.setEnabled(true);
                    }
                });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    showMessage("Erro: " + ex.getMessage(), Color.RED);
                    loginButton.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void showMessage(String text, Color color) {
        messageLabel.setText(text);
        messageLabel.setForeground(color);
    }
    
    /**
     * Retorna se o login foi bem-sucedido
     */
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}