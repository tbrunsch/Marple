package dd.kms.marple.actions.debugsupport;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.gui.evaluator.textfields.ExpressionInputTextField;
import dd.kms.marple.gui.table.ActionProviderRenderer;
import dd.kms.marple.gui.table.ColumnDescription;
import dd.kms.marple.gui.table.ListBasedTableModel;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

abstract class AbstractSlotPanel<T> extends JPanel
{
	private static final KeyStroke	UP_KEY				= KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
	private static final KeyStroke	DOWN_KEY			= KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);

	private final JScrollPane				scrollPane;
	final JTable							table;
	final List<T> 							tableList;
	private final ListBasedTableModel<T>	tableModel;

	private final CellEditor				cellEditor;

	private final JLabel					descriptionLabel;

	final InspectionContext					inspectionContext;

	int										yPos;

	AbstractSlotPanel(String title, String description, InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.tableList = createTableList();
		this.inspectionContext = inspectionContext;

		setBorder(BorderFactory.createTitledBorder(title));

		List<ColumnDescription<T>> columnDescriptions = createColumnDescriptions();
		tableModel = new ListBasedTableModel<>(this.tableList, columnDescriptions);
		table = new JTable(tableModel);
		scrollPane = new JScrollPane(table);

		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(1).setCellRenderer(new ActionProviderRenderer());

		cellEditor = new CellEditor(inspectionContext);
		table.setDefaultEditor(ActionProvider.class, cellEditor);

		ActionProviderListeners.addMouseListeners(table);

		descriptionLabel = new JLabel(description);

		/*
		 * Disable table feature that pressing up/down arrow moves to the row above/below
		 * to prevent it from disabling the navigation in the completion suggestion popup menu
		 */
		disableTableKey(UP_KEY);
		disableTableKey(DOWN_KEY);

		add(scrollPane,			new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 1.0, NORTHWEST,	BOTH,		DEFAULT_INSETS, 0, 0));
		add(descriptionLabel,	new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 0.0, WEST,			HORIZONTAL, DEFAULT_INSETS, 0, 0));
	}

	abstract List<T> createTableList();
	abstract List<ColumnDescription<T>> createColumnDescriptions();
	abstract void onContentChanged();

	void setThisValue(ObjectInfo thisValue) {
		cellEditor.setThisValue(thisValue);
	}

	void updateContent() {
		tableList.clear();
		tableList.addAll(createTableList());

		fireTableChanged();

		onContentChanged();
	}

	private void disableTableKey(KeyStroke key) {
		disableTableKey(table, key);
		disableTableKey(scrollPane, key);
	}

	private void disableTableKey(JComponent component, KeyStroke key) {
		InputMap inputMap = component.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		while (inputMap != null && inputMap.get(key) != null) {
			inputMap.remove(key);
			inputMap = inputMap.getParent();
		}
	}

	void fireTableChanged() {
		table.tableChanged(new TableModelEvent(tableModel));
	}

	private static class CellEditor extends AbstractCellEditor implements TableCellEditor
	{
		private final ExpressionInputTextField	editorTextField;

		private final Map<Integer, String>		rowToExpression	= new HashMap<>();

		private boolean							editingFinished	= false;
		private int								currentRow		= -1;

		CellEditor(InspectionContext inspectionContext) {
			editorTextField = new ExpressionInputTextField(inspectionContext);
			editorTextField.requestFocus();
			editorTextField.setEvaluationResultConsumer(o -> {
				editingFinished = true;
				rowToExpression.put(currentRow, editorTextField.getText());
				stopCellEditing();
			});
		}

		void setThisValue(ObjectInfo thisValue) {
			editorTextField.setThisValue(thisValue);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			currentRow = row;
			editingFinished = false;
			editorTextField.setText(rowToExpression.get(currentRow));
			return editorTextField;
		}

		@Override
		public Object getCellEditorValue() {
			try {
				return editorTextField.evaluateText();
			} catch (ParseException e) {
				return null;
			}
		}

		@Override
		public boolean stopCellEditing() {
			return editingFinished && super.stopCellEditing();
		}
	}
}
