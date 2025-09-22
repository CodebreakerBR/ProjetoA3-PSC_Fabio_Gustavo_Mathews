
package com.gestao.projetos.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.service.UsuarioService;

public class ProjetoView extends JInternalFrame {
    private JPanel membrosPanel;
    private List<JTextField> camposMembros;
    private boolean projetoSalvo = false;

    public ProjetoView() {
        setTitle("Cadastro de Projetos");
        setSize(600, 500);
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);

        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                if (!projetoSalvo) {
                    int resposta = JOptionPane.showConfirmDialog(null, "O projeto não foi salvo. Deseja continuar?", "Aviso", JOptionPane.YES_NO_OPTION);
                    if (resposta == JOptionPane.YES_OPTION) {
                        limparCampos();
                    } else {
                        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                    }
                }
            }
        });

        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel projetoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        projetoPanel.setBorder(BorderFactory.createTitledBorder("Projeto"));
        JTextField txtNomeProjeto = new JTextField();
        JTextField txtEquipe = new JTextField();
        JTextArea txtDescricao = new JTextArea(3, 20);
        projetoPanel.add(new JLabel("Nome do Projeto:")); projetoPanel.add(txtNomeProjeto);
        projetoPanel.add(new JLabel("Equipe Responsável:")); projetoPanel.add(txtEquipe);
        projetoPanel.add(new JLabel("Descrição do projeto:")); projetoPanel.add(new JScrollPane(txtDescricao));

        JPanel datasPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        datasPanel.setBorder(BorderFactory.createTitledBorder("Datas"));
        JTextField txtDataInicio = new JTextField();
        JTextField txtDataTermino = new JTextField();
        datasPanel.add(new JLabel("Data de Início:")); datasPanel.add(txtDataInicio);
        datasPanel.add(new JLabel("Data de Término Prevista:")); datasPanel.add(txtDataTermino);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        JComboBox<String> comboStatus = new JComboBox<>(new String[]{"Planejado", "Em andamento", "Concluído", "Cancelado"});
        statusPanel.add(new JLabel("Status do Projeto:")); statusPanel.add(comboStatus);

        JPanel equipePanel = new JPanel(new BorderLayout());
        equipePanel.setBorder(BorderFactory.createTitledBorder("Equipe"));
        membrosPanel = new JPanel();
        membrosPanel.setLayout(new BoxLayout(membrosPanel, BoxLayout.Y_AXIS));
        camposMembros = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            JTextField campo = new JTextField("Membro " + i);
            camposMembros.add(campo);
            membrosPanel.add(campo);
        }
        JButton btnAddMembro = new JButton("Add Membro");
        btnAddMembro.addActionListener(e -> {
            SelecionarUsuarioView selecionarUsuario = new SelecionarUsuarioView(nome -> adicionarMembro(nome));
            selecionarUsuario.setVisible(true);
        });
        equipePanel.add(new JScrollPane(membrosPanel), BorderLayout.CENTER);
        equipePanel.add(btnAddMembro, BorderLayout.SOUTH);

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> {
            projetoSalvo = true;
            JOptionPane.showMessageDialog(null, "Projeto salvo com sucesso!");
        });

        painelPrincipal.add(projetoPanel);
        painelPrincipal.add(datasPanel);
        painelPrincipal.add(statusPanel);
        painelPrincipal.add(equipePanel);
        painelPrincipal.add(Box.createVerticalStrut(10));
        painelPrincipal.add(btnSalvar);
        add(painelPrincipal);
    }

    private void adicionarMembro(String nome) {
        for (JTextField campo : camposMembros) {
            if (campo.getText().startsWith("Membro")) {
                campo.setText(nome);
                return;
            }
        }
        JTextField novoCampo = new JTextField(nome);
        camposMembros.add(novoCampo);
        membrosPanel.add(novoCampo);
        membrosPanel.revalidate();
        membrosPanel.repaint();
    }

    private void limparCampos() {
        for (JTextField campo : camposMembros) {
            campo.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProjetoView tela = new ProjetoView();
            tela.setVisible(true);
        });
    }
}
