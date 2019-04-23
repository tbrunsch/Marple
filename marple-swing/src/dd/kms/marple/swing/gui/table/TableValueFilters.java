package dd.kms.marple.swing.gui.table;

import dd.kms.marple.InspectionContext;

import java.awt.*;

public class TableValueFilters
{
	public static final TableValueFilter	NONE	= new NoTableValueFilter();

	public static TableValueFilter createSelectionFilter(InspectionContext<?> inspectionContext) {
		return new TableValueFilterSelection(inspectionContext);
	}

	public static TableValueFilter createWildcardFilter() {
		return new TableValueFilterWildcard();
	}

	private static class NoTableValueFilter implements TableValueFilter
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
