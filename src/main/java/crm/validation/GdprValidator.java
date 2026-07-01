package crm.validation;

import crm.model.entity.Contact;

import java.util.List;

/**
 * Validator GDPR - verifică consimțământul.
 */
public class GdprValidator extends Validator<Contact> {

    @Override
    protected void doValidate(Contact contact, List<String> errors) {
        if (Boolean.TRUE.equals(contact.getGdprConsent())) {
            if (contact.getGdprConsent()==Boolean.FALSE) {
                errors.add("The GDPR consent date is missing");
            }
        }
    }
}
