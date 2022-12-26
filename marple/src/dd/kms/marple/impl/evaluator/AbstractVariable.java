package dd.kms.marple.impl.evaluator;

import dd.kms.marple.api.evaluator.Variable;

abstract class AbstractVariable implements Variable
{
	private final String	name;
	private final Class<?>	type;
	private final boolean	isFinal;

	AbstractVariable(String name, Class<?> type, boolean isFinal) {
		this.name = name;
		this.type = type;
		this.isFinal = isFinal;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public boolean isFinal() {
		return isFinal;
	}
}
