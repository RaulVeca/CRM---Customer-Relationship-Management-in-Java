package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Auction;
import crm.model.enums.AuctionStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SINGLETON + DAO PATTERN - JDBC access to the {@code auctions} table.
 */
public class AuctionDao extends AbstractDao<Auction> {

    private static volatile AuctionDao instance;

    private AuctionDao() {
    }

    public static AuctionDao getInstance() {
        if (instance == null) {
            synchronized (AuctionDao.class) {
                if (instance == null) {
                    instance = new AuctionDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "auctions";
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO auctions (course_id, title, description, starting_price, status, " +
               "closes_at, winner_company_id, winning_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE auctions SET course_id=?, title=?, description=?, starting_price=?, " +
               "status=?, closes_at=?, winner_company_id=?, winning_amount=? WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Auction a) throws SQLException {
        int i = 1;
        setLongOrNull(ps, i++, a.getCourseId());
        ps.setString(i++, a.getTitle());
        ps.setString(i++, a.getDescription());
        ps.setBigDecimal(i++, a.getStartingPrice());
        ps.setString(i++, a.getStatus() != null ? a.getStatus().name() : AuctionStatus.OPEN.name());
        setTimestampOrNull(ps, i++, a.getClosesAt());
        setLongOrNull(ps, i++, a.getWinnerCompanyId());
        ps.setBigDecimal(i, a.getWinningAmount());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Auction a) throws SQLException {
        setInsertParameters(ps, a);
        ps.setLong(9, a.getId());
    }

    @Override
    protected void setEntityId(Auction entity, Long id) {
        entity.setId(id);
    }

    @Override
    protected Long getEntityId(Auction entity) {
        return entity.getId();
    }

    @Override
    protected Auction mapResultSetToEntity(ResultSet rs) throws SQLException {
        Auction a = new Auction();
        a.setId(rs.getLong("id"));
        a.setCourseId(getLongOrNull(rs, "course_id"));
        a.setTitle(rs.getString("title"));
        a.setDescription(rs.getString("description"));
        a.setStartingPrice(rs.getBigDecimal("starting_price"));
        a.setStatus(parseEnum(rs.getString("status"), AuctionStatus.class));
        a.setClosesAt(getLocalDateTime(rs, "closes_at"));
        a.setWinnerCompanyId(getLongOrNull(rs, "winner_company_id"));
        a.setWinningAmount(rs.getBigDecimal("winning_amount"));
        a.setCreatedAt(getLocalDateTime(rs, "created_at"));
        a.setUpdatedAt(getLocalDateTime(rs, "updated_at"));
        return a;
    }

    public List<Auction> findByStatus(AuctionStatus status) {
        String sql = "SELECT * FROM auctions WHERE status = ? ORDER BY id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<Auction> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findByStatus", e);
        }
    }

    // Helpers
    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void setLongOrNull(PreparedStatement ps, int idx, Long val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.BIGINT);
        else ps.setLong(idx, val);
    }

    private void setTimestampOrNull(PreparedStatement ps, int idx, java.time.LocalDateTime val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.TIMESTAMP);
        else ps.setTimestamp(idx, Timestamp.valueOf(val));
    }

    private Long getLongOrNull(ResultSet rs, String col) throws SQLException {
        long val = rs.getLong(col);
        return rs.wasNull() ? null : val;
    }

    private java.time.LocalDateTime getLocalDateTime(ResultSet rs, String col) throws SQLException {
        Timestamp ts = rs.getTimestamp(col);
        return ts == null ? null : ts.toLocalDateTime();
    }
}
