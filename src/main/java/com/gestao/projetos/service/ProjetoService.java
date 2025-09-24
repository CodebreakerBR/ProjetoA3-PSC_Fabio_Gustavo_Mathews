
package com.gestao.projetos.service;

import com.gestao.projetos.model.Projeto;
import java.sql.*;
import java.util.List;

public class ProjetoService {

    public boolean salvarProjeto(Projeto projeto) {
        String sqlProjeto = "INSERT INTO projetos (nome, equipe, descricao, data_inicio, data_termino, status) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlMembro = "INSERT INTO membros_projeto (projeto_id, nome_membro) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/seu_banco", "usuario", "senha");
             PreparedStatement stmtProjeto = conn.prepareStatement(sqlProjeto, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtMembro = conn.prepareStatement(sqlMembro)) {

            stmtProjeto.setString(1, projeto.getNome());
            stmtProjeto.setString(2, projeto.getEquipe());
            stmtProjeto.setString(3, projeto.getDescricao());
            
            // Converter LocalDate para String ou definir como NULL
            if (projeto.getDataInicio() != null) {
                stmtProjeto.setString(4, projeto.getDataInicio().toString());
            } else {
                stmtProjeto.setString(4, null);
            }
            
            if (projeto.getDataTerminoPrevista() != null) {
                stmtProjeto.setString(5, projeto.getDataTerminoPrevista().toString());
            } else {
                stmtProjeto.setString(5, null);
            }
            
            stmtProjeto.setString(6, projeto.getStatus());

            int rows = stmtProjeto.executeUpdate();
            if (rows == 0) return false;

            ResultSet generatedKeys = stmtProjeto.getGeneratedKeys();
            if (generatedKeys.next()) {
                int projetoId = generatedKeys.getInt(1);
                
                // TODO: Implementar salvamento de membros quando List<Usuario> estiver disponível
                /*
                List<Usuario> membros = projeto.getMembros();
                if (membros != null) {
                    for (Usuario membro : membros) {
                        stmtMembro.setInt(1, projetoId);
                        stmtMembro.setString(2, membro.getNome()); // ou membro.getId().toString()
                        stmtMembro.executeUpdate();
                    }
                }
                */
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
