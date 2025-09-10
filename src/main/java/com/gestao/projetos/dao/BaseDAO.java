package com.gestao.projetos.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interface genérica para operações CRUD básicas
 * 
 * @param <T> Tipo da entidade
 * @param <ID> Tipo do identificador
 */
public interface BaseDAO<T, ID> {
    
    /**
     * Salva uma nova entidade
     * 
     * @param entity Entidade a ser salva
     * @return Entidade salva com ID preenchido
     * @throws SQLException em caso de erro na operação
     */
    T save(T entity) throws SQLException;
    
    /**
     * Atualiza uma entidade existente
     * 
     * @param entity Entidade a ser atualizada
     * @return Entidade atualizada
     * @throws SQLException em caso de erro na operação
     */
    T update(T entity) throws SQLException;
    
    /**
     * Remove uma entidade pelo ID
     * 
     * @param id ID da entidade a ser removida
     * @throws SQLException em caso de erro na operação
     */
    void delete(ID id) throws SQLException;
    
    /**
     * Busca uma entidade pelo ID
     * 
     * @param id ID da entidade
     * @return Optional contendo a entidade se encontrada
     * @throws SQLException em caso de erro na operação
     */
    Optional<T> findById(ID id) throws SQLException;
    
    /**
     * Lista todas as entidades
     * 
     * @return Lista de todas as entidades
     * @throws SQLException em caso de erro na operação
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Verifica se uma entidade existe pelo ID
     * 
     * @param id ID da entidade
     * @return true se existe, false caso contrário
     * @throws SQLException em caso de erro na operação
     */
    boolean exists(ID id) throws SQLException;
    
    /**
     * Conta o total de entidades
     * 
     * @return Número total de entidades
     * @throws SQLException em caso de erro na operação
     */
    long count() throws SQLException;
}
