package dd.kms.marple.gui.table;

import javax.swing.*;
import java.util.function.Predicate;

public class ListBasedTableRowFilter extends RowFilter<ListBasedTableModel<?>, Integer>
{
	@Override
	public boolean include(Entry<? extends ListBasedTableModel<?>, ? extends Integer> entry) {
		ListBasedTableModel<?> model = entry.getModel();
		int numColumns = entry.getValueCount();
		for (int col = 0; col < numColumns; col++) {
			Predicate<Object> valueFilter = model.getValueFilter(col);
			Object value = entry.getValue(col);
			if (!valueFilter.test(value)) {
				return false;
			}
		}
		return true;
	}
}
