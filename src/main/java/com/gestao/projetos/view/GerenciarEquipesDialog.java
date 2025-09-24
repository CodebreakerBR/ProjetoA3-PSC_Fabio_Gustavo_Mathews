package com.gestao.projetos.view;

import com.gestao.projetos.controller.ProjetoController;
import com.gestao.projetos.model.Projeto;
import com.gestao.projetos.model.Equipe;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Diálogo para gerenciar equipes atribuídas a um projeto
 */
public class GerenciarEquipesDialog extends JDialog {
    
    private final ProjetoController controller;
    private final Projeto projeto;
    
    // Componentes da interface
    private JTable tableEquipesAtribuidas;
    private DefaultTableModel modelEquipesAtribuidas;
    
    private JList<Equipe> listEquipesDisponiveis;
    private DefaultListModel<Equipe> modelEquipesDisponiveis;
    
    private JTextField txtPapelEquipe;
    private JButton btnAtribuir;
    private JButton btnRemover;
    private JButton btnFechar;
    
    // Papéis predefinidos para equipes
    private final String[] papeisPredefinidos = {
        "EQUIPE_PRINCIPAL", 
        "EQUIPE_APOIO", 
        "EQUIPE_CONSULTORIA", 
        "EQUIPE_TESTE",
        "EQUIPE_DESENVOLVIMENTO",
        "EQUIPE_INFRAESTRUTURA"
    };

    public GerenciarEquipesDialog(JFrame parent, ProjetoController controller, Projeto projeto) {
        super(parent, "Gerenciar Equipes - " + projeto.getNome(), true);
        this.controller = controller;
        this.projeto = projeto;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDialog();
        carregarDados();
    }

