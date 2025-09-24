package com.gestao.projetos.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo para representar uma permissão no sistema
 */
public class Permissao {
    
    private Long id;
    private String chave;
    private String descricao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Permissao() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public Permissao(String chave, String descricao) {
        this();
        this.chave = chave;
        this.descricao = descricao;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getChave() {
        return chave;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    /**
     * Valida se a permissão está válida
     */
    public boolean isValid() {
        return chave != null && !chave.trim().isEmpty() &&
               descricao != null && !descricao.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permissao permissao = (Permissao) o;
        return Objects.equals(id, permissao.id) && 
               Objects.equals(chave, permissao.chave);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chave);
    }

    @Override
    public String toString() {
        return "Permissao{" +
               "id=" + id +
               ", chave='" + chave + '\'' +
               ", descricao='" + descricao + '\'' +
               ", criadoEm=" + criadoEm +
               ", atualizadoEm=" + atualizadoEm +
               '}';
    }
}