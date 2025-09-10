package com.gestao.projetos.model;

/**
 * Enum que representa os diferentes tipos de status de uma tarefa
 */
public enum StatusTarefa {
    NOVA("NOVA", "Tarefa criada, aguardando início"),
    EM_ANDAMENTO("EM_ANDAMENTO", "Tarefa sendo executada"),
    BLOQUEADA("BLOQUEADA", "Tarefa impedida por dependências"),
    CONCLUIDA("CONCLUIDA", "Tarefa finalizada"),
    CANCELADA("CANCELADA", "Tarefa cancelada");

    private final String codigo;
    private final String descricao;

    StatusTarefa(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public static StatusTarefa fromCodigo(String codigo) {
        for (StatusTarefa status : values()) {
            if (status.codigo.equals(codigo)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status de tarefa inválido: " + codigo);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
