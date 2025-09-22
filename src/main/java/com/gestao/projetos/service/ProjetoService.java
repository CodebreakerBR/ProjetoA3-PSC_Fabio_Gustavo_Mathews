
package com.gestao.projetos.service;

import com.gestao.projetos.dao.ProjetoDAO;
import com.gestao.projetos.model.Projeto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProjetoService {

    private static final Logger logger = LoggerFactory.getLogger(ProjetoService.class);
    private final ProjetoDAO projetoDAO;

    public ProjetoService() {
        this.projetoDAO = new ProjetoDAO();
    }

    public ProjetoService(ProjetoDAO projetoDAO) {
        this.projetoDAO = projetoDAO;
    }

    public Projeto salvar(Projeto projeto) throws SQLException {
        if (projeto == null || !projeto.isValid()) {
            throw new IllegalArgumentException("Projeto inválido ou nulo");
        }
        return projetoDAO.save(projeto);
    }

    public Projeto atualizar(Projeto projeto) throws SQLException {
        if (projeto == null || projeto.getId() == null) {
            throw new IllegalArgumentException("Projeto ou ID não pode ser nulo");
        }
        if (!projetoDAO.exists(projeto.getId())) {
            throw new IllegalArgumentException("Projeto não encontrado: " + projeto.getId());
        }
        return projetoDAO.update(projeto);
    }

    public void remover(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }
        if (!projetoDAO.exists(id)) {
            throw new IllegalArgumentException("Projeto não encontrado: " + id);
        }
        projetoDAO.delete(id);
    }

    public Optional<Projeto> buscarPorId(Long id) throws SQLException {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return projetoDAO.findById(id);
    }

    public List<Projeto> listarTodos() throws SQLException {
        return projetoDAO.findAll();
    }
}
