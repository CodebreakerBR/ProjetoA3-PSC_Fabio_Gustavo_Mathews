package com.gestao.projetos.view;

import com.gestao.projetos.controller.MainController;
import com.gestao.projetos.service.AuthorizationService;
import com.gestao.projetos.util.DatabaseUtil;
import com.gestao.projetos.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Tela principal do sistema
 */
public class MainFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private final MainController controller;
    private final AuthorizationService authService;
    
    // Componentes da interface
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JDesktopPane desktopPane;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private JLabel connectionLabel;
    private JLabel userLabel;

    public MainFrame() {
        this.controller = new MainController(this);
        this.authService = new AuthorizationService();
        initializeComponents();
        setupLayout();
        setupMenus();
        setupToolBar();
        setupEventHandlers();
        setupFrame();
        updateUserInfo();
    }

    /**
     * Inicializa os componentes da interface
     */
    private void initializeComponents() {
        menuBar = new JMenuBar();
        toolBar = new JToolBar();
        desktopPane = new JDesktopPane();
        statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Sistema iniciado");
        connectionLabel = new JLabel();
        userLabel = new JLabel();
        
        // Configurações do desktop pane
        desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktopPane.setBackground(new Color(240, 240, 240));
        
        // Configurações da barra de status
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(userLabel);
        rightPanel.add(connectionLabel);
        statusPanel.add(rightPanel, BorderLayout.EAST);
        
        updateConnectionStatus();
    }

    /**
     * Configura o layout da janela
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        add(toolBar, BorderLayout.NORTH);
        add(desktopPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        setJMenuBar(menuBar);
    }

    /**
     * Configura os menus da aplicação
     */
    private void setupMenus() {
        // Menu Arquivo
        JMenu menuArquivo = new JMenu("Arquivo");
        menuArquivo.setMnemonic('A');
        
        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.setMnemonic('S');
        itemSair.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        itemSair.addActionListener(e -> controller.sair());
        
        menuArquivo.add(itemSair);
        
        // Menu Cadastros
        JMenu menuCadastros = new JMenu("Cadastros");
        menuCadastros.setMnemonic('C');
        
        // Usuários - apenas ADMINISTRADOR
        if (authService.podeAcessar(AuthorizationService.RECURSO_USUARIOS)) {
            JMenuItem itemUsuarios = new JMenuItem("Usuários");
            itemUsuarios.setMnemonic('U');
            itemUsuarios.setAccelerator(KeyStroke.getKeyStroke("ctrl U"));
            itemUsuarios.addActionListener(e -> controller.abrirGestaoUsuarios());
            menuCadastros.add(itemUsuarios);
        }
        
        // Projetos - ADMINISTRADOR e GERENTE
        if (authService.podeAcessar(AuthorizationService.RECURSO_PROJETOS)) {
            JMenuItem itemProjetos = new JMenuItem("Projetos");
            itemProjetos.setMnemonic('P');
            itemProjetos.setAccelerator(KeyStroke.getKeyStroke("ctrl P"));
            itemProjetos.addActionListener(e -> controller.abrirGestaoProjetos());
            menuCadastros.add(itemProjetos);
            
            JMenuItem itemEquipes = new JMenuItem("Equipes");
            itemEquipes.setMnemonic('E');
            itemEquipes.setAccelerator(KeyStroke.getKeyStroke("ctrl E"));
            itemEquipes.addActionListener(e -> controller.abrirGestaoEquipes());
            menuCadastros.add(itemEquipes);
        }
        
        // Tarefas - todos os papéis
        if (authService.podeAcessar(AuthorizationService.RECURSO_TAREFAS)) {
            JMenuItem itemTarefas = new JMenuItem("Tarefas");
            itemTarefas.setMnemonic('T');
            itemTarefas.setAccelerator(KeyStroke.getKeyStroke("ctrl T"));
            itemTarefas.addActionListener(e -> controller.abrirGestaoTarefas());
            menuCadastros.add(itemTarefas);
        }
        
        // Menu Relatórios
        JMenu menuRelatorios = new JMenu("Relatórios");
        menuRelatorios.setMnemonic('R');
        
        // Dashboard - todos os papéis
        if (authService.podeAcessar(AuthorizationService.RECURSO_DASHBOARD)) {
            JMenuItem itemDashboard = new JMenuItem("Dashboard");
            itemDashboard.setMnemonic('D');
            itemDashboard.setAccelerator(KeyStroke.getKeyStroke("F1"));
            itemDashboard.addActionListener(e -> controller.abrirDashboard());
            menuRelatorios.add(itemDashboard);
        }
        
        // Relatórios - ADMINISTRADOR e GERENTE
        if (authService.podeAcessar(AuthorizationService.RECURSO_RELATORIOS)) {
            if (menuRelatorios.getItemCount() > 0) {
                menuRelatorios.addSeparator();
            }
            
            JMenuItem itemRelatoriosProjetos = new JMenuItem("Relatório de Projetos");
            itemRelatoriosProjetos.setMnemonic('P');
            itemRelatoriosProjetos.addActionListener(e -> controller.abrirRelatoriosProjetos());
            menuRelatorios.add(itemRelatoriosProjetos);
            
            // Relatório de usuários apenas para ADMINISTRADOR
            if (authService.podeAcessar(AuthorizationService.RECURSO_USUARIOS)) {
                JMenuItem itemRelatoriosUsuarios = new JMenuItem("Relatório de Usuários");
                itemRelatoriosUsuarios.setMnemonic('U');
                itemRelatoriosUsuarios.addActionListener(e -> controller.abrirRelatoriosUsuarios());
                menuRelatorios.add(itemRelatoriosUsuarios);
            }
        }
        
        // Menu Janela
        JMenu menuJanela = new JMenu("Janela");
        menuJanela.setMnemonic('J');
        
        JMenuItem itemCascata = new JMenuItem("Cascata");
        itemCascata.addActionListener(e -> arrangeWindowsCascade());
        
        JMenuItem itemLadoALado = new JMenuItem("Lado a Lado");
        itemLadoALado.addActionListener(e -> arrangeWindowsTile());
        
        JMenuItem itemMinimizarTodas = new JMenuItem("Minimizar Todas");
        itemMinimizarTodas.addActionListener(e -> minimizeAllWindows());
        
        JMenuItem itemFecharTodas = new JMenuItem("Fechar Todas");
        itemFecharTodas.addActionListener(e -> closeAllWindows());
        
        menuJanela.add(itemCascata);
        menuJanela.add(itemLadoALado);
        menuJanela.addSeparator();
        menuJanela.add(itemMinimizarTodas);
        menuJanela.add(itemFecharTodas);
        
        // Menu Ajuda
        JMenu menuAjuda = new JMenu("Ajuda");
        menuAjuda.setMnemonic('j');
        
        JMenuItem itemSobre = new JMenuItem("Sobre");
        itemSobre.setMnemonic('S');
        itemSobre.setAccelerator(KeyStroke.getKeyStroke("F12"));
        itemSobre.addActionListener(e -> controller.mostrarSobre());
        
        menuAjuda.add(itemSobre);
        
        // Adiciona menus à barra (apenas se tiverem itens)
        menuBar.add(menuArquivo);
        if (menuCadastros.getItemCount() > 0) {
            menuBar.add(menuCadastros);
        }
        if (menuRelatorios.getItemCount() > 0) {
            menuBar.add(menuRelatorios);
        }
        menuBar.add(menuJanela);
        menuBar.add(menuAjuda);
    }

    /**
     * Configura a barra de ferramentas
     */
    private void setupToolBar() {
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        
        // Botões da toolbar baseados em permissões
        boolean hasItems = false;
        
        // Usuários - apenas ADMINISTRADOR
        if (authService.podeAcessar(AuthorizationService.RECURSO_USUARIOS)) {
            JButton btnUsuarios = createToolbarButton("Usuários", "user.png", 
                e -> controller.abrirGestaoUsuarios());
            toolBar.add(btnUsuarios);
            hasItems = true;
        }
        
        // Projetos - ADMINISTRADOR e GERENTE
        if (authService.podeAcessar(AuthorizationService.RECURSO_PROJETOS)) {
            JButton btnProjetos = createToolbarButton("Projetos", "project.png", 
                e -> controller.abrirGestaoProjetos());
            JButton btnEquipes = createToolbarButton("Equipes", "team.png", 
                e -> controller.abrirGestaoEquipes());
            
            toolBar.add(btnProjetos);
            toolBar.add(btnEquipes);
            hasItems = true;
        }
        
        // Tarefas - todos os papéis
        if (authService.podeAcessar(AuthorizationService.RECURSO_TAREFAS)) {
            JButton btnTarefas = createToolbarButton("Tarefas", "task.png", 
                e -> controller.abrirGestaoTarefas());
            toolBar.add(btnTarefas);
            hasItems = true;
        }
        
        // Dashboard - todos os papéis
        if (authService.podeAcessar(AuthorizationService.RECURSO_DASHBOARD)) {
            if (hasItems) {
                toolBar.addSeparator();
            }
            JButton btnDashboard = createToolbarButton("Dashboard", "dashboard.png", 
                e -> controller.abrirDashboard());
            toolBar.add(btnDashboard);
            hasItems = true;
        }
        
        // Espaço flexível
        toolBar.add(Box.createHorizontalGlue());
        
        // Botão de logout
        JButton btnLogout = createToolbarButton("Logout", "logout.png", 
            e -> {
                performLogout();
            });
        
        // Status da conexão
        JButton btnConexao = createToolbarButton("Testar Conexão", "database.png", 
            e -> {
                updateConnectionStatus();
                controller.testarConexao();
            });
        
        toolBar.add(btnLogout);
        toolBar.add(btnConexao);
    }

    /**
     * Cria um botão para a toolbar
     */
    private JButton createToolbarButton(String text, String iconName, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setToolTipText(text);
        button.setFocusable(false);
        button.addActionListener(action);
        
        // Tentativa de carregar ícone (opcional)
        try {
            // ImageIcon icon = new ImageIcon(getClass().getResource("/icons/" + iconName));
            // button.setIcon(icon);
            // button.setText(null); // Remove texto se tiver ícone
        } catch (Exception e) {
            // Mantém apenas o texto se não conseguir carregar o ícone
        }
        
        return button;
    }

    /**
     * Configura os manipuladores de eventos
     */
    private void setupEventHandlers() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.sair();
            }
        });
    }

    /**
     * Configura as propriedades da janela principal
     */
    private void setupFrame() {
        setTitle("Sistema de Gestão de Projetos e Equipes");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Ícone da aplicação
        try {
            // setIconImage(new ImageIcon(getClass().getResource("/icons/app.png")).getImage());
        } catch (Exception e) {
            // Ícone padrão se não conseguir carregar
        }
    }

    /**
     * Atualiza o status da conexão com o banco
     */
    public void updateConnectionStatus() {
        SwingUtilities.invokeLater(() -> {
            boolean connected = DatabaseUtil.testConnection();
            if (connected) {
                connectionLabel.setText("Conectado");
                connectionLabel.setForeground(Color.GREEN.darker());
            } else {
                connectionLabel.setText("Desconectado");
                connectionLabel.setForeground(Color.RED);
            }
        });
    }

    /**
     * Atualiza a mensagem da barra de status
     */
    public void updateStatusMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }

    /**
     * Adiciona uma janela interna ao desktop
     */
    public void addInternalFrame(JInternalFrame frame) {
        desktopPane.add(frame);
        try {
            frame.setSelected(true);
        } catch (Exception e) {
            logger.error("Erro ao selecionar janela interna", e);
        }
    }

    /**
     * Organiza as janelas em cascata
     */
    private void arrangeWindowsCascade() {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        int x = 0, y = 0;
        
        for (JInternalFrame frame : frames) {
            if (!frame.isIcon()) {
                frame.setLocation(x, y);
                x += 25;
                y += 25;
            }
        }
    }

    /**
     * Organiza as janelas lado a lado
     */
    private void arrangeWindowsTile() {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        int visibleFrames = 0;
        
        for (JInternalFrame frame : frames) {
            if (!frame.isIcon()) {
                visibleFrames++;
            }
        }
        
        if (visibleFrames == 0) return;
        
        int cols = (int) Math.ceil(Math.sqrt(visibleFrames));
        int rows = (int) Math.ceil((double) visibleFrames / cols);
        
        Dimension desktopSize = desktopPane.getSize();
        int frameWidth = desktopSize.width / cols;
        int frameHeight = desktopSize.height / rows;
        
        int index = 0;
        for (JInternalFrame frame : frames) {
            if (!frame.isIcon()) {
                int col = index % cols;
                int row = index / cols;
                
                frame.setBounds(
                    col * frameWidth,
                    row * frameHeight,
                    frameWidth,
                    frameHeight
                );
                index++;
            }
        }
    }

    /**
     * Minimiza todas as janelas
     */
    private void minimizeAllWindows() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            try {
                frame.setIcon(true);
            } catch (Exception e) {
                logger.error("Erro ao minimizar janela", e);
            }
        }
    }

    /**
     * Fecha todas as janelas
     */
    private void closeAllWindows() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            try {
                frame.setClosed(true);
            } catch (Exception e) {
                logger.error("Erro ao fechar janela", e);
            }
        }
    }

    /**
     * Realiza logout do sistema
     */
    private void performLogout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Deseja realmente sair do sistema?",
            "Confirmar Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            logger.info("Usuário solicitou logout");
            
            // Fechar todas as janelas internas
            closeAllWindows();
            
            // Fechar a janela principal
            this.dispose();
            
            // Encerrar sessão e voltar para login
            controller.realizarLogout();
        }
    }

    /**
     * Atualiza as informações do usuário na barra de status
     */
    private void updateUserInfo() {
        SessionManager sessionManager = SessionManager.getInstance();
        if (sessionManager.isSessionActive()) {
            String userName = sessionManager.getCurrentUserName();
            String userRole = authService.getNivelAcessoMaisAlto();
            userLabel.setText("Usuário: " + userName + " (" + userRole + ")");
            userLabel.setForeground(new Color(0, 100, 0)); // Verde
        } else {
            userLabel.setText("Não logado");
            userLabel.setForeground(Color.RED);
        }
    }
    
    /**
     * Atualiza os componentes da interface baseado nas permissões do usuário
     */
    public void atualizarPermissoes() {
        // Remove todos os componentes dos menus e toolbar
        menuBar.removeAll();
        toolBar.removeAll();
        
        // Recria os menus e toolbar com as novas permissões
        setupMenus();
        setupToolBar();
        
        // Atualiza informações do usuário
        updateUserInfo();
        
        // Revalida e repinta os componentes
        menuBar.revalidate();
        menuBar.repaint();
        toolBar.revalidate();
        toolBar.repaint();
        statusPanel.revalidate();
        statusPanel.repaint();
        
        logger.info("Interface atualizada com permissões do usuário: {}", 
                   authService.getDescricaoPrivilegios());
    }
    
    /**
     * Verifica se o usuário pode acessar um recurso específico
     */
    public boolean podeAcessarRecurso(String recurso) {
        return authService.podeAcessar(recurso);
    }
    
    /**
     * Obtém o desktop pane
     */
    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }
}
