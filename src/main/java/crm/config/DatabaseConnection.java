package crm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * SINGLETON PATTERN - DatabaseConnection
 * 
 * Gestionează connection pool-ul către baza de date MySQL.
 * O singură instanță în toată aplicația pentru eficiență.
 * 
 * Folosește HikariCP - cel mai performant connection pool pentru JDBC.
 * Thread-safe prin double-checked locking pattern.
 */
public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private static volatile DatabaseConnection instance;
    private final HikariDataSource dataSource;

    private DatabaseConnection() {
        AppConfig config = AppConfig.getInstance();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getProperty("db.url"));
        hikariConfig.setUsername(config.getProperty("db.username"));
        hikariConfig.setPassword(config.getProperty("db.password"));
        hikariConfig.setDriverClassName(config.getProperty("db.driver"));

        // Pool settings
        hikariConfig.setMaximumPoolSize(config.getIntProperty("db.pool.maxSize", 20));
        hikariConfig.setMinimumIdle(config.getIntProperty("db.pool.minIdle", 5));
        hikariConfig.setConnectionTimeout(config.getIntProperty("db.pool.connectionTimeout", 30000));
        hikariConfig.setIdleTimeout(config.getIntProperty("db.pool.idleTimeout", 600000));
        hikariConfig.setMaxLifetime(config.getIntProperty("db.pool.maxLifetime", 1800000));

        // Performance optimizations
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        hikariConfig.setPoolName("CrmHikariPool");

        try {
            this.dataSource = new HikariDataSource(hikariConfig);
            logger.info("Connection pool initialized successfully (size: {})",
                hikariConfig.getMaximumPoolSize());
        } catch (Exception e) {
            logger.error("Error initializing the connection pool", e);
            throw new IllegalStateException("Nu pot conecta la baza de date", e);
        }
    }

    /**
     * Double-Checked Locking - thread-safe singleton
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Obține o conexiune din pool. NU închide conexiunea -
     * doar o returnează în pool prin close().
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Închide complet pool-ul. Apelat la oprirea aplicației.
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Connection pool closed");
        }
    }
}
