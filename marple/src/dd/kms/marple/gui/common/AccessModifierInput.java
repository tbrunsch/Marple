package dd.kms.marple.gui.common;

import dd.kms.zenodot.common.AccessModifier;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class AccessModifierInput extends JPanel
{
	private static final int	NUM_ACCESS_MODIFIERS	= AccessModifier.values().length;

	private final JSlider				slider				= new JSlider(0, NUM_ACCESS_MODIFIERS-1);
	private final JLabel				accessModifierLabel	= new JLabel();

	private final Set<ChangeListener>	listeners			= new HashSet<>();

	public AccessModifierInput() {
		super(new GridBagLayout());

		add(slider,					new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
		add(accessModifierLabel,	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, CENTER, HORIZONTAL, new Insets(3, 5, 10, 5), 0, 0));

		accessModifierLabel.setHorizontalAlignment(SwingConstants.CENTER);
		accessModifierLabel.setOpaque(true);

		updateLabelText();

		addListeners();
	}

	private void addListeners() {
		slider.addChangeListener(e -> onSliderChanged());
	}

	private void updateLabelText() {
		AccessModifier accessModifier = getAccessModifier();
		accessModifierLabel.setText(accessModifier.toString());
		accessModifierLabel.setBackground(GuiCommons.getAccessModifierColor(accessModifier));
	}

	public AccessModifier getAccessModifier() {
		return AccessModifier.values()[NUM_ACCESS_MODIFIERS - 1 - slider.getValue()];
	}

	public void setAccessModifier(AccessModifier accessModifier) {
		slider.setValue(NUM_ACCESS_MODIFIERS - 1 - accessModifier.ordinal());
	}

	private void onSliderChanged() {
		updateLabelText();
		fireStateChanged();
	}

	/*
	 * Listener management
	 */
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}

	private void fireStateChanged() {
		ChangeEvent changeEvent = new ChangeEvent(this);
		for (ChangeListener listener : listeners) {
			listener.stateChanged(changeEvent);
		}
	}
}
