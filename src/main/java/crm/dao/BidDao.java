package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Bid;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SINGLETON + DAO PATTERN - JDBC access to the {@code bids} table.
 */
public class BidDao extends AbstractDao<Bid> {

    private static volatile BidDao instance;

    private BidDao() {
    }

    public static BidDao getInstance() {
        if (instance == null) {
            synchronized (BidDao.class) {
                if (instance == null) {
                    instance = new BidDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "bids";
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO bids (auction_id, company_id, company_name, amount) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE bids SET auction_id=?, company_id=?, company_name=?, amount=? WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Bid b) throws SQLException {
        int i = 1;
        setLongOrNull(ps, i++, b.getAuctionId());
        setLongOrNull(ps, i++, b.getCompanyId());
        ps.setString(i++, b.getCompanyName());
        ps.setBigDecimal(i, b.getAmount());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Bid b) throws SQLException {
        setInsertParameters(ps, b);
        ps.setLong(5, b.getId());
    }

    @Override
    protected void setEntityId(Bid entity, Long id) {
        entity.setId(id);
    }

    @Override
    protected Long getEntityId(Bid entity) {
        return entity.getId();
    }

    @Override
    protected Bid mapResultSetToEntity(ResultSet rs) throws SQLException {
        Bid b = new Bid();
        b.setId(rs.getLong("id"));
        b.setAuctionId(getLongOrNull(rs, "auction_id"));
        b.setCompanyId(getLongOrNull(rs, "company_id"));
        b.setCompanyName(rs.getString("company_name"));
        b.setAmount(rs.getBigDecimal("amount"));
        b.setCreatedAt(getLocalDateTime(rs, "created_at"));
        b.setUpdatedAt(getLocalDateTime(rs, "updated_at"));
        return b;
    }

    public List<Bid> findByAuctionId(Long auctionId) {
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY amount DESC, id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Bid> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findByAuctionId", e);
        }
    }

    public Optional<Bid> findHighestBid(Long auctionId) {
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY amount DESC, id ASC LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToEntity(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findHighestBid", e);
        }
    }

    // Helpers
    private void setLongOrNull(PreparedStatement ps, int idx, Long val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.BIGINT);
        else ps.setLong(idx, val);
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
