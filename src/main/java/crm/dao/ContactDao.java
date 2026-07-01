package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Contact;
import crm.model.enums.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SINGLETON + DAO PATTERN
 * 
 * ContactDao - acces JDBC la tabela contacts.
 * Extinde AbstractDao care implementează Template Method.
 */
public class ContactDao extends AbstractDao<Contact> {

    private static volatile ContactDao instance;

    private ContactDao() {
        // Constructor privat pentru Singleton
    }

    public static ContactDao getInstance() {
        if (instance == null) {
            synchronized (ContactDao.class) {
                if (instance == null) {
                    instance = new ContactDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "contacts";
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO contacts (contact_type, first_name, last_name, company_name, " +
               "fiscal_code, registration_number, industry, employee_count, email, phone, " +
               "birth_date, address_street, address_city, address_county, address_postal_code, " +
               "lead_source, lead_status, lead_score, experience_level, learning_goal, " +
               "first_contact_date, last_contact_date, assigned_to, gdpr_consent, " +
               "gdpr_consent_date, marketing_consent) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE contacts SET contact_type=?, first_name=?, last_name=?, company_name=?, " +
               "fiscal_code=?, registration_number=?, industry=?, employee_count=?, email=?, " +
               "phone=?, birth_date=?, address_street=?, address_city=?, address_county=?, " +
               "address_postal_code=?, lead_source=?, lead_status=?, lead_score=?, " +
               "experience_level=?, learning_goal=?, first_contact_date=?, last_contact_date=?, " +
               "assigned_to=?, gdpr_consent=?, gdpr_consent_date=?, marketing_consent=? " +
               "WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Contact c) throws SQLException {
        int i = 1;
        ps.setString(i++, c.getContactType() != null ? c.getContactType().name() : null);
        ps.setString(i++, c.getFirstName());
        ps.setString(i++, c.getLastName());
        ps.setString(i++, c.getCompanyName());
        ps.setString(i++, c.getFiscalCode());
        ps.setString(i++, c.getRegistrationNumber());
        ps.setString(i++, c.getIndustry());
        setIntOrNull(ps, i++, c.getEmployeeCount());
        ps.setString(i++, c.getEmail());
        ps.setString(i++, c.getPhone());
        setDateOrNull(ps, i++, c.getBirthDate());
        ps.setString(i++, c.getAddressStreet());
        ps.setString(i++, c.getAddressCity());
        ps.setString(i++, c.getAddressCounty());
        ps.setString(i++, c.getAddressPostalCode());
        ps.setString(i++, c.getLeadSource() != null ? c.getLeadSource().name() : null);
        ps.setString(i++, c.getLeadStatus() != null ? c.getLeadStatus().name() : LeadStatus.NEW.name());
        setIntOrNull(ps, i++, c.getLeadScore() != null ? c.getLeadScore() : 0);
        ps.setString(i++, c.getExperienceLevel() != null ? c.getExperienceLevel().name() : null);
        ps.setString(i++, c.getLearningGoal());
        setDateTimeOrNull(ps, i++, c.getFirstContactDate());
        setDateTimeOrNull(ps, i++, c.getLastContactDate());
        setLongOrNull(ps, i++, c.getAssignedTo());
        ps.setBoolean(i++, Boolean.TRUE.equals(c.getGdprConsent()));
        setDateTimeOrNull(ps, i++, c.getGdprConsentDate());
        ps.setBoolean(i, Boolean.TRUE.equals(c.getMarketingConsent()));
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Contact c) throws SQLException {
        setInsertParameters(ps, c);
        ps.setLong(27, c.getId());  // Index pentru WHERE id=?
    }

    @Override
    protected void setEntityId(Contact entity, Long id) {
        entity.setId(id);
    }

    @Override
    protected Long getEntityId(Contact entity) {
        return entity.getId();
    }

    @Override
    protected Contact mapResultSetToEntity(ResultSet rs) throws SQLException {
        Contact c = new Contact();
        c.setId(rs.getLong("id"));
        c.setContactType(parseEnum(rs.getString("contact_type"), ContactType.class));
        c.setFirstName(rs.getString("first_name"));
        c.setLastName(rs.getString("last_name"));
        c.setCompanyName(rs.getString("company_name"));
        c.setFiscalCode(rs.getString("fiscal_code"));
        c.setRegistrationNumber(rs.getString("registration_number"));
        c.setIndustry(rs.getString("industry"));
        c.setEmployeeCount(getIntOrNull(rs, "employee_count"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setBirthDate(getLocalDate(rs, "birth_date"));
        c.setAddressStreet(rs.getString("address_street"));
        c.setAddressCity(rs.getString("address_city"));
        c.setAddressCounty(rs.getString("address_county"));
        c.setAddressPostalCode(rs.getString("address_postal_code"));
        c.setLeadSource(parseEnum(rs.getString("lead_source"), LeadSource.class));
        c.setLeadStatus(parseEnum(rs.getString("lead_status"), LeadStatus.class));
        c.setLeadScore(rs.getInt("lead_score"));
        c.setExperienceLevel(parseEnum(rs.getString("experience_level"), ExperienceLevel.class));
        c.setLearningGoal(rs.getString("learning_goal"));
        c.setFirstContactDate(getLocalDateTime(rs, "first_contact_date"));
        c.setLastContactDate(getLocalDateTime(rs, "last_contact_date"));
        c.setAssignedTo(getLongOrNull(rs, "assigned_to"));
        c.setGdprConsent(rs.getBoolean("gdpr_consent"));
        c.setGdprConsentDate(getLocalDateTime(rs, "gdpr_consent_date"));
        c.setMarketingConsent(rs.getBoolean("marketing_consent"));
        c.setCreatedAt(getLocalDateTime(rs, "created_at"));
        c.setUpdatedAt(getLocalDateTime(rs, "updated_at"));
        return c;
    }

    // =====================================================
    // Custom queries
    // =====================================================

    /**
     * Override paginat al listării: persoanele individuale (B2C) apar înaintea
     * companiilor (B2B). Doar ordinea de afișare se schimbă - valorile
     * atributelor rămân neatinse.
     */
    @Override
    public List<Contact> findAll(int offset, int limit) {
        String sql = "SELECT * FROM contacts " +
                "ORDER BY CASE WHEN contact_type = 'INDIVIDUAL' THEN 0 ELSE 1 END, id DESC " +
                "LIMIT ? OFFSET ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);
            return mapResults(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findAll ordonat (contacts)", e);
        }
    }

    /**
     * Caută un contact după email, indiferent de cum a fost scris (majuscule /
     * minuscule). Comparația case-insensitive este esențială pentru unicitatea
     * emailului: la înregistrare un email deja existent trebuie găsit chiar dacă
     * a fost salvat cu altă capitalizare, iar la login utilizatorul se poate
     * autentifica indiferent cum tastează adresa. Aliniat cu AdminDao/EmployeeDao.
     */
    public Optional<Contact> findByEmail(String email) {
        String sql = "SELECT * FROM contacts WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToEntity(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findByEmail", e);
        }
    }

    /**
     * The password stored for the contact with this email, or {@code null} if no
     * such contact exists. Used to verify the password at sign-in. Matched
     * case-insensitively on the email, like {@link #findByEmail(String)}.
     */
    public String findPasswordByEmail(String email) {
        String sql = "SELECT password FROM contacts WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("password") : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findPasswordByEmail (contacts)", e);
        }
    }

    /**
     * Sets the password for a contact. Used at registration to store the password
     * the visitor chose, right after the contact row itself is created.
     */
    public void updatePassword(Long contactId, String password) {
        String sql = "UPDATE contacts SET password = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, password);
            ps.setLong(2, contactId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Eroare updatePassword (contacts)", e);
        }
    }

    public List<Contact> findByLeadStatus(LeadStatus status) {
        String sql = "SELECT * FROM contacts WHERE lead_status = ? ORDER BY created_at DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            return mapResults(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findByLeadStatus", e);
        }
    }

