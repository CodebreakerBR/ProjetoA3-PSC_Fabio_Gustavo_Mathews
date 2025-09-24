package com.gestao.projetos.view;

import com.gestao.projetos.controller.ProjetoController;
import com.gestao.projetos.model.Projeto;
import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.model.Equipe;
import com.gestao.projetos.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Tela de gestão de projetos
 */
public class ProjetoFrame extends JInternalFrame {
    
    private final ProjetoController controller;
    
    // Componentes da interface
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    private JTextField txtNome;
    private JTextArea txtDescricao;
    private JTextField txtDataInicio;
    private JTextField txtDataTermino;
    private JComboBox<String> cboStatus;
    private JComboBox<Usuario> cboGerente;
    
    private JTextField txtPesquisa;
    private JButton btnPesquisar;
    private JButton btnLimparPesquisa;
    
    private JButton btnNovo;
    private JButton btnEditar;
    private JButton btnExcluir;
    private JButton btnSalvar;
    private JButton btnCancelar;
    private JButton btnGerenciarEquipes;
    private JButton btnEstatisticas;
    
    // Filtros
    private JComboBox<String> cboFiltroStatus;
    private JComboBox<Usuario> cboFiltroGerente;
    private JButton btnFiltrarStatus;
    private JButton btnFiltrarGerente;
    private JButton btnProjetosAtrasados;
    
    private Projeto projetoSelecionado;
    private boolean editando = false;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ProjetoFrame() {
        super("Gestão de Projetos", true, true, true, true);
        
        this.controller = new ProjetoController(this);
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupFrame();
        configurarEstadoInicial();
        
        // Carrega dados iniciais
        controller.carregarProjetos();
    }

