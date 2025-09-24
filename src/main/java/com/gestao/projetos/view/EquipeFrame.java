
package com.gestao.projetos.view;

import com.gestao.projetos.controller.EquipeController;
import com.gestao.projetos.model.Equipe;
import com.gestao.projetos.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tela de gestão de equipes - Apenas para administradores
 */
public class EquipeFrame extends JInternalFrame {
    
    private final EquipeController controller;
    
    // Componentes da interface
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    private JTextField txtNome;
    private JTextArea txtDescricao;
    private JCheckBox chkAtiva;
    
    private JList<Usuario> listUsuariosDisponiveis;
    private JList<Usuario> listMembrosSelecionados;
    private JComboBox<Usuario> cboGerente;
    
    private JTextField txtPesquisa;
    private JButton btnPesquisar;
    private JButton btnLimparPesquisa;
    
    private JButton btnNova;
    private JButton btnEditar;
    private JButton btnExcluir;
    private JButton btnSalvar;
    private JButton btnCancelar;
    private JButton btnEstatisticas;
    
    private JButton btnAdicionarMembro;
    private JButton btnRemoverMembro;
    
    private Equipe equipeSelecionada;
    private boolean editando = false;
    private List<Usuario> usuariosDisponiveis = new ArrayList<>();

    public EquipeFrame() {
        super("Gestão de Equipes", true, true, true, true);
        this.controller = new EquipeController(this);
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupFrame();
        
        controller.carregarUsuariosDisponiveis();
        controller.carregarEquipes();
    }

    /**
     * Inicializa os componentes da interface
     */
    private void initializeComponents() {
        // Tabela
        String[] colunas = {"ID", "Nome", "Descrição", "Gerente", "Membros", "Status", "Criada em"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        
        // Configurar larguras das colunas
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Nome
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // Descrição
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Gerente
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Membros
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // Status
        table.getColumnModel().getColumn(6).setPreferredWidth(120); // Criada em
        
        // Campos do formulário
        txtNome = new JTextField(20);
        txtDescricao = new JTextArea(3, 20);
        txtDescricao.setLineWrap(true);
        txtDescricao.setWrapStyleWord(true);
        chkAtiva = new JCheckBox("Equipe ativa", true);
        
        // Listas de usuários
        listUsuariosDisponiveis = new JList<>();
        listUsuariosDisponiveis.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        listMembrosSelecionados = new JList<>();
        listMembrosSelecionados.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        cboGerente = new JComboBox<>();
        
        // Campo de pesquisa
        txtPesquisa = new JTextField(15);
        btnPesquisar = new JButton("Pesquisar");
        btnLimparPesquisa = new JButton("Limpar");
        
        // Botões principais
        btnNova = new JButton("Nova Equipe");
        btnEditar = new JButton("Editar");
        btnExcluir = new JButton("Excluir");
        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
        btnEstatisticas = new JButton("Estatísticas");
        
        // Botões de membros
        btnAdicionarMembro = new JButton(">");
        btnRemoverMembro = new JButton("<");
        
        configurarEstadoInicial();
    }

    /**
     * Configura o layout da janela
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel superior com pesquisa
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Painel central com tabela e formulário
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setLeftComponent(createTablePanel());
        centerSplitPane.setRightComponent(createFormPanel());
        centerSplitPane.setDividerLocation(600);
        
        add(centerSplitPane, BorderLayout.CENTER);
        
        // Painel inferior com botões
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Cria o painel superior com pesquisa
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Pesquisa"));
        
        panel.add(new JLabel("Pesquisar:"));
        panel.add(txtPesquisa);
        panel.add(btnPesquisar);
        panel.add(btnLimparPesquisa);
        
        return panel;
    }

    /**
     * Cria o painel da tabela
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Equipes"));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Cria o painel do formulário
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Detalhes da Equipe"));
        
        // Dados básicos
        JPanel basicPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        basicPanel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        basicPanel.add(txtNome, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        basicPanel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1;
        JScrollPane descScrollPane = new JScrollPane(txtDescricao);
        descScrollPane.setPreferredSize(new Dimension(200, 60));
        basicPanel.add(descScrollPane, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        basicPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        basicPanel.add(chkAtiva, gbc);
        
        panel.add(basicPanel);
        
        // Painel de seleção de membros
        JPanel membersPanel = createMembersPanel();
        panel.add(membersPanel);
        
        // Painel de seleção de gerente
        JPanel gerentePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gerentePanel.setBorder(BorderFactory.createTitledBorder("Gerente da Equipe"));
        gerentePanel.add(new JLabel("Gerente:"));
        gerentePanel.add(cboGerente);
        
        panel.add(gerentePanel);
        
        return panel;
    }

    /**
     * Cria o painel de seleção de membros
     */
    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Membros da Equipe"));
        
        // Painel esquerdo - usuários disponíveis
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Usuários Disponíveis"), BorderLayout.NORTH);
        JScrollPane leftScrollPane = new JScrollPane(listUsuariosDisponiveis);
        leftScrollPane.setPreferredSize(new Dimension(150, 120));
        leftPanel.add(leftScrollPane, BorderLayout.CENTER);
        
        // Painel central - botões
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(btnAdicionarMembro);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(btnRemoverMembro);
        centerPanel.add(Box.createVerticalGlue());
        
        // Painel direito - membros selecionados
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Membros da Equipe"), BorderLayout.NORTH);
        JScrollPane rightScrollPane = new JScrollPane(listMembrosSelecionados);
        rightScrollPane.setPreferredSize(new Dimension(150, 120));
        rightPanel.add(rightScrollPane, BorderLayout.CENTER);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }

