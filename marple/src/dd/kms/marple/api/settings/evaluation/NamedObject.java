package dd.kms.marple.api.settings.evaluation;

public class NamedObject
{
	private final String	name;
	private final Object	object;

	public NamedObject(String name, Object object) {
		this.name = name;
		this.object = object;
	}

	public String getName() {
		return name;
	}

	public Object getObject() {
		return object;
	}
}
