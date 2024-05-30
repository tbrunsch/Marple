package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

enum ViewOption
{
	DEFAULT		(null),
	AS_LIST		("View as List"),
	AS_ITERABLE	("View as Iterable"),
	AS_MAP		("View as Map"),
	AS_MULTIMAP	("View as Multimap"),
	AS_OBJECT	("View as Object");

	private final String	text;

	ViewOption(String text) {
		this.text = text;
	}

	String getText() {
		return text;
	}
}
