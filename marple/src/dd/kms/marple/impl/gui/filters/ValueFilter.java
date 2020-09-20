package dd.kms.marple.impl.gui.filters;

import java.awt.*;
import java.util.function.Predicate;

public interface ValueFilter extends Predicate<Object>
{
	boolean isActive();
	void addAvailableValue(Object o);
	Component getEditor();
	void addFilterChangedListener(Runnable listener);
	void removeFilterChangedListener(Runnable listener);
}
