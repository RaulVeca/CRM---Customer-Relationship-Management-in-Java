package crm.validation;

import crm.model.entity.Contact;

import java.util.List;
import java.util.regex.Pattern;

public class PhoneValidator extends Validator<Contact> {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9\\s().-]{7,20}$"
    );

    @Override
    protected void doValidate(Contact contact, List<String> errors) {
        String phone = contact.getPhone();
        if (phone == null || phone.trim().isEmpty()) {
            return;  // Telefon este opțional
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.add("Invalid phone format: " + phone);
        }
    }
}
