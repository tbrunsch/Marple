package dd.kms.marple.impl.gui.customactions;

import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.impl.gui.common.KeyInput;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class KeyRepresentationCellEditor extends AbstractCellEditor implements TableCellEditor
{
	private final KeyInput		keyInput		= new KeyInput(this::onKeyEntered);
	private KeyRepresentation	key				= null;


	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (value != null && !(value instanceof KeyRepresentation)) {
			return null;
		}
		key = null;
		keyInput.waitForKey();
		return keyInput;
	}

	@Override
	public Object getCellEditorValue() {
		return key;
	}

	private void onKeyEntered(KeyRepresentation key) {
		this.key = key;
		stopCellEditing();
	}
}
