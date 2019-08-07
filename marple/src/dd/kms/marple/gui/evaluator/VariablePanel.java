package dd.kms.marple.gui.evaluator;

import com.google.common.base.Joiner;
import dd.kms.marple.DebugSupport;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.evaluator.ExpressionEvaluators;
import dd.kms.marple.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.gui.table.ActionProviderRenderer;
import dd.kms.marple.gui.table.ColumnDescription;
import dd.kms.marple.gui.table.ColumnDescriptionBuilder;
import dd.kms.marple.gui.table.ListBasedTableModel;
import dd.kms.zenodot.settings.ParserSettingsUtils;
import dd.kms.zenodot.settings.Variable;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.REMAINDER;

public class VariablePanel extends JPanel
{
	public static final String	WINDOW_TITLE	= "Variables";

	private final JScrollPane					scrollPane;
	private final JTable						table;
	private final ListBasedTableModel<Variable> tableModel;

	private final JButton						importButton		= new JButton("Import from 'DebugSupport'");
	private final JButton						exportButton		= new JButton("Export to 'DebugSupport'");
	private final JButton						deleteButton		= new JButton("Delete selected variables");

	private final InspectionContext				inspectionContext;
	private final List<Variable>				variables			= new ArrayList<>();

	public VariablePanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.inspectionContext = inspectionContext;

		List<ColumnDescription<Variable>> columnDescriptions = createColumnDescriptions();
		tableModel = new ListBasedTableModel<>(this.variables, columnDescriptions);
		table = new JTable(tableModel);
		scrollPane = new JScrollPane(table);

		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(1).setCellRenderer(new ActionProviderRenderer());

		add(scrollPane,		new GridBagConstraints(0, 0, REMAINDER, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,	GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));

		add(importButton,	new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, DEFAULT_INSETS, 0, 0));
		add(exportButton,	new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, DEFAULT_INSETS, 0, 0));
		add(deleteButton,	new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, DEFAULT_INSETS, 0, 0));

		updateContent();

		addListeners();
	}

	public void editVariableName(String name) {
		updateContent();
		for (int i = 0; i < variables.size(); i++) {
			Variable variable = variables.get(i);
			if (name.equals(variable.getName())) {
				editVariableName(i);
				return;
			}
		}
	}

	private void editVariableName(int row) {
		table.editCellAt(row, 0);
		TableCellEditor cellEditor = table.getCellEditor();
		Component editorComponent = cellEditor.getTableCellEditorComponent(table, variables.get(row).getName(), true, row, 0);
		if (editorComponent instanceof JTextComponent) {
			JTextComponent textEditorComponent = (JTextComponent) editorComponent;
			textEditorComponent.requestFocusInWindow();
			textEditorComponent.selectAll();
		}
	}

	public void updateContent() {
		variables.clear();
		variables.addAll(ExpressionEvaluators.getVariables(inspectionContext));
		tableModel.fireTableChanged(new TableModelEvent(tableModel));

		updateButtons();
	}

	private List<ColumnDescription<Variable>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<>("Name",					String.class, 			Variable::getName)					.editorSettings(this::changeVariableName).build(),
			new ColumnDescriptionBuilder<>("Value",					ActionProvider.class, 	this::getValueAsActionProvider).build(),
			new ColumnDescriptionBuilder<>("Use hard reference",	Boolean.class,			Variable::isUseHardReference)		.editorSettings(this::changeUseHardReference).build()
		);
	}

	private ActionProvider getValueAsActionProvider(Variable variable) {
		Object value = variable.getValue();
		return new ActionProviderBuilder(inspectionContext.getDisplayText(value), value, inspectionContext)
			.evaluateAs(variable.getName(), null)
			.executeDefaultAction(true)
			.build();
	}

	private void addListeners() {
		importButton.addActionListener(e -> importVariables());
		exportButton.addActionListener(e -> exportVariables());
		deleteButton.addActionListener(e -> deleteSelectedVariables());
		table.getSelectionModel().addListSelectionListener(e -> updateButtons());
		tableModel.addTableModelListener(e -> updateParserSettings());

		ActionProviderListeners.addMouseListeners(table);
	}

	private void updateButtons() {
		Collection<String> debugSupportVariableNames = DebugSupport.getSlotNames();
		if (debugSupportVariableNames.isEmpty()) {
			importButton.setEnabled(false);
			importButton.setToolTipText(null);
		} else {
			importButton.setEnabled(true);
			importButton.setToolTipText("Import variables " + Joiner.on(", ").join(DebugSupport.getSlotNames()));
		}

		if (variables.isEmpty()) {
			exportButton.setEnabled(false);
			exportButton.setToolTipText(null);
		} else {
			exportButton.setEnabled(true);
			exportButton.setToolTipText("Export variables as named slots of class 'DebugSupport'");
		}

		deleteButton.setEnabled(!table.getSelectionModel().isSelectionEmpty());
	}

	private void importVariables() {
		variables.clear();
		for (String name : DebugSupport.getSlotNames()) {
			Object value = DebugSupport.getSlotValue(name);
			Variable variable = ParserSettingsUtils.createVariable(name, value, false);
			variables.add(variable);
		}
		tableModel.fireTableChanged(new TableModelEvent(tableModel));
	}

	private void exportVariables() {
		DebugSupport.clearNamedSlots();
		for (Variable variable : variables) {
			DebugSupport.setSlotValue(variable.getName(), variable.getValue());
		}
	}

	private void deleteSelectedVariables() {
		int[] rowIndicesToDelete = table.getSelectedRows();
		for (int i = rowIndicesToDelete.length - 1; i >= 0; i--) {
			int index = rowIndicesToDelete[i];
			variables.remove(index);
		}
		tableModel.fireTableChanged(new TableModelEvent(tableModel));
	}

	private void changeVariableName(List<Variable> variables, int elementIndex, Object nameAsObject) {
		if (!(nameAsObject instanceof String)) {
			return;
		}
		String name = (String) nameAsObject;
		Variable oldVariable = variables.get(elementIndex);
		if (!acceptVariableName(oldVariable, name)) {
			return;
		}
		Variable newVariable = ParserSettingsUtils.createVariable(name, oldVariable.getValue(), oldVariable.isUseHardReference());
		variables.set(elementIndex, newVariable);
	}

	private boolean acceptVariableName(Variable oldVariable, String name) {
		if (name == null || name.isEmpty()) {
			return false;
		}
		for (Variable variable : variables) {
			if (variable != oldVariable && Objects.equals(variable.getName(), name)) {
				return false;
			}
		}
		return true;
	}

	private void changeUseHardReference(List<Variable> variables, int elementIndex, Object useHardReferenceAsObject) {
		if (!(useHardReferenceAsObject instanceof Boolean)) {
			return;
		}
		boolean useHardReference = (Boolean) useHardReferenceAsObject;
		Variable oldVariable = variables.get(elementIndex);
		Variable newVariable = ParserSettingsUtils.createVariable(oldVariable.getName(), oldVariable.getValue(), useHardReference);
		variables.set(elementIndex, newVariable);
	}

	private void updateParserSettings() {
		ExpressionEvaluators.setVariables(variables, inspectionContext);
	}
}
