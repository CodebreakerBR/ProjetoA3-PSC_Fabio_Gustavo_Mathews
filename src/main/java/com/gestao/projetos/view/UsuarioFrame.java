package com.gestao.projetos.view;

import com.gestao.projetos.controller.UsuarioController;
import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Tela de gestão de usuários
 */
public class UsuarioFrame extends JInternalFrame {
    
    private final UsuarioController controller;
    
    // Componentes da interface
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    private JTextField txtNome;
    private JTextField txtEmail;
    private JCheckBox chkAtivo;
    
    private JTextField txtPesquisa;
    private JButton btnPesquisar;
    private JButton btnLimparPesquisa;
    
    private JButton btnNovo;
    private JButton btnEditar;
    private JButton btnExcluir;
    private JButton btnSalvar;
    private JButton btnCancelar;
    
    private Usuario usuarioSelecionado;
    private boolean editando = false;

    public UsuarioFrame() {
        super("Gestão de Usuários", true, true, true, true);
        this.controller = new UsuarioController(this);
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupFrame();
        
        controller.carregarUsuarios();
    }

    /**
     * Inicializa os componentes da interface
     */
    private void initializeComponents() {
        // Tabela
        String[] colunas = {"ID", "Nome", "Email", "Ativo", "Criado em"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela somente leitura
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: return Long.class; // ID
                    case 3: return Boolean.class; // Ativo
                    default: return String.class;
                }
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Configuração das colunas
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Nome
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // Email
        table.getColumnModel().getColumn(3).setPreferredWidth(60);  // Ativo
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Criado em
        
        // Ordenação
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        
        // Campos de entrada
        txtNome = new JTextField(30);
        txtEmail = new JTextField(30);
        chkAtivo = new JCheckBox("Ativo");
        chkAtivo.setSelected(true);
        
        // Campo de pesquisa
        txtPesquisa = new JTextField(20);
        btnPesquisar = new JButton("Pesquisar");
        btnLimparPesquisa = new JButton("Limpar");
        
        // Botões de ação
        btnNovo = new JButton("Novo");
        btnEditar = new JButton("Editar");
        btnExcluir = new JButton("Excluir");
        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
        
        configurarEstadoInicial();
    }

