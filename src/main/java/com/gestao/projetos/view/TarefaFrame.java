package com.gestao.projetos.view;

import com.gestao.projetos.controller.TarefaController;
import com.gestao.projetos.model.*;
import com.gestao.projetos.service.ProjetoService;
import com.gestao.projetos.service.EquipeService;
import com.gestao.projetos.service.UsuarioService;
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
 * Tela completa de gestão de tarefas com suporte a projetos e equipes
 */
public class TarefaFrame extends JInternalFrame {

    private final TarefaController controller;
    private final ProjetoService projetoService;
    private final EquipeService equipeService;
    private final UsuarioService usuarioService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Componentes da UI
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    // Formulário
    private JTextField txtTitulo;
    private JTextArea txtDescricao;
    private JComboBox<StatusTarefa> cmbStatus;
    private JSpinner spinnerPrioridade;
    private JSpinner spinnerEstimativaHoras;
    private JSpinner spinnerHorasTrabalhadas;
    private JFormattedTextField txtDataInicioPrevista;
    private JFormattedTextField txtDataFimPrevista;
    private JComboBox<Projeto> cmbProjeto;
    private JComboBox<Usuario> cmbResponsavel;
    private JComboBox<Equipe> cmbEquipe;
    
    // Pesquisa e filtros
    private JTextField txtPesquisa;
    private JButton btnPesquisar;
    private JButton btnLimparPesquisa;
    private JComboBox<String> cmbFiltroProjeto;
    private JComboBox<String> cmbFiltroEquipe;
    private JComboBox<StatusTarefa> cmbFiltroStatus;
    private JButton btnFiltrarProjeto;
    private JButton btnFiltrarEquipe;
    private JButton btnFiltrarStatus;
    private JButton btnTarefasAtrasadas;

    // Botões de ação
    private JButton btnSalvar;
    private JButton btnCancelar;
    private JButton btnNovo;
    private JButton btnEditar;
    private JButton btnExcluir;

    private Tarefa tarefaSelecionada;
    private boolean editando = false;

    public TarefaFrame() {
        super("Gestão de Tarefas", true, true, true, true);
        this.controller = new TarefaController(this);
        this.projetoService = new ProjetoService();
        this.equipeService = new EquipeService();
        this.usuarioService = new UsuarioService();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupFrame();
        configurarEstadoInicial();
        carregarDados();
        controller.carregarTarefas();
    }

