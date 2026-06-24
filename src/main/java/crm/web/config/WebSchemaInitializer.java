package crm.web.config;

import crm.config.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the tables introduced by the web/AI features (employees, opportunity
 * bids) if they do not already exist.
 *
 * <p>The legacy {@code schema.sql} is out of sync with the live database, so the
 * new feature tables are provisioned here with idempotent {@code CREATE TABLE IF
 * NOT EXISTS} statements instead. Invoked once at startup, after the connection
 * pool is ready.</p>
 */
public final class WebSchemaInitializer {

    private static final Logger logger = LoggerFactory.getLogger(WebSchemaInitializer.class);

    private static final String[] DDL = {
        """
        CREATE TABLE IF NOT EXISTS employees (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            company_id BIGINT NOT NULL,
            first_name VARCHAR(100),
            last_name VARCHAR(100),
            email VARCHAR(255),
            job_title VARCHAR(150),
            work_profile VARCHAR(50),
            interest_profiles VARCHAR(500),
            experience_level VARCHAR(50),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            INDEX idx_employees_company (company_id)
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS auctions (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            course_id BIGINT,
            title VARCHAR(255) NOT NULL,
            description TEXT,
            starting_price DECIMAL(12,2) NOT NULL DEFAULT 0,
            status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
            closes_at TIMESTAMP NULL,
            winner_company_id BIGINT,
            winning_amount DECIMAL(12,2),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            INDEX idx_auctions_status (status)
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS bids (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            auction_id BIGINT NOT NULL,
            company_id BIGINT NOT NULL,
            company_name VARCHAR(255),
            amount DECIMAL(12,2) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            INDEX idx_bids_auction (auction_id)
        )
        """
    };

    private WebSchemaInitializer() {
    }

    public static void ensureTables() {
        DatabaseConnection db = DatabaseConnection.getInstance();
        try (Connection conn = db.getConnection(); Statement st = conn.createStatement()) {
            for (String ddl : DDL) {
                st.execute(ddl);
            }
            logger.info("Web feature tables ensured (employees, auctions, bids)");
        } catch (SQLException e) {
            logger.error("Failed to ensure web feature tables", e);
            throw new IllegalStateException("Could not initialize web feature schema", e);
        }
    }
}
