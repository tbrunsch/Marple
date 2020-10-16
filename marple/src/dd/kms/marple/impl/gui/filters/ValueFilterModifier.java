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
	private final JPanel				editor						= new JPanel(new GridBagLayout());
	private final AccessModifierInput	accessModifierInput			= new AccessModifierInput();
	private final JCheckBox				nonStaticModifiersCheckBox	= new JCheckBox("non-static");
	private final JCheckBox				staticModifiersCheckBox		= new JCheckBox("static");

	private final boolean				configureStaticMode;

	ValueFilterModifier(boolean configureStaticMode) {
		this.configureStaticMode = configureStaticMode;

		editor.add(accessModifierInput,			new GridBagConstraints(0, 0, REMAINDER, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		editor.add(nonStaticModifiersCheckBox,	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		editor.add(staticModifiersCheckBox,		new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		accessModifierInput.setAccessModifier(AccessModifier.PRIVATE);
		accessModifierInput.addChangeListener(e -> fireFilterChanged());

		nonStaticModifiersCheckBox.setSelected(true);
		nonStaticModifiersCheckBox.setEnabled(false);

		boolean allowStaticModifiers = configureStaticMode ? false : true;
		staticModifiersCheckBox.setSelected(allowStaticModifiers);
		staticModifiersCheckBox.addItemListener(e -> fireFilterChanged());
	}

	@Override
	public boolean isActive() {
		return getMinimumAccessModifier() != AccessModifier.PRIVATE || !allowStaticModifiers();
	}

	@Override
	public void addAvailableValue(Object o) {
		/* do nothing */
	}

	@Override
	public Component getEditor() {
		return configureStaticMode ? editor : accessModifierInput;
	}

	@Override
	public Object getSettings() {
		return new ValueFilterModifierSettings(getMinimumAccessModifier(), allowStaticModifiers());
	}

	@Override
	public void applySettings(Object settings) {
		if (settings instanceof ValueFilterModifierSettings) {
			ValueFilterModifierSettings filterSettings = (ValueFilterModifierSettings) settings;
			accessModifierInput.setAccessModifier(filterSettings.getMinimumAccessModifier());
			staticModifiersCheckBox.setSelected(filterSettings.isAllowStaticModifiers());
		}
	}

	@Override
	public boolean test(Object o) {
		if (o instanceof MemberInfo) {
			MemberInfo memberInfo = (MemberInfo) o;
			return memberInfo.getAccessModifier().compareTo(getMinimumAccessModifier()) <= 0
				&& (!memberInfo.isStatic() || allowStaticModifiers());
		}
		return false;
	}

	private AccessModifier getMinimumAccessModifier() {
		return accessModifierInput.getAccessModifier();
	}

	private boolean allowStaticModifiers() {
		return staticModifiersCheckBox.isSelected();
	}

	private static class ValueFilterModifierSettings
	{
		private final AccessModifier	minimumAccessModifier;
		private final boolean			allowStaticModifiers;

		ValueFilterModifierSettings(AccessModifier minimumAccessModifier, boolean allowStaticModifiers) {
			this.minimumAccessModifier = minimumAccessModifier;
			this.allowStaticModifiers = allowStaticModifiers;
		}

		AccessModifier getMinimumAccessModifier() {
			return minimumAccessModifier;
		}

		boolean isAllowStaticModifiers() {
			return allowStaticModifiers;
		}
	}
}
