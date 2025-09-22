package com.gestao.projetos.view;

import com.gestao.projetos.controller.TarefaController;
import com.gestao.projetos.model.StatusTarefa;
import com.gestao.projetos.model.Tarefa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TarefaFrame extends JInternalFrame {

    private final TarefaController controller;

    // Componentes da UI
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtTitulo;
    private JTextArea txtDescricao;
    private JComboBox<StatusTarefa> cmbStatus;
    private JSpinner spinnerPrioridade;
    private JSpinner spinnerEstimativaHoras;
    private JSpinner spinnerHorasTrabalhadas;
    private JFormattedTextField txtDataFimPrevista;
    private JTextField txtProjetoId;
    private JTextField txtResponsavelId;
    private JTextField txtPesquisa;
    private JButton btnPesquisar;

    private JButton btnSalvar;
    private JButton btnCancelar;
    private JButton btnNovo;
    private JButton btnEditar;
    private JButton btnExcluir;

    private Tarefa tarefaSelecionada;

    public TarefaFrame() {
        super("Gestão de Tarefas", true, true, true, true);
        this.controller = new TarefaController(this);

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        controller.carregarTarefas();
        setSize(1024, 768);
    }

    private void initializeComponents() {
        String[] colunas = {"ID", "Título", "Status", "Prioridade", "Estimativa (h)", "Trabalhadas (h)", "Fim Previsto", "Projeto ID", "Responsável ID"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        txtTitulo = new JTextField(30);
        txtDescricao = new JTextArea(5, 30);
        txtDescricao.setLineWrap(true);
        txtDescricao.setWrapStyleWord(true);
        cmbStatus = new JComboBox<>(StatusTarefa.values());
        spinnerPrioridade = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        spinnerEstimativaHoras = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.5));
        spinnerHorasTrabalhadas = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.5));

        try {
            javax.swing.text.MaskFormatter mask = new javax.swing.text.MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
            txtDataFimPrevista = new JFormattedTextField(mask);
            txtDataFimPrevista.setToolTipText("Use o formato DD/MM/AAAA");
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            txtDataFimPrevista = new JFormattedTextField();
        }

        txtProjetoId = new JTextField(10);
        txtResponsavelId = new JTextField(10);
        txtPesquisa = new JTextField(20);
        btnPesquisar = new JButton("Pesquisar");
        btnNovo = new JButton("Novo");
        btnEditar = new JButton("Editar");
        btnExcluir = new JButton("Excluir");
        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");

        configurarEstadoInicial();
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel painelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelPesquisa.setBorder(BorderFactory.createTitledBorder("Pesquisa de Tarefas"));
        painelPesquisa.add(new JLabel("Buscar por Título:"));
        painelPesquisa.add(txtPesquisa);
        painelPesquisa.add(btnPesquisar);
        add(painelPesquisa, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel();
        painelBotoes.add(btnNovo);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);

        JPanel painelFormulario = new JPanel(new GridBagLayout());
        painelFormulario.setBorder(BorderFactory.createTitledBorder("Dados da Tarefa"));
        painelFormulario.setPreferredSize(new Dimension(380, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; painelFormulario.add(new JLabel("Título:"), gbc);
        gbc.gridy++; painelFormulario.add(txtTitulo, gbc);
        gbc.gridy++; painelFormulario.add(new JLabel("Descrição:"), gbc);
        gbc.gridy++; painelFormulario.add(new JScrollPane(txtDescricao), gbc);
        gbc.gridy++; painelFormulario.add(new JLabel("Status:"), gbc);
        gbc.gridy++; painelFormulario.add(cmbStatus, gbc);
        gbc.gridy++; painelFormulario.add(new JLabel("Prioridade (1-5):"), gbc);
        gbc.gridy++; painelFormulario.add(spinnerPrioridade, gbc);
        gbc.gridy++; painelFormulario.add(new JLabel("Estimativa de Horas:"), gbc);
        gbc.gridy++; painelFormulario.add(spinnerEstimativaHoras, gbc);
        gbc.gridy++; painelFormulario.add(new JLabel("Horas Trabalhadas:"), gbc);
        gbc.gridy++; painelFormulario.add(spinnerHorasTrabalhadas, gbc);
        gbc.gridy++; painelFormulario.add(new JLabel("Data Fim Prevista:"), gbc);
        gbc.gridy++; painelFormulario.add(txtDataFimPrevista, gbc);
        gbc.gridy++; painelFormulario.add(new JLabel("ID do Projeto:"), gbc);
        gbc.gridy++; painelFormulario.add(txtProjetoId, gbc);
        gbc.gridy++; painelFormulario.add(new JLabel("ID do Responsável:"), gbc);
        gbc.gridy++; painelFormulario.add(txtResponsavelId, gbc);
        gbc.weighty = 1.0; gbc.gridy++; painelFormulario.add(new JLabel(), gbc);
        add(painelFormulario, BorderLayout.EAST);
    }

    private void setupEventHandlers() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (table.getSelectedRow() != -1) {
                    Long id = (Long) tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
                    controller.selecionarTarefa(id);
                } else {
                    controller.selecionarTarefa(null);
                }
            }
        });

        btnNovo.addActionListener(e -> iniciarNovaTarefa());
        btnEditar.addActionListener(e -> iniciarEdicao());
        btnCancelar.addActionListener(e -> cancelarEdicao());
        btnSalvar.addActionListener(e -> salvarTarefa());
        btnPesquisar.addActionListener(e -> controller.pesquisarTarefas(txtPesquisa.getText()));
        btnExcluir.addActionListener(e -> {
            if (tarefaSelecionada != null) {
                controller.excluirTarefa(tarefaSelecionada.getId());
            } else {
                showError("Selecione uma tarefa para excluir.");
            }
        });
    }

    private void salvarTarefa() {
        String titulo = txtTitulo.getText().trim();
        String descricao = txtDescricao.getText().trim();
        StatusTarefa status = (StatusTarefa) cmbStatus.getSelectedItem();
        int prioridade = (int) spinnerPrioridade.getValue();
        Double estimativaHoras = (Double) spinnerEstimativaHoras.getValue();
        Double horasTrabalhadas = (Double) spinnerHorasTrabalhadas.getValue();
        Long projetoId = null;
        try {
            if (!txtProjetoId.getText().trim().isEmpty()) {
                projetoId = Long.parseLong(txtProjetoId.getText().trim());
            }
        } catch (NumberFormatException e) {
            showError("ID do Projeto inválido. Deve ser um número.");
            return;
        }
        Long responsavelId = null;
        try {
            if (!txtResponsavelId.getText().trim().isEmpty()) {
                responsavelId = Long.parseLong(txtResponsavelId.getText().trim());
            }
        } catch (NumberFormatException e) {
            showError("ID do Responsável inválido. Deve ser um número.");
            return;
        }
        LocalDate dataFim = null;
        try {
            String textoData = txtDataFimPrevista.getText();
            if (textoData != null && !textoData.contains("_")) {
                dataFim = LocalDate.parse(textoData, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        } catch (DateTimeParseException e) {
            showError("Formato de data inválido ou data incompleta. Use DD/MM/AAAA.");
            return;
        }
        controller.salvarTarefa(titulo, descricao, status, prioridade, estimativaHoras, horasTrabalhadas, dataFim, projetoId, responsavelId, tarefaSelecionada);
    }

    public void atualizarTabela(List<Tarefa> tarefas) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Tarefa tarefa : tarefas) {
            tableModel.addRow(new Object[]{
                    tarefa.getId(),
                    tarefa.getTitulo(),
                    tarefa.getStatus(),
                    tarefa.getPrioridadeTexto(),
                    tarefa.getEstimativaHoras(),
                    tarefa.getHorasTrabalhadas(),
                    tarefa.getDataFimPrevista() != null ? tarefa.getDataFimPrevista().format(formatter) : "",
                    tarefa.getProjetoId(),
                    tarefa.getResponsavelId()
            });
        }
    }

    public void selecionarTarefa(Tarefa tarefa) {
        this.tarefaSelecionada = tarefa;
        if (tarefa != null) {
            preencherFormulario(tarefa);
            configurarEstadoSelecao();
        } else {
            limparFormulario();
            configurarEstadoInicial();
        }
    }

    private void preencherFormulario(Tarefa tarefa) {
        txtTitulo.setText(tarefa.getTitulo());
        txtDescricao.setText(tarefa.getDescricao());
        cmbStatus.setSelectedItem(tarefa.getStatus());
        spinnerPrioridade.setValue(tarefa.getPrioridade());
        spinnerEstimativaHoras.setValue(tarefa.getEstimativaHoras() != null ? tarefa.getEstimativaHoras() : 0.0);
        spinnerHorasTrabalhadas.setValue(tarefa.getHorasTrabalhadas() != null ? tarefa.getHorasTrabalhadas() : 0.0);
        txtProjetoId.setText(tarefa.getProjetoId() != null ? String.valueOf(tarefa.getProjetoId()) : "");
        txtResponsavelId.setText(tarefa.getResponsavelId() != null ? String.valueOf(tarefa.getResponsavelId()) : "");
        txtDataFimPrevista.setText(tarefa.getDataFimPrevista() != null ? tarefa.getDataFimPrevista().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
    }

    private void limparFormulario() {
        txtTitulo.setText("");
        txtDescricao.setText("");
        cmbStatus.setSelectedIndex(0);
        spinnerPrioridade.setValue(3);
        spinnerEstimativaHoras.setValue(0.0);
        spinnerHorasTrabalhadas.setValue(0.0);
        txtDataFimPrevista.setText("");
        txtProjetoId.setText("");
        txtResponsavelId.setText("");
    }

    private void habilitarFormulario(boolean habilitar) {
        txtTitulo.setEditable(habilitar);
        txtDescricao.setEditable(habilitar);
        cmbStatus.setEnabled(habilitar);
        spinnerPrioridade.setEnabled(habilitar);
        spinnerEstimativaHoras.setEnabled(habilitar);
        spinnerHorasTrabalhadas.setEnabled(habilitar);
        txtDataFimPrevista.setEditable(habilitar);
        txtProjetoId.setEditable(habilitar);
        txtResponsavelId.setEditable(habilitar);
    }

    private void configurarEstadoInicial() {
        habilitarFormulario(false);
        btnNovo.setEnabled(true);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
    }

    private void configurarEstadoSelecao() {
        habilitarFormulario(false);
        btnNovo.setEnabled(true);
        btnEditar.setEnabled(true);
        btnExcluir.setEnabled(true);
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
    }

    private void configurarEstadoEdicao() {
        habilitarFormulario(true);
        btnNovo.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnSalvar.setEnabled(true);
        btnCancelar.setEnabled(true);
        txtTitulo.requestFocus();
    }

    private void iniciarNovaTarefa() {
        tarefaSelecionada = null;
        limparFormulario();
        configurarEstadoEdicao();
    }

    private void iniciarEdicao() {
        if (tarefaSelecionada != null) {
            configurarEstadoEdicao();
        } else {
            showError("Por favor, selecione uma tarefa para editar.");
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

    public void finalizarEdicao() {
        table.clearSelection();
        limparFormulario();
        configurarEstadoInicial();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean confirmarAcao(String message) {
        int result = JOptionPane.showConfirmDialog(this, message, "Confirmação", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
}