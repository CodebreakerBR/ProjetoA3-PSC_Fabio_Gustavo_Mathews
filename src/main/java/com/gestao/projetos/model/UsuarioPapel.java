package com.gestao.projetos.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo para representar a associação entre usuário e papel
 */
public class UsuarioPapel {
    
    private Long id;
    private Long usuarioId;
    private Long papelId;
    private LocalDateTime atribuidoEm;
    private LocalDateTime expiraEm;
    private boolean ativo;

    public UsuarioPapel() {
        this.atribuidoEm = LocalDateTime.now();
        this.ativo = true;
    }

    public UsuarioPapel(Long usuarioId, Long papelId) {
        this();
        this.usuarioId = usuarioId;
        this.papelId = papelId;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getPapelId() {
        return papelId;
    }

    public LocalDateTime getAtribuidoEm() {
        return atribuidoEm;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public boolean isAtivo() {
        return ativo;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setPapelId(Long papelId) {
        this.papelId = papelId;
    }

    public void setAtribuidoEm(LocalDateTime atribuidoEm) {
        this.atribuidoEm = atribuidoEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /**
     * Valida se a associação usuário-papel está válida
     */
    public boolean isValid() {
        return usuarioId != null && usuarioId > 0 &&
               papelId != null && papelId > 0;
    }

    /**
     * Verifica se a associação está expirada
     */
    public boolean isExpirada() {
        return expiraEm != null && LocalDateTime.now().isAfter(expiraEm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioPapel that = (UsuarioPapel) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(usuarioId, that.usuarioId) && 
               Objects.equals(papelId, that.papelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, usuarioId, papelId);
    }

    @Override
    public String toString() {
        return "UsuarioPapel{" +
               "id=" + id +
               ", usuarioId=" + usuarioId +
               ", papelId=" + papelId +
               ", atribuidoEm=" + atribuidoEm +
               ", expiraEm=" + expiraEm +
               ", ativo=" + ativo +
               '}';
    }
}