    /**
     * Cria o painel inferior com botões
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        panel.add(btnNova);
        panel.add(btnEditar);
        panel.add(btnExcluir);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnSalvar);
        panel.add(btnCancelar);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnEstatisticas);
        
        return panel;
    }

    /**
     * Configura os manipuladores de eventos
     */
    private void setupEventHandlers() {
        // Seleção na tabela
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        int modelRow = table.convertRowIndexToModel(selectedRow);
                        Long id = (Long) tableModel.getValueAt(modelRow, 0);
                        controller.selecionarEquipe(id);
                    }
                }
            }
        });
        
        // Botões principais
        btnNova.addActionListener(e -> iniciarNovaEquipe());
        btnEditar.addActionListener(e -> iniciarEdicao());
        btnExcluir.addActionListener(e -> excluirEquipe());
        btnSalvar.addActionListener(e -> salvarEquipe());
        btnCancelar.addActionListener(e -> cancelarEdicao());
        btnEstatisticas.addActionListener(e -> controller.obterEstatisticas());
        
        // Botões de membros
        btnAdicionarMembro.addActionListener(e -> adicionarMembros());
        btnRemoverMembro.addActionListener(e -> removerMembros());
        
        // Pesquisa
        btnPesquisar.addActionListener(e -> pesquisar());
        btnLimparPesquisa.addActionListener(e -> limparPesquisa());
        txtPesquisa.addActionListener(e -> pesquisar());
    }

    /**
     * Configura as propriedades da janela
     */
    private void setupFrame() {
        setSize(1200, 700);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
    }

    /**
     * Configura o estado inicial dos componentes
     */
    private void configurarEstadoInicial() {
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnNova.setEnabled(true); // Botão "Nova Equipe" sempre habilitado
        
        txtNome.setEnabled(false);
        txtDescricao.setEnabled(false);
        chkAtiva.setEnabled(false);
        listUsuariosDisponiveis.setEnabled(false);
        listMembrosSelecionados.setEnabled(false);
        cboGerente.setEnabled(false);
        btnAdicionarMembro.setEnabled(false);
        btnRemoverMembro.setEnabled(false);
    }

    /**
     * Configura o estado após finalizar edição
     */
    private void configurarEstadoAposEdicao() {
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnNova.setEnabled(true); // Sempre habilitado para criar nova equipe
        btnEstatisticas.setEnabled(true); // Sempre habilitado
        
        txtNome.setEnabled(false);
        txtDescricao.setEnabled(false);
        chkAtiva.setEnabled(false);
        listUsuariosDisponiveis.setEnabled(false);
        listMembrosSelecionados.setEnabled(false);
        cboGerente.setEnabled(false);
        btnAdicionarMembro.setEnabled(false);
        btnRemoverMembro.setEnabled(false);
    }

    /**
     * Inicia a criação de uma nova equipe
     */
    private void iniciarNovaEquipe() {
        equipeSelecionada = null;
        editando = true;
        
        limparFormulario();
        configurarEstadoEdicao();
        
        txtNome.requestFocus();
    }

    /**
     * Inicia a edição da equipe selecionada
     */
    private void iniciarEdicao() {
        if (equipeSelecionada == null) {
            showError("Selecione uma equipe para editar");
            return;
        }
        
        editando = true;
        configurarEstadoEdicao();
        preencherFormulario(equipeSelecionada);
    }

    /**
     * Salva a equipe (nova ou editada)
     */
    private void salvarEquipe() {
        if (!validarFormulario()) {
            return;
        }
        
        String nome = txtNome.getText().trim();
        String descricao = txtDescricao.getText().trim();
        boolean ativa = chkAtiva.isSelected();
        
        // Obter membros selecionados
        DefaultListModel<Usuario> modelMembros = (DefaultListModel<Usuario>) listMembrosSelecionados.getModel();
        List<Long> membrosIds = new ArrayList<>();
        for (int i = 0; i < modelMembros.size(); i++) {
            membrosIds.add(modelMembros.get(i).getId());
        }
        
        // Obter gerente selecionado
        Usuario gerente = (Usuario) cboGerente.getSelectedItem();
        Long gerenteId = gerente != null ? gerente.getId() : null;
        
        if (equipeSelecionada == null) {
            // Nova equipe
            controller.criarEquipe(nome, descricao, membrosIds, gerenteId);
        } else {
            // Atualizar equipe existente
            controller.atualizarEquipe(equipeSelecionada.getId(), nome, descricao, membrosIds, gerenteId);
        }
    }

    /**
     * Cancela a edição
     */
    private void cancelarEdicao() {
        editando = false;
        equipeSelecionada = null;
        
        limparFormulario();
        configurarEstadoAposEdicao();
    }

    /**
     * Exclui a equipe selecionada
     */
    private void excluirEquipe() {
        if (equipeSelecionada == null) {
            showError("Selecione uma equipe para excluir");
            return;
        }
        
        controller.excluirEquipe(equipeSelecionada.getId());
    }

    /**
     * Adiciona membros selecionados à equipe
     */
    private void adicionarMembros() {
        List<Usuario> selecionados = listUsuariosDisponiveis.getSelectedValuesList();
        if (selecionados.isEmpty()) {
            return;
        }
        
        DefaultListModel<Usuario> modelDisponiveis = (DefaultListModel<Usuario>) listUsuariosDisponiveis.getModel();
        DefaultListModel<Usuario> modelMembros = (DefaultListModel<Usuario>) listMembrosSelecionados.getModel();
        
        for (Usuario usuario : selecionados) {
            modelDisponiveis.removeElement(usuario);
            modelMembros.addElement(usuario);
        }
        
        atualizarComboGerente();
    }

    /**
     * Remove membros selecionados da equipe
     */
    private void removerMembros() {
        List<Usuario> selecionados = listMembrosSelecionados.getSelectedValuesList();
        if (selecionados.isEmpty()) {
            return;
        }
        
        DefaultListModel<Usuario> modelDisponiveis = (DefaultListModel<Usuario>) listUsuariosDisponiveis.getModel();
        DefaultListModel<Usuario> modelMembros = (DefaultListModel<Usuario>) listMembrosSelecionados.getModel();
        
        for (Usuario usuario : selecionados) {
            modelMembros.removeElement(usuario);
            modelDisponiveis.addElement(usuario);
        }
        
        atualizarComboGerente();
    }

    /**
     * Atualiza o combo de gerente com os membros da equipe
     */
    private void atualizarComboGerente() {
        DefaultListModel<Usuario> modelMembros = (DefaultListModel<Usuario>) listMembrosSelecionados.getModel();
        
        cboGerente.removeAllItems();
        for (int i = 0; i < modelMembros.size(); i++) {
            cboGerente.addItem(modelMembros.get(i));
        }
    }

    /**
     * Realiza a pesquisa
     */
    private void pesquisar() {
        String texto = txtPesquisa.getText().trim();
        if (texto.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
    }

    /**
     * Limpa a pesquisa
     */
    private void limparPesquisa() {
        txtPesquisa.setText("");
        rowSorter.setRowFilter(null);
    }

    /**
     * Valida o formulário
     */
    private boolean validarFormulario() {
        if (txtNome.getText().trim().isEmpty()) {
            showError("Nome da equipe é obrigatório");
            txtNome.requestFocus();
            return false;
        }
        
        DefaultListModel<Usuario> modelMembros = (DefaultListModel<Usuario>) listMembrosSelecionados.getModel();
        if (modelMembros.size() == 0) {
            showError("A equipe deve ter pelo menos um membro");
            return false;
        }
        
        if (cboGerente.getSelectedItem() == null) {
            showError("É necessário selecionar um gerente para a equipe");
            cboGerente.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * Atualiza a tabela com a lista de equipes
     */
    public void atualizarTabela(List<Equipe> equipes) {
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Equipe equipe : equipes) {
            String gerente = equipe.getMembros().stream()
                    .filter(u -> "GERENTE".equals(u.getCargo()))
                    .map(Usuario::getNome)
                    .findFirst()
                    .orElse("Não definido");
            
            int totalMembros = equipe.getMembros().size();
            String status = equipe.isAtiva() ? "Ativa" : "Inativa";
            String criadaEm = equipe.getCriadoEm() != null ? 
                            equipe.getCriadoEm().format(formatter) : "";
            
            Object[] row = {
                equipe.getId(),
                equipe.getNome(),
                equipe.getDescricao() != null ? 
                    (equipe.getDescricao().length() > 50 ? 
                        equipe.getDescricao().substring(0, 47) + "..." : 
                        equipe.getDescricao()) : "",
                gerente,
                totalMembros,
                status,
                criadaEm
            };
            
            tableModel.addRow(row);
        }
        
        tableModel.fireTableDataChanged(); // Força atualização da tabela
    }

    /**
     * Seleciona uma equipe na interface
     */
    public void selecionarEquipe(Equipe equipe) {
        this.equipeSelecionada = equipe;
        configurarEstadoSelecao();
        
        if (!editando) {
            preencherFormulario(equipe);
        }
    }

    /**
     * Preenche o formulário com os dados da equipe
     */
    private void preencherFormulario(Equipe equipe) {
        txtNome.setText(equipe.getNome());
        txtDescricao.setText(equipe.getDescricao());
        chkAtiva.setSelected(equipe.isAtiva());
        
        // Limpar listas
        DefaultListModel<Usuario> modelDisponiveis = new DefaultListModel<>();
        DefaultListModel<Usuario> modelMembros = new DefaultListModel<>();
        
        // Preencher lista de usuários disponíveis (todos menos os da equipe)
        List<Long> membrosIds = equipe.getMembros().stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());
        
        for (Usuario usuario : usuariosDisponiveis) {
            if (!membrosIds.contains(usuario.getId())) {
                modelDisponiveis.addElement(usuario);
            }
        }
        
        // Preencher lista de membros da equipe
        for (Usuario membro : equipe.getMembros()) {
            modelMembros.addElement(membro);
        }
        
        listUsuariosDisponiveis.setModel(modelDisponiveis);
        listMembrosSelecionados.setModel(modelMembros);
        
        // Atualizar combo de gerente e selecionar o atual
        atualizarComboGerente();
        Usuario gerenteAtual = equipe.getMembros().stream()
                .filter(u -> "GERENTE".equals(u.getCargo()))
                .findFirst()
                .orElse(null);
        
        if (gerenteAtual != null) {
            cboGerente.setSelectedItem(gerenteAtual);
        }
    }

    /**
     * Limpa o formulário
     */
    private void limparFormulario() {
        txtNome.setText("");
        txtDescricao.setText("");
        chkAtiva.setSelected(true);
        
        // Restaurar listas
        DefaultListModel<Usuario> modelDisponiveis = new DefaultListModel<>();
        for (Usuario usuario : usuariosDisponiveis) {
            modelDisponiveis.addElement(usuario);
        }
        
        listUsuariosDisponiveis.setModel(modelDisponiveis);
        listMembrosSelecionados.setModel(new DefaultListModel<>());
        
        cboGerente.removeAllItems();
    }

    /**
     * Configura o estado quando há seleção
     */
    private void configurarEstadoSelecao() {
        btnEditar.setEnabled(true);
        btnExcluir.setEnabled(true);
    }

    /**
     * Configura o estado de edição
     */
    private void configurarEstadoEdicao() {
        btnSalvar.setEnabled(true);
        btnCancelar.setEnabled(true);
        btnNova.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        
        txtNome.setEnabled(true);
        txtDescricao.setEnabled(true);
        chkAtiva.setEnabled(true);
        listUsuariosDisponiveis.setEnabled(true);
        listMembrosSelecionados.setEnabled(true);
        cboGerente.setEnabled(true);
        btnAdicionarMembro.setEnabled(true);
        btnRemoverMembro.setEnabled(true);
    }

    /**
     * Finaliza a edição com sucesso
     */
    public void finalizarEdicao() {
        editando = false;
        equipeSelecionada = null;
        
        limparFormulario();
        configurarEstadoAposEdicao();
    }

    /**
     * Atualiza a lista de usuários disponíveis
     */
    public void atualizarUsuariosDisponiveis(List<Usuario> usuarios) {
        this.usuariosDisponiveis = usuarios;
        
        if (!editando) {
            DefaultListModel<Usuario> model = new DefaultListModel<>();
            for (Usuario usuario : usuarios) {
                model.addElement(usuario);
            }
            listUsuariosDisponiveis.setModel(model);
        }
    }

    /**
     * Mostra estatísticas
     */
    public void mostrarEstatisticas(String estatisticas) {
        JOptionPane.showMessageDialog(this, estatisticas, "Estatísticas de Equipes", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exibe mensagem de erro
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Exibe mensagem de sucesso
     */
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Confirma uma ação
     */
    public boolean confirmarAcao(String message) {
        int result = JOptionPane.showConfirmDialog(
            this, 
            message, 
            "Confirmação", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
}
