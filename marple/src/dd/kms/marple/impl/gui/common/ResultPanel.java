package dd.kms.marple.impl.gui.common;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.completion.CodeCompletionDecorators;
import dd.kms.marple.impl.gui.inspector.views.fieldview.FieldTree;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NORTH;

public class ResultPanel extends JPanel
{
	private final InspectionContext context;

	public ResultPanel(InspectionContext context) {
		super(new GridBagLayout());
		this.context = context;

		setBorder(BorderFactory.createTitledBorder("Result"));
	}

	public void displayException(@Nullable Throwable t) {
		String error = ExceptionFormatter.formatParseException(t);
		JLabel exceptionLabel = new JLabel(error);
		CodeCompletionDecorators.configureExceptionComponent(exceptionLabel);
		displayComponent(exceptionLabel);
	}

	public void displayResult(ObjectInfo resultInfo) {
		FieldTree fieldTree = new FieldTree(resultInfo, context);
		displayComponent(fieldTree);
	}

	public void displayText(String text) {
		JLabel label = new JLabel(text);
		displayComponent(label);
	}

	private void displayComponent(JComponent component) {
		removeAll();
		add(component, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, NORTH, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		revalidate();
		repaint();
	}
}
