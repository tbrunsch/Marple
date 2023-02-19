package dd.kms.marple.impl.gui.table;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.ClassInputTextField;
import dd.kms.zenodot.api.ParseException;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.function.Consumer;

public class ClassCellEditor extends AbstractCellEditor implements TableCellEditor
{
	private final ClassInputTextField	editorTextField;

	private boolean						editingFinished	= false;

	public ClassCellEditor(Consumer<Throwable> exceptionConsumer, InspectionContext context) {
		editorTextField = new ClassInputTextField(context);
		editorTextField.requestFocus();
		editorTextField.setEvaluationResultConsumer(o -> {
			editingFinished = true;
			stopCellEditing();
		});
		editorTextField.setExceptionConsumer(exceptionConsumer);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		editingFinished = false;
		editorTextField.setText(value instanceof Class<?> ? ((Class<?>) value).getName() : String.valueOf(value));
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

	@Override
	public boolean isCellEditable(EventObject e) {
		if (e instanceof MouseEvent) {
			return ((MouseEvent) e).getClickCount() >= 2;
		}
		return true;
	}
}
