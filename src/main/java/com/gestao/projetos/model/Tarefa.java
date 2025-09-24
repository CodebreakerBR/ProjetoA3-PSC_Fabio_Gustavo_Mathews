package com.gestao.projetos.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Tarefa {
    private Long id;
    private String titulo;
    private String descricao;
    private StatusTarefa status;
    private int prioridade; // 1-5 (1=Baixa, 5=Crítica)
    private Double estimativaHoras;
    private Double horasTrabalhadas;
    private LocalDate dataInicioPrevista;
    private LocalDate dataFimPrevista;
    private LocalDate dataInicioReal;
    private LocalDate dataFimReal;
    private Projeto projeto;
    private Long projetoId;
    private Usuario responsavel;
    private Long responsavelId;
    private Equipe equipe;
    private Long equipeId;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Tarefa() {
        this.status = StatusTarefa.NOVA;
        this.prioridade = 3; // Prioridade média
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public Tarefa(String titulo, String descricao, Projeto projeto) {
        this();
        this.titulo = titulo;
        this.descricao = descricao;
        this.projeto = projeto;
        this.projetoId = projeto != null ? projeto.getId() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
        this.atualizadoEm = LocalDateTime.now();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
        this.atualizadoEm = LocalDateTime.now();
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public void setStatus(StatusTarefa status) {
        this.status = status;
        this.atualizadoEm = LocalDateTime.now();
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        if (prioridade < 1 || prioridade > 5) {
            throw new IllegalArgumentException("Prioridade deve estar entre 1 e 5");
        }
        this.prioridade = prioridade;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Double getEstimativaHoras() {
        return estimativaHoras;
    }

    public void setEstimativaHoras(Double estimativaHoras) {
        this.estimativaHoras = estimativaHoras;
        this.atualizadoEm = LocalDateTime.now(); // <-- ADICIONADO
    }

    public Double getHorasTrabalhadas() {
        return horasTrabalhadas;
    }

    public void setHorasTrabalhadas(Double horasTrabalhadas) {
        this.horasTrabalhadas = horasTrabalhadas;
        this.atualizadoEm = LocalDateTime.now(); // <-- ADICIONADO
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

    public Projeto getProjeto() {
        return projeto;
    }

    public void setProjeto(Projeto projeto) {
        this.projeto = projeto;
        this.projetoId = projeto != null ? projeto.getId() : null;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Long getProjetoId() {
        return projetoId;
    }

    public void setProjetoId(Long projetoId) {
        this.projetoId = projetoId;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Usuario responsavel) {
        this.responsavel = responsavel;
        this.responsavelId = responsavel != null ? responsavel.getId() : null;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Long getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(Long responsavelId) {
        this.responsavelId = responsavelId;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
        this.equipeId = equipe != null ? equipe.getId() : null;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Long getEquipeId() {
        return equipeId;
    }

    public void setEquipeId(Long equipeId) {
        this.equipeId = equipeId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarefa tarefa = (Tarefa) o;
        return Objects.equals(id, tarefa.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return titulo + " (" + status.getDescricao() + ")";
    }

    public boolean isValid() {
        return titulo != null && !titulo.trim().isEmpty() &&
                projetoId != null &&
                prioridade >= 1 && prioridade <= 5 &&
                (dataFimPrevista == null || dataInicioPrevista == null ||
                        !dataFimPrevista.isBefore(dataInicioPrevista));
    }

    public boolean isAtrasada() {
        if (dataFimPrevista == null || status == StatusTarefa.CONCLUIDA ||
                status == StatusTarefa.CANCELADA) {
            return false;
        }
        return LocalDate.now().isAfter(dataFimPrevista);
    }

    public boolean isConcluida() {
        return status == StatusTarefa.CONCLUIDA;
    }

    public boolean isAtiva() {
        return status != StatusTarefa.CANCELADA && status != StatusTarefa.CONCLUIDA;
    }

    public String getPrioridadeTexto() {
        switch (prioridade) {
            case 1: return "Muito Baixa";
            case 2: return "Baixa";
            case 3: return "Média";
            case 4: return "Alta";
            case 5: return "Crítica";
            default: return "Indefinida";
        }
    }
}
