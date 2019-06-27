package dd.kms.marple.gui.inspector.views.iterableview;

enum Operation
{
	FILTER		("Filter"),
	MAP			("Map"),
	FOR_EACH	("ForEach");

	private final String	displayText;

	Operation(String displayText) {
		this.displayText = displayText;
	}

	@Override
	public String toString() {
		return displayText;
	}
}
