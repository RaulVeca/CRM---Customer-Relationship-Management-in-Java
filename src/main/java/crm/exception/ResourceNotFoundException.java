package crm.exception;

public class ResourceNotFoundException extends CrmException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entityType, Long id) {
        super(String.format("%s cu ID %d nu există", entityType, id));
    }
}
