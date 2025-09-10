package com.gestao.projetos;

import com.gestao.projetos.util.DatabaseUtil;
import com.gestao.projetos.view.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Classe principal da aplicação
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Configura o look and feel do sistema
        configurarLookAndFeel();
        
        // Configura tratamento de exceções não capturadas
        configurarTratamentoExcecoes();
        
        // Testa a conexão com o banco de dados
        if (!testarConexaoBanco()) {
            logger.warn("Não foi possível conectar ao banco de dados. Continuando em modo demo...");
            // Continua a execução mesmo sem banco para demonstração
        }
        
        // Inicia a aplicação na thread do Swing
        SwingUtilities.invokeLater(() -> {
            try {
                logger.info("Iniciando Sistema de Gestão de Projetos e Equipes");
                
                // Cria e exibe a janela principal
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                
                logger.info("Sistema iniciado com sucesso");
                
            } catch (Exception e) {
                logger.error("Erro ao iniciar a aplicação", e);
                mostrarErroInicializacao(e);
            }
        });
    }

    /**
     * Configura o Look and Feel da aplicação
     */
    private static void configurarLookAndFeel() {
        try {
            // Tenta usar o look and feel do sistema operacional
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Configurações adicionais de aparência
            configurarPropriedadesUI();
            
            logger.info("Look and Feel configurado: {}", UIManager.getLookAndFeel().getName());
            
        } catch (Exception e) {
            logger.warn("Erro ao configurar Look and Feel, usando padrão", e);
            
            try {
                // Fallback para Nimbus se disponível
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e2) {
                logger.warn("Erro ao configurar Nimbus, usando Metal", e2);
            }
        }
    }

    /**
     * Configura propriedades adicionais da UI
     */
    private static void configurarPropriedadesUI() {
        // Configurações gerais
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Configurações de fonte
        Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        
        UIManager.put("Button.font", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("TextArea.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("Menu.font", defaultFont);
        UIManager.put("MenuItem.font", defaultFont);
        
        // Configurações de cores (tema mais moderno)
        UIManager.put("Panel.background", new Color(245, 245, 245));
        UIManager.put("Button.background", new Color(230, 230, 230));
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.alternateRowColor", new Color(248, 248, 248));
        
        // Configurações de bordas
        UIManager.put("Button.border", BorderFactory.createRaisedBevelBorder());
        UIManager.put("TextField.border", BorderFactory.createLoweredBevelBorder());
    }

    /**
     * Configura o tratamento de exceções não capturadas
     */
    private static void configurarTratamentoExcecoes() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            logger.error("Exceção não capturada na thread: {}", thread.getName(), exception);
            
            SwingUtilities.invokeLater(() -> {
                String message = "Ocorreu um erro inesperado no sistema.\n\n" +
                               "Erro: " + exception.getMessage() + "\n\n" +
                               "Por favor, verifique os logs para mais detalhes.";
                
                JOptionPane.showMessageDialog(
                    null,
                    message,
                    "Erro do Sistema",
                    JOptionPane.ERROR_MESSAGE
                );
            });
        });
    }

    /**
     * Testa a conexão com o banco de dados
     */
    private static boolean testarConexaoBanco() {
        try {
            logger.info("Testando conexão com o banco de dados...");
            
            boolean conectado = DatabaseUtil.testConnection();
            if (conectado) {
                logger.info("Conexão com banco de dados estabelecida com sucesso");
                logger.info("Status do pool: {}", DatabaseUtil.getPoolStatus());
                return true;
            } else {
                logger.error("Falha ao conectar com o banco de dados");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Erro ao testar conexão com banco de dados: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Mostra erro de conexão com o banco
     */
    private static void mostrarErroConexao() {
        SwingUtilities.invokeLater(() -> {
            String message = "Não foi possível conectar ao banco de dados!\n\n" +
                           "Verifique se:\n" +
                           "• O MySQL está executando\n" +
                           "• As configurações em database.properties estão corretas\n" +
                           "• O banco de dados 'gestao_projetos' existe\n" +
                           "• As credenciais de acesso estão válidas\n\n" +
                           "A aplicação será encerrada.";
            
            JOptionPane.showMessageDialog(
                null,
                message,
                "Erro de Conexão",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }

    /**
     * Mostra erro de inicialização
     */
    private static void mostrarErroInicializacao(Exception e) {
        SwingUtilities.invokeLater(() -> {
            String message = "Erro ao inicializar a aplicação!\n\n" +
                           "Erro: " + e.getMessage() + "\n\n" +
                           "Verifique os logs para mais detalhes.";
            
            JOptionPane.showMessageDialog(
                null,
                message,
                "Erro de Inicialização",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
}
