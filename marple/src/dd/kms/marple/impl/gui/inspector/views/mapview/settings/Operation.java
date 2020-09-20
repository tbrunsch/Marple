package dd.kms.marple.impl.gui.inspector.views.mapview.settings;

public enum Operation
{
	FILTER	("Filter"),
	MAP		("Map");

	private final String	displayText;

	Operation(String displayText) {
		this.displayText = displayText;
	}

	@Override
	public String toString() {
		return displayText;
	}
}
