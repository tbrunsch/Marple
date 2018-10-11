package com.AMS.jBEAM.objectInspection.swing.gui.table;

import com.AMS.jBEAM.objectInspection.common.WildcardRegex;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.regex.Pattern;

public class TableValueFilter_Wildcard extends AbstractTableValueFilter
{
    private String  text            = "";
    private Pattern filterPattern   = WildcardRegex.createRegexPattern(text);

    @Override
    public boolean isActive() {
        return !text.equals("");
    }

    @Override
    public void addAvailableValue(Object o) {
        /* do nothing */
    }

    @Override
    public JComponent getEditor() {
        final JTextField textField = new JTextField(text);
        textField.requestFocus();
        textField.selectAll();
        textField.getDocument().addDocumentListener(new DocumentListener() {
            private void onDocumentChanged() {
                setText(textField.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                onDocumentChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onDocumentChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onDocumentChanged();
            }
        });
        return textField;
    }

    @Override
    public boolean test(Object o) {
        if (o == null) {
            // Do not filter null if no text is specified, but filter it otherwise
            return text.isEmpty();
        }
        return filterPattern.matcher(o.toString()).matches();
    }

    void setText(String text) {
        this.text = text;
        filterPattern = WildcardRegex.createRegexPattern(text);
        fireFilterChanged();
    }
}
