
package com.gestao.projetos.service;

import com.gestao.projetos.dao.ProjetoDAO;
import com.gestao.projetos.model.Projeto;
import com.gestao.projetos.model.Equipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para operações relacionadas a projetos
 */
public class ProjetoService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjetoService.class);
    private final ProjetoDAO projetoDAO;

    public ProjetoService() {
        this.projetoDAO = new ProjetoDAO();
    }

    /**
     * Cria um novo projeto
     */
    public Projeto criar(Projeto projeto) throws SQLException {
        logger.info("Criando novo projeto: {}", projeto.getNome());
        return projetoDAO.save(projeto);
    }

    /**
     * Atualiza um projeto existente
     */
    public Projeto atualizar(Projeto projeto) throws SQLException {
        logger.info("Atualizando projeto ID: {}", projeto.getId());
        return projetoDAO.update(projeto);
    }

    /**
     * Busca um projeto por ID
     */
    public Optional<Projeto> buscarPorId(Long id) throws SQLException {
        logger.debug("Buscando projeto por ID: {}", id);
        return projetoDAO.findById(id);
    }

    /**
     * Lista todos os projetos
     */
    public List<Projeto> listarTodos() throws SQLException {
        logger.debug("Listando todos os projetos");
        return projetoDAO.findAll();
    }

    /**
     * Pesquisa projetos por termo
     */
    public List<Projeto> pesquisar(String termo) throws SQLException {
        logger.debug("Pesquisando projetos com termo: {}", termo);
        // Implementar pesquisa no DAO
        return projetoDAO.findAll(); // Por enquanto retorna todos
    }

    /**
     * Lista projetos por status
     */
    public List<Projeto> listarPorStatus(String status) throws SQLException {
        logger.debug("Listando projetos por status: {}", status);
        return projetoDAO.findByStatus(status);
    }

    /**
     * Lista projetos por gerente
     */
    public List<Projeto> listarPorGerente(Long gerenteId) throws SQLException {
        logger.debug("Listando projetos por gerente ID: {}", gerenteId);
        return projetoDAO.findByGerente(gerenteId);
    }

    /**
     * Lista projetos atrasados
     */
    public List<Projeto> listarAtrasados() throws SQLException {
        logger.debug("Listando projetos atrasados");
        return projetoDAO.findAtrasados();
    }

    /**
     * Exclui um projeto
     */
    public void excluir(Long id) throws SQLException {
        logger.info("Excluindo projeto ID: {}", id);
        projetoDAO.delete(id);
    }

    /**
     * Atribui uma equipe a um projeto
     */
    public void atribuirEquipe(Long projetoId, Long equipeId, String papelEquipe) throws SQLException {
        logger.info("Atribuindo equipe ID: {} ao projeto ID: {} com papel: {}", 
                   equipeId, projetoId, papelEquipe);
        projetoDAO.atribuirEquipe(projetoId, equipeId, papelEquipe);
    }

    /**
     * Remove uma equipe de um projeto
     */
    public void removerEquipe(Long projetoId, Long equipeId) throws SQLException {
        logger.info("Removendo equipe ID: {} do projeto ID: {}", equipeId, projetoId);
        projetoDAO.removerEquipe(projetoId, equipeId);
    }

    /**
     * Lista equipes atribuídas a um projeto
     */
    public List<Equipe> listarEquipesProjeto(Long projetoId) throws SQLException {
        logger.debug("Listando equipes do projeto ID: {}", projetoId);
        return projetoDAO.findEquipesByProjeto(projetoId);
    }

    /**
     * Verifica se um projeto existe
     */
    public boolean existe(Long id) throws SQLException {
        return projetoDAO.exists(id);
    }

    /**
     * Conta o total de projetos
     */
    public long contarTodos() throws SQLException {
        return projetoDAO.count();
    }

    /**
     * Salva um projeto (método de compatibilidade)
     */
    public boolean salvarProjeto(Projeto projeto) {
        try {
            if (projeto.getId() == null) {
                criar(projeto);
            } else {
                atualizar(projeto);
            }
            return true;
        } catch (SQLException e) {
            logger.error("Erro ao salvar projeto", e);
            return false;
        }
    }
}
