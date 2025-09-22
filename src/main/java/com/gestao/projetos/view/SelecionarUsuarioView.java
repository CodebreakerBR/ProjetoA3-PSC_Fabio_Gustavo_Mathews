
package com.gestao.projetos.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.service.UsuarioService;

public class SelecionarUsuarioView extends JFrame {
    private JTextField txtBusca;
    private JList<String> listaUsuarios;
    private DefaultListModel<String> modeloLista;
    private List<Usuario> usuarios;


    public SelecionarUsuarioView(Consumer<String> callback) throws SQLException {
        setTitle("Selecionar Usuário");
        setSize(400, 300);
        setLocationRelativeTo(null);

        txtBusca = new JTextField();
        modeloLista = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloLista);


        usuarios = new ArrayList<>();
        try {
            usuarios = new UsuarioService().listarTodos();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao carregar usuários: " + ex.getMessage());
        }


        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { atualizarLista(txtBusca.getText()); }
            public void removeUpdate(DocumentEvent e) { atualizarLista(txtBusca.getText()); }
            public void changedUpdate(DocumentEvent e) { atualizarLista(txtBusca.getText()); }
        });

        JButton btnAdicionar = new JButton("Adicionar");
        btnAdicionar.addActionListener(e -> {
            String selecionado = listaUsuarios.getSelectedValue();
            if (selecionado != null) {
                callback.accept(selecionado);
                dispose();
            }
        });

        setLayout(new BorderLayout());
        add(txtBusca, BorderLayout.NORTH);
        add(new JScrollPane(listaUsuarios), BorderLayout.CENTER);
        add(btnAdicionar, BorderLayout.SOUTH);
    }

    private void atualizarLista(String filtro) {
        modeloLista.clear();
        for (Usuario u : usuarios) {
            if (u.getNome().toLowerCase().contains(filtro.toLowerCase())) {
                modeloLista.addElement(u.getNome());
            }
        }
    }
}