    /**
     * Configura o layout da janela
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel superior - pesquisa
        JPanel painelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelPesquisa.setBorder(BorderFactory.createTitledBorder("Pesquisa"));
        painelPesquisa.add(new JLabel("Buscar:"));
        painelPesquisa.add(txtPesquisa);
        painelPesquisa.add(btnPesquisar);
        painelPesquisa.add(btnLimparPesquisa);
        
        // Painel central - tabela
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Usuários"));
        
        // Painel direito - formulário
        JPanel painelFormulario = createFormPanel();
        
        // Painel inferior - botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotoes.add(btnNovo);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        
        // Layout principal
        add(painelPesquisa, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(painelFormulario, BorderLayout.EAST);
        add(painelBotoes, BorderLayout.SOUTH);
    }

    /**
     * Cria o painel do formulário
     */
    private JPanel createFormPanel() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Dados do Usuário"));
        painel.setPreferredSize(new Dimension(300, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nome
        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Nome:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        painel.add(txtNome, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        painel.add(txtEmail, gbc);
        
        // Ativo
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painel.add(chkAtivo, gbc);
        
        // Espaço flexível
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        painel.add(new JPanel(), gbc);
        
        return painel;
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
                    controller.selecionarUsuario(id);
                } else {
                    limparSelecao();
                }
            }
        });
        
        // Duplo clique na tabela
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !editando) {
                    btnEditar.doClick();
                }
            }
        });
        
        // Botões
        btnNovo.addActionListener(e -> iniciarNovoUsuario());
        btnEditar.addActionListener(e -> iniciarEdicao());
        btnExcluir.addActionListener(e -> controller.excluirUsuario());
        btnSalvar.addActionListener(e -> salvarUsuario());
        btnCancelar.addActionListener(e -> cancelarEdicao());
        
        // Pesquisa
        btnPesquisar.addActionListener(e -> pesquisar());
        btnLimparPesquisa.addActionListener(e -> limparPesquisa());
        
        txtPesquisa.addActionListener(e -> pesquisar());
    }

    /**
     * Configura as propriedades da janela
     */
    private void setupFrame() {
        setSize(900, 600);
        setResizable(true);
        setMaximizable(true);
        setIconifiable(true);
        setClosable(true);
    }

    /**
     * Configura o estado inicial dos componentes
     */
    private void configurarEstadoInicial() {
        editando = false;
        usuarioSelecionado = null;
        
        txtNome.setEditable(false);
        txtEmail.setEditable(false);
        chkAtivo.setEnabled(false);
        
        btnNovo.setEnabled(true);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
    }

    /**
     * Inicia a criação de um novo usuário
     */
    private void iniciarNovoUsuario() {
        editando = true;
        usuarioSelecionado = null;
        
        txtNome.setText("");
        txtEmail.setText("");
        chkAtivo.setSelected(true);
        
        txtNome.setEditable(true);
        txtEmail.setEditable(true);
        chkAtivo.setEnabled(true);
        
        btnNovo.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnSalvar.setEnabled(true);
        btnCancelar.setEnabled(true);
        
        txtNome.requestFocus();
    }

    /**
     * Inicia a edição do usuário selecionado
     */
    private void iniciarEdicao() {
        if (usuarioSelecionado == null) return;
        
        editando = true;
        
        txtNome.setEditable(true);
        txtEmail.setEditable(true);
        chkAtivo.setEnabled(true);
        
        btnNovo.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnSalvar.setEnabled(true);
        btnCancelar.setEnabled(true);
        
        txtNome.requestFocus();
        txtNome.selectAll();
    }

    /**
     * Salva o usuário (novo ou editado)
     */
    private void salvarUsuario() {
        if (!validarFormulario()) {
            return;
        }
        
        String nome = txtNome.getText().trim();
        String email = txtEmail.getText().trim();
        boolean ativo = chkAtivo.isSelected();
        
        if (usuarioSelecionado == null) {
            // Novo usuário
            controller.criarUsuario(nome, email, ativo);
        } else {
            // Edição
            controller.atualizarUsuario(usuarioSelecionado.getId(), nome, email, ativo);
        }
    }

    /**
     * Cancela a edição
     */
    private void cancelarEdicao() {
        editando = false;
        
        if (usuarioSelecionado != null) {
            preencherFormulario(usuarioSelecionado);
            configurarEstadoSelecao();
        } else {
            limparFormulario();
            configurarEstadoInicial();
        }
    }

    /**
     * Realiza a pesquisa
     */
    private void pesquisar() {
        String termo = txtPesquisa.getText().trim();
        controller.pesquisarUsuarios(termo);
    }

    /**
     * Limpa a pesquisa
     */
    private void limparPesquisa() {
        txtPesquisa.setText("");
        controller.carregarUsuarios();
    }

    /**
     * Valida o formulário
     */
    private boolean validarFormulario() {
        String nome = txtNome.getText().trim();
        String email = txtEmail.getText().trim();
        
        if (!ValidationUtil.isNotEmpty(nome)) {
            showError("Nome é obrigatório");
            txtNome.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.hasMinLength(nome, 2)) {
            showError("Nome deve ter pelo menos 2 caracteres");
            txtNome.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidEmail(email)) {
            showError("Email inválido");
            txtEmail.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * Atualiza a tabela com a lista de usuários
     */
    public void atualizarTabela(List<Usuario> usuarios) {
        tableModel.setRowCount(0);
        
        for (Usuario usuario : usuarios) {
            Object[] row = {
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.isAtivo(),
                ValidationUtil.formatDateTime(usuario.getCriadoEm())
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Seleciona um usuário na interface
     */
    public void selecionarUsuario(Usuario usuario) {
        this.usuarioSelecionado = usuario;
        
        if (usuario != null) {
            preencherFormulario(usuario);
            configurarEstadoSelecao();
        } else {
            limparSelecao();
        }
    }

    /**
     * Preenche o formulário com os dados do usuário
     */
    private void preencherFormulario(Usuario usuario) {
        txtNome.setText(usuario.getNome());
        txtEmail.setText(usuario.getEmail());
        chkAtivo.setSelected(usuario.isAtivo());
    }

    /**
     * Limpa o formulário
     */
    private void limparFormulario() {
        txtNome.setText("");
        txtEmail.setText("");
        chkAtivo.setSelected(true);
    }

    /**
     * Limpa a seleção
     */
    private void limparSelecao() {
        usuarioSelecionado = null;
        limparFormulario();
        configurarEstadoInicial();
    }

    /**
     * Configura o estado quando há seleção
     */
    private void configurarEstadoSelecao() {
        if (editando) return;
        
        txtNome.setEditable(false);
        txtEmail.setEditable(false);
        chkAtivo.setEnabled(false);
        
        btnNovo.setEnabled(true);
        btnEditar.setEnabled(true);
        btnExcluir.setEnabled(true);
        btnSalvar.setEnabled(false);
        btnCancelar.setEnabled(false);
    }

    /**
     * Finaliza a edição com sucesso
     */
    public void finalizarEdicao() {
        editando = false;
        configurarEstadoInicial();
        table.clearSelection();
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
        return JOptionPane.showConfirmDialog(
            this, 
            message, 
            "Confirmação", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }
}
