package crm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * SINGLETON PATTERN - AppConfig
 * 
 * Clasă singleton care încarcă și expune proprietățile aplicației.
 * Garantează existența unei singure instanțe în întreaga aplicație.
 * 
 * Implementare thread-safe folosind "Bill Pugh Singleton Pattern"
 * cu Initialization-on-demand holder idiom.
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final String CONFIG_FILE = "application.properties";

    private final Properties properties;

    // Constructor privat - împiedică instanțierea externă
    private AppConfig() {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Bill Pugh Singleton - thread-safe fără sincronizare explicită
     * Inner class este încărcată doar când e accesată prima dată
     */
    private static class SingletonHolder {
        private static final AppConfig INSTANCE = new AppConfig();
    }

    public static AppConfig getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private void loadDefaultProperties() {
        // Valori default pentru dezvoltare
        properties.setProperty("app.name", "CRM Training IT");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("app.environment", "development");


        properties.setProperty("db.driver", "org.mariadb.jdbc.Driver");
        properties.setProperty("db.url", "jdbc:mariadb://localhost:3306/crm_training?createDatabaseIfNotExist=true");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
        properties.setProperty("db.init.schema", "true");

        // HikariCP
        properties.setProperty("db.pool.maximumPoolSize", "10");
        properties.setProperty("db.pool.minimumIdle", "2");
        properties.setProperty("db.pool.connectionTimeout", "30000");
        properties.setProperty("db.pool.idleTimeout", "600000");
        properties.setProperty("db.pool.maxLifetime", "1800000");

        logger.info("Default values loaded");
    }
    private void loadProperties() {
        // 1. Încarcă valori default
        loadDefaultProperties();

        // 2. Încearcă să suprascrie cu fișierul (dacă există)
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.warn("File not found, using default values");
                return; // NU mai aruncă excepție!
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("Missing property: {}", key);
        }
        return value;
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid value for {}: {}", key, value);
            return defaultValue;
        }
    }
}
