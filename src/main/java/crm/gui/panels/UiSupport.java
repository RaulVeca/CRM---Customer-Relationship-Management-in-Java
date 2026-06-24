package crm.gui.panels;

import crm.model.entity.Contact;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

final class UiSupport {

    static final int PAGE_SIZE = 500;
    static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private UiSupport() {
    }

    static void configureTable(JTable table) {
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
    }

    static Long selectedId(JTable table, DefaultTableModel model) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object value = model.getValueAt(modelRow, 0);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    static void showError(Component parent, Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = ex.getClass().getSimpleName();
        }
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "CRM Training IT", JOptionPane.INFORMATION_MESSAGE);
    }

    static String text(JTextComponent field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    static String requiredText(JTextComponent field, String label) {
        String value = text(field);
        if (value.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value;
    }

    static String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static Integer parseInteger(String value, String label) {
        String trimmed = emptyToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return Integer.valueOf(trimmed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    static Long parseLong(String value, String label) {
        String trimmed = emptyToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return Long.valueOf(trimmed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    static BigDecimal parseMoney(String value, String label) {
        String trimmed = emptyToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid amount.");
        }
    }

    static LocalDate parseDate(String value, String label) {
        String trimmed = emptyToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(label + " must use yyyy-MM-dd.");
        }
    }

    static LocalDateTime parseDateTime(String value, String label) {
        String trimmed = emptyToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(trimmed, DATE_TIME_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(label + " must use yyyy-MM-dd HH:mm.");
        }
    }

    static String formatDate(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    static String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMAT);
    }

    static String money(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    static String enumName(Enum<?> value) {
        if (value == null) {
            return "";
        }
        String[] words = value.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }
        return result.toString();
    }

    static String contactName(Contact contact) {
        if (contact == null) {
            return "";
        }
        return contact.getFullName().orElse(contact.getEmail() != null ? contact.getEmail() : "Contact #" + contact.getId());
    }

    static <E extends Enum<E>> JComboBox<E> enumCombo(E[] values) {
        JComboBox<E> combo = new JComboBox<E>(values);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Enum<?>) {
                    setText(enumName((Enum<?>) value));
                }
                return this;
            }
        });
        return combo;
    }
}
