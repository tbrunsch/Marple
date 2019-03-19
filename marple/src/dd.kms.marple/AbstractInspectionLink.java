package dd.kms.marple;

abstract class AbstractInspectionLink implements InspectionLinkIF
{
	private final String linkText;

	AbstractInspectionLink(String linkText) {
		this.linkText = linkText;
	}

	abstract void doInspect(ObjectInspector inspector);

	@Override
	public void run() {
		ObjectInspector.getInspector().inspect(this);
	}

	@Override
	public String toString() {
		return linkText;
	}

	@Override
	public final void inspect(ObjectInspector inspector) {
		inspector.runLater(() -> doInspect(inspector));
	}
}
