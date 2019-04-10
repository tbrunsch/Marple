package dd.kms.marple.swing.gui.table;

import dd.kms.zenodot.common.RegexUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.regex.Pattern;

class TableValueFilterWildcard extends AbstractTableValueFilter
{
	private String	text			= "";
	private Pattern	filterPattern	= RegexUtils.createRegexForWildcardString(text);

	@Override
	public boolean isActive() {
		return !text.equals("");
	}

	@Override
	public void addAvailableValue(Object o) {
		/* do nothing */
	}

	@Override
	public Component getEditor() {
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
		filterPattern = RegexUtils.createRegexForWildcardString(text);
		fireFilterChanged();
	}
}
