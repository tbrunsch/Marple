package dd.kms.marple.gui.inspector.views.iterableview.settings;

public enum FilterResultType
{
	LIST	("List"),
	INDEX_MAP("Map: index => value");

	private final String	displayText;

	FilterResultType(String displayText) {
		this.displayText = displayText;
	}

	@Override
	public String toString() {
		return displayText;
	}
}
