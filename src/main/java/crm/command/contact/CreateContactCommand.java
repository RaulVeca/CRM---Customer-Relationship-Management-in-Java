package crm.command.contact;

import crm.command.AbstractCommand;
import crm.model.entity.Contact;
import crm.service.contact.ContactService;

/**
 * COMMAND PATTERN - CreateContactCommand
 * 
 * Încapsulează operația de creare contact ca obiect.
 * Poate fi pus în coadă, logat, sau rulat tranzacțional.
 */
public class CreateContactCommand extends AbstractCommand<Contact> {

    private final Contact contact;

    public CreateContactCommand(Contact contact) {
        this.contact = contact;
    }

    @Override
    protected Contact doExecute() {
        return ContactService.getInstance().createContact(contact);
    }
}
