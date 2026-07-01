package crm.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.patterns.Command;

/**
 * COMMAND PATTERN - clasă abstractă pentru comenzi.
 * 
 * Furnizează logging implicit prin Template Method:
 * - Logging început comandă
 * - Apel doExecute() (implementat de subclase)
 * - Logging sfârșit comandă cu durată
 */
public abstract class AbstractCommand<R> implements Command<R> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public final R execute() {
        logger.debug("Executing command: {}", getName());
        long start = System.currentTimeMillis();
        try {
            R result = doExecute();
            long duration = System.currentTimeMillis() - start;
            logger.debug("Command finished: {} (duration: {}ms)", getName(), duration);
            return result;
        } catch (RuntimeException e) {
            logger.error("Error executing command {}: {}", getName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Logica concretă a comenzii, implementată de subclase.
     */
    protected abstract R doExecute();
}
