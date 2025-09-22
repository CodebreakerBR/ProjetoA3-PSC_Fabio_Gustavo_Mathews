package com.gestao.projetos.controller;

import com.gestao.projetos.view.*;
import com.gestao.projetos.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gestao.projetos.view.ProjetoView;

import javax.swing.*;

/**
 * Controlador principal da aplicação
 */
public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final MainFrame mainFrame;

    public MainController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * Abre a janela de gestão de usuários
     */
    public void abrirGestaoUsuarios() {
        try {
            JInternalFrame[] frames = mainFrame.getDesktopPane().getAllFrames();
            for (JInternalFrame frame : frames) {
                if (frame instanceof UsuarioFrame) {
                    frame.toFront();
                    frame.setSelected(true);
                    return;
                }
            }

            UsuarioFrame usuarioFrame = new UsuarioFrame();
            mainFrame.addInternalFrame(usuarioFrame);
            usuarioFrame.setVisible(true);
            mainFrame.updateStatusMessage("Gestão de Usuários aberta");

        } catch (Exception e) {
            logger.error("Erro ao abrir gestão de usuários", e);
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Erro ao abrir gestão de usuários: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    /**
     * Abre a janela de gestão de projetos
     */
    public void abrirGestaoProjetos() {
        try {
            // Verifica se já existe uma janela aberta
            JInternalFrame[] frames = mainFrame.getDesktopPane().getAllFrames();
            for (JInternalFrame frame : frames) {
                if (frame instanceof ProjetoView) {
                    frame.toFront();
                    frame.setSelected(true);
                    return;
                }
            }

            // Cria nova janela
            ProjetoView projetoView = new ProjetoView();
            mainFrame.addInternalFrame(projetoView);
            projetoView.setVisible(true);
            mainFrame.updateStatusMessage("Gestão de Projetos aberta");

        } catch (Exception e) {
            logger.error("Erro ao abrir gestão de projetos", e);
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Erro ao abrir gestão de projetos: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }



    /**
     * Abre a janela de gestão de equipes
     */
    public void abrirGestaoEquipes() {
        try {
            JInternalFrame[] frames = mainFrame.getDesktopPane().getAllFrames();
            for (JInternalFrame frame : frames) {
                if (frame instanceof EquipeView) {
                    frame.toFront();
                    frame.setSelected(true);
                    return;
                }
            }

            EquipeView equipeView = new EquipeView();
            mainFrame.addInternalFrame(equipeView);
            equipeView.setVisible(true);
            mainFrame.updateStatusMessage("Gestão de Equipes aberta");

        } catch (Exception e) {
            logger.error("Erro ao abrir gestão de equipes", e);
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Erro ao abrir gestão de equipes: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    /**
     * Abre a janela de gestão de tarefas
     */
    public void abrirGestaoTarefas() {
        try {
            // Implementação futura
            JOptionPane.showMessageDialog(
                mainFrame,
                "Gestão de Tarefas será implementada em breve",
                "Em Desenvolvimento",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (Exception e) {
            logger.error("Erro ao abrir gestão de tarefas", e);
        }
    }

    /**
     * Abre o dashboard
     */
    public void abrirDashboard() {
        try {
            // Implementação futura
            JOptionPane.showMessageDialog(
                mainFrame,
                "Dashboard será implementado em breve",
                "Em Desenvolvimento",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (Exception e) {
            logger.error("Erro ao abrir dashboard", e);
        }
    }

    /**
     * Abre relatórios de projetos
     */
    public void abrirRelatoriosProjetos() {
        try {
            // Implementação futura
            JOptionPane.showMessageDialog(
                mainFrame,
                "Relatórios de Projetos será implementado em breve",
                "Em Desenvolvimento",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (Exception e) {
            logger.error("Erro ao abrir relatórios de projetos", e);
        }
    }

    /**
     * Abre relatórios de usuários
     */
    public void abrirRelatoriosUsuarios() {
        try {
            // Implementação futura
            JOptionPane.showMessageDialog(
                mainFrame,
                "Relatórios de Usuários será implementado em breve",
                "Em Desenvolvimento",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (Exception e) {
            logger.error("Erro ao abrir relatórios de usuários", e);
        }
    }

    /**
     * Testa a conexão com o banco de dados
     */
    public void testarConexao() {
        try {
            mainFrame.updateStatusMessage("Testando conexão...");
            
            boolean conectado = DatabaseUtil.testConnection();
            if (conectado) {
                String poolStatus = DatabaseUtil.getPoolStatus();
                JOptionPane.showMessageDialog(
                    mainFrame,
                    "Conexão com o banco de dados estabelecida com sucesso!\n\n" + poolStatus,
                    "Teste de Conexão",
                    JOptionPane.INFORMATION_MESSAGE
                );
                mainFrame.updateStatusMessage("Conexão testada com sucesso");
            } else {
                JOptionPane.showMessageDialog(
                    mainFrame,
                    "Falha ao conectar com o banco de dados!\n" +
                    "Verifique as configurações de conexão.",
                    "Teste de Conexão",
                    JOptionPane.ERROR_MESSAGE
                );
                mainFrame.updateStatusMessage("Falha na conexão com o banco");
            }
            
            mainFrame.updateConnectionStatus();
            
        } catch (Exception e) {
            logger.error("Erro ao testar conexão", e);
            JOptionPane.showMessageDialog(
                mainFrame,
                "Erro ao testar conexão: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            mainFrame.updateStatusMessage("Erro ao testar conexão");
        }
    }

    /**
     * Mostra a janela sobre o sistema
     */
    public void mostrarSobre() {
        StringBuilder sobre = new StringBuilder();
        sobre.append("Sistema de Gestão de Projetos e Equipes\n\n");
        sobre.append("Versão: 1.0.0\n");
        sobre.append("Desenvolvido em Java com Swing e MySQL\n");
        sobre.append("Padrão de Arquitetura: MVC\n\n");
        sobre.append("Recursos:\n");
        sobre.append("• Gestão de Usuários\n");
        sobre.append("• Gestão de Projetos\n");
        sobre.append("• Gestão de Equipes\n");
        sobre.append("• Gestão de Tarefas\n");
        sobre.append("• Relatórios e Dashboard\n\n");
        sobre.append("Desenvolvedores: Fabio, Gustavo, Mathews\n");
        sobre.append("Disciplina: PSC - Programação de Soluções Computacionais\n");
        
        JOptionPane.showMessageDialog(
            mainFrame,
            sobre.toString(),
            "Sobre o Sistema",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Sai da aplicação
     */
    public void sair() {
        try {
            int opcao = JOptionPane.showConfirmDialog(
                mainFrame,
                "Deseja realmente sair do sistema?",
                "Confirmar Saída",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (opcao == JOptionPane.YES_OPTION) {
                mainFrame.updateStatusMessage("Finalizando sistema...");
                
                // Fecha todas as janelas internas
                JInternalFrame[] frames = mainFrame.getDesktopPane().getAllFrames();
                for (JInternalFrame frame : frames) {
                    try {
                        frame.setClosed(true);
                    } catch (Exception e) {
                        logger.warn("Erro ao fechar janela interna", e);
                    }
                }
                
                // Fecha o pool de conexões
                DatabaseUtil.closeDataSource();
                
                logger.info("Sistema finalizado pelo usuário");
                System.exit(0);
            }
            
        } catch (Exception e) {
            logger.error("Erro ao sair da aplicação", e);
            System.exit(1);
        }
    }
}
