package com.gestao.projetos.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe que representa um projeto no sistema
 */
public class Projeto {
    private Long id;
    private String nome;
    private String descricao;
    private LocalDate dataInicioPrevista;
    private LocalDate dataFimPrevista;
    private LocalDate dataInicioReal;
    private LocalDate dataFimReal;
    private StatusProjeto status;
    private Usuario gerente;
    private Long gerenteId;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Construtores
    public Projeto() {
        this.status = StatusProjeto.PLANEJADO;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public Projeto(String nome, String descricao, Usuario gerente) {
        this();
        this.nome = nome;
        this.descricao = descricao;
        this.gerente = gerente;
        this.gerenteId = gerente != null ? gerente.getId() : null;
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

    public LocalDate getDataInicioPrevista() {
        return dataInicioPrevista;
    }

    public void setDataInicioPrevista(LocalDate dataInicioPrevista) {
        this.dataInicioPrevista = dataInicioPrevista;
        this.atualizadoEm = LocalDateTime.now();
    }

    public LocalDate getDataFimPrevista() {
        return dataFimPrevista;
    }

    public void setDataFimPrevista(LocalDate dataFimPrevista) {
        this.dataFimPrevista = dataFimPrevista;
        this.atualizadoEm = LocalDateTime.now();
    }

    public LocalDate getDataInicioReal() {
        return dataInicioReal;
    }

    public void setDataInicioReal(LocalDate dataInicioReal) {
        this.dataInicioReal = dataInicioReal;
        this.atualizadoEm = LocalDateTime.now();
    }

    public LocalDate getDataFimReal() {
        return dataFimReal;
    }

    public void setDataFimReal(LocalDate dataFimReal) {
        this.dataFimReal = dataFimReal;
        this.atualizadoEm = LocalDateTime.now();
    }

    public StatusProjeto getStatus() {
        return status;
    }

    public void setStatus(StatusProjeto status) {
        this.status = status;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Usuario getGerente() {
        return gerente;
    }

    public void setGerente(Usuario gerente) {
        this.gerente = gerente;
        this.gerenteId = gerente != null ? gerente.getId() : null;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Long getGerenteId() {
        return gerenteId;
    }

    public void setGerenteId(Long gerenteId) {
        this.gerenteId = gerenteId;
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

    // Métodos auxiliares
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Projeto projeto = (Projeto) o;
        return Objects.equals(id, projeto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nome + " (" + status.getDescricao() + ")";
    }

    // Validações
    public boolean isValid() {
        return nome != null && !nome.trim().isEmpty() &&
               gerenteId != null &&
               (dataFimPrevista == null || dataInicioPrevista == null || 
                !dataFimPrevista.isBefore(dataInicioPrevista));
    }

    public boolean isAtrasado() {
        if (dataFimPrevista == null || status == StatusProjeto.CONCLUIDO || 
            status == StatusProjeto.CANCELADO) {
            return false;
        }
        return LocalDate.now().isAfter(dataFimPrevista);
    }

    public boolean isAtivo() {
        return status != StatusProjeto.CANCELADO && status != StatusProjeto.CONCLUIDO;
    }
}