    private void initializeComponents() {
        // Configurar tabela
        String[] colunas = {"ID", "Título", "Projeto", "Equipe", "Responsável", "Status", "Prioridade", "Est.(h)", "Trab.(h)", "Início Prev.", "Fim Prev."};
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

        // Componentes do formulário
        txtTitulo = new JTextField(25);
        txtDescricao = new JTextArea(4, 25);
        txtDescricao.setLineWrap(true);
        txtDescricao.setWrapStyleWord(true);

        cmbStatus = new JComboBox<>(StatusTarefa.values());
        cmbProjeto = new JComboBox<>();
        cmbResponsavel = new JComboBox<>();
        cmbEquipe = new JComboBox<>();
        
        spinnerPrioridade = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        spinnerEstimativaHoras = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999.0, 0.5));
        spinnerHorasTrabalhadas = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999.0, 0.5));
        
        txtDataInicioPrevista = new JFormattedTextField();
        txtDataInicioPrevista.setColumns(10);
        txtDataFimPrevista = new JFormattedTextField();
        txtDataFimPrevista.setColumns(10);
        
        // Componentes de pesquisa e filtros
        txtPesquisa = new JTextField(20);
        btnPesquisar = new JButton("Pesquisar");
        btnLimparPesquisa = new JButton("Limpar");
        
        cmbFiltroProjeto = new JComboBox<>();
        cmbFiltroEquipe = new JComboBox<>();
        cmbFiltroStatus = new JComboBox<>(StatusTarefa.values());
        
        btnFiltrarProjeto = new JButton("Filtrar por Projeto");
        btnFiltrarEquipe = new JButton("Filtrar por Equipe");
        btnFiltrarStatus = new JButton("Filtrar por Status");
        btnTarefasAtrasadas = new JButton("Tarefas Atrasadas");

        // Botões de ação
        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
        btnNovo = new JButton("Nova Tarefa");
        btnEditar = new JButton("Editar");
        btnExcluir = new JButton("Excluir");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Painel superior com pesquisa e filtros
        add(createTopPanel(), BorderLayout.NORTH);

        // Painel central com split entre tabela e formulário
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createTablePanel());
        splitPane.setRightComponent(createFormPanel());
        splitPane.setDividerLocation(600);
        add(splitPane, BorderLayout.CENTER);

        // Painel inferior com botões
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Pesquisa e Filtros"));

        // Pesquisa
        panel.add(new JLabel("Pesquisar:"));
        panel.add(txtPesquisa);
        panel.add(btnPesquisar);
        panel.add(btnLimparPesquisa);

        panel.add(Box.createHorizontalStrut(20));

        // Filtros
        panel.add(new JLabel("Projeto:"));
        panel.add(cmbFiltroProjeto);
        panel.add(btnFiltrarProjeto);

        panel.add(new JLabel("Equipe:"));
        panel.add(cmbFiltroEquipe);
        panel.add(btnFiltrarEquipe);

        panel.add(new JLabel("Status:"));
        panel.add(cmbFiltroStatus);
        panel.add(btnFiltrarStatus);

        panel.add(btnTarefasAtrasadas);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Tarefas"));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Detalhes da Tarefa"));

        // Informações básicas
        JPanel basicPanel = createBasicInfoPanel();
        panel.add(basicPanel);

        // Datas
        JPanel datesPanel = createDatesPanel();
        panel.add(datesPanel);

        // Relacionamentos
        JPanel relationshipsPanel = createRelationshipsPanel();
        panel.add(relationshipsPanel);

        // Estimativas
        JPanel estimatesPanel = createEstimatesPanel();
        panel.add(estimatesPanel);

        return panel;
    }

    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Informações Básicas"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Título
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Título:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(txtTitulo, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Status:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cmbStatus, gbc);

        // Prioridade
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Prioridade (1-5):"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(spinnerPrioridade, gbc);

        // Descrição
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Descrição:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JScrollPane scrollDesc = new JScrollPane(txtDescricao);
        scrollDesc.setPreferredSize(new Dimension(250, 80));
        panel.add(scrollDesc, gbc);

        return panel;
    }

    private JPanel createDatesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datas"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Data início prevista
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Início Previsto:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtDataInicioPrevista, gbc);

        // Data fim prevista
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Fim Previsto:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtDataFimPrevista, gbc);

        // Adicionar hints para formato
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel lblHint = new JLabel("Formato: dd/MM/yyyy (ex: 25/12/2024)");
        lblHint.setFont(lblHint.getFont().deriveFont(Font.ITALIC, 10f));
        lblHint.setForeground(Color.GRAY);
        panel.add(lblHint, gbc);

        return panel;
    }

    private JPanel createRelationshipsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Relacionamentos"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Projeto
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Projeto:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(cmbProjeto, gbc);

        // Responsável
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Responsável:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cmbResponsavel, gbc);

        // Equipe
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Equipe:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cmbEquipe, gbc);

        return panel;
    }

    private JPanel createEstimatesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Estimativas de Tempo"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Estimativa
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Estimativa (horas):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(spinnerEstimativaHoras, gbc);

        // Horas trabalhadas
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Horas Trabalhadas:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(spinnerHorasTrabalhadas, gbc);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        panel.add(btnNovo);
        panel.add(btnEditar);
        panel.add(btnExcluir);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnSalvar);
        panel.add(btnCancelar);

        return panel;
    }

    private void setupEventHandlers() {
        // Event handlers da tabela
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    selecionarTarefa();
                } else if (e.getClickCount() == 2) {
                    iniciarEdicao();
                }
            }
        });

        // Botões de ação
        btnNovo.addActionListener(e -> iniciarNovaTarefa());
        btnEditar.addActionListener(e -> iniciarEdicao());
        btnExcluir.addActionListener(e -> excluirTarefa());
        btnSalvar.addActionListener(e -> salvarTarefa());
        btnCancelar.addActionListener(e -> cancelarEdicao());

        // Pesquisa e filtros
        btnPesquisar.addActionListener(e -> realizarPesquisa());
        btnLimparPesquisa.addActionListener(e -> limparPesquisa());
        btnFiltrarProjeto.addActionListener(e -> filtrarPorProjeto());
        btnFiltrarEquipe.addActionListener(e -> filtrarPorEquipe());
        btnFiltrarStatus.addActionListener(e -> filtrarPorStatus());
        btnTarefasAtrasadas.addActionListener(e -> mostrarTarefasAtrasadas());

        // Enter na pesquisa
        txtPesquisa.addActionListener(e -> realizarPesquisa());
    }

    private void setupFrame() {
        setSize(1200, 800);
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    }

    private void configurarEstadoInicial() {
        editando = false;
        tarefaSelecionada = null;
        habilitarFormulario(false);
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
    }

    private void carregarDados() {
        try {
            carregarProjetos();
            carregarEquipes();
            carregarUsuarios();
        } catch (Exception e) {
            showError("Erro ao carregar dados: " + e.getMessage());
        }
    }

    private void carregarProjetos() throws Exception {
        List<Projeto> projetos = projetoService.listarTodos();
        
        cmbProjeto.removeAllItems();
        cmbProjeto.addItem(null); // Opção vazia
        cmbFiltroProjeto.removeAllItems();
        cmbFiltroProjeto.addItem("-- Todos os Projetos --");
        
        for (Projeto projeto : projetos) {
            cmbProjeto.addItem(projeto);
            cmbFiltroProjeto.addItem(projeto.getNome());
        }
    }

    private void carregarEquipes() throws Exception {
        List<Equipe> equipes = equipeService.listarAtivas();
        
        cmbEquipe.removeAllItems();
        cmbEquipe.addItem(null); // Opção vazia
        cmbFiltroEquipe.removeAllItems();
        cmbFiltroEquipe.addItem("-- Todas as Equipes --");
        
        for (Equipe equipe : equipes) {
            cmbEquipe.addItem(equipe);
            cmbFiltroEquipe.addItem(equipe.getNome());
        }
    }

    private void carregarUsuarios() throws Exception {
        List<Usuario> usuarios = usuarioService.listarAtivos();
        
        cmbResponsavel.removeAllItems();
        cmbResponsavel.addItem(null); // Opção vazia
        
        for (Usuario usuario : usuarios) {
            cmbResponsavel.addItem(usuario);
        }
    }

    private void selecionarTarefa() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            Long id = (Long) tableModel.getValueAt(modelRow, 0);
            
            try {
                tarefaSelecionada = controller.buscarTarefaPorId(id);
                if (tarefaSelecionada != null) {
                    preencherFormulario(tarefaSelecionada);
                    configurarEstadoSelecao();
                }
            } catch (Exception e) {
                showError("Erro ao carregar tarefa: " + e.getMessage());
            }
        }
    }

    private void preencherFormulario(Tarefa tarefa) {
        if (tarefa == null) return;

        txtTitulo.setText(tarefa.getTitulo());
        txtDescricao.setText(tarefa.getDescricao());
        cmbStatus.setSelectedItem(tarefa.getStatus());
        spinnerPrioridade.setValue(tarefa.getPrioridade());
        
        if (tarefa.getEstimativaHoras() != null) {
            spinnerEstimativaHoras.setValue(tarefa.getEstimativaHoras());
        }
        if (tarefa.getHorasTrabalhadas() != null) {
            spinnerHorasTrabalhadas.setValue(tarefa.getHorasTrabalhadas());
        }

        // Datas
        if (tarefa.getDataInicioPrevista() != null) {
            txtDataInicioPrevista.setText(tarefa.getDataInicioPrevista().format(dateFormatter));
        } else {
            txtDataInicioPrevista.setText("");
        }
        
        if (tarefa.getDataFimPrevista() != null) {
            txtDataFimPrevista.setText(tarefa.getDataFimPrevista().format(dateFormatter));
        } else {
            txtDataFimPrevista.setText("");
        }

        // Relacionamentos
        if (tarefa.getProjeto() != null) {
            cmbProjeto.setSelectedItem(tarefa.getProjeto());
        } else {
            cmbProjeto.setSelectedItem(null);
        }
        
        if (tarefa.getResponsavel() != null) {
            cmbResponsavel.setSelectedItem(tarefa.getResponsavel());
        } else {
            cmbResponsavel.setSelectedItem(null);
        }
        
        if (tarefa.getEquipe() != null) {
            cmbEquipe.setSelectedItem(tarefa.getEquipe());
        } else {
            cmbEquipe.setSelectedItem(null);
        }
    }

    private void limparFormulario() {
        txtTitulo.setText("");
        txtDescricao.setText("");
        cmbStatus.setSelectedItem(StatusTarefa.NOVA);
        spinnerPrioridade.setValue(3);
        spinnerEstimativaHoras.setValue(0.0);
        spinnerHorasTrabalhadas.setValue(0.0);
        txtDataInicioPrevista.setText("");
        txtDataFimPrevista.setText("");
        cmbProjeto.setSelectedItem(null);
        cmbResponsavel.setSelectedItem(null);
        cmbEquipe.setSelectedItem(null);
    }

    private void habilitarFormulario(boolean habilitado) {
        txtTitulo.setEnabled(habilitado);
        txtDescricao.setEnabled(habilitado);
        cmbStatus.setEnabled(habilitado);
        spinnerPrioridade.setEnabled(habilitado);
        spinnerEstimativaHoras.setEnabled(habilitado);
        spinnerHorasTrabalhadas.setEnabled(habilitado);
        txtDataInicioPrevista.setEnabled(habilitado);
        txtDataFimPrevista.setEnabled(habilitado);
        cmbProjeto.setEnabled(habilitado);
        cmbResponsavel.setEnabled(habilitado);
        cmbEquipe.setEnabled(habilitado);
    }

    private void configurarEstadoSelecao() {
        editando = false;
        habilitarFormulario(false);
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
        btnEditar.setEnabled(true);
        btnExcluir.setEnabled(true);
    }

    private void configurarEstadoEdicao() {
        editando = true;
        habilitarFormulario(true);
        btnSalvar.setEnabled(true);
        btnCancelar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnNovo.setEnabled(false);
    }

    private void iniciarNovaTarefa() {
        tarefaSelecionada = null;
        limparFormulario();
        configurarEstadoEdicao();
        txtTitulo.requestFocus();
    }

    private void iniciarEdicao() {
        if (tarefaSelecionada != null) {
            configurarEstadoEdicao();
            txtTitulo.requestFocus();
        }
    }

    private void cancelarEdicao() {
        if (tarefaSelecionada != null) {
            preencherFormulario(tarefaSelecionada);
            configurarEstadoSelecao();
        } else {
            limparFormulario();
            configurarEstadoInicial();
        }
    }

    private void salvarTarefa() {
        if (!validarFormulario()) {
            return;
        }

        try {
            Tarefa tarefa = tarefaSelecionada != null ? tarefaSelecionada : new Tarefa();
            
            // Dados básicos
            tarefa.setTitulo(txtTitulo.getText().trim());
            tarefa.setDescricao(txtDescricao.getText().trim());
            tarefa.setStatus((StatusTarefa) cmbStatus.getSelectedItem());
            tarefa.setPrioridade((Integer) spinnerPrioridade.getValue());
            tarefa.setEstimativaHoras((Double) spinnerEstimativaHoras.getValue());
            tarefa.setHorasTrabalhadas((Double) spinnerHorasTrabalhadas.getValue());

            // Datas
            String dataInicioStr = txtDataInicioPrevista.getText().trim();
            if (!dataInicioStr.isEmpty()) {
                try {
                    tarefa.setDataInicioPrevista(LocalDate.parse(dataInicioStr, dateFormatter));
                } catch (DateTimeParseException e) {
                    showError("Data de início inválida. Use o formato dd/MM/yyyy");
                    return;
                }
            }

            String dataFimStr = txtDataFimPrevista.getText().trim();
            if (!dataFimStr.isEmpty()) {
                try {
                    tarefa.setDataFimPrevista(LocalDate.parse(dataFimStr, dateFormatter));
                } catch (DateTimeParseException e) {
                    showError("Data de fim inválida. Use o formato dd/MM/yyyy");
                    return;
                }
            }

            // Relacionamentos
            Projeto projeto = (Projeto) cmbProjeto.getSelectedItem();
            if (projeto != null) {
                tarefa.setProjeto(projeto);
            }

            Usuario responsavel = (Usuario) cmbResponsavel.getSelectedItem();
            if (responsavel != null) {
                tarefa.setResponsavel(responsavel);
            }

            Equipe equipe = (Equipe) cmbEquipe.getSelectedItem();
            if (equipe != null) {
                tarefa.setEquipe(equipe);
            }

            // Salvar
            if (tarefaSelecionada == null) {
                controller.criarTarefa(tarefa);
                showSuccess("Tarefa criada com sucesso!");
            } else {
                controller.atualizarTarefa(tarefa);
                showSuccess("Tarefa atualizada com sucesso!");
            }

            configurarEstadoInicial();
            limparFormulario();
            controller.carregarTarefas();

        } catch (Exception e) {
            showError("Erro ao salvar tarefa: " + e.getMessage());
        }
    }

    private void excluirTarefa() {
        if (tarefaSelecionada == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Tem certeza que deseja excluir a tarefa '" + tarefaSelecionada.getTitulo() + "'?",
            "Confirmar Exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.removerTarefa(tarefaSelecionada.getId());
                showSuccess("Tarefa excluída com sucesso!");
                configurarEstadoInicial();
                limparFormulario();
                controller.carregarTarefas();
            } catch (Exception e) {
                showError("Erro ao excluir tarefa: " + e.getMessage());
            }
        }
    }

    private boolean validarFormulario() {
        if (!ValidationUtil.isNotEmpty(txtTitulo.getText())) {
            showError("O título da tarefa é obrigatório.");
            txtTitulo.requestFocus();
            return false;
        }

        if (cmbProjeto.getSelectedItem() == null) {
            showError("É necessário selecionar um projeto.");
            cmbProjeto.requestFocus();
            return false;
        }

        // Validar datas se ambas estiverem preenchidas
        String dataInicioStr = txtDataInicioPrevista.getText().trim();
        String dataFimStr = txtDataFimPrevista.getText().trim();
        
        if (!dataInicioStr.isEmpty() && !dataFimStr.isEmpty()) {
            try {
                LocalDate dataInicio = LocalDate.parse(dataInicioStr, dateFormatter);
                LocalDate dataFim = LocalDate.parse(dataFimStr, dateFormatter);
                
                if (dataFim.isBefore(dataInicio)) {
                    showError("A data de fim não pode ser anterior à data de início.");
                    txtDataFimPrevista.requestFocus();
                    return false;
                }
            } catch (DateTimeParseException e) {
                showError("Formato de data inválido. Use dd/MM/yyyy");
                return false;
            }
        }

        return true;
    }

    private void realizarPesquisa() {
        String termo = txtPesquisa.getText().trim();
        try {
            controller.pesquisarTarefas(termo);
        } catch (Exception e) {
            showError("Erro na pesquisa: " + e.getMessage());
        }
    }

    private void limparPesquisa() {
        txtPesquisa.setText("");
        controller.carregarTarefas();
    }

    private void filtrarPorProjeto() {
        String projetoNome = (String) cmbFiltroProjeto.getSelectedItem();
        if (projetoNome != null && !projetoNome.startsWith("--")) {
            try {
                // Encontrar projeto pelo nome e filtrar
                List<Projeto> projetos = projetoService.listarTodos();
                Projeto projeto = projetos.stream()
                    .filter(p -> p.getNome().equals(projetoNome))
                    .findFirst()
                    .orElse(null);
                
                if (projeto != null) {
                    controller.filtrarPorProjeto(projeto.getId());
                }
            } catch (Exception e) {
                showError("Erro ao filtrar por projeto: " + e.getMessage());
            }
        } else {
            controller.carregarTarefas();
        }
    }

    private void filtrarPorEquipe() {
        String equipeNome = (String) cmbFiltroEquipe.getSelectedItem();
        if (equipeNome != null && !equipeNome.startsWith("--")) {
            try {
                // Encontrar equipe pelo nome e filtrar
                List<Equipe> equipes = equipeService.listarAtivas();
                Equipe equipe = equipes.stream()
                    .filter(e -> e.getNome().equals(equipeNome))
                    .findFirst()
                    .orElse(null);
                
                if (equipe != null) {
                    controller.filtrarPorEquipe(equipe.getId());
                }
            } catch (Exception e) {
                showError("Erro ao filtrar por equipe: " + e.getMessage());
            }
        } else {
            controller.carregarTarefas();
        }
    }

    private void filtrarPorStatus() {
        StatusTarefa status = (StatusTarefa) cmbFiltroStatus.getSelectedItem();
        if (status != null) {
            try {
                controller.filtrarPorStatus(status);
            } catch (Exception e) {
                showError("Erro ao filtrar por status: " + e.getMessage());
            }
        }
    }

    private void mostrarTarefasAtrasadas() {
        try {
            controller.carregarTarefasAtrasadas();
        } catch (Exception e) {
            showError("Erro ao carregar tarefas atrasadas: " + e.getMessage());
        }
    }

    public void atualizarTabela(List<Tarefa> tarefas) {
        try {
            tableModel.setRowCount(0);
            for (Tarefa tarefa : tarefas) {
                Object[] linha = {
                    tarefa.getId(),
                    tarefa.getTitulo(),
                    tarefa.getProjeto() != null ? tarefa.getProjeto().getNome() : "N/A",
                    tarefa.getEquipe() != null ? tarefa.getEquipe().getNome() : "N/A",
                    tarefa.getResponsavel() != null ? tarefa.getResponsavel().getNome() : "N/A",
                    tarefa.getStatus().getDescricao(),
                    tarefa.getPrioridadeTexto(),
                    tarefa.getEstimativaHoras() != null ? tarefa.getEstimativaHoras() : 0.0,
                    tarefa.getHorasTrabalhadas() != null ? tarefa.getHorasTrabalhadas() : 0.0,
                    tarefa.getDataInicioPrevista() != null ? tarefa.getDataInicioPrevista().format(dateFormatter) : "",
                    tarefa.getDataFimPrevista() != null ? tarefa.getDataFimPrevista().format(dateFormatter) : ""
                };
                tableModel.addRow(linha);
            }
            
            // Limpar seleção
            table.clearSelection();
            configurarEstadoInicial();
            limparFormulario();
            
        } catch (Exception e) {
            showError("Erro ao atualizar tabela: " + e.getMessage());
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean confirmarAcao(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirmação", 
               JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}