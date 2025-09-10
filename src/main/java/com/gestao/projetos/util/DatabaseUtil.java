package com.gestao.projetos.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Classe responsável por gerenciar as conexões com o banco de dados
 * usando pool de conexões HikariCP
 */
public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static HikariDataSource dataSource;
    private static final String CONFIG_FILE = "database.properties";
    private static boolean initialized = false;
    private static Exception initializationError = null;

    // Removed static block - initialization is now lazy

    /**
     * Ensures the database is initialized. Should be called before any database operation.
     */
    private static void ensureInitialized() {
        if (!initialized) {
            try {
                initializeDataSource();
                initialized = true;
                initializationError = null;
            } catch (Exception e) {
                logger.error("Erro ao inicializar o pool de conexões", e);
                initializationError = e;
                // Don't throw - let the application continue
            }
        }
    }

    /**
     * Inicializa o pool de conexões com base no arquivo de configuração
     */
    private static void initializeDataSource() throws IOException {
        Properties props = loadDatabaseProperties();
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver"));
        
        // Configurações do pool
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.min", "5")));
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.max", "20")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.timeout", "30000")));
        
        // Configurações adicionais de performance e segurança
        config.setIdleTimeout(600000); // 10 minutos
        config.setMaxLifetime(1800000); // 30 minutos
        config.setLeakDetectionThreshold(60000); // 1 minuto
        
        // Configurações de validação
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        dataSource = new HikariDataSource(config);
        
        logger.info("Pool de conexões inicializado com sucesso");
        logger.info("URL: {}", props.getProperty("db.url"));
        logger.info("Pool mínimo: {}, Pool máximo: {}", 
                   config.getMinimumIdle(), config.getMaximumPoolSize());
    }

    /**
     * Carrega as propriedades do banco de dados do arquivo de configuração
     */
    private static Properties loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        
        try (InputStream input = DatabaseUtil.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            
            if (input == null) {
                throw new IOException("Arquivo de configuração não encontrado: " + CONFIG_FILE);
            }
            
            props.load(input);
            
            // Validação das propriedades obrigatórias
            String[] requiredProps = {"db.url", "db.username", "db.driver"};
            for (String prop : requiredProps) {
                if (props.getProperty(prop) == null || props.getProperty(prop).trim().isEmpty()) {
                    throw new IOException("Propriedade obrigatória não encontrada: " + prop);
                }
            }
            
        }
        
        return props;
    }

    /**
     * Obtém uma conexão do pool
     * 
     * @return Conexão com o banco de dados
     * @throws SQLException se não conseguir obter a conexão
     */
    public static Connection getConnection() throws SQLException {
        ensureInitialized();
        
        if (dataSource == null || initializationError != null) {
            throw new SQLException("Pool de conexões não foi inicializado: " + 
                (initializationError != null ? initializationError.getMessage() : "Erro desconhecido"));
        }
        
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false); // Usar transações manuais por padrão
        
        return connection;
    }

    /**
     * Fecha o pool de conexões
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Pool de conexões fechado");
        }
    }

    /**
     * Executa commit na transação
     */
    public static void commit(Connection connection) {
        if (connection != null) {
            try {
                connection.commit();
            } catch (SQLException e) {
                logger.error("Erro ao fazer commit da transação", e);
            }
        }
    }

    /**
     * Executa rollback na transação
     */
    public static void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Erro ao fazer rollback da transação", e);
            }
        }
    }

    /**
     * Fecha a conexão de forma segura
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback(); // Rollback se há transação pendente
                }
                connection.close();
            } catch (SQLException e) {
                logger.error("Erro ao fechar conexão", e);
            }
        }
    }

    /**
     * Obtém informações sobre o status do pool de conexões
     */
    public static String getPoolStatus() {
        ensureInitialized();
        
        if (dataSource == null || initializationError != null) {
            return "Pool não inicializado: " + 
                (initializationError != null ? initializationError.getMessage() : "Erro desconhecido");
        }
        
        return String.format(
            "Pool Status - Ativas: %d, Idle: %d, Total: %d, Aguardando: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }

    /**
     * Testa a conectividade com o banco de dados
     */
    public static boolean testConnection() {
        ensureInitialized();
        
        if (dataSource == null || initializationError != null) {
            logger.warn("Não foi possível testar conexão: " + 
                (initializationError != null ? initializationError.getMessage() : "Pool não inicializado"));
            return false;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            logger.error("Erro ao testar conexão", e);
            return false;
        }
    }
}
