package dd.kms.marple.swing.gui.table;

import java.awt.*;
import java.util.function.Predicate;

public interface TableValueFilter extends Predicate<Object>
{
	boolean isActive();
	void addAvailableValue(Object o);
	Component getEditor();
	void addFilterChangedListener(Runnable listener);
	void removeFilterChangedListener(Runnable listener);
}
