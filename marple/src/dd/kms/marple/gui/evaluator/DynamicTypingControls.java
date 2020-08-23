package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.evaluator.ExpressionEvaluators;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

import javax.swing.*;
import java.awt.*;

class DynamicTypingControls
{
	private static final String	INFO_TEXT	= "<html><p>" +
		"If activated, then the declared type of (sub) expressions is ignored and the runtime type is used instead for, e.g., " +
		"suggesting methods and fields and for method overload resolution.<br/>" +
		"<ul>" +
		"<li><b>+</b> avoids casts</li>" +
		"<li><b>-</b> parsing methods triggers potential side effects</li>" +
		"<li><b>-</b> method overload resolution may yield different results compared to static typing</li>" +
		"</ul>" +
		"</p></html>";

	private final JRadioButton		dynamicTypingOffRadioButton	= new JRadioButton("off");
	private final JRadioButton		dynamicTypingOnRadioButton	= new JRadioButton("on");
	private final ButtonGroup		dynamicTypingButtonGroup	= new ButtonGroup();
	private final JLabel			dynamicTypingInfo			= new JLabel(INFO_TEXT);

	private final JCheckBox			dynamicTypingCheckBox		= new JCheckBox("dynamic typing");

	private final InspectionContext	inspectionContext;

	DynamicTypingControls(InspectionContext inspectionContext) {
		this.inspectionContext = inspectionContext;

		dynamicTypingButtonGroup.add(dynamicTypingOffRadioButton);
		dynamicTypingButtonGroup.add(dynamicTypingOnRadioButton);

		dynamicTypingCheckBox.setToolTipText(INFO_TEXT);

		updateControls();

		dynamicTypingOffRadioButton.addActionListener(e -> onEnableDynamicTypingChangedByRadioButtons());
		dynamicTypingOnRadioButton.addActionListener(e -> onEnableDynamicTypingChangedByRadioButtons());

		dynamicTypingCheckBox.addActionListener(e -> onEnableDynamicTypingChangedByCheckBox());
	}

	JRadioButton getDynamicTypingOffRadioButton() {
		return dynamicTypingOffRadioButton;
	}

	JRadioButton getDynamicTypingOnRadioButton() {
		return dynamicTypingOnRadioButton;
	}

	JLabel getDynamicTypingInfo() {
		return dynamicTypingInfo;
	}

	JCheckBox getDynamicTypingCheckBox() {
		return dynamicTypingCheckBox;
	}

	void updateControls() {
		boolean enableDynamicTyping = isEnableDynamicTyping();
		JRadioButton radioButton = enableDynamicTyping ? dynamicTypingOnRadioButton : dynamicTypingOffRadioButton;
		radioButton.setSelected(true);
		dynamicTypingCheckBox.setSelected(enableDynamicTyping);
		updateDynamicTypingInfo();
	}

	private boolean isEnableDynamicTyping() {
		return ExpressionEvaluators.getValue(ParserSettings::isEnableDynamicTyping, inspectionContext);
	}

	private void setEnableDynamicTyping(boolean enable) {
		ExpressionEvaluators.setValue(enable, ParserSettingsBuilder::enableDynamicTyping, inspectionContext);
	}

	private void updateDynamicTypingInfo() {
		Color fg = isEnableDynamicTyping() ? Color.RED.darker() : Color.GRAY;
		dynamicTypingInfo.setForeground(fg);
	}

	private void onEnableDynamicTypingChangedByRadioButtons() {
		setEnableDynamicTyping(dynamicTypingOnRadioButton.isSelected());
		updateControls();
	}

	private void onEnableDynamicTypingChangedByCheckBox() {
		setEnableDynamicTyping(dynamicTypingCheckBox.isSelected());
		updateControls();
	}
}
