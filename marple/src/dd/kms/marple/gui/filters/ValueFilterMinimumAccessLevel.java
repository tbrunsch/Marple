package dd.kms.marple.gui.filters;

import dd.kms.marple.gui.common.AccessModifierInput;
import dd.kms.zenodot.common.AccessModifier;

import java.awt.*;

class ValueFilterMinimumAccessLevel extends AbstractValueFilter
{
	private AccessModifier	minimumAccessLevel	= AccessModifier.PRIVATE;

	@Override
	public boolean isActive() {
		return minimumAccessLevel != AccessModifier.PRIVATE;
	}

	@Override
	public void addAvailableValue(Object o) {
		/* do nothing */
	}

	@Override
	public Component getEditor() {
		AccessModifierInput accessModifierInput = new AccessModifierInput();
		accessModifierInput.setAccessModifier(minimumAccessLevel);

		accessModifierInput.addChangeListener(e -> setMinimumAccessLevel(accessModifierInput.getAccessModifier()));

		return accessModifierInput;
	}

	@Override
	public boolean test(Object o) {
		if (o instanceof AccessModifier) {
			AccessModifier accessModifier = (AccessModifier) o;
			return accessModifier.compareTo(minimumAccessLevel) <= 0;
		}
		return false;
	}

	private void setMinimumAccessLevel(AccessModifier minimumAccessLevel) {
		this.minimumAccessLevel = minimumAccessLevel;
		fireFilterChanged();
	}
}
