package com.gestao.projetos.view;

import com.gestao.projetos.controller.DashboardController;
import com.gestao.projetos.model.StatusProjeto;
import com.gestao.projetos.model.StatusTarefa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Tela de Dashboard com estatísticas, gráficos e indicadores do sistema
 */
public class DashboardFrame extends JInternalFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardFrame.class);
    private final DashboardController controller;
    
    // Componentes principais
    private JPanel mainPanel;
    private JPanel statsPanel;
    private JPanel chartsPanel;
    private JPanel indicatorsPanel;
    
    // Componentes de estatísticas
    private JLabel lblTotalProjetos;
    private JLabel lblProjetosAndamento;
    private JLabel lblProjetosConcluidos;
    private JLabel lblProjetosAtrasados;
    private JLabel lblTotalTarefas;
    private JLabel lblTarefasAndamento;
    private JLabel lblTarefasConcluidas;
    private JLabel lblTarefasAtrasadas;
    private JLabel lblTotalUsuarios;
    private JLabel lblUsuariosAtivos;
    private JLabel lblTotalEquipes;
    
    // Componentes de gráficos (usando painéis personalizados)
    private ChartPanel projetosStatusChart;
    private ChartPanel tarefasStatusChart;
    private ChartPanel projetosProgressChart;
    
    // Componentes de indicadores
    private JProgressBar progressGeralProjetos;
    private JProgressBar progressGeralTarefas;
    private JLabel lblIndicadorAtrasos;
    private JLabel lblIndicadorDesempenho;
    
    // Timer para atualização automática
    private Timer updateTimer;
    
    public DashboardFrame() {
        this.controller = new DashboardController(this);
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupFrame();
        
        // Carregar dados iniciais
        refreshData();
        
        // Configurar atualização automática a cada 30 segundos
        setupAutoRefresh();
    }
    
    /**
     * Inicializa os componentes da interface
     */
    private void initializeComponents() {
        // Painéis principais
        mainPanel = new JPanel(new BorderLayout(10, 10));
        statsPanel = new JPanel(new GridLayout(2, 6, 10, 10));
        chartsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        indicatorsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        // Labels de estatísticas - Projetos
        lblTotalProjetos = createStatLabel("0", "Total de Projetos", Color.BLUE);
        lblProjetosAndamento = createStatLabel("0", "Em Andamento", new Color(255, 165, 0));
        lblProjetosConcluidos = createStatLabel("0", "Concluídos", Color.GREEN);
        lblProjetosAtrasados = createStatLabel("0", "Atrasados", Color.RED);
        
        // Labels de estatísticas - Tarefas
        lblTotalTarefas = createStatLabel("0", "Total de Tarefas", Color.BLUE);
        lblTarefasAndamento = createStatLabel("0", "Em Andamento", new Color(255, 165, 0));
        lblTarefasConcluidas = createStatLabel("0", "Concluídas", Color.GREEN);
        lblTarefasAtrasadas = createStatLabel("0", "Atrasadas", Color.RED);
        
        // Labels de estatísticas - Usuários e Equipes
        lblTotalUsuarios = createStatLabel("0", "Total de Usuários", Color.BLUE);
        lblUsuariosAtivos = createStatLabel("0", "Usuários Ativos", Color.GREEN);
        lblTotalEquipes = createStatLabel("0", "Total de Equipes", Color.BLUE);
        
        // Espaço para completar o grid
        JLabel lblEmpty = new JLabel();
        
        // Adicionar ao painel de estatísticas
        statsPanel.add(createStatPanel(lblTotalProjetos));
        statsPanel.add(createStatPanel(lblProjetosAndamento));
        statsPanel.add(createStatPanel(lblProjetosConcluidos));
        statsPanel.add(createStatPanel(lblProjetosAtrasados));
        statsPanel.add(createStatPanel(lblTotalUsuarios));
        statsPanel.add(createStatPanel(lblUsuariosAtivos));
        
        statsPanel.add(createStatPanel(lblTotalTarefas));
        statsPanel.add(createStatPanel(lblTarefasAndamento));
        statsPanel.add(createStatPanel(lblTarefasConcluidas));
        statsPanel.add(createStatPanel(lblTarefasAtrasadas));
        statsPanel.add(createStatPanel(lblTotalEquipes));
        statsPanel.add(lblEmpty);
        
        // Componentes de gráficos
        projetosStatusChart = new ChartPanel("Status dos Projetos");
        tarefasStatusChart = new ChartPanel("Status das Tarefas");
        projetosProgressChart = new ChartPanel("Progresso dos Projetos");
        
        chartsPanel.add(projetosStatusChart);
        chartsPanel.add(tarefasStatusChart);
        chartsPanel.add(projetosProgressChart);
        
        // Componentes de indicadores
        progressGeralProjetos = new JProgressBar(0, 100);
        progressGeralProjetos.setStringPainted(true);
        progressGeralProjetos.setString("0%");
        
        progressGeralTarefas = new JProgressBar(0, 100);
        progressGeralTarefas.setStringPainted(true);
        progressGeralTarefas.setString("0%");
        
        lblIndicadorAtrasos = createIndicatorLabel("0", "Projetos/Tarefas Atrasados", Color.RED);
        lblIndicadorDesempenho = createIndicatorLabel("0%", "Eficiência Geral", Color.GREEN);
        
        // Adicionar aos indicadores
        indicatorsPanel.add(createIndicatorPanel("Progresso dos Projetos", progressGeralProjetos));
        indicatorsPanel.add(createIndicatorPanel("Progresso das Tarefas", progressGeralTarefas));
        indicatorsPanel.add(createIndicatorPanel("Indicador de Atrasos", lblIndicadorAtrasos));
        indicatorsPanel.add(createIndicatorPanel("Desempenho Geral", lblIndicadorDesempenho));
    }
    
    /**
     * Configura o layout da janela
     */
    private void setupLayout() {
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel superior com estatísticas
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Estatísticas Gerais", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14)
        ));
        topPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Painel central com gráficos
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Gráficos e Análises", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14)
        ));
        centerPanel.add(chartsPanel, BorderLayout.CENTER);
        
        // Painel inferior com indicadores
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Indicadores de Desempenho", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14)
        ));
        bottomPanel.add(indicatorsPanel, BorderLayout.CENTER);
        
        // Botão de atualização
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("🔄 Atualizar");
        btnRefresh.setToolTipText("Atualizar dados do dashboard");
        btnRefresh.addActionListener(e -> refreshData());
        buttonPanel.add(btnRefresh);
        
        // Adicionar ao painel principal
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.PAGE_END);
        
        add(mainPanel);
    }
    
    /**
     * Configura os manipuladores de eventos
     */
    private void setupEventHandlers() {
        // Adicionar listener para fechar o timer quando a janela for fechada
        addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
                if (updateTimer != null) {
                    updateTimer.cancel();
                }
            }
        });
    }
    
    /**
     * Configura as propriedades da janela
     */
    private void setupFrame() {
        setTitle("📊 Dashboard - Sistema de Gestão de Projetos");
        setSize(1200, 800);
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setIconifiable(true);
        
        try {
            setMaximum(true);
        } catch (Exception e) {
            logger.warn("Não foi possível maximizar a janela: {}", e.getMessage());
        }
    }
    
    /**
     * Configura atualização automática dos dados
     */
    private void setupAutoRefresh() {
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> refreshData());
            }
        }, 30000, 30000); // Atualiza a cada 30 segundos
    }
    
    /**
     * Atualiza todos os dados do dashboard
     */
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                controller.atualizarDados();
            } catch (Exception e) {
                logger.error("Erro ao atualizar dados do dashboard", e);
                JOptionPane.showMessageDialog(this, 
                    "Erro ao atualizar dados: " + e.getMessage(),
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Atualiza as estatísticas de projetos
     */
    public void updateProjetosStats(Map<String, Long> stats) {
        lblTotalProjetos.setText(String.valueOf(stats.getOrDefault("total", 0L)));
        lblProjetosAndamento.setText(String.valueOf(stats.getOrDefault("em_andamento", 0L)));
        lblProjetosConcluidos.setText(String.valueOf(stats.getOrDefault("concluidos", 0L)));
        lblProjetosAtrasados.setText(String.valueOf(stats.getOrDefault("atrasados", 0L)));
        
        // Atualizar gráfico de status dos projetos
        projetosStatusChart.updateData(stats);
        
        // Calcular e atualizar progresso geral dos projetos
        long total = stats.getOrDefault("total", 0L);
        long concluidos = stats.getOrDefault("concluidos", 0L);
        int progresso = total > 0 ? (int) ((concluidos * 100) / total) : 0;
        progressGeralProjetos.setValue(progresso);
        progressGeralProjetos.setString(progresso + "%");
        
        // Atualizar gráfico de progresso geral com dados dos projetos
        Map<String, Object> progressData = new java.util.HashMap<>();
        progressData.put("eficiencia", (double) progresso);
        progressData.put("total", total);
        progressData.put("concluidos", concluidos);
        progressData.put("em_andamento", stats.getOrDefault("em_andamento", 0L));
        progressData.put("atrasados", stats.getOrDefault("atrasados", 0L));
        projetosProgressChart.updateProgressData(progressData);
    }
    
    /**
     * Atualiza as estatísticas de tarefas
     */
    public void updateTarefasStats(Map<String, Long> stats) {
        lblTotalTarefas.setText(String.valueOf(stats.getOrDefault("total", 0L)));
        lblTarefasAndamento.setText(String.valueOf(stats.getOrDefault("em_andamento", 0L)));
        lblTarefasConcluidas.setText(String.valueOf(stats.getOrDefault("concluidas", 0L)));
        lblTarefasAtrasadas.setText(String.valueOf(stats.getOrDefault("atrasadas", 0L)));
        
        // Atualizar gráfico de status das tarefas
        tarefasStatusChart.updateData(stats);
        
        // Calcular e atualizar progresso geral das tarefas
        long total = stats.getOrDefault("total", 0L);
        long concluidas = stats.getOrDefault("concluidas", 0L);
        int progresso = total > 0 ? (int) ((concluidas * 100) / total) : 0;
        progressGeralTarefas.setValue(progresso);
        progressGeralTarefas.setString(progresso + "%");
    }
    
    /**
     * Atualiza as estatísticas de usuários
     */
    public void updateUsuariosStats(Map<String, Long> stats) {
        lblTotalUsuarios.setText(String.valueOf(stats.getOrDefault("total", 0L)));
        lblUsuariosAtivos.setText(String.valueOf(stats.getOrDefault("ativos", 0L)));
    }
    
    /**
     * Atualiza as estatísticas de equipes
     */
    public void updateEquipesStats(Map<String, Long> stats) {
        lblTotalEquipes.setText(String.valueOf(stats.getOrDefault("total", 0L)));
    }
    
    /**
     * Atualiza os indicadores de desempenho
     */
    public void updateIndicadores(Map<String, Object> indicadores) {
        // Atualizar indicador de atrasos
        Long totalAtrasos = (Long) indicadores.getOrDefault("total_atrasos", 0L);
        lblIndicadorAtrasos.setText(String.valueOf(totalAtrasos));
        
        // Atualizar indicador de desempenho (eficiência)
        Double eficiencia = (Double) indicadores.getOrDefault("eficiencia", 0.0);
        lblIndicadorDesempenho.setText(String.format("%.1f%%", eficiencia));
        
        // O gráfico de progresso geral agora é atualizado apenas pelos dados dos projetos
        // na função updateProjetosStats()
    }
    
    /**
     * Cria um label para estatísticas
     */
    private JLabel createStatLabel(String value, String description, Color color) {
        JLabel label = new JLabel(value, SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        label.setForeground(color);
        label.setToolTipText(description);
        return label;
    }
    
    /**
     * Cria um painel para estatística
     */
    private JPanel createStatPanel(JLabel statLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        panel.setBackground(Color.WHITE);
        
        panel.add(statLabel, BorderLayout.CENTER);
        
        // Adicionar descrição se disponível no tooltip
        if (statLabel.getToolTipText() != null) {
            JLabel descLabel = new JLabel(statLabel.getToolTipText(), SwingConstants.CENTER);
            descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            descLabel.setForeground(Color.GRAY);
            panel.add(descLabel, BorderLayout.SOUTH);
        }
        
        return panel;
    }
    
    /**
     * Cria um label para indicadores
     */
    private JLabel createIndicatorLabel(String value, String description, Color color) {
        JLabel label = new JLabel(value, SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        label.setForeground(color);
        label.setToolTipText(description);
        return label;
    }
    
    /**
     * Cria um painel para indicador
     */
    private JPanel createIndicatorPanel(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(Color.WHITE);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Classe interna para painéis de gráficos personalizados
     */
    private class ChartPanel extends JPanel {
        private String title;
        private Map<String, Long> data;
        private Map<String, Object> progressData;
        
        public ChartPanel(String title) {
            this.title = title;
            setBorder(BorderFactory.createTitledBorder(title));
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(350, 250)); // Aumentado para dar mais espaço
            setMinimumSize(new Dimension(300, 200));
        }
        
        public void updateData(Map<String, Long> data) {
            this.data = data;
            repaint();
        }
        
        public void updateProgressData(Map<String, Object> progressData) {
            this.progressData = progressData;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (data != null && !data.isEmpty()) {
                drawBarChart(g2);
            } else if (progressData != null && !progressData.isEmpty()) {
                drawProgressChart(g2);
            } else {
                drawNoDataMessage(g2);
            }
            
            g2.dispose();
        }
        
        private void drawBarChart(Graphics2D g2) {
            int width = getWidth() - 40;
            int height = getHeight() - 100; // Mais espaço para labels e legenda
            int x = 20;
            int y = 30;
            
            if (data.isEmpty()) return;
            
            // Filtrar apenas dados relevantes (com valor > 0)
            Map<String, Long> filteredData = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                if (entry.getValue() > 0) {
                    filteredData.put(entry.getKey(), entry.getValue());
                }
            }
            
            if (filteredData.isEmpty()) {
                drawNoDataMessage(g2);
                return;
            }
            
            // Calcular valor máximo para escala
            long maxValue = filteredData.values().stream().mapToLong(Long::longValue).max().orElse(1L);
            if (maxValue == 0) maxValue = 1;
            
            int barWidth = Math.max(30, width / filteredData.size()); // Largura mínima de 30px
            Color[] colors = {Color.BLUE, new Color(255, 165, 0), Color.GREEN, Color.RED, Color.CYAN, Color.MAGENTA};
            int colorIndex = 0;
            
            // Calcular posição inicial para centralizar as barras
            int totalBarsWidth = barWidth * filteredData.size();
            int startX = (getWidth() - totalBarsWidth) / 2;
            x = startX;
            
            for (Map.Entry<String, Long> entry : filteredData.entrySet()) {
                long value = entry.getValue();
                int barHeight = Math.max(5, (int) ((value * height) / maxValue)); // Altura mínima de 5px
                
                // Desenhar barra
                g2.setColor(colors[colorIndex % colors.length]);
                g2.fillRect(x + 5, y + height - barHeight, barWidth - 10, barHeight);
                
                // Desenhar borda da barra
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(x + 5, y + height - barHeight, barWidth - 10, barHeight);
                
                // Desenhar valor acima da barra
                g2.setColor(Color.BLACK);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                String valueStr = String.valueOf(value);
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (barWidth - fm.stringWidth(valueStr)) / 2;
                int textY = y + height - barHeight - 8;
                if (textY < 20) textY = 20; // Garantir que não saia da área visível
                g2.drawString(valueStr, textX, textY);
                
                // Desenhar label abaixo do gráfico (com quebra de linha se necessário)
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
                String label = entry.getKey().replace("_", " ");
                
                // Quebrar label se for muito longo
                if (label.length() > 10) {
                    String[] words = label.split(" ");
                    if (words.length > 1) {
                        String line1 = words[0];
                        String line2 = "";
                        for (int i = 1; i < words.length; i++) {
                            line2 += words[i];
                            if (i < words.length - 1) line2 += " ";
                        }
                        
                        FontMetrics fmSmall = g2.getFontMetrics();
                        int label1X = x + (barWidth - fmSmall.stringWidth(line1)) / 2;
                        int label2X = x + (barWidth - fmSmall.stringWidth(line2)) / 2;
                        
                        g2.drawString(line1, label1X, y + height + 15);
                        g2.drawString(line2, label2X, y + height + 28);
                    } else {
                        // Label muito longo, truncar
                        if (label.length() > 12) {
                            label = label.substring(0, 9) + "...";
                        }
                        FontMetrics fmSmall = g2.getFontMetrics();
                        int labelX = x + (barWidth - fmSmall.stringWidth(label)) / 2;
                        g2.drawString(label, labelX, y + height + 15);
                    }
                } else {
                    FontMetrics fmSmall = g2.getFontMetrics();
                    int labelX = x + (barWidth - fmSmall.stringWidth(label)) / 2;
                    g2.drawString(label, labelX, y + height + 15);
                }
                
                x += barWidth;
                colorIndex++;
            }
            
            // Desenhar legenda se houver espaço suficiente
            if (getHeight() > 280 && filteredData.size() <= 4) {
                drawLegend(g2, filteredData, colors);
            }
        }
        
        private void drawProgressChart(Graphics2D g2) {
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2 - 10;
            int radius = Math.min(getWidth(), getHeight()) / 4;
            
            // Obter dados dos projetos
            Double eficiencia = (Double) progressData.getOrDefault("eficiencia", 0.0);
            Long total = (Long) progressData.getOrDefault("total", 0L);
            Long concluidos = (Long) progressData.getOrDefault("concluidos", 0L);
            Long emAndamento = (Long) progressData.getOrDefault("em_andamento", 0L);
            Long atrasados = (Long) progressData.getOrDefault("atrasados", 0L);
            
            int angle = (int) (eficiencia * 3.6); // Converter para graus
            
            // Fundo do círculo
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            
            // Progresso (concluídos)
            g2.setColor(Color.GREEN);
            g2.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, -angle);
            
            // Texto central - Percentual
            g2.setColor(Color.BLACK);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            String percentText = String.format("%.1f%%", eficiencia);
            FontMetrics fm = g2.getFontMetrics();
            int textX = centerX - fm.stringWidth(percentText) / 2;
            g2.drawString(percentText, textX, centerY - 5);
            
            // Texto central - Subtítulo
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            String subtitle = "Concluídos";
            FontMetrics fmSmall = g2.getFontMetrics();
            int subtitleX = centerX - fmSmall.stringWidth(subtitle) / 2;
            g2.drawString(subtitle, subtitleX, centerY + 10);
            
            // Informações detalhadas na parte inferior
            if (total > 0) {
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
                FontMetrics fmInfo = g2.getFontMetrics();
                
                int infoY = centerY + radius + 20;
                int lineHeight = 15;
                
                // Total de projetos
                String totalInfo = "Total: " + total;
                int totalX = centerX - fmInfo.stringWidth(totalInfo) / 2;
                g2.setColor(Color.BLUE);
                g2.drawString(totalInfo, totalX, infoY);
                
                // Projetos concluídos
                String concluidosInfo = "Concluídos: " + concluidos;
                int concluidosX = centerX - fmInfo.stringWidth(concluidosInfo) / 2;
                g2.setColor(Color.GREEN);
                g2.drawString(concluidosInfo, concluidosX, infoY + lineHeight);
                
                // Projetos em andamento
                String andamentoInfo = "Em Andamento: " + emAndamento;
                int andamentoX = centerX - fmInfo.stringWidth(andamentoInfo) / 2;
                g2.setColor(new Color(255, 165, 0));
                g2.drawString(andamentoInfo, andamentoX, infoY + lineHeight * 2);
                
                // Projetos atrasados (se houver)
                if (atrasados > 0) {
                    String atrasadosInfo = "Atrasados: " + atrasados;
                    int atrasadosX = centerX - fmInfo.stringWidth(atrasadosInfo) / 2;
                    g2.setColor(Color.RED);
                    g2.drawString(atrasadosInfo, atrasadosX, infoY + lineHeight * 3);
                }
            }
        }
        
        private void drawNoDataMessage(Graphics2D g2) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
            String message = "Nenhum dado disponível";
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2.drawString(message, x, y);
        }
        
        private void drawLegend(Graphics2D g2, Map<String, Long> data, Color[] colors) {
            // Posicionar a legenda bem na parte inferior, abaixo dos labels
            int legendY = getHeight() - 25; // Mais próximo da borda inferior
            int legendX = 10;
            int colorIndex = 0;
            
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
            FontMetrics fm = g2.getFontMetrics();
            
            // Calcular largura disponível para distribuir as legendas
            int availableWidth = getWidth() - 20;
            int itemsPerRow = Math.max(1, data.size()); // Tentar colocar tudo em uma linha
            
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                // Desenhar quadrado colorido (menor)
                g2.setColor(colors[colorIndex % colors.length]);
                g2.fillRect(legendX, legendY - 6, 8, 8);
                g2.setColor(Color.BLACK);
                g2.drawRect(legendX, legendY - 6, 8, 8);
                
                // Desenhar texto da legenda (mais compacto)
                String legendText = entry.getKey().replace("_", " ");
                if (legendText.length() > 8) {
                    legendText = legendText.substring(0, 6) + "..";
                }
                g2.drawString(legendText, legendX + 12, legendY);
                
                legendX += fm.stringWidth(legendText) + 25;
                
                // Quebrar linha se necessário (menos espaço)
                if (legendX > getWidth() - 80) {
                    legendX = 10;
                    legendY += 12; // Menor espaçamento vertical
                }
                
                colorIndex++;
            }
        }
    }
    
    /**
     * Exibe uma mensagem de erro
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Exibe uma mensagem de sucesso
     */
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }
}