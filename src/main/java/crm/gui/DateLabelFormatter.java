package crm.gui;

import javax.swing.*;

public class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
    private String datePattern = "yyyy-MM-dd";

    private java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat(datePattern);

    @Override
    public Object stringToValue(String text) throws java.text.ParseException {
        return dateFormatter.parseObject(text);
    }

    @Override
    public String valueToString(Object value) {
        if (value != null) {
            if (value instanceof java.util.Calendar) {
                java.util.Calendar calendar = (java.util.Calendar) value;
                return dateFormatter.format(calendar.getTime());
            }
        }
        return "";
    }
}