    /**
     * Inicializa os componentes da interface
     */
    private void initializeComponents() {
        // Tabela de equipes atribuídas
        String[] colunas = {"ID", "Nome", "Descrição", "Papel no Projeto", "Membros"};
        modelEquipesAtribuidas = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableEquipesAtribuidas = new JTable(modelEquipesAtribuidas);
        tableEquipesAtribuidas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Lista de equipes disponíveis
        modelEquipesDisponiveis = new DefaultListModel<>();
        listEquipesDisponiveis = new JList<>(modelEquipesDisponiveis);
        listEquipesDisponiveis.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listEquipesDisponiveis.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Equipe) {
                    Equipe equipe = (Equipe) value;
                    setText(equipe.getNome() + " (" + equipe.getMembros().size() + " membros)");
                }
                return this;
            }
        });
        
        // Papel da equipe com combo
        txtPapelEquipe = new JTextField(20);
        
        // Botões
        btnAtribuir = new JButton("Atribuir Equipe");
        btnRemover = new JButton("Remover Equipe");
        btnFechar = new JButton("Fechar");
    }

    /**
     * Configura o layout da janela
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel central com split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Lado esquerdo - Equipes atribuídas
        JPanel painelAtribuidas = new JPanel(new BorderLayout());
        painelAtribuidas.setBorder(BorderFactory.createTitledBorder("Equipes Atribuídas ao Projeto"));
        painelAtribuidas.add(new JScrollPane(tableEquipesAtribuidas), BorderLayout.CENTER);
        
        JPanel painelBotoesAtribuidas = new JPanel(new FlowLayout());
        painelBotoesAtribuidas.add(btnRemover);
        painelAtribuidas.add(painelBotoesAtribuidas, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(painelAtribuidas);
        
        // Lado direito - Equipes disponíveis
        JPanel painelDisponiveis = new JPanel(new BorderLayout());
        painelDisponiveis.setBorder(BorderFactory.createTitledBorder("Equipes Disponíveis"));
        painelDisponiveis.add(new JScrollPane(listEquipesDisponiveis), BorderLayout.CENTER);
        
        // Painel para atribuição
        JPanel painelAtribuicao = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        painelAtribuicao.add(new JLabel("Papel da Equipe:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPanel painelCombo = new JPanel(new BorderLayout());
        
        // Criar combo com papéis predefinidos
        JComboBox<String> comboPapeis = new JComboBox<>(papeisPredefinidos);
        comboPapeis.setEditable(true);
        comboPapeis.addActionListener(e -> {
            Object selected = comboPapeis.getSelectedItem();
            if (selected != null) {
                txtPapelEquipe.setText(selected.toString());
            }
        });
        
        painelCombo.add(comboPapeis, BorderLayout.NORTH);
        painelCombo.add(txtPapelEquipe, BorderLayout.CENTER);
        painelAtribuicao.add(painelCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        painelAtribuicao.add(btnAtribuir, gbc);
        
        painelDisponiveis.add(painelAtribuicao, BorderLayout.SOUTH);
        splitPane.setRightComponent(painelDisponiveis);
        
        splitPane.setDividerLocation(400);
        add(splitPane, BorderLayout.CENTER);
        
        // Painel inferior com botão fechar
        JPanel painelInferior = new JPanel(new FlowLayout());
        painelInferior.add(btnFechar);
        add(painelInferior, BorderLayout.SOUTH);
    }

    /**
     * Configura os manipuladores de eventos
     */
    private void setupEventHandlers() {
        btnAtribuir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atribuirEquipe();
            }
        });
        
        btnRemover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removerEquipe();
            }
        });
        
        btnFechar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Duplo clique na lista para atribuir
        listEquipesDisponiveis.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    atribuirEquipe();
                }
            }
        });
        
        // Duplo clique na tabela para remover
        tableEquipesAtribuidas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    removerEquipe();
                }
            }
        });
    }

    /**
     * Configura as propriedades do diálogo
     */
    private void setupDialog() {
        setSize(800, 500);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Carrega os dados iniciais
     */
    private void carregarDados() {
        carregarEquipesAtribuidas();
        carregarEquipesDisponiveis();
    }

    /**
     * Carrega as equipes já atribuídas ao projeto
     */
    private void carregarEquipesAtribuidas() {
        modelEquipesAtribuidas.setRowCount(0);
        
        List<Equipe> equipesAtribuidas = controller.listarEquipesProjeto(projeto.getId());
        
        for (Equipe equipe : equipesAtribuidas) {
            Object[] row = {
                equipe.getId(),
                equipe.getNome(),
                equipe.getDescricao(),
                "EQUIPE_PRINCIPAL", // Por enquanto papel fixo - pode ser melhorado
                equipe.getMembros().size()
            };
            modelEquipesAtribuidas.addRow(row);
        }
    }

    /**
     * Carrega todas as equipes disponíveis (exceto as já atribuídas)
     */
    private void carregarEquipesDisponiveis() {
        modelEquipesDisponiveis.clear();
        
        List<Equipe> todasEquipes = controller.listarTodasEquipes();
        List<Equipe> equipesAtribuidas = controller.listarEquipesProjeto(projeto.getId());
        
        // Adiciona apenas equipes não atribuídas
        for (Equipe equipe : todasEquipes) {
            boolean jaAtribuida = equipesAtribuidas.stream()
                .anyMatch(e -> e.getId().equals(equipe.getId()));
            
            if (!jaAtribuida && equipe.isAtiva()) {
                modelEquipesDisponiveis.addElement(equipe);
            }
        }
    }

    /**
     * Atribui a equipe selecionada ao projeto
     */
    private void atribuirEquipe() {
        Equipe equipeSelecionada = listEquipesDisponiveis.getSelectedValue();
        if (equipeSelecionada == null) {
            JOptionPane.showMessageDialog(this, 
                "Selecione uma equipe para atribuir", 
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String papel = txtPapelEquipe.getText().trim();
        if (papel.isEmpty()) {
            papel = "EQUIPE_PRINCIPAL"; // Papel padrão
        }
        
        try {
            controller.atribuirEquipe(projeto.getId(), equipeSelecionada.getId(), papel);
            
            // Atualiza as listas
            carregarEquipesAtribuidas();
            carregarEquipesDisponiveis();
            
            // Limpa o campo de papel
            txtPapelEquipe.setText("");
            
            JOptionPane.showMessageDialog(this, 
                "Equipe '" + equipeSelecionada.getNome() + "' atribuída com sucesso!", 
                "Sucesso", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao atribuir equipe: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Remove a equipe selecionada do projeto
     */
    private void removerEquipe() {
        int selectedRow = tableEquipesAtribuidas.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Selecione uma equipe para remover", 
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long equipeId = (Long) modelEquipesAtribuidas.getValueAt(selectedRow, 0);
        String nomeEquipe = (String) modelEquipesAtribuidas.getValueAt(selectedRow, 1);
        
        int confirmacao = JOptionPane.showConfirmDialog(this,
            "Tem certeza que deseja remover a equipe '" + nomeEquipe + "' do projeto?",
            "Confirmar Remoção",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                controller.removerEquipe(projeto.getId(), equipeId);
                
                // Atualiza as listas
                carregarEquipesAtribuidas();
                carregarEquipesDisponiveis();
                
                JOptionPane.showMessageDialog(this, 
                    "Equipe '" + nomeEquipe + "' removida com sucesso!", 
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao remover equipe: " + e.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}