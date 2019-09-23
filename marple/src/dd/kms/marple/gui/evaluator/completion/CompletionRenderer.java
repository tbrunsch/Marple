package dd.kms.marple.gui.evaluator.completion;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.FunctionCompletion;

import javax.swing.*;
import java.awt.*;

class CompletionRenderer implements ListCellRenderer<Object>
{
	private final JPanel					panel			= new JPanel(new GridBagLayout());
	private final DefaultListCellRenderer	leftRenderer	= new DefaultListCellRenderer();
	private final DefaultListCellRenderer	rightRenderer	= new DefaultListCellRenderer();

	CompletionRenderer() {
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

		Icon icon = value instanceof Completion ? ((Completion) value).getIcon() : null;
		leftRenderer.setIcon(icon);

		return panel;
	}
}
