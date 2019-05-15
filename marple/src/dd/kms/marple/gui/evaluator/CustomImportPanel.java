package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.awt.GridBagConstraints.*;

class CustomImportPanel extends JPanel
{
	private static final Insets	DEFAULT_INSETS	= new Insets(5, 5, 5, 5);

	private final JLabel						titleLabel;

	private final DefaultListModel<String>		importListModel		= new DefaultListModel<>();
	private final JList<String>					importList			= new JList<>(importListModel);

	private final JTextField					evaluationTextField;
	private final JButton						addButton			= new JButton("+");
	private final JButton						deleteButton		= new JButton("-");

	private final Function<String, Boolean> 	importVerifier;
	private final Consumer<List<String>>		importsConsumer;

	CustomImportPanel(String title, Collection<String> initialImports, Function<String, Boolean> importVerifier, Consumer<List<String>> importsConsumer, InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.importVerifier = importVerifier;
		this.importsConsumer = importsConsumer;

		titleLabel = new JLabel(title);

		evaluationTextField = new EvaluationTextField(string -> {}, inspectionContext);
		InputVerifier inputVerifier = new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				return input instanceof JTextComponent && importVerifier.apply(((JTextComponent) input).getText());
			}
		};
		evaluationTextField.setInputVerifier(inputVerifier);

		add(titleLabel,				new GridBagConstraints(0, 0, REMAINDER, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));

		add(importList,				new GridBagConstraints(0, 1, REMAINDER, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		add(evaluationTextField,	new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
		add(addButton,				new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(deleteButton,			new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		deleteButton.setEnabled(false);

		updateContent(initialImports);

		addListeners();
	}

	void updateContent(Collection<String> imports) {
		importListModel.clear();
		for (String imp : imports) {
			importListModel.addElement(imp);
		}
	}

	private void addListeners() {
		importList.getSelectionModel().addListSelectionListener(e -> onSelectionChanged());
		addButton.addActionListener(e -> onAddButtonClicked());
		deleteButton.addActionListener(e -> onDeleteButtonClicked());
	}

	private void updateParserSettings() {
		int numImports = importListModel.getSize();
		List<String> imports = new ArrayList<>(numImports);
		for (int i = 0; i < numImports; i++) {
			imports.add(importListModel.get(i));
		}
		importsConsumer.accept(imports);
	}

	/*
	 * Listeners
	 */
	private void onSelectionChanged() {
		boolean noItemSelected = importList.getSelectedIndices().length == 0;
		deleteButton.setEnabled(!noItemSelected);
	}

	private void onAddButtonClicked() {
		String importExpression = evaluationTextField.getText();
		if (importVerifier.apply(importExpression)) {
			importListModel.addElement(importExpression);
		}
		updateParserSettings();
	}

	private void onDeleteButtonClicked() {
		int[] selectedRows = importList.getSelectedIndices();
		for (int i = selectedRows.length - 1; i >= 0; i--) {
			int row = selectedRows[i];
			importListModel.remove(row);
		}
		updateParserSettings();
	}
}
