package dd.kms.marple.impl.gui.evaluator;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.evaluator.ExpressionEvaluators;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class EvaluationModePanel extends JPanel
{
	// Do not rely on EvaluationMode.values() to return the values in the order we need
	private static final BiMap<Integer, EvaluationMode>	EVALUATION_MODE_BY_VALUE	= ImmutableBiMap.of(
		0, EvaluationMode.STATIC_TYPING,
		1, EvaluationMode.MIXED,
		2, EvaluationMode.DYNAMIC_TYPING
	);

	private static final int	NUM_EVALUATION_MODES	= EVALUATION_MODE_BY_VALUE.size();

	private static final String	INFO_STATIC_TYPING		= "<html><b>Static typing</b>" +
		"<ul>" +
		"<li>evaluation based on declared types</li>" +
		"<li>no side-effects until expression evaluation has completed successfully</li>" +
		"</ul>" +
		"</html>";

	private static final String	INFO_MIXED_TYPING		= "<html><b>Mixed typing</b>" +
		"<ul>" +
		"<li>evaluation based on runtime types</li>" +
		"<li>no methods are executed to determine their runtime return types</li>" +
		"<li>no side-effects until expression evaluation has completed successfully</li>" +
		"<li>avoids most casts</li>" +
		"</ul>" +
		"</html>";

	private static final String	INFO_DYNAMIC_TYPING		= "<html><b>Dynamic typing</b>" +
		"<ul>" +
		"<li>evaluation based on runtime types</li>" +
		"<li>methods are executed to determine their runtime return types</li>" +
		"<li>possible side-effects even before expression evaluation has completed successfully</li>" +
		"<li>avoids all casts</li>" +
		"</ul>" +
		"</html>";

	private static final Map<EvaluationMode, String>	EVALUATION_MODE_TEXTS		= ImmutableMap.of(
		EvaluationMode.STATIC_TYPING,	"static typing",
		EvaluationMode.MIXED,			"mixed typing",
		EvaluationMode.DYNAMIC_TYPING,	"dynamic typing"
	);

	private static final Map<EvaluationMode, String>	EVALUATION_MODE_INFO_TEXTS	= ImmutableMap.of(
		EvaluationMode.STATIC_TYPING,	INFO_STATIC_TYPING,
		EvaluationMode.MIXED,			INFO_MIXED_TYPING,
		EvaluationMode.DYNAMIC_TYPING,	INFO_DYNAMIC_TYPING
	);

	private final JSlider	evaluationModeSlider	= new JSlider(0, NUM_EVALUATION_MODES - 1);
	private final JLabel	evaluationModeLabel		= new JLabel();

	private final InspectionContext	context;

	EvaluationModePanel(InspectionContext context) {
		super(new GridBagLayout());

		this.context = context;

		add(evaluationModeSlider,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
		add(evaluationModeLabel,	new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		evaluationModeLabel.setHorizontalAlignment(SwingConstants.CENTER);

		setPreferredEvaluationModeLabelSize();

		updateControls();

		evaluationModeSlider.addChangeListener(e -> onTypingChangedBySlider());
	}

	private void setPreferredEvaluationModeLabelSize() {
		String oldText = evaluationModeLabel.getText();
		Dimension maxPreferredSize = new Dimension(0, 0);
		for (EvaluationMode evaluationMode : EvaluationMode.values()) {
			evaluationModeLabel.setText(EVALUATION_MODE_TEXTS.get(evaluationMode));
			Dimension preferredSize = evaluationModeLabel.getPreferredSize();
			maxPreferredSize = new Dimension(Math.max(maxPreferredSize.width, preferredSize.width + 20), Math.max(maxPreferredSize.height, preferredSize.height));
		}
		evaluationModeLabel.setText(oldText);
		evaluationModeLabel.setPreferredSize(maxPreferredSize);
	}

	private EvaluationMode getEvaluationMode() {
		return ExpressionEvaluators.getValue(ParserSettings::getEvaluationMode, context);
	}

	private void setEvaluationMode(EvaluationMode evaluationMode) {
		ExpressionEvaluators.setValue(evaluationMode, ParserSettingsBuilder::evaluationMode, context);
	}

	private void onTypingChangedBySlider() {
		int value = evaluationModeSlider.getValue();
		EvaluationMode evaluationMode = EVALUATION_MODE_BY_VALUE.get(value);
		setEvaluationMode(evaluationMode);
		updateControls();
	}

	void updateControls() {
		EvaluationMode evaluationMode = getEvaluationMode();

		int value = EVALUATION_MODE_BY_VALUE.inverse().get(evaluationMode);
		String evaluationModeText = EVALUATION_MODE_TEXTS.get(evaluationMode);
		String evaluationModeInfoText = EVALUATION_MODE_INFO_TEXTS.get(evaluationMode);

		evaluationModeSlider.setValue(value);
		evaluationModeSlider.setToolTipText(evaluationModeInfoText);

		evaluationModeLabel.setText(evaluationModeText);
		evaluationModeLabel.setToolTipText(evaluationModeInfoText);
	}
}
