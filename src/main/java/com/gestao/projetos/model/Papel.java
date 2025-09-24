package com.gestao.projetos.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo para representar um papel no sistema
 */
public class Papel {
    
    private Long id;
    private String nome;
    private String descricao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Papel() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public Papel(String nome, String descricao) {
        this();
        this.nome = nome;
        this.descricao = descricao;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
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

    public void setNome(String nome) {
        this.nome = nome;
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
     * Valida se o papel está válido
     */
    public boolean isValid() {
        return nome != null && !nome.trim().isEmpty() &&
               descricao != null && !descricao.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Papel papel = (Papel) o;
        return Objects.equals(id, papel.id) && 
               Objects.equals(nome, papel.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome);
    }

    @Override
    public String toString() {
        return "Papel{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               ", descricao='" + descricao + '\'' +
               ", criadoEm=" + criadoEm +
               ", atualizadoEm=" + atualizadoEm +
               '}';
    }
}