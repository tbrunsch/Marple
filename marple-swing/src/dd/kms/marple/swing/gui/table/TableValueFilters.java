package dd.kms.marple.swing.gui.table;

import dd.kms.marple.InspectionContext;

public class TableValueFilters
{
	public static TableValueFilter createSelectionFilter(InspectionContext<?> inspectionContext) {
		return new TableValueFilterSelection(inspectionContext);
	}

	public static TableValueFilter createWildcardFilter() {
		return new TableValueFilterWildcard();
	}
}
