package crm.web.config;

import crm.config.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
        """,
        """
        CREATE TABLE IF NOT EXISTS course_metrics (
            course_id BIGINT PRIMARY KEY,
            impressions BIGINT NOT NULL DEFAULT 0,
            clicks BIGINT NOT NULL DEFAULT 0,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS admins (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            first_name VARCHAR(100) NOT NULL,
            last_name VARCHAR(100) NOT NULL,
            email VARCHAR(255) NOT NULL UNIQUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS trainers (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            first_name VARCHAR(100) NOT NULL,
            last_name VARCHAR(100) NOT NULL,
            email VARCHAR(255) NOT NULL UNIQUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS meditation_sessions (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            trainer_id BIGINT NOT NULL,
            contact_id BIGINT NOT NULL,
            contact_email VARCHAR(255),
            session_date DATE NOT NULL,
            start_hour INT NOT NULL,
            end_hour INT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_med_sessions_trainer_date (trainer_id, session_date)
        )
        """
    };

    /**
     * The trainers that may sign in through the "Cont admin" option, using their
     * {@code @adminit.ro} sign-in addresses. Seeded idempotently
     * ({@code INSERT IGNORE} on the unique email) so a restart never duplicates
     * them.
     */
    private static final String[][] ADMIN_SEED = {
        {"Andrei",   "Birceanu",   "andreibirceanu@adminit.ro"},
        {"Costache", "Măzărescu",  "costachemazarescu@adminit.ro"},
        {"Oana",     "Badache",    "oanabadache@adminit.ro"},
    };

    /**
     * The trainers that deliver the courses. Seeded idempotently
     * ({@code INSERT IGNORE} on the unique email) so a restart never duplicates
     * them.
     */
    private static final String[][] TRAINER_SEED = {
        {"Andrei",  "Birceanu",   "andreibirceanu@trainerit.ro"},
        {"Sorin",   "Dima",       "sorindima@trainerit.ro"},
        {"Claudiu", "Antonescu",  "claudiuantonescu@trainerit.ro"},
    };

    private WebSchemaInitializer() {
    }

    public static void ensureTables() {
        DatabaseConnection db = DatabaseConnection.getInstance();
        try (Connection conn = db.getConnection(); Statement st = conn.createStatement()) {
            for (String ddl : DDL) {
                st.execute(ddl);
            }
            seedAdmins(conn);
            seedTrainers(conn);
            logger.info("Web feature tables ensured (employees, auctions, bids, admins, trainers, meditation_sessions)");
        } catch (SQLException e) {
            logger.error("Failed to ensure web feature tables", e);
            throw new IllegalStateException("Could not initialize web feature schema", e);
        }
    }

    private static void seedAdmins(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO admins (first_name, last_name, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] admin : ADMIN_SEED) {
                ps.setString(1, admin[0]);
                ps.setString(2, admin[1]);
                ps.setString(3, admin[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedTrainers(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO trainers (first_name, last_name, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] trainer : TRAINER_SEED) {
                ps.setString(1, trainer[0]);
                ps.setString(2, trainer[1]);
                ps.setString(3, trainer[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
