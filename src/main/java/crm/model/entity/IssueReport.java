package crm.model.entity;

import lombok.*;

/**
 * A problem reported by a client from the portal's "Report an issue" window.
 * Backed by the {@code issue_reports} table and surfaced to admins in the admin
 * portal's Issues view.
 *
 * <p>The reporter is identified only by the email/name of their signed-in
 * session (the site has no per-request auth), and every report starts life with
 * status {@code OPEN}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class IssueReport extends BaseEntity {

    private String reporterEmail;
    private String reporterName;
    private String message;
    private String status;
}
