package crm.validation;

import crm.model.entity.Contact;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator pentru email Contact.
 */
public class EmailValidator extends Validator<Contact> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$"
    );

    @Override
    protected void doValidate(Contact contact, List<String> errors) {
        String email = contact.getEmail();
        if (email == null || email.trim().isEmpty()) {
            errors.add("Email-ul este obligatoriu");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Format email invalid: " + email);
        }
        if (email.length() > 100) {
            errors.add("Email-ul depășește 100 caractere");
        }
    }
}
