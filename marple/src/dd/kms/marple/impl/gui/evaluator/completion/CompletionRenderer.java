package dd.kms.marple.impl.gui.evaluator.completion;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionClass;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.FunctionCompletion;

import javax.swing.*;
import java.awt.*;

class CompletionRenderer implements ListCellRenderer<Object>
{
	private final JPanel					panel				= new JPanel(new GridBagLayout());
	private final DefaultListCellRenderer	leftRenderer		= new DefaultListCellRenderer();
	private final DefaultListCellRenderer	rightRenderer		= new DefaultListCellRenderer();

	private final ParserMediator		parserMediator;

	CompletionRenderer(ParserMediator parserMediator) {
		this.parserMediator = parserMediator;

		panel.add(leftRenderer,		new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(rightRenderer,	new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		leftRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (value instanceof FunctionCompletion) {
			String returnValueDescription = ((FunctionCompletion) value).getReturnValueDescription();
			rightRenderer.getListCellRendererComponent(list, returnValueDescription, index, isSelected, cellHasFocus);
			rightRenderer.setForeground(isSelected ? Color.WHITE : Color.LIGHT_GRAY);
		} else {
			rightRenderer.setText("");
		}

		boolean temporarilyImportedClassCompletion = isTemporarilyImportedClassCompletion(value);
		if (temporarilyImportedClassCompletion) {
			Color background = leftRenderer.getBackground();
			Color newBackground = isBright(background) ? background.darker() : background.brighter();
			leftRenderer.setBackground(newBackground);
		}

		Icon icon = value instanceof Completion ? ((Completion) value).getIcon() : null;
		leftRenderer.setIcon(icon);

		return panel;
	}

	private boolean isBright(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		return r + g + b >= 3 * 128;
	}

	private boolean isTemporarilyImportedClassCompletion(Object value) {
		if (!(value instanceof CustomBasicCompletion)) {
			return false;
		}
		CustomBasicCompletion completion = (CustomBasicCompletion) value;
		CodeCompletion codeCompletion = completion.getCodeCompletion();
		if (!(codeCompletion instanceof CodeCompletionClass)) {
			return false;
		}
		CodeCompletionClass classCompletion = (CodeCompletionClass) codeCompletion;
		String normalizedClassName = classCompletion.getClassInfo().getNormalizedName();
		return parserMediator.isClassImportedTemporarily(normalizedClassName);
	}
}
