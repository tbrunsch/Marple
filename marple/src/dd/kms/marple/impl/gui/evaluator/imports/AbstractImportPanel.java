package dd.kms.marple.impl.gui.evaluator.imports;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.framework.common.PreferenceUtils;
import dd.kms.marple.impl.gui.evaluator.textfields.AbstractInputTextField;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.settings.ParserSettings;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

abstract class AbstractImportPanel<T> extends JPanel
{
	private final JLabel						titleLabel;

	private final DefaultListModel<T>			importListModel		= new DefaultListModel<>();
	private final JList<T>						importList			= new JList<>(importListModel);
	private final JScrollPane					importScrollPane	= new JScrollPane(importList);

	private final AbstractInputTextField<T>		evaluationTextField;
	private final JButton						addButton			= new JButton("+");
	private final JButton						deleteButton		= new JButton("-");

	final InspectionContext						context;

	AbstractImportPanel(String title, InspectionContext context) {
		super(new GridBagLayout());

		this.context = context;

		titleLabel = new JLabel(title);

		evaluationTextField = createEvaluationTextField();
		evaluationTextField.addInputVerifier();

		add(titleLabel,				new GridBagConstraints(0, 0, REMAINDER, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));

		add(importScrollPane,		new GridBagConstraints(0, 1, REMAINDER, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

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
		return context.getSettings().getEvaluator().getParserSettings();
	}

	void setParserSettings(ParserSettings parserSettings) {
		InspectionSettings settings = context.getSettings();
		settings.getEvaluator().setParserSettings(parserSettings);
		PreferenceUtils.writeSettings(context);
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
