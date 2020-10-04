package dd.kms.marple.impl.gui.filters;

import dd.kms.marple.impl.gui.common.AccessModifierInput;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.wrappers.MemberInfo;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class ValueFilterModifier extends AbstractValueFilter
{
	private final boolean	configureStaticMode;

	private AccessModifier	minimumAccessModifier	= AccessModifier.PRIVATE;
	private boolean			allowStaticModifiers	= false;

	ValueFilterModifier(boolean configureStaticMode) {
		this.configureStaticMode = configureStaticMode;
		allowStaticModifiers = configureStaticMode ? false : true;
	}

	@Override
	public boolean isActive() {
		return minimumAccessModifier != AccessModifier.PRIVATE || !allowStaticModifiers;
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

		if (!configureStaticMode) {
			return accessModifierInput;
		}

		JPanel panel = new JPanel(new GridBagLayout());

		JCheckBox nonStaticModifiersCheckBox = new JCheckBox("non-static");
		nonStaticModifiersCheckBox.setSelected(true);
		nonStaticModifiersCheckBox.setEnabled(false);

		JCheckBox staticModifiersCheckBox = new JCheckBox("static");
		staticModifiersCheckBox.setSelected(allowStaticModifiers);

		staticModifiersCheckBox.addItemListener(e -> setAllowStaticModifiers(staticModifiersCheckBox.isSelected()));

		panel.add(accessModifierInput,			new GridBagConstraints(0, 0, REMAINDER, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		panel.add(nonStaticModifiersCheckBox,	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		panel.add(staticModifiersCheckBox,		new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		return panel;
	}

	@Override
	public boolean test(Object o) {
		if (o instanceof MemberInfo) {
			MemberInfo memberInfo = (MemberInfo) o;
			return memberInfo.getAccessModifier().compareTo(minimumAccessModifier) <= 0
				&& (!memberInfo.isStatic() || allowStaticModifiers);
		}
		return false;
	}

	private void setMinimumAccessModifier(AccessModifier minimumAccessModifier) {
		this.minimumAccessModifier = minimumAccessModifier;
		fireFilterChanged();
	}

	private void setAllowStaticModifiers(boolean allowStaticModifiers) {
		this.allowStaticModifiers = allowStaticModifiers;
		fireFilterChanged();
	}
}
