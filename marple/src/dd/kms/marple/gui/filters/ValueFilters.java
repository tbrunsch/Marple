package dd.kms.marple.gui.filters;

import dd.kms.marple.InspectionContext;

import java.awt.*;

public class ValueFilters
{
	public static final ValueFilter NONE	= new NoTableValueFilter();

	public static ValueFilter createSelectionFilter(InspectionContext inspectionContext) {
		return new ValueFilterSelection(inspectionContext);
	}

	public static ValueFilter createWildcardFilter() {
		return new ValueFilterWildcard();
	}

	public static ValueFilter createMinimumAccessLevelFilter() { return new ValueFilterMinimumAccessLevel(); }

	private static class NoTableValueFilter implements ValueFilter
	{
		@Override
		public boolean isActive() {
			return false;
		}

		@Override
		public void addAvailableValue(Object o) {
			/* do nothing */
		}

		@Override
		public Component getEditor() {
			return null;
		}

		@Override
		public void addFilterChangedListener(Runnable listener) {
			/* do nothing */
		}

		@Override
		public void removeFilterChangedListener(Runnable listener) {
			/* do nothing */
		}

		@Override
		public boolean test(Object o) {
			return true;
		}
	}
}
