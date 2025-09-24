package com.gestao.projetos.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe que representa as credenciais de autenticação de um usuário
 */
public class Credencial {
    private Long id;
    private String hash;
    private String salt;
    private Long usuarioId;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Construtores
    public Credencial() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public Credencial(String hash, String salt, Long usuarioId) {
        this();
        this.hash = hash;
        this.salt = salt;
        this.usuarioId = usuarioId;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
        this.atualizadoEm = LocalDateTime.now();
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
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
        Credencial that = (Credencial) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, usuarioId);
    }

    @Override
    public String toString() {
        return "Credencial{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                '}';
    }

    // Validações
    public boolean isValid() {
        return hash != null && !hash.trim().isEmpty() &&
               salt != null && !salt.trim().isEmpty() &&
               usuarioId != null && usuarioId > 0;
    }
}