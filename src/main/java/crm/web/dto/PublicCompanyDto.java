package crm.web.dto;

import crm.model.entity.Contact;

/**
 * Public projection of a corporate client showing only its business activity -
 * the information a prospective individual visitor or investor may browse.
 */
public record PublicCompanyDto(
        Long id,
        String companyName,
        String industry,
        Integer employeeCount
) {
    public static PublicCompanyDto from(Contact c) {
        return new PublicCompanyDto(
                c.getId(),
                c.getCompanyName(),
                c.getIndustry(),
                c.getEmployeeCount()
        );
    }
}
