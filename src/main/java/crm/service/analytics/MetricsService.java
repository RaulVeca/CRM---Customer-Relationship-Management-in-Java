package crm.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.config.DatabaseConnection;
import crm.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SINGLETON - MetricsService.
 *
 * <p>Lightweight click-stream counters for the public course catalog, backing
 * the Click-Through Rate metric. Each course row keeps a running count of
 * impressions (the card was shown) and clicks (the visitor opened it). Stored in
 * the {@code course_metrics} table via idempotent upserts.</p>
 */
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    private static volatile MetricsService instance;

    private final DatabaseConnection db;

    private MetricsService() {
        this.db = DatabaseConnection.getInstance();
    }

    public static MetricsService getInstance() {
        if (instance == null) {
            synchronized (MetricsService.class) {
                if (instance == null) {
                    instance = new MetricsService();
                }
            }
        }
        return instance;
    }

    /** Records one impression for each course currently shown in the catalog. */
    public void recordImpressions(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) return;
        String sql = "INSERT INTO course_metrics (course_id, impressions, clicks) VALUES (?, 1, 0) "
                + "ON DUPLICATE KEY UPDATE impressions = impressions + 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Long id : courseIds) {
                if (id == null) continue;
                ps.setLong(1, id);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Error in recordImpressions", e);
        }
    }

    /** Records one click-through for a course. */
    public void recordClick(Long courseId) {
        if (courseId == null) return;
        String sql = "INSERT INTO course_metrics (course_id, impressions, clicks) VALUES (?, 0, 1) "
                + "ON DUPLICATE KEY UPDATE clicks = clicks + 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error in recordClick", e);
        }
    }

    /** courseId -&gt; [impressions, clicks] for every tracked course. */
    public Map<Long, long[]> getAllCounts() {
        Map<Long, long[]> counts = new HashMap<>();
        String sql = "SELECT course_id, impressions, clicks FROM course_metrics";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                counts.put(rs.getLong("course_id"),
                        new long[]{rs.getLong("impressions"), rs.getLong("clicks")});
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error in getAllCounts", e);
        }
        logger.debug("Loaded CTR counts for {} courses", counts.size());
        return counts;
    }
}
