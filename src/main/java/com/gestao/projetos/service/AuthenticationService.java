package com.gestao.projetos.service;

import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.model.Credencial;
import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.dao.CredencialDAO;
import com.gestao.projetos.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private final UsuarioDAO usuarioDAO;
    private final CredencialDAO credencialDAO;
    
    public AuthenticationService() {
        this.usuarioDAO = new UsuarioDAO();
        this.credencialDAO = new CredencialDAO();
        logger.debug("AuthenticationService inicializado");
    }

    public Usuario authenticate(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            logger.warn("Tentativa de login com credenciais vazias");
            return null;
        }

        try {
            // Buscar usuário pelo email usando DAO
            Optional<Usuario> usuarioOpt = usuarioDAO.findByEmail(email.trim().toLowerCase());
            
            if (usuarioOpt.isEmpty()) {
                registrarLogAcesso(null, "LOGIN_FAILED", false, "Usuário não encontrado: " + email);
                logger.warn("Tentativa de login com usuário não encontrado: {}", email);
                return null;
            }
            
            Usuario usuario = usuarioOpt.get();
            
            if (!usuario.isAtivo()) {
                registrarLogAcesso(usuario.getId(), "LOGIN_FAILED", false, "Usuário inativo");
                logger.warn("Tentativa de login com usuário inativo: {}", email);
                return null;
            }
            
            // Buscar credencial do usuário usando DAO
            Optional<Credencial> credencialOpt = credencialDAO.findByUserId(usuario.getId());
            
            if (credencialOpt.isEmpty()) {
                registrarLogAcesso(usuario.getId(), "LOGIN_FAILED", false, "Credencial não encontrada");
                logger.warn("Credencial não encontrada para usuário: {}", email);
                return null;
            }
            
            Credencial credencial = credencialOpt.get();
            
            // Verificar senha
            if (BCrypt.checkpw(password, credencial.getHash())) {
                registrarLogAcesso(usuario.getId(), "LOGIN", true, null);
                logger.info("Usuário autenticado com sucesso: {}", email);
                return usuario;
            } else {
                registrarLogAcesso(usuario.getId(), "LOGIN_FAILED", false, "Senha incorreta");
                logger.warn("Tentativa de login com senha incorreta para usuário: {}", email);
                return null;
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao autenticar usuário: {}", email, e);
            registrarLogAcesso(null, "LOGIN_ERROR", false, "Erro de banco de dados: " + e.getMessage());
            return null;
        }
    }

    public Usuario createUser(String nome, String email, String password) {
        if (nome == null || email == null || password == null ||
            nome.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            logger.warn("Tentativa de criar usuário com dados inválidos");
            return null;
        }

        try {
            // Verificar se usuário já existe usando DAO
            Optional<Usuario> existente = usuarioDAO.findByEmail(email.trim().toLowerCase());
            if (existente.isPresent()) {
                logger.warn("Tentativa de criar usuário que já existe: {}", email);
                return null;
            }

            Connection conn = null;
            try {
                conn = DatabaseUtil.getConnection();
                conn.setAutoCommit(false);

                // Criar usuário usando DAO
                Usuario novoUsuario = new Usuario();
                novoUsuario.setNome(nome.trim());
                novoUsuario.setEmail(email.trim().toLowerCase());
                novoUsuario.setAtivo(true);

                Usuario usuarioSalvo = usuarioDAO.save(novoUsuario);
                if (usuarioSalvo == null || usuarioSalvo.getId() == null) {
                    throw new SQLException("Falha ao criar usuário");
                }

                // Criar credencial usando DAO
                String salt = BCrypt.gensalt(12);
                String hashedPassword = BCrypt.hashpw(password, salt);

                Credencial credencial = new Credencial();
                credencial.setUsuarioId(usuarioSalvo.getId());
                credencial.setHash(hashedPassword);
                credencial.setSalt(salt);

                Credencial credencialSalva = credencialDAO.save(credencial);
                if (credencialSalva == null) {
                    throw new SQLException("Falha ao criar credencial");
                }

                conn.commit();
                logger.info("Usuário criado com sucesso: {}", email);
                return usuarioSalvo;

            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        logger.error("Erro ao fazer rollback", ex);
                    }
                }
                throw e;
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        logger.error("Erro ao fechar conexão", e);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao criar usuário: {}", email, e);
            return null;
        }
    }

    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        if (userId == null || currentPassword == null || newPassword == null ||
            currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            return false;
        }

        try {
            // Buscar credencial atual usando DAO
            Optional<Credencial> credencialOpt = credencialDAO.findByUserId(userId);
            
            if (credencialOpt.isEmpty()) {
                logger.warn("Credencial não encontrada para usuário ID: {}", userId);
                return false;
            }
            
            Credencial credencial = credencialOpt.get();
            
            // Verificar senha atual
            if (!BCrypt.checkpw(currentPassword, credencial.getHash())) {
                logger.warn("Tentativa de alteração de senha com senha atual incorreta para usuário ID: {}", userId);
                return false;
            }
            
            // Gerar novo hash
            String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            
            // Atualizar credencial usando DAO
            boolean updated = credencialDAO.updateHashByUserId(userId, newHash);
            
            if (updated) {
                logger.info("Senha alterada com sucesso para usuário ID: {}", userId);
                return true;
            } else {
                logger.warn("Falha ao atualizar senha para usuário ID: {}", userId);
                return false;
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao alterar senha para usuário ID: {}", userId, e);
            return false;
        }
    }

    private void registrarLogAcesso(Long userId, String acao, boolean sucesso, String detalhes) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = """
                INSERT INTO log_acesso (acao, usuario_id, sucesso, ip_origem, user_agent)
                VALUES (?, ?, ?, ?, ?)
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, acao);
                if (userId != null) {
                    stmt.setLong(2, userId);
                } else {
                    stmt.setNull(2, java.sql.Types.BIGINT);
                }
                stmt.setBoolean(3, sucesso);
                stmt.setString(4, "127.0.0.1"); // IP local por enquanto
                stmt.setString(5, detalhes != null ? detalhes : "Desktop Application");

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.warn("Erro ao registrar log de acesso", e);
        }
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        return true;
    }
}