    /**
     * Inicializa os componentes da interface
     */
    private void initializeComponents() {
        // Tabela
        String[] colunas = {"ID", "Nome", "Descrição", "Status", "Data Início", "Data Término", "Gerente"};
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
        
        // Campos do formulário
        txtNome = new JTextField(20);
        txtDescricao = new JTextArea(3, 20);
        txtDescricao.setLineWrap(true);
        txtDescricao.setWrapStyleWord(true);
        
        txtDataInicio = new JTextField(10);
        txtDataTermino = new JTextField(10);
        
        cboStatus = new JComboBox<>(new String[]{"PLANEJADO", "EM_ANDAMENTO", "PAUSADO", "CANCELADO", "CONCLUIDO"});
        cboGerente = new JComboBox<>();
        cboGerente.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Usuario) {
                    Usuario usuario = (Usuario) value;
                    setText(usuario.getNome() + " (" + usuario.getEmail() + ")");
                }
                return this;
            }
        });
        
        // Filtros
        cboFiltroStatus = new JComboBox<>(new String[]{"Todos", "PLANEJADO", "EM_ANDAMENTO", "PAUSADO", "CANCELADO", "CONCLUIDO"});
        cboFiltroGerente = new JComboBox<>();
        cboFiltroGerente.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Usuario) {
                    Usuario usuario = (Usuario) value;
                    setText(usuario.getNome());
                } else if (value == null) {
                    setText("Todos os Gerentes");
                }
                return this;
            }
        });
        
        // Pesquisa
        txtPesquisa = new JTextField(20);
        btnPesquisar = new JButton("Pesquisar");
        btnLimparPesquisa = new JButton("Limpar");
        
        // Botões principais
        btnNovo = new JButton("Novo");
        btnEditar = new JButton("Editar");
        btnExcluir = new JButton("Excluir");
        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
        btnGerenciarEquipes = new JButton("Gerenciar Equipes");
        btnEstatisticas = new JButton("Estatísticas");
        
        // Botões de filtro
        btnFiltrarStatus = new JButton("Filtrar por Status");
        btnFiltrarGerente = new JButton("Filtrar por Gerente");
        btnProjetosAtrasados = new JButton("Projetos Atrasados");
        
        // Carrega dados nos combos
        carregarGerentes();
    }

    /**
     * Carrega os gerentes nos comboboxes
     */
    private void carregarGerentes() {
        List<Usuario> usuarios = controller.listarPossiveisGerentes();
        
        cboGerente.removeAllItems();
        cboGerente.addItem(null); // Opção "Nenhum"
        for (Usuario usuario : usuarios) {
            cboGerente.addItem(usuario);
        }
        
        cboFiltroGerente.removeAllItems();
        cboFiltroGerente.addItem(null); // Representa "Todos"
        for (Usuario usuario : usuarios) {
            cboFiltroGerente.addItem(usuario);
        }
    }

    /**
     * Configura o layout da janela
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel superior - Pesquisa e filtros
        add(createTopPanel(), BorderLayout.NORTH);
        
        // Painel central - Split entre tabela e formulário
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createTablePanel());
        splitPane.setRightComponent(createFormPanel());
        splitPane.setDividerLocation(600);
        add(splitPane, BorderLayout.CENTER);
        
        // Painel inferior - Botões
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * Cria o painel superior com pesquisa e filtros
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Pesquisa
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Pesquisar:"));
        searchPanel.add(txtPesquisa);
        searchPanel.add(btnPesquisar);
        searchPanel.add(btnLimparPesquisa);
        
        // Filtros
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cboFiltroStatus);
        filterPanel.add(btnFiltrarStatus);
        filterPanel.add(new JLabel("Gerente:"));
        filterPanel.add(cboFiltroGerente);
        filterPanel.add(btnFiltrarGerente);
        filterPanel.add(btnProjetosAtrasados);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(filterPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Cria o painel da tabela
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Projetos"));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Cria o painel do formulário
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Dados do Projeto"));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nome
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nome:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(txtNome, gbc);
        
        // Descrição
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.3;
        formPanel.add(new JScrollPane(txtDescricao), gbc);
        
        // Data de Início
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        formPanel.add(new JLabel("Data Início:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPanel dataInicioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dataInicioPanel.add(txtDataInicio);
        dataInicioPanel.add(new JLabel(" (dd/MM/yyyy)"));
        formPanel.add(dataInicioPanel, gbc);
        
        // Data de Término
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Data Término:"), gbc);
        gbc.gridx = 1;
        JPanel dataTerminoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dataTerminoPanel.add(txtDataTermino);
        dataTerminoPanel.add(new JLabel(" (dd/MM/yyyy)"));
        formPanel.add(dataTerminoPanel, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Status:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(cboStatus, gbc);
        
        // Gerente
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Gerente:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cboGerente, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Cria o painel inferior com botões
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        panel.add(btnNovo);
        panel.add(btnEditar);
        panel.add(btnExcluir);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(btnSalvar);
        panel.add(btnCancelar);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(btnGerenciarEquipes);
        panel.add(btnEstatisticas);
        
        return panel;
    }

    /**
     * Configura os manipuladores de eventos
     */
    private void setupEventHandlers() {
        // Seleção na tabela
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    Long id = (Long) tableModel.getValueAt(modelRow, 0);
                    controller.selecionarProjeto(id);
                }
            }
        });
        
        // Duplo clique na tabela para editar
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    if (btnEditar.isEnabled()) {
                        iniciarEdicao();
                    }
                }
            }
        });
        
        // Botões
        btnNovo.addActionListener(e -> iniciarNovoProjeto());
        btnEditar.addActionListener(e -> iniciarEdicao());
        btnExcluir.addActionListener(e -> controller.excluirProjeto());
        btnSalvar.addActionListener(e -> salvarProjeto());
        btnCancelar.addActionListener(e -> cancelarEdicao());
        btnGerenciarEquipes.addActionListener(e -> gerenciarEquipes());
        btnEstatisticas.addActionListener(e -> controller.obterEstatisticas());
        
        // Pesquisa
        btnPesquisar.addActionListener(e -> pesquisar());
        btnLimparPesquisa.addActionListener(e -> limparPesquisa());
        txtPesquisa.addActionListener(e -> pesquisar());
        
        // Filtros
        btnFiltrarStatus.addActionListener(e -> filtrarPorStatus());
        btnFiltrarGerente.addActionListener(e -> filtrarPorGerente());
        btnProjetosAtrasados.addActionListener(e -> controller.listarProjetosAtrasados());
    }

    /**
     * Configura as propriedades da janela
     */
    private void setupFrame() {
        setSize(1000, 700);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
    }

    /**
     * Configura o estado inicial dos componentes
     */
    private void configurarEstadoInicial() {
        editando = false;
        habilitarFormulario(false);
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnGerenciarEquipes.setEnabled(false);
    }

    /**
     * Inicia a criação de um novo projeto
     */
    private void iniciarNovoProjeto() {
        editando = true;
        projetoSelecionado = null;
        limparFormulario();
        habilitarFormulario(true);
        
        btnNovo.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnSalvar.setEnabled(true);
        btnCancelar.setEnabled(true);
        btnGerenciarEquipes.setEnabled(false);
        
        txtNome.requestFocus();
    }

    /**
     * Inicia a edição do projeto selecionado
     */
    private void iniciarEdicao() {
        if (projetoSelecionado == null) {
            exibirMensagemAviso("Selecione um projeto para editar");
            return;
        }
        
        if (!controller.podeEditarProjeto(projetoSelecionado)) {
            exibirMensagemAviso("Você não tem permissão para editar este projeto");
            return;
        }
        
        editando = true;
        habilitarFormulario(true);
        
        btnNovo.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnSalvar.setEnabled(true);
        btnCancelar.setEnabled(true);
        btnGerenciarEquipes.setEnabled(false);
        
        txtNome.requestFocus();
    }

    /**
     * Salva o projeto (novo ou editado)
     */
    private void salvarProjeto() {
        if (!validarFormulario()) {
            return;
        }
        
        String nome = txtNome.getText().trim();
        String descricao = txtDescricao.getText().trim();
        String status = (String) cboStatus.getSelectedItem();
        Usuario gerente = (Usuario) cboGerente.getSelectedItem();
        Long gerenteId = gerente != null ? gerente.getId() : null;
        
        LocalDate dataInicio = null;
        LocalDate dataTermino = null;
        
        try {
            String dataInicioStr = txtDataInicio.getText().trim();
            if (!dataInicioStr.isEmpty()) {
                dataInicio = LocalDate.parse(dataInicioStr, dateFormatter);
            }
            
            String dataTerminoStr = txtDataTermino.getText().trim();
            if (!dataTerminoStr.isEmpty()) {
                dataTermino = LocalDate.parse(dataTerminoStr, dateFormatter);
            }
        } catch (DateTimeParseException e) {
            exibirMensagemErro("Formato de data inválido. Use dd/MM/yyyy");
            return;
        }
        
        if (projetoSelecionado == null) {
            // Novo projeto
            controller.criarProjeto(nome, descricao, dataInicio, dataTermino, status, gerenteId);
        } else {
            // Atualizar projeto existente
            controller.atualizarProjeto(projetoSelecionado.getId(), nome, descricao, 
                                     dataInicio, dataTermino, status, gerenteId);
        }
    }

    /**
     * Cancela a edição
     */
    private void cancelarEdicao() {
        editando = false;
        habilitarFormulario(false);
        
        if (projetoSelecionado != null) {
            preencherFormulario(projetoSelecionado);
            configurarEstadoSelecao();
        } else {
            limparFormulario();
            configurarEstadoInicial();
        }
    }

    /**
     * Gerencia equipes do projeto
     */
    private void gerenciarEquipes() {
        if (projetoSelecionado == null) {
            exibirMensagemAviso("Selecione um projeto para gerenciar equipes");
            return;
        }
        
        try {
            // Obtém o JFrame pai
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            
            // Abre o diálogo de gerenciamento de equipes
            GerenciarEquipesDialog dialog = new GerenciarEquipesDialog(parentFrame, controller, projetoSelecionado);
            dialog.setVisible(true);
            
        } catch (Exception e) {
            exibirMensagemErro("Erro ao abrir gerenciamento de equipes: " + e.getMessage());
        }
    }

    /**
     * Realiza a pesquisa
     */
    private void pesquisar() {
        String termo = txtPesquisa.getText().trim();
        if (termo.isEmpty()) {
            controller.carregarProjetos();
        } else {
            controller.pesquisarProjetos(termo);
        }
    }

    /**
     * Limpa a pesquisa
     */
    private void limparPesquisa() {
        txtPesquisa.setText("");
        controller.carregarProjetos();
    }

    /**
     * Filtra projetos por status
     */
    private void filtrarPorStatus() {
        String status = (String) cboFiltroStatus.getSelectedItem();
        if ("Todos".equals(status)) {
            controller.carregarProjetos();
        } else {
            controller.listarProjetosPorStatus(status);
        }
    }

    /**
     * Filtra projetos por gerente
     */
    private void filtrarPorGerente() {
        Object selecionado = cboFiltroGerente.getSelectedItem();
        if (selecionado == null) {
            controller.carregarProjetos();
        } else if (selecionado instanceof Usuario) {
            Usuario gerente = (Usuario) selecionado;
            controller.listarProjetosPorGerente(gerente.getId());
        }
    }

    /**
     * Valida o formulário
     */
    private boolean validarFormulario() {
        if (txtNome.getText().trim().isEmpty()) {
            exibirMensagemAviso("Nome do projeto é obrigatório");
            txtNome.requestFocus();
            return false;
        }
        
        if (cboStatus.getSelectedItem() == null) {
            exibirMensagemAviso("Status do projeto é obrigatório");
            cboStatus.requestFocus();
            return false;
        }
        
        // Validar datas se preenchidas
        try {
            String dataInicioStr = txtDataInicio.getText().trim();
            String dataTerminoStr = txtDataTermino.getText().trim();
            
            LocalDate dataInicio = null;
            LocalDate dataTermino = null;
            
            if (!dataInicioStr.isEmpty()) {
                dataInicio = LocalDate.parse(dataInicioStr, dateFormatter);
            }
            
            if (!dataTerminoStr.isEmpty()) {
                dataTermino = LocalDate.parse(dataTerminoStr, dateFormatter);
            }
            
            if (dataInicio != null && dataTermino != null && dataInicio.isAfter(dataTermino)) {
                exibirMensagemAviso("Data de início não pode ser posterior à data de término");
                txtDataInicio.requestFocus();
                return false;
            }
            
        } catch (DateTimeParseException e) {
            exibirMensagemAviso("Formato de data inválido. Use dd/MM/yyyy");
            return false;
        }
        
        return true;
    }

    /**
     * Habilita/desabilita os campos do formulário
     */
    private void habilitarFormulario(boolean habilitar) {
        txtNome.setEnabled(habilitar);
        txtDescricao.setEnabled(habilitar);
        txtDataInicio.setEnabled(habilitar);
        txtDataTermino.setEnabled(habilitar);
        cboStatus.setEnabled(habilitar);
        cboGerente.setEnabled(habilitar);
    }

    /**
     * Atualiza a tabela com a lista de projetos
     */
    public void atualizarTabela(List<Projeto> projetos) {
        tableModel.setRowCount(0);
        
        for (Projeto projeto : projetos) {
            Object[] row = {
                projeto.getId(),
                projeto.getNome(),
                projeto.getDescricao(),
                projeto.getStatus(),
                projeto.getDataInicio() != null ? 
                    projeto.getDataInicio().format(dateFormatter) : "",
                projeto.getDataFimPrevista() != null ? 
                    projeto.getDataFimPrevista().format(dateFormatter) : "",
                projeto.getGerenteId() != null ? "ID: " + projeto.getGerenteId() : ""
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Seleciona um projeto na interface
     */
    public void selecionarProjeto(Projeto projeto) {
        this.projetoSelecionado = projeto;
        preencherFormulario(projeto);
        configurarEstadoSelecao();
    }

    /**
     * Preenche o formulário com os dados do projeto
     */
    private void preencherFormulario(Projeto projeto) {
        txtNome.setText(projeto.getNome());
        txtDescricao.setText(projeto.getDescricao());
        
        txtDataInicio.setText(projeto.getDataInicio() != null ? 
            projeto.getDataInicio().format(dateFormatter) : "");
        txtDataTermino.setText(projeto.getDataFimPrevista() != null ? 
            projeto.getDataFimPrevista().format(dateFormatter) : "");
        
        cboStatus.setSelectedItem(projeto.getStatus());
        
        // Selecionar gerente
        if (projeto.getGerenteId() != null) {
            for (int i = 0; i < cboGerente.getItemCount(); i++) {
                Usuario usuario = cboGerente.getItemAt(i);
                if (usuario != null && usuario.getId().equals(projeto.getGerenteId())) {
                    cboGerente.setSelectedItem(usuario);
                    break;
                }
            }
        } else {
            cboGerente.setSelectedItem(null);
        }
    }

    /**
     * Limpa o formulário
     */
    private void limparFormulario() {
        txtNome.setText("");
        txtDescricao.setText("");
        txtDataInicio.setText("");
        txtDataTermino.setText("");
        cboStatus.setSelectedIndex(0);
        cboGerente.setSelectedItem(null);
    }

    /**
     * Limpa a seleção
     */
    public void limparSelecao() {
        projetoSelecionado = null;
        table.clearSelection();
        limparFormulario();
        configurarEstadoInicial();
    }

    /**
     * Configura o estado quando há seleção
     */
    private void configurarEstadoSelecao() {
        btnNovo.setEnabled(true);
        btnEditar.setEnabled(controller.podeEditarProjeto(projetoSelecionado));
        btnExcluir.setEnabled(controller.podeEditarProjeto(projetoSelecionado));
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
        btnGerenciarEquipes.setEnabled(true);
        habilitarFormulario(false);
    }

    /**
     * Finaliza a edição com sucesso
     */
    public void finalizarEdicao() {
        editando = false;
        habilitarFormulario(false);
        
        btnNovo.setEnabled(true);
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
        
        if (projetoSelecionado != null) {
            configurarEstadoSelecao();
        } else {
            configurarEstadoInicial();
        }
        
        limparFormulario();
        limparSelecao();
    }

    /**
     * Obtém o projeto selecionado
     */
    public Projeto getProjetoSelecionado() {
        return projetoSelecionado;
    }

    /**
     * Exibe mensagem de sucesso
     */
    public void exibirMensagemSucesso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exibe mensagem de aviso
     */
    public void exibirMensagemAviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Exibe mensagem de erro
     */
    public void exibirMensagemErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}