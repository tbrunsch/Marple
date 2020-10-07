package dd.kms.marple.impl.gui.inspector.views.iterableview.settings;

public enum Operation
{
	FILTER		("Filter"),
	MAP			("Map"),
	FOR_EACH	("ForEach"),
	COLLECT		("Collect"),
	TO_MAP		("ToMap"),
	COUNT		("Count"),
	GROUP		("Group");

	private final String	displayText;

	Operation(String displayText) {
		this.displayText = displayText;
	}

	@Override
	public String toString() {
		return displayText;
	}
}
