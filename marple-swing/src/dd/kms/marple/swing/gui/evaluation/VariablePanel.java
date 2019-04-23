package dd.kms.marple.swing.gui.evaluation;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.swing.gui.table.ColumnDescription;
import dd.kms.marple.swing.gui.table.ColumnDescriptionBuilder;
import dd.kms.marple.swing.gui.table.ListBasedTableModel;
import dd.kms.zenodot.settings.Variable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VariablePanel extends JPanel
{
	private final JScrollPane		scrollPane;
	private final JTable			table;
	private final JButton			deleteButton	= new JButton("Delete selected variables");

	private final List<Variable>	variables;

	public VariablePanel(List<Variable> variables, InspectionContext<Component> inspectionContext) {
		super(new GridBagLayout());

		this.variables = new ArrayList<>(variables);

		List<ColumnDescription<Variable>> columnDescriptions = createColumnDescriptions();
		table = new JTable(new ListBasedTableModel<>(this.variables, columnDescriptions));
		scrollPane = new JScrollPane(table);

		table.setDefaultRenderer(Object.class, new CellRenderer(inspectionContext));

		add(scrollPane,		new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,	GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		add(deleteButton,	new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,	 	GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		updateDeleteButton();

		addListeners();
	}

	private List<ColumnDescription<Variable>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<Variable>("Name",					String.class, 	Variable::getName)				.editorSettings(this::changeVariableName).build(),
			new ColumnDescriptionBuilder<Variable>("Value",					Object.class, 	Variable::getValue)				.build(),
			new ColumnDescriptionBuilder<Variable>("Use hard reference",	Boolean.class,	Variable::isUseHardReference)	.editorSettings(this::changeUseHardReference).build()
		);
	}

	public List<Variable> getVariables() {
		return new ArrayList<>(variables);
	}

	private void addListeners() {
		deleteButton.addActionListener(e -> deleteSelectedVariables());
		table.getSelectionModel().addListSelectionListener(e -> updateDeleteButton());
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
		table.revalidate();
		table.repaint();
	}

	private Variable changeVariableName(Variable oldVariable, Object nameAsObject) {
		if (!(nameAsObject instanceof String)) {
			return oldVariable;
		}
		String name = (String) nameAsObject;
		if (!acceptVariableName(oldVariable, name)) {
			return oldVariable;
		}
		return new Variable(name, oldVariable.getValue(), oldVariable.isUseHardReference());
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
		return new Variable(oldVariable.getName(), oldVariable.getValue(), useHardReference);
	}

	private static class CellRenderer extends DefaultTableCellRenderer
	{
		private final InspectionContext<Component> inspectionContext;

		private CellRenderer(InspectionContext<Component> inspectionContext) {
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