    public List<Contact> findHotLeads(int minScore, int limit) {
        String sql = "SELECT * FROM contacts WHERE lead_score >= ? " +
                     "ORDER BY lead_score DESC LIMIT ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, minScore);
            ps.setInt(2, limit);
            return mapResults(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findHotLeads", e);
        }
    }

    public List<Contact> findByAssignedTo(Long userId) {
        String sql = "SELECT * FROM contacts WHERE assigned_to = ? ORDER BY lead_score DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            return mapResults(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findByAssignedTo", e);
        }
    }

    public List<Contact> search(String searchTerm, int offset, int limit) {
        String sql = "SELECT * FROM contacts WHERE " +
                "LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR " +
                "LOWER(email) LIKE ? OR LOWER(company_name) LIKE ? OR " +
                "LOWER(phone) LIKE ? " +
                "ORDER BY id DESC LIMIT ? OFFSET ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + searchTerm.toLowerCase() + "%";
            for (int i = 1; i <= 5; i++) ps.setString(i, pattern);
            ps.setInt(6, limit);
            ps.setInt(7, offset);
            return mapResults(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Eroare search", e);
        }
    }

    public List<Contact> findStaleLeads(LocalDateTime threshold) {
        String sql = "SELECT * FROM contacts WHERE last_contact_date < ? " +
                "AND lead_status IN ('CONTACTED', 'INTERESTED')";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(threshold));
            return mapResults(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findStaleLeads", e);
        }
    }

    public long countByLeadStatus(LeadStatus status) {
        String sql = "SELECT COUNT(*) FROM contacts WHERE lead_status = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare countByLeadStatus", e);
        }
    }

    // =====================================================
    // Helper methods
    // =====================================================

    private List<Contact> mapResults(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<Contact> list = new ArrayList<>();
            while (rs.next()) list.add(mapResultSetToEntity(rs));
            return list;
        }
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, val);
    }

    private void setLongOrNull(PreparedStatement ps, int idx, Long val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.BIGINT);
        else ps.setLong(idx, val);
    }

    private void setDateOrNull(PreparedStatement ps, int idx, LocalDate val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.DATE);
        else ps.setDate(idx, Date.valueOf(val));
    }

    private void setDateTimeOrNull(PreparedStatement ps, int idx, LocalDateTime val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.TIMESTAMP);
        else ps.setTimestamp(idx, Timestamp.valueOf(val));
    }

    private Integer getIntOrNull(ResultSet rs, String col) throws SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : val;
    }

    private Long getLongOrNull(ResultSet rs, String col) throws SQLException {
        long val = rs.getLong(col);
        return rs.wasNull() ? null : val;
    }

    private LocalDate getLocalDate(ResultSet rs, String col) throws SQLException {
        Date d = rs.getDate(col);
        return d == null ? null : d.toLocalDate();
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String col) throws SQLException {
        Timestamp ts = rs.getTimestamp(col);
        return ts == null ? null : ts.toLocalDateTime();
    }
}
