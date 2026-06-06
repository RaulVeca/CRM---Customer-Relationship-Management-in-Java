package crm.validation;

import crm.exception.ValidationException;
import crm.model.entity.Contact;

import java.util.List;

/**
 * Helper pentru construcția și execuția lanțului de validare Contact.
 * Combinând Chain of Responsibility cu Facade.
 */
public class ContactValidatorChain {

    private final Validator<Contact> firstValidator;

    public ContactValidatorChain() {
        // Construim lanțul: Email -> Phone -> ContactType -> Gdpr
        this.firstValidator = new EmailValidator();
        Validator<Contact> phone = new PhoneValidator();
        Validator<Contact> contactType = new ContactTypeValidator();
        Validator<Contact> gdpr = new GdprValidator();

        firstValidator.setNext(phone);
        phone.setNext(contactType);
        contactType.setNext(gdpr);
    }

    /**
     * Validează contactul. Aruncă ValidationException dacă există erori.
     */
    public void validateAndThrow(Contact contact) {
        List<String> errors = firstValidator.validate(contact);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /**
     * Validează și returnează lista de erori (nu aruncă excepție).
     */
    public List<String> validate(Contact contact) {
        return firstValidator.validate(contact);
    }
}
