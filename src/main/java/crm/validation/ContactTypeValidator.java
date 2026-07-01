package crm.validation;

import crm.model.entity.Contact;
import crm.model.enums.ContactType;

import java.util.List;

public class ContactTypeValidator extends Validator<Contact> {

    @Override
    protected void doValidate(Contact contact, List<String> errors) {
        if (contact.getContactType() == null) {
            errors.add("The contact type (INDIVIDUAL/CORPORATE) is required");
            return;
        }

        if (contact.getContactType() == ContactType.INDIVIDUAL) {
            boolean hasName = (contact.getFirstName() != null && !contact.getFirstName().isEmpty()) 
                    || (contact.getLastName() != null && !contact.getLastName().isEmpty());
            if (!hasName) {
                errors.add("The first or last name is required for individuals");
            }
        } else if (contact.getContactType() == ContactType.CORPORATE) {
            if (contact.getCompanyName() == null || contact.getCompanyName().isEmpty()) {
                errors.add("The company name is required for corporate contacts");
            }
        }
    }
}
