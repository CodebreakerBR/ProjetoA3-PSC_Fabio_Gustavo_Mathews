
package com.gestao.projetos.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;


public class EquipeView extends JInternalFrame {

    public EquipeView() {
        setTitle("Gestão de Equipes");
        setSize(800, 600);
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);

        JTable tabelaEquipes = new JTable();
        JScrollPane scrollPane = new JScrollPane(tabelaEquipes);
        add(scrollPane, BorderLayout.CENTER);

        // Simulação de dados
        Map<String, List<String>> projetoEquipes = new HashMap<>();
        projetoEquipes.put("Projeto A", Arrays.asList("Ana", "Carlos", "João"));
        projetoEquipes.put("Projeto B", Arrays.asList("Carlos", "Maria"));
        projetoEquipes.put("Projeto C", Arrays.asList("Ana", "Pedro"));

        // Contagem de participação por membro
        Map<String, Integer> membroContagem = new HashMap<>();
        for (List<String> membros : projetoEquipes.values()) {
            for (String membro : membros) {
                membroContagem.put(membro, membroContagem.getOrDefault(membro, 0) + 1);
            }
        }

        // Modelo da tabela
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Projeto");
        model.addColumn("Membro");
        model.addColumn("Participações");

        for (Map.Entry<String, List<String>> entry : projetoEquipes.entrySet()) {
            String projeto = entry.getKey();
            for (String membro : entry.getValue()) {
                int participacoes = membroContagem.get(membro);
                model.addRow(new Object[] {
                    projeto,
                    membro,
                    participacoes > 1 ? membro + " (em múltiplos projetos)" : membro
                });
            }
        }

        tabelaEquipes.setModel(model);
    }
}
