package dd.kms.marple.swing.gui.table;

public class TableValueFilters
{
	public static TableValueFilter createSelectionFilter() {
		return new TableValueFilterSelection();
	}

	public static TableValueFilter createWildcardFilter() {
		return new TableValueFilterWildcard();
	}
}
