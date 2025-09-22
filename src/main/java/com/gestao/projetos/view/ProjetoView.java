package com.gestao.projetos.view;

import javax.swing.*;
import java.awt.*;
import com.gestao.projetos.view.ProjetoView;

public class ProjetoView extends JInternalFrame {

    private JPanel membrosPanel;

    public ProjetoView() {
        setTitle("Cadastro de Projetos");
        setSize(600, 500);
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);


// Painel principal com BoxLayout
        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

// Seção: Projeto
        JPanel projetoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        projetoPanel.setBorder(BorderFactory.createTitledBorder("Projeto"));

        projetoPanel.add(new JLabel("Nome do Projeto:"));
        JTextField txtNomeProjeto = new JTextField();
        projetoPanel.add(txtNomeProjeto);

        projetoPanel.add(new JLabel("Equipe Responsável:"));
        JTextField txtEquipe = new JTextField();
        projetoPanel.add(txtEquipe);

        projetoPanel.add(new JLabel("Descrição do projeto:"));
        JTextArea txtDescricao = new JTextArea(3, 20);
        projetoPanel.add(new JScrollPane(txtDescricao));


// Seção: Datas
        JPanel datasPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        datasPanel.setBorder(BorderFactory.createTitledBorder("Datas"));

        datasPanel.add(new JLabel("Data de Início:"));
        JTextField txtDataInicio = new JTextField();
        datasPanel.add(txtDataInicio);

        datasPanel.add(new JLabel("Data de Término Prevista:"));
        JTextField txtDataTermino = new JTextField();
        datasPanel.add(txtDataTermino);

// Seção: Status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));

        statusPanel.add(new JLabel("Status do Projeto:"));
        String[] statusOpcoes = {"Planejado", "Em andamento", "Concluído", "Cancelado"};
        JComboBox<String> comboStatus = new JComboBox<>(statusOpcoes);
        statusPanel.add(comboStatus);

        // Seção: Membros da Equipe
        JPanel equipePanel = new JPanel();
        equipePanel.setLayout(new BorderLayout());
        equipePanel.setBorder(BorderFactory.createTitledBorder("Equipe"));

        membrosPanel = new JPanel();
        membrosPanel.setLayout(new BoxLayout(membrosPanel, BoxLayout.Y_AXIS));

        // Adiciona campos iniciais de membros
        for (int i = 1; i <= 5; i++) {
            membrosPanel.add(new JTextField("Membro " + i));
        }

        JButton btnAddMembro = new JButton("Add Membro");
        btnAddMembro.addActionListener(e -> membrosPanel.add(new JTextField("Add Campo Membro")));

        equipePanel.add(new JScrollPane(membrosPanel), BorderLayout.CENTER);
        equipePanel.add(btnAddMembro, BorderLayout.SOUTH);

        // Botão Avançar
        JButton btnAvancar = new JButton("Avançar");

        // Adiciona tudo ao painel principal
        painelPrincipal.add(projetoPanel);
        painelPrincipal.add(datasPanel);
        painelPrincipal.add(statusPanel);
        painelPrincipal.add(equipePanel);
        painelPrincipal.add(Box.createVerticalStrut(10));
        painelPrincipal.add(btnAvancar);

        add(painelPrincipal);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProjetoView tela = new ProjetoView();
            tela.setVisible(true);
        });
    }
}
