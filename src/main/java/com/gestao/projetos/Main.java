package com.gestao.projetos;

import com.gestao.projetos.util.DatabaseUtil;
import com.gestao.projetos.util.SessionManager;
import com.gestao.projetos.view.LoginFrame;
import com.gestao.projetos.view.MainFrame;
import com.gestao.projetos.model.Usuario;
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
            logger.error("Não foi possível conectar ao banco de dados. Sistema será encerrado.");
            mostrarErroConexao();
            System.exit(1);
        }
        
        // Inicia a aplicação na thread do Swing
        SwingUtilities.invokeLater(() -> {
            try {
                logger.info("Iniciando Sistema de Gestão de Projetos e Equipes");
                
                // Mostrar tela de login primeiro
                if (realizarLogin()) {
                    // Login bem-sucedido, mostrar aplicação principal
                    iniciarAplicacaoPrincipal();
                } else {
                    // Login cancelado ou falhou
                    logger.info("Login cancelado pelo usuário");
                    System.exit(0);
                }
                
            } catch (Exception e) {
                logger.error("Erro ao iniciar a aplicação", e);
                mostrarErroInicializacao(e);
                System.exit(1);
            }
        });
    }
    
    /**
     * Exibe a tela de login e processa autenticação
     * 
     * @return true se login foi bem-sucedido
     */
    private static boolean realizarLogin() {
        logger.info("Exibindo tela de login");
        
        try {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true); // Modal - bloqueia até ser fechado
            
            // Verificar se o login foi bem-sucedido
            return loginFrame.isLoginSuccessful();
            
        } catch (Exception e) {
            logger.error("Erro no processo de login", e);
        }
        
        return false;
    }

    private static boolean loginComCredenciaisBanco() {
        try {
            Usuario adminUser = buscarUsuarioAdmin();

            if (adminUser != null) {
                SessionManager.getInstance().startSession(adminUser);
                logger.info("Login automático realizado para: {}", adminUser.getEmail());
                return true;
            } else {
                logger.warn("Usuário admin não encontrado no banco de dados");
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro ao realizar login com credenciais do banco", e);
            return false;
        }
    }

    private static Usuario buscarUsuarioAdmin() {
        try {
            java.sql.Connection conn = DatabaseUtil.getConnection();

            String sql = "SELECT id, nome, email, ativo FROM usuario WHERE email = ? AND ativo = true";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "admin@gestao.com");

            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getLong("id"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setAtivo(rs.getBoolean("ativo"));

                rs.close();
                stmt.close();
                conn.close();

                return usuario;
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            logger.error("Erro ao buscar usuário admin no banco", e);
        }

        return null;
    }

    private static void iniciarAplicacaoPrincipal() {
        try {
            logger.info("Iniciando aplicação principal");

            MainFrame mainFrame = new MainFrame();

            mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    encerrarAplicacao();
                }
            });

            mainFrame.setVisible(true);

            logger.info("Sistema iniciado com sucesso para usuário: {}",
                       SessionManager.getInstance().getCurrentUserEmail());
        } catch (Exception e) {
            logger.error("Erro ao iniciar aplicação principal", e);
            mostrarErroInicializacao(e);
            encerrarAplicacao();
        }
    }

    private static void encerrarAplicacao() {
        logger.info("Encerrando aplicação");
        try {
            SessionManager sessionManager = SessionManager.getInstance();
            if (sessionManager.isSessionActive()) {
                logger.info("Encerrando sessão para usuário: {}",
                           sessionManager.getCurrentUserEmail());
                sessionManager.endSession();
            }

            DatabaseUtil.closeDataSource();

            logger.info("Aplicação encerrada com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao encerrar aplicação", e);
        } finally {
            System.exit(0);
        }
    }

    private static void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            configurarPropriedadesUI();

            logger.info("Look and Feel configurado: {}", UIManager.getLookAndFeel().getName());
        } catch (Exception e) {
            logger.warn("Erro ao configurar Look and Feel, usando padrão", e);

            try {
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

    private static void configurarPropriedadesUI() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

        UIManager.put("Button.font", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("TextArea.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("Menu.font", defaultFont);
        UIManager.put("MenuItem.font", defaultFont);

        UIManager.put("Panel.background", new Color(245, 245, 245));
        UIManager.put("Button.background", new Color(230, 230, 230));
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.alternateRowColor", new Color(248, 248, 248));

        UIManager.put("Button.border", BorderFactory.createRaisedBevelBorder());
        UIManager.put("TextField.border", BorderFactory.createLoweredBevelBorder());
    }

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
