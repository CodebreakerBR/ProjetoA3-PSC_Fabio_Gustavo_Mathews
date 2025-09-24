package com.gestao.projetos.model;

/**
 * Enum que representa os diferentes tipos de status de um projeto
 */

    public enum StatusProjeto {
        PLANEJADO("PLANEJADO", "Projeto em fase de planejamento"),
        EM_ANDAMENTO("EM_ANDAMENTO", "Projeto em execução"),
        PAUSADO("PAUSADO", "Projeto temporariamente pausado"),
        CANCELADO("CANCELADO", "Projeto cancelado"),
        CONCLUIDO("CONCLUIDO", "Projeto finalizado com sucesso");

        private final String codigo;
        private final String descricao;

        StatusProjeto(String codigo, String descricao) {
            this.codigo = codigo;
            this.descricao = descricao;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getDescricao() {
            return descricao;
        }

        public static StatusProjeto fromCodigo(String codigo) {
            for (StatusProjeto status : values()) {
                if (status.getCodigo().equals(codigo)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Status de projeto inválido: " + codigo);
        }

        @Override
        public String toString() {
            return descricao;
        }
    }