package dd.kms.marple.impl.gui.customactions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.CompiledExpressionInputTextField;
import dd.kms.zenodot.api.ParseException;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;

class ActionExpressionCellEditor extends AbstractCellEditor implements TableCellEditor
{
		private final CompiledExpressionInputTextField	editorTextField;

		private final IntFunction<Class<?>> 			requiredClassProvider;

		private boolean									editingFinished	= false;
		private int										currentRow		= -1;

		ActionExpressionCellEditor(IntFunction<Class<?>> requiredClassProvider, Consumer<Throwable> exceptionConsumer, InspectionContext context) {
			editorTextField = new CompiledExpressionInputTextField(context);
			editorTextField.requestFocus();
			editorTextField.setEvaluationResultConsumer(o -> {
				editingFinished = true;
				stopCellEditing();
			});
			editorTextField.setExceptionConsumer(exceptionConsumer);

			this.requiredClassProvider = requiredClassProvider;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (value != null && !(value instanceof String)) {
				return null;
			}

			currentRow = row;
			editingFinished = false;
			editorTextField.setText((String) value);
			editorTextField.setThisType(requiredClassProvider.apply(row));
			return editorTextField;
		}

		@Override
		public String getCellEditorValue() {
			try {
				editorTextField.evaluateText();
				return editorTextField.getExpression();
			} catch (ParseException e) {
				return null;
			}
		}

		@Override
		public boolean stopCellEditing() {
			return editingFinished && super.stopCellEditing();
		}
	}
