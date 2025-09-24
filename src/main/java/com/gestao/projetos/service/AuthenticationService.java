package com.gestao.projetos.service;

import com.gestao.projetos.model.Usuario;
import com.gestao.projetos.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    public AuthenticationService() {
        // JBCrypt não precisa de instância, usa métodos estáticos
        logger.debug("AuthenticationService inicializado");
    }

    public Usuario authenticate(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            logger.warn("Tentativa de login com credenciais vazias");
            return null;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = """
                SELECT u.id, u.nome, u.email, u.ativo, c.hash
                FROM usuario u
                INNER JOIN credencial c ON u.id = c.usuario_id
                WHERE u.email = ? AND u.ativo = TRUE
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email.trim().toLowerCase());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String hashedPassword = rs.getString("hash");

                        if (BCrypt.checkpw(password, hashedPassword)) {
                            Usuario usuario = new Usuario();
                            usuario.setId(rs.getLong("id"));
                            usuario.setNome(rs.getString("nome"));
                            usuario.setEmail(rs.getString("email"));
                            usuario.setAtivo(rs.getBoolean("ativo"));

                            registrarLogAcesso(usuario.getId(), "LOGIN", true, null);

                            logger.info("Usuário autenticado com sucesso: {}", email);
                            return usuario;
                        } else {
                            Long userId = rs.getLong("id");
                            registrarLogAcesso(userId, "LOGIN_FAILED", false, "Senha incorreta");
                            logger.warn("Tentativa de login com senha incorreta para usuário: {}", email);
                        }
                    } else {
                        registrarLogAcesso(null, "LOGIN_FAILED", false, "Usuário não encontrado: " + email);
                        logger.warn("Tentativa de login com usuário não encontrado: {}", email);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao autenticar usuário: {}", email, e);
            registrarLogAcesso(null, "LOGIN_ERROR", false, "Erro de banco de dados: " + e.getMessage());
        }
        return null;
    }

    public boolean userExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT COUNT(*) FROM usuario WHERE email = ? AND ativo = TRUE";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email.trim().toLowerCase());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência do usuário: {}", email, e);
        }
        return false;
    }

    public Usuario createUser(String nome, String email, String password) {
        if (nome == null || email == null || password == null ||
            nome.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            logger.warn("Tentativa de criar usuário com dados inválidos");
            return null;
        }

        if (userExists(email)) {
            logger.warn("Tentativa de criar usuário que já existe: {}", email);
            return null;
        }

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            String userSql = "INSERT INTO usuario (nome, email, ativo) VALUES (?, ?, TRUE)";
            long userId;

            try (PreparedStatement userStmt = conn.prepareStatement(userSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, nome.trim());
                userStmt.setString(2, email.trim().toLowerCase());

                int affectedRows = userStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Falha ao criar usuário");
                }

                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Falha ao obter ID do usuário criado");
                    }
                }
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String credentialSql = "INSERT INTO credencial (hash, salt, usuario_id) VALUES (?, ?, ?)";

            try (PreparedStatement credStmt = conn.prepareStatement(credentialSql)) {
                credStmt.setString(1, hashedPassword);
                credStmt.setString(2, "bcrypt"); // Salt é gerenciado pelo BCrypt
                credStmt.setLong(3, userId);

                credStmt.executeUpdate();
            }

            conn.commit();

            Usuario usuario = new Usuario();
            usuario.setId(userId);
            usuario.setNome(nome.trim());
            usuario.setEmail(email.trim().toLowerCase());
            usuario.setAtivo(true);

            logger.info("Usuário criado com sucesso: {}", email);
            return usuario;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Erro ao fazer rollback", ex);
                }
            }
            logger.error("Erro ao criar usuário: {}", email, e);
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
        return null;
    }

    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        if (userId == null || currentPassword == null || newPassword == null ||
            currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            return false;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            String checkSql = "SELECT hash FROM credencial WHERE usuario_id = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setLong(1, userId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String currentHash = rs.getString("hash");

                        if (!BCrypt.checkpw(currentPassword, currentHash)) {
                            logger.warn("Tentativa de alteração de senha com senha atual incorreta para usuário ID: {}", userId);
                            return false;
                        }

                        String updateSql = "UPDATE credencial SET hash = ?, atualizado_em = CURRENT_TIMESTAMP WHERE usuario_id = ?";

                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                            updateStmt.setLong(2, userId);

                            int affectedRows = updateStmt.executeUpdate();
                            if (affectedRows > 0) {
                                logger.info("Senha alterada com sucesso para usuário ID: {}", userId);
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao alterar senha para usuário ID: {}", userId, e);
        }
        return false;
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
