package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Contact;
import crm.model.enums.LeadStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for contacts (B2C leads and B2B corporate clients).
 * Delegates every operation to the existing {@link CrmFacade}.
 */
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final CrmFacade facade;

    public ContactController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public List<Contact> list(@RequestParam(required = false) String q,
                              @RequestParam(required = false) LeadStatus status,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        if (status != null) {
            return facade.listContactsByStatus(status);
        }
        if (q != null && !q.isBlank()) {
            return facade.searchContacts(q, page, size);
        }
        return facade.getAllContacts(page, size);
    }

    @GetMapping("/hot")
    public List<Contact> hotLeads(@RequestParam(defaultValue = "10") int limit) {
        return facade.getHotLeads(limit);
    }

    @GetMapping("/{id}")
    public Contact getById(@PathVariable Long id) {
        return facade.getContact(id);
    }

    @GetMapping("/by-email")
    public ResponseEntity<Contact> byEmail(@RequestParam String email) {
        return facade.findContactByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contact create(@RequestBody Contact contact) {
        return facade.createContact(contact);
    }

    @PutMapping("/{id}")
    public Contact update(@PathVariable Long id, @RequestBody Contact contact) {
        contact.setId(id);
        return facade.updateContact(contact);
    }

    @PatchMapping("/{id}/lead-status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam LeadStatus status) {
        facade.changeLeadStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = facade.deleteContact(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
