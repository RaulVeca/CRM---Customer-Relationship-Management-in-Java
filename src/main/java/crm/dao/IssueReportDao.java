package crm.dao;

import crm.model.entity.IssueReport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * SINGLETON + DAO PATTERN - JDBC access to the {@code issue_reports} table,
 * which records problems clients report from the portal.
 */
public class IssueReportDao extends AbstractDao<IssueReport> {

    private static volatile IssueReportDao instance;

    private IssueReportDao() {
    }

    public static IssueReportDao getInstance() {
        if (instance == null) {
            synchronized (IssueReportDao.class) {
                if (instance == null) {
                    instance = new IssueReportDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "issue_reports";
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO issue_reports (reporter_email, reporter_name, message, status) "
                + "VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE issue_reports SET reporter_email=?, reporter_name=?, message=?, status=? "
                + "WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, IssueReport r) throws SQLException {
        ps.setString(1, r.getReporterEmail());
        ps.setString(2, r.getReporterName());
        ps.setString(3, r.getMessage());
        ps.setString(4, r.getStatus());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, IssueReport r) throws SQLException {
        setInsertParameters(ps, r);
        ps.setLong(5, r.getId());
    }

    @Override
    protected void setEntityId(IssueReport entity, Long id) {
        entity.setId(id);
    }

    @Override
    protected Long getEntityId(IssueReport entity) {
        return entity.getId();
    }

    @Override
    protected IssueReport mapResultSetToEntity(ResultSet rs) throws SQLException {
        IssueReport r = new IssueReport();
        r.setId(rs.getLong("id"));
        r.setReporterEmail(rs.getString("reporter_email"));
        r.setReporterName(rs.getString("reporter_name"));
        r.setMessage(rs.getString("message"));
        r.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    }
}
