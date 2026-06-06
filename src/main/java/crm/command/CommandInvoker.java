package crm.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.patterns.Command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * COMMAND PATTERN - CommandInvoker (Invoker)
 * 
 * Execută comenzi și păstrează istoricul pentru audit / posibil undo.
 * Folosește Singleton pentru a fi accesibil din orice modul.
 */
public class CommandInvoker {

    private static final Logger logger = LoggerFactory.getLogger(CommandInvoker.class);
    private static final int HISTORY_SIZE = 100;

    private static volatile CommandInvoker instance;

    private final Deque<Command<?>> history = new ArrayDeque<>();

    private CommandInvoker() {}

    public static CommandInvoker getInstance() {
        if (instance == null) {
            synchronized (CommandInvoker.class) {
                if (instance == null) {
                    instance = new CommandInvoker();
                }
            }
        }
        return instance;
    }

    /**
     * Execută o comandă și o adaugă în istoric.
     */
    public <R> R invoke(Command<R> command) {
        logger.debug("Invocare comandă: {}", command.getName());
        R result = command.execute();
        addToHistory(command);
        return result;
    }

    private void addToHistory(Command<?> command) {
        if (history.size() >= HISTORY_SIZE) {
            history.pollFirst();
        }
        history.offerLast(command);
    }

    public Deque<Command<?>> getHistory() {
        return new ArrayDeque<>(history);
    }

    public void clearHistory() {
        history.clear();
    }
}
