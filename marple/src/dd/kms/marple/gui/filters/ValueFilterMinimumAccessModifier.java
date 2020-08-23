package dd.kms.marple.gui.filters;

import dd.kms.marple.gui.common.AccessModifierInput;
import dd.kms.zenodot.api.common.AccessModifier;

import java.awt.*;

class ValueFilterMinimumAccessModifier extends AbstractValueFilter
{
	private AccessModifier	minimumAccessModifier = AccessModifier.PRIVATE;

	@Override
	public boolean isActive() {
		return minimumAccessModifier != AccessModifier.PRIVATE;
	}

	@Override
	public void addAvailableValue(Object o) {
		/* do nothing */
	}

	@Override
	public Component getEditor() {
		AccessModifierInput accessModifierInput = new AccessModifierInput();
		accessModifierInput.setAccessModifier(minimumAccessModifier);

		accessModifierInput.addChangeListener(e -> setMinimumAccessModifier(accessModifierInput.getAccessModifier()));

		return accessModifierInput;
	}

	@Override
	public boolean test(Object o) {
		if (o instanceof AccessModifier) {
			AccessModifier accessModifier = (AccessModifier) o;
			return accessModifier.compareTo(minimumAccessModifier) <= 0;
		}
		return false;
	}

	private void setMinimumAccessModifier(AccessModifier minimumAccessModifier) {
		this.minimumAccessModifier = minimumAccessModifier;
		fireFilterChanged();
	}
}
