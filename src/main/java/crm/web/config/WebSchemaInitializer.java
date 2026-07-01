package crm.web.config;

import crm.config.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the tables introduced by the web/AI features (employees, course
 * metrics, admins, trainers, meditation sessions) if they do not already exist.
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
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            INDEX idx_employees_company (company_id)
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
            relaxEnrollmentPrice(conn);
            ensureEmployeeColumns(conn);
            ensurePasswordColumns(conn);
            seedAdmins(conn);
            seedTrainers(conn);
            logger.info("Web feature tables ensured (employees, course_metrics, admins, trainers, meditation_sessions)");
        } catch (SQLException e) {
            logger.error("Failed to ensure web feature tables", e);
            throw new IllegalStateException("Could not initialize web feature schema", e);
        }
    }

    /**
     * Course pricing was removed, so the application no longer writes the legacy
     * {@code enrollments.price} column (which is {@code NOT NULL} on older
     * databases). Relax it to nullable so inserts that omit it succeed. Idempotent
     * and best-effort: a failure here must not stop startup.
     */
    private static void relaxEnrollmentPrice(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE enrollments MODIFY COLUMN price DECIMAL(10,2) NULL DEFAULT NULL");
        } catch (SQLException e) {
            logger.warn("Could not relax enrollments.price (already nullable or table absent): {}", e.getMessage());
        }
    }

    /**
     * Brings the live {@code employees} table in line with the current model:
     * an {@code email} column is added if it is missing (older databases were
     * created before employees carried an email), and the obsolete
     * {@code experience_level} column is dropped if it is still present. Both
     * steps are idempotent and best-effort — an "already exists" / "does not
     * exist" error on a database that is already in the target shape is expected
     * and must not stop startup.
     */
    private static void ensureEmployeeColumns(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE employees ADD COLUMN email VARCHAR(255)");
            logger.info("Added email column to employees");
        } catch (SQLException e) {
            logger.debug("employees.email already present or table absent: {}", e.getMessage());
        }
        try (Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE employees DROP COLUMN experience_level");
            logger.info("Dropped obsolete experience_level column from employees");
        } catch (SQLException e) {
            logger.debug("employees.experience_level already absent or table missing: {}", e.getMessage());
        }
    }

    /**
     * Every account a visitor can sign in with now needs a password. The
     * {@code contacts}, {@code admins} and {@code employees} tables each get a
     * {@code password} column, {@code NOT NULL DEFAULT '1234'}, so every account
     * that already exists is given the shared default password {@code 1234} and
     * can keep signing in. Idempotent and best-effort: a duplicate-column error on
     * a database that already has the column is expected and must not stop startup.
     */
    private static void ensurePasswordColumns(Connection conn) {
        for (String table : new String[] {"contacts", "admins", "employees"}) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE " + table
                        + " ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '1234'");
                logger.info("Added password column to {} (existing accounts default to '1234')", table);
            } catch (SQLException e) {
                logger.debug("Password column on {} already present or table absent: {}",
                        table, e.getMessage());
            }
            // Backfill any pre-existing NULL/empty passwords with the default.
            try (Statement st = conn.createStatement()) {
                st.execute("UPDATE " + table + " SET password = '1234' "
                        + "WHERE password IS NULL OR password = ''");
            } catch (SQLException e) {
                logger.debug("Could not backfill passwords on {}: {}", table, e.getMessage());
            }
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
