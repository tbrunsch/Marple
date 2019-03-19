package dd.kms.marple;

class ObjectInspectionLink extends AbstractInspectionLink
{
	private final Object object;

	ObjectInspectionLink(Object object) {
		super(object.toString());
		this.object = object;
	}

	@Override
	public Object getObjectToInspect() {
		return object;
	}

	@Override
	void doInspect(ObjectInspector inspector) {
		Object objectToInspect = getObjectToInspect();
		inspector.beginInspection(objectToInspect);
		inspector.inspectObject(objectToInspect);
		inspector.endInspection();
	}
}
