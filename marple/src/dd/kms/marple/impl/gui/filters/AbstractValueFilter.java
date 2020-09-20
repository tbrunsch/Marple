package dd.kms.marple.impl.gui.filters;

import java.util.HashSet;
import java.util.Set;

abstract class AbstractValueFilter implements ValueFilter
{
	private final Set<Runnable> filterChangedListeners	= new HashSet<>();

	@Override
	public void addFilterChangedListener(Runnable listener) {
		filterChangedListeners.add(listener);
	}

	@Override
	public void removeFilterChangedListener(Runnable listener) {
		filterChangedListeners.remove(listener);
	}

	void fireFilterChanged() {
		filterChangedListeners.forEach(Runnable::run);
	}
}
