package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.evaluator.ExpressionEvaluators;
import dd.kms.marple.gui.table.ColumnDescription;
import dd.kms.marple.gui.table.ColumnDescriptionBuilder;
import dd.kms.marple.gui.table.ListBasedTableModel;
import dd.kms.zenodot.settings.ParserSettingsUtils;
import dd.kms.zenodot.settings.Variable;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VariablePanel extends JPanel
{
	private final JScrollPane					scrollPane;
	private final JTable						table;
	private final ListBasedTableModel<Variable> tableModel;
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

		table.setDefaultRenderer(Object.class, new CellRenderer(inspectionContext));

		add(scrollPane,		new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,	GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		add(deleteButton,	new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,	 	GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

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

		updateDeleteButton();
	}

	private List<ColumnDescription<Variable>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<>("Name",					String.class, 	Variable::getName)				.editorSettings(this::changeVariableName).build(),
			new ColumnDescriptionBuilder<>("Value",					Object.class, 	Variable::getValue)				.build(),
			new ColumnDescriptionBuilder<>("Use hard reference",	Boolean.class,	Variable::isUseHardReference)	.editorSettings(this::changeUseHardReference).build()
		);
	}

	private void addListeners() {
		deleteButton.addActionListener(e -> deleteSelectedVariables());
		table.getSelectionModel().addListSelectionListener(e -> updateDeleteButton());
		tableModel.addTableModelListener(e -> updateParserSettings());
	}

	private void updateDeleteButton() {
		deleteButton.setEnabled(!table.getSelectionModel().isSelectionEmpty());
	}

	private void deleteSelectedVariables() {
		int[] rowIndicesToDelete = table.getSelectedRows();
		for (int i = rowIndicesToDelete.length - 1; i >= 0; i--) {
			int index = rowIndicesToDelete[i];
			variables.remove(index);
		}
		tableModel.fireTableChanged(new TableModelEvent(tableModel));
	}

	private Variable changeVariableName(Variable oldVariable, Object nameAsObject) {
		if (!(nameAsObject instanceof String)) {
			return oldVariable;
		}
		String name = (String) nameAsObject;
		if (!acceptVariableName(oldVariable, name)) {
			return oldVariable;
		}
		return ParserSettingsUtils.createVariable(name, oldVariable.getValue(), oldVariable.isUseHardReference());
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

	private Variable changeUseHardReference(Variable oldVariable, Object useHardReferenceAsObject) {
		if (!(useHardReferenceAsObject instanceof Boolean)) {
			return oldVariable;
		}
		boolean useHardReference = (Boolean) useHardReferenceAsObject;
		return ParserSettingsUtils.createVariable(oldVariable.getName(), oldVariable.getValue(), useHardReference);
	}

	private void updateParserSettings() {
		ExpressionEvaluators.setVariables(variables, inspectionContext);
	}

	private static class CellRenderer extends DefaultTableCellRenderer
	{
		private final InspectionContext inspectionContext;

		private CellRenderer(InspectionContext inspectionContext) {
			this.inspectionContext = inspectionContext;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (column == 1 && rendererComponent instanceof JLabel) {
				JLabel rendererLabel = (JLabel) rendererComponent;
				rendererLabel.setText(inspectionContext.getDisplayText(value));
			}
			return rendererComponent;
		}
	}
}
