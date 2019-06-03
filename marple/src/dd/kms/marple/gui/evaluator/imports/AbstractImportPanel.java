package dd.kms.marple.gui.evaluator.imports;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.evaluator.textfields.AbstractInputTextField;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.settings.ParserSettings;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.awt.GridBagConstraints.*;

abstract class AbstractImportPanel<T> extends JPanel
{
	private static final Insets	DEFAULT_INSETS	= new Insets(5, 5, 5, 5);

	private final JLabel						titleLabel;

	private final DefaultListModel<T>			importListModel		= new DefaultListModel<>();
	private final JList<T>						importList			= new JList<>(importListModel);

	private final AbstractInputTextField<T>		evaluationTextField;
	private final JButton						addButton			= new JButton("+");
	private final JButton						deleteButton		= new JButton("-");

	final InspectionContext						inspectionContext;

	AbstractImportPanel(String title, InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.inspectionContext = inspectionContext;

		titleLabel = new JLabel(title);

		evaluationTextField = createEvaluationTextField();
		evaluationTextField.addInputVerifier();

		add(titleLabel,				new GridBagConstraints(0, 0, REMAINDER, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));

		add(importList,				new GridBagConstraints(0, 1, REMAINDER, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		add(evaluationTextField,	new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
		add(addButton,				new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(deleteButton,			new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		deleteButton.setEnabled(false);

		addListeners();

		SwingUtilities.invokeLater(this::updateContent);
	}

	abstract AbstractInputTextField<T> createEvaluationTextField();
	abstract Collection<T> getImports();
	abstract void setImports(List<T> imports);

	void updateContent() {
		Collection<T> imports = getImports();
		importListModel.clear();
		for (T imp : imports) {
			importListModel.addElement(imp);
		}
	}

	private void addListeners() {
		importList.getSelectionModel().addListSelectionListener(e -> onSelectionChanged());
		addButton.addActionListener(e -> onAddButtonClicked());
		deleteButton.addActionListener(e -> onDeleteButtonClicked());
	}

	private void updateImports() {
		int numImports = importListModel.getSize();
		List<T> imports = new ArrayList<>(numImports);
		for (int i = 0; i < numImports; i++) {
			imports.add(importListModel.get(i));
		}
		setImports(imports);
	}

	ParserSettings getParserSettings() {
		return inspectionContext.getSettings().getEvaluator().getParserSettings();
	}

	void setParserSettings(ParserSettings parserSettings) {
		inspectionContext.getSettings().getEvaluator().setParserSettings(parserSettings);
	}

	/*
	 * Listeners
	 */
	private void onSelectionChanged() {
		boolean noItemSelected = importList.getSelectedIndices().length == 0;
		deleteButton.setEnabled(!noItemSelected);
	}

	private void onAddButtonClicked() {
		try {
			T imp = evaluationTextField.evaluateText();
			importListModel.addElement(imp);
			updateImports();
		} catch (ParseException e) {
			/* happens if the import cannot be parsed */
		}
	}

	private void onDeleteButtonClicked() {
		int[] selectedRows = importList.getSelectedIndices();
		for (int i = selectedRows.length - 1; i >= 0; i--) {
			int row = selectedRows[i];
			importListModel.remove(row);
		}
		updateImports();
	}
}
