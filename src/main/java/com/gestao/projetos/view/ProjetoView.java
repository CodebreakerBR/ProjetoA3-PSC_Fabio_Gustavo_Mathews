package com.gestao.projetos.view;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Objects;

import java.awt.*;
import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.model.Projeto;
import com.gestao.projetos.service.UsuarioService;
import com.gestao.projetos.service.ProjetoService;
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
        btnAddMembro.addActionListener(e -> {
            SelecionarUsuarioView selecionarUsuario = new SelecionarUsuarioView(nome -> adicionarMembro(nome));
            selecionarUsuario.setVisible(true);
        });


        // Botão Avançar
        JButton btnSalvar = new JButton("Salvar");
btnSalvar.addActionListener(e -> {
    String nomeProjeto = txtNomeProjeto.getText();
    String equipe = txtEquipe.getText();
    String descricao = txtDescricao.getText();
    String dataInicio = txtDataInicio.getText();
    String dataTermino = txtDataTermino.getText();
    String status = (String) comboStatus.getSelectedItem();

    List<String> membros = new ArrayList<>();
    for (Component comp : membrosPanel.getComponents()) {
        if (comp instanceof JTextField) {
            membros.add(((JTextField) comp).getText());
        }
    }

    if (nomeProjeto.isEmpty() || equipe.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Preencha os campos obrigatórios: Nome do Projeto e Equipe.");
        return;
    }


    Projeto projeto = new Projeto();
    projeto.setNome(nomeProjeto);
    projeto.setEquipe(equipe);
    projeto.setDescricao(descricao);
    projeto.setDataInicio(dataInicio);
    projeto.setDataTermino(dataTermino);
    projeto.setStatus(status);
    projeto.setMembros(membros);

    ProjetoService service = new ProjetoService();
    boolean sucesso = service.salvarProjeto(projeto);

    if (sucesso) {
        JOptionPane.showMessageDialog(null, "Projeto salvo com sucesso!");
    } else {
        JOptionPane.showMessageDialog(null, "Erro ao salvar projeto.");
    }
});

        // Adiciona tudo ao painel principal
        painelPrincipal.add(projetoPanel);
        painelPrincipal.add(datasPanel);
        painelPrincipal.add(statusPanel);
        painelPrincipal.add(equipePanel);
        painelPrincipal.add(Box.createVerticalStrut(10));
        painelPrincipal.add(btnSalvar);

        add(painelPrincipal);
    }

    private void adicionarMembro(String nome) {
        List<JTextField> camposMembros = new ArrayList<>();
        for (JTextField campo : camposMembros) {
            if (campo.getText().isEmpty() || campo.getText().startsWith("Membro")) {
                campo.setText(nome);
                return;
            }
        }
        // Se todos preenchidos, adiciona novo campo
        JTextField novoCampo = new JTextField(nome);
        camposMembros.add(novoCampo);
        membrosPanel.add(novoCampo);
        membrosPanel.revalidate();
        membrosPanel.repaint();
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProjetoView tela = new ProjetoView();
            tela.setVisible(true);
        });
    }
}
