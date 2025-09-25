package com.gestao.projetos.controller;

import com.gestao.projetos.service.DashboardService;
import com.gestao.projetos.view.DashboardFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para o Dashboard
 */
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final DashboardFrame view;
    private final DashboardService dashboardService;
    
    public DashboardController(DashboardFrame view) {
        this.view = view;
        this.dashboardService = new DashboardService();
    }
    
    /**
     * Atualiza todos os dados do dashboard
     */
    public void atualizarDados() {
        try {
            logger.info("Atualizando dados do dashboard");
            
            // Atualizar estatísticas de projetos
            atualizarEstatisticasProjetos();
            
            // Atualizar estatísticas de tarefas
            atualizarEstatisticasTarefas();
            
            // Atualizar estatísticas de usuários
            atualizarEstatisticasUsuarios();
            
            // Atualizar estatísticas de equipes
            atualizarEstatisticasEquipes();
            
            // Atualizar indicadores
            atualizarIndicadores();
            
            logger.info("Dados do dashboard atualizados com sucesso");
            
        } catch (Exception e) {
            logger.error("Erro ao atualizar dados do dashboard", e);
            view.showError("Erro ao atualizar dados: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza as estatísticas de projetos
     */
    private void atualizarEstatisticasProjetos() throws SQLException {
        logger.debug("Atualizando estatísticas de projetos");
        
        Map<String, Long> stats = new HashMap<>();
        
        // Obter contadores de projetos
        stats.put("total", dashboardService.contarTotalProjetos());
        stats.put("em_andamento", dashboardService.contarProjetosEmAndamento());
        stats.put("concluidos", dashboardService.contarProjetosConcluidos());
        stats.put("atrasados", dashboardService.contarProjetosAtrasados());
        stats.put("planejados", dashboardService.contarProjetosPlanejados());
        stats.put("pausados", dashboardService.contarProjetosPausados());
        stats.put("cancelados", dashboardService.contarProjetosCancelados());
        
        view.updateProjetosStats(stats);
        
        logger.debug("Estatísticas de projetos atualizadas: {}", stats);
    }
    
    /**
     * Atualiza as estatísticas de tarefas
     */
    private void atualizarEstatisticasTarefas() throws SQLException {
        logger.debug("Atualizando estatísticas de tarefas");
        
        Map<String, Long> stats = new HashMap<>();
        
        // Obter contadores de tarefas
        stats.put("total", dashboardService.contarTotalTarefas());
        stats.put("em_andamento", dashboardService.contarTarefasEmAndamento());
        stats.put("concluidas", dashboardService.contarTarefasConcluidas());
        stats.put("atrasadas", dashboardService.contarTarefasAtrasadas());
        stats.put("novas", dashboardService.contarTarefasNovas());
        stats.put("bloqueadas", dashboardService.contarTarefasBloqueadas());
        stats.put("canceladas", dashboardService.contarTarefasCanceladas());
        
        view.updateTarefasStats(stats);
        
        logger.debug("Estatísticas de tarefas atualizadas: {}", stats);
    }
    
    /**
     * Atualiza as estatísticas de usuários
     */
    private void atualizarEstatisticasUsuarios() throws SQLException {
        logger.debug("Atualizando estatísticas de usuários");
        
        Map<String, Long> stats = new HashMap<>();
        
        // Obter contadores de usuários
        stats.put("total", dashboardService.contarTotalUsuarios());
        stats.put("ativos", dashboardService.contarUsuariosAtivos());
        stats.put("inativos", dashboardService.contarUsuariosInativos());
        stats.put("administradores", dashboardService.contarAdministradores());
        stats.put("gerentes", dashboardService.contarGerentes());
        stats.put("colaboradores", dashboardService.contarColaboradores());
        
        view.updateUsuariosStats(stats);
        
        logger.debug("Estatísticas de usuários atualizadas: {}", stats);
    }
    
    /**
     * Atualiza as estatísticas de equipes
     */
    private void atualizarEstatisticasEquipes() throws SQLException {
        logger.debug("Atualizando estatísticas de equipes");
        
        Map<String, Long> stats = new HashMap<>();
        
        // Obter contadores de equipes
        stats.put("total", dashboardService.contarTotalEquipes());
        stats.put("ativas", dashboardService.contarEquipesAtivas());
        
        view.updateEquipesStats(stats);
        
        logger.debug("Estatísticas de equipes atualizadas: {}", stats);
    }
    
    /**
     * Atualiza os indicadores de desempenho
     */
    private void atualizarIndicadores() throws SQLException {
        logger.debug("Atualizando indicadores de desempenho");
        
        Map<String, Object> indicadores = new HashMap<>();
        
        // Calcular indicadores
        long totalAtrasos = dashboardService.contarProjetosAtrasados() + 
                           dashboardService.contarTarefasAtrasadas();
        indicadores.put("total_atrasos", totalAtrasos);
        
        // Calcular eficiência geral (baseada na proporção de itens concluídos)
        long totalProjetos = dashboardService.contarTotalProjetos();
        long projetosConcluidos = dashboardService.contarProjetosConcluidos();
        long totalTarefas = dashboardService.contarTotalTarefas();
        long tarefasConcluidas = dashboardService.contarTarefasConcluidas();
        
        double eficienciaProjetos = totalProjetos > 0 ? 
            (projetosConcluidos * 100.0) / totalProjetos : 0.0;
        double eficienciaTarefas = totalTarefas > 0 ? 
            (tarefasConcluidas * 100.0) / totalTarefas : 0.0;
        
        // Média ponderada da eficiência
        double eficienciaGeral = (eficienciaProjetos + eficienciaTarefas) / 2.0;
        indicadores.put("eficiencia", eficienciaGeral);
        
        // Outros indicadores
        indicadores.put("taxa_conclusao_projetos", eficienciaProjetos);
        indicadores.put("taxa_conclusao_tarefas", eficienciaTarefas);
        
        // Indicador de carga de trabalho
        long usuariosAtivos = dashboardService.contarUsuariosAtivos();
        double cargaTrabalho = usuariosAtivos > 0 ? 
            (double) totalTarefas / usuariosAtivos : 0.0;
        indicadores.put("carga_trabalho_media", cargaTrabalho);
        
        view.updateIndicadores(indicadores);
        
        logger.debug("Indicadores atualizados: {}", indicadores);
    }
    
    /**
     * Força atualização completa dos dados
     */
    public void forcarAtualizacao() {
        logger.info("Forçando atualização completa do dashboard");
        atualizarDados();
    }
    
    /**
     * Obtém resumo das estatísticas para relatórios
     */
    public Map<String, Object> obterResumoEstatisticas() {
        Map<String, Object> resumo = new HashMap<>();
        
        try {
            // Projetos
            Map<String, Long> projetos = new HashMap<>();
            projetos.put("total", dashboardService.contarTotalProjetos());
            projetos.put("em_andamento", dashboardService.contarProjetosEmAndamento());
            projetos.put("concluidos", dashboardService.contarProjetosConcluidos());
            projetos.put("atrasados", dashboardService.contarProjetosAtrasados());
            resumo.put("projetos", projetos);
            
            // Tarefas
            Map<String, Long> tarefas = new HashMap<>();
            tarefas.put("total", dashboardService.contarTotalTarefas());
            tarefas.put("em_andamento", dashboardService.contarTarefasEmAndamento());
            tarefas.put("concluidas", dashboardService.contarTarefasConcluidas());
            tarefas.put("atrasadas", dashboardService.contarTarefasAtrasadas());
            resumo.put("tarefas", tarefas);
            
            // Usuários
            Map<String, Long> usuarios = new HashMap<>();
            usuarios.put("total", dashboardService.contarTotalUsuarios());
            usuarios.put("ativos", dashboardService.contarUsuariosAtivos());
            resumo.put("usuarios", usuarios);
            
            // Equipes
            Map<String, Long> equipes = new HashMap<>();
            equipes.put("total", dashboardService.contarTotalEquipes());
            resumo.put("equipes", equipes);
            
        } catch (SQLException e) {
            logger.error("Erro ao obter resumo de estatísticas", e);
        }
        
        return resumo;
    }
    
    /**
     * Verifica se há alertas críticos
     */
    public boolean verificarAlertasCriticos() {
        try {
            long projetosAtrasados = dashboardService.contarProjetosAtrasados();
            long tarefasAtrasadas = dashboardService.contarTarefasAtrasadas();
            
            // Considera crítico se mais de 20% dos projetos estão atrasados
            long totalProjetos = dashboardService.contarTotalProjetos();
            double percentualAtrasoProjetos = totalProjetos > 0 ? 
                (projetosAtrasados * 100.0) / totalProjetos : 0.0;
            
            return percentualAtrasoProjetos > 20.0 || tarefasAtrasadas > 10;
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar alertas críticos", e);
            return false;
        }
    }
    
    /**
     * Obtém mensagens de alerta
     */
    public String obterMensagensAlerta() {
        StringBuilder alertas = new StringBuilder();
        
        try {
            long projetosAtrasados = dashboardService.contarProjetosAtrasados();
            long tarefasAtrasadas = dashboardService.contarTarefasAtrasadas();
            
            if (projetosAtrasados > 0) {
                alertas.append("⚠️ ").append(projetosAtrasados)
                       .append(" projeto(s) atrasado(s)\n");
            }
            
            if (tarefasAtrasadas > 0) {
                alertas.append("⚠️ ").append(tarefasAtrasadas)
                       .append(" tarefa(s) atrasada(s)\n");
            }
            
            if (alertas.length() == 0) {
                alertas.append("✅ Nenhum alerta crítico");
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao obter mensagens de alerta", e);
            alertas.append("Erro ao verificar alertas");
        }
        
        return alertas.toString().trim();
    }
}