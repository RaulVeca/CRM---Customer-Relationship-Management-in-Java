package crm.exception;

public class CrmException extends RuntimeException {

    public CrmException(String message) {
        super(message);
    }

    public CrmException(String message, Throwable cause) {
        super(message, cause);
    }
}
