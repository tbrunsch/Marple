package dd.kms.marple.gui.inspector.views.iterableview;

enum OperationResultType
{
	LIST	("List"),
	INDEX_MAP("Map: index => value");

	private final String	displayText;

	OperationResultType(String displayText) {
		this.displayText = displayText;
	}

	@Override
	public String toString() {
		return displayText;
	}
}
