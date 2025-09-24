
package com.gestao.projetos.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.gestao.projetos.model.Usuario;

public class Projeto {

    // Atributos principais
    private Long id;
    private String nome;
    private String descricao;
    private String status;
    private String responsavel;

    // Datas do projeto (alinhadas ao schema do banco)
    private LocalDate dataInicio;           // data_inicio
    private LocalDate dataFimPrevista;      // data_fim_prevista  
    private LocalDate dataFimReal;          // data_fim_real
    
    // Gerenciamento
    private Long gerenteId;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Membros vinculados ao projeto
    private List<Usuario> membros;
    
    // Equipes vinculadas ao projeto
    private List<Equipe> equipes;

    // Construtor vazio
    public Projeto() {
        this.membros = new ArrayList<>();
        this.equipes = new ArrayList<>();
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
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
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<Equipe> getEquipes() {
        return equipes;
    }

    public void setEquipes(List<Equipe> equipes) {
        this.equipes = equipes != null ? equipes : new ArrayList<>();
        this.atualizadoEm = LocalDateTime.now();
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
        this.atualizadoEm = LocalDateTime.now();
    }

    public LocalDate getDataFimPrevista() {
        return dataFimPrevista;
    }

    public void setDataFimPrevista(LocalDate dataFimPrevista) {
        this.dataFimPrevista = dataFimPrevista;
        this.atualizadoEm = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public LocalDate getDataFimReal() {
        return dataFimReal;
    }

    public void setDataFimReal(LocalDate dataFimReal) {
        this.dataFimReal = dataFimReal;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Long getGerenteId() {
        return gerenteId;
    }

    public void setGerenteId(Long gerenteId) {
        this.gerenteId = gerenteId;
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

    public List<Usuario> getMembros() {
        return membros;
    }

    public void setMembros(List<Usuario> membros) {
        this.membros = membros != null ? membros : new ArrayList<>();
        this.atualizadoEm = LocalDateTime.now();
    }

    // Métodos auxiliares para gerenciar equipes
    public void adicionarEquipe(Equipe equipe) {
        if (equipe != null && !this.equipes.contains(equipe)) {
            this.equipes.add(equipe);
            this.atualizadoEm = LocalDateTime.now();
        }
    }

    public void removerEquipe(Equipe equipe) {
        if (equipe != null) {
            this.equipes.remove(equipe);
            this.atualizadoEm = LocalDateTime.now();
        }
    }

    public boolean temEquipe(Equipe equipe) {
        return equipe != null && this.equipes.contains(equipe);
    }

    // Métodos auxiliares para gerenciar membros
    public void adicionarMembro(Usuario membro) {
        if (membro != null && !this.membros.contains(membro)) {
            this.membros.add(membro);
            this.atualizadoEm = LocalDateTime.now();
        }
    }

    public void removerMembro(Usuario membro) {
        if (membro != null) {
            this.membros.remove(membro);
            this.atualizadoEm = LocalDateTime.now();
        }
    }

    public boolean temMembro(Usuario membro) {
        return membro != null && this.membros.contains(membro);
    }

    // Método de validação simples
    public boolean isValid() {
        return nome != null && !nome.isEmpty()
            && status != null && !status.isEmpty();
    }

    // Método para verificar se o projeto está atrasado
    public boolean isAtrasado() {
        if (dataFimPrevista == null) {
            return false;
        }
        
        LocalDate hoje = LocalDate.now();
        return hoje.isAfter(dataFimPrevista) && 
               !"CONCLUIDO".equals(status) && 
               !"CANCELADO".equals(status);
    }

    // Método para calcular o progresso (pode ser implementado futuramente)
    public double calcularProgresso() {
        // Por enquanto retorna 0, mas pode ser implementado com base nas tarefas
        return 0.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Projeto projeto = (Projeto) obj;
        return id != null && id.equals(projeto.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Projeto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", status='" + status + '\'' +
                ", equipes=" + (equipes != null ? equipes.size() : 0) +
                ", membros=" + (membros != null ? membros.size() : 0) +
                '}';
    }
}
