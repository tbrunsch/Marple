package dd.kms.marple.impl.gui.filters;

import dd.kms.zenodot.api.common.RegexUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Function;
import java.util.regex.Pattern;

class ValueFilterWildcard extends AbstractValueFilter
{
	private final JTextField				filterTF;

	private final Function<Object, String>	stringRepresentationProvider;

	private Pattern							filterPattern;

	ValueFilterWildcard(Function<Object, String> stringRepresentationProvider) {
		this.stringRepresentationProvider = stringRepresentationProvider;

		filterTF = new JTextField();
		filterTF.requestFocus();
		filterTF.selectAll();
		filterTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				onFilterTextChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				onFilterTextChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				onFilterTextChanged();
			}
		});

		filterPattern = RegexUtils.createRegexForWildcardString(getText());
	}

	@Override
	public boolean isActive() {
		return !getText().equals("");
	}

	@Override
	public void addAvailableValue(Object o) {
		/* do nothing */
	}

	@Override
	public Component getEditor() {
		return filterTF;
	}

	@Override
	public Object getSettings() {
		return new ValueFilterWildcardSettings(getText());
	}

	@Override
	public void applySettings(Object settings) {
		if (settings instanceof ValueFilterWildcardSettings) {
			ValueFilterWildcardSettings filterSettings = (ValueFilterWildcardSettings) settings;
			filterTF.setText(filterSettings.getText());
		}
	}

	@Override
	public boolean test(Object o) {
		if (o == null) {
			// Do not filter null if no text is specified, but filter it otherwise
			return getText().isEmpty();
		}
		return filterPattern.matcher(stringRepresentationProvider.apply(o)).matches();
	}

	private void onFilterTextChanged() {
		filterPattern = RegexUtils.createRegexForWildcardString(getText());
		fireFilterChanged();
	}

	private String getText() {
		return filterTF.getText();
	}

	private static class ValueFilterWildcardSettings
	{
		private final String	text;

		ValueFilterWildcardSettings(String text) {
			this.text = text;
		}

		String getText() {
			return text;
		}
	}
}
