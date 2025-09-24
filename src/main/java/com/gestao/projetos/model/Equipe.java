package com.gestao.projetos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classe que representa uma equipe no sistema
 */
public class Equipe {
    private Long id;
    private String nome;
    private String descricao;
    private boolean ativa;
    private List<Usuario> membros;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Construtores
    public Equipe() {
        this.ativa = true;
        this.membros = new ArrayList<>();
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public Equipe(String nome, String descricao) {
        this();
        this.nome = nome;
        this.descricao = descricao;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
        this.atualizadoEm = LocalDateTime.now();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
        this.atualizadoEm = LocalDateTime.now();
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
        this.atualizadoEm = LocalDateTime.now();
    }

    public List<Usuario> getMembros() {
        return new ArrayList<>(membros);
    }

    public void setMembros(List<Usuario> membros) {
        this.membros = membros != null ? new ArrayList<>(membros) : new ArrayList<>();
        this.atualizadoEm = LocalDateTime.now();
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    // Métodos para gerenciar membros
    public void adicionarMembro(Usuario usuario) {
        if (usuario != null && !membros.contains(usuario)) {
            membros.add(usuario);
            this.atualizadoEm = LocalDateTime.now();
        }
    }

    public void removerMembro(Usuario usuario) {
        if (membros.remove(usuario)) {
            this.atualizadoEm = LocalDateTime.now();
        }
    }

    public boolean contemMembro(Usuario usuario) {
        return membros.contains(usuario);
    }

    public int getQuantidadeMembros() {
        return membros.size();
    }

    // Métodos auxiliares
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipe equipe = (Equipe) o;
        return Objects.equals(id, equipe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nome + " (" + membros.size() + " membros)";
    }

    // Validações
    public boolean isValid() {
        return nome != null && !nome.trim().isEmpty();
    }
}
