package dd.kms.marple.swing.actions;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.ExpressionEvaluator;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.swing.gui.GuiCommons;
import dd.kms.marple.swing.gui.evaluation.VariablePanel;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.Variable;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AddVariableAction implements InspectionAction
{
	private final String						suggestedName;
	private final Object						value;
	private final InspectionContext<Component>	inspectionContext;

	public AddVariableAction(String suggestedName, Object value, InspectionContext<Component> inspectionContext) {
		this.value = value;
		this.inspectionContext = inspectionContext;
		this.suggestedName = suggestedName;
	}

	@Override
	public String getName() {
		return "Add to variables";
	}

	@Override
	public String getDescription() {
		return "Adds the specified variable to the pool of variables for easier access in expressions.";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		String name = createVariableName(suggestedName);
		ImmutableList.Builder<Variable> variablesBuilder = ImmutableList.builder();
		variablesBuilder.addAll(getVariables());
		variablesBuilder.add(new Variable(name, value, false));
		VariablePanel variablePanel = new VariablePanel(variablesBuilder.build(), inspectionContext);

		GuiCommons.showPanel("Variables", variablePanel);

		ExpressionEvaluator evaluator = inspectionContext.getEvaluator();
		ParserSettings parserSettings = evaluator.getParserSettings().builder()
			.variables(variablePanel.getVariables())
			.build();
		evaluator.setParserSettings(parserSettings);
	}

	private String createVariableName(String suggestedName) {
		Set<String> variableNames = getVariables().stream().map(Variable::getName).collect(Collectors.toSet());
		String name = suggestedName;
		int index = 1;
		while (variableNames.contains(name)) {
			name = suggestedName + (++index);
		}
		return name;
	}

	private List<Variable> getVariables() {
		ExpressionEvaluator evaluator = inspectionContext.getEvaluator();
		ParserSettings parserSettings = evaluator.getParserSettings();
		return parserSettings.getVariables();
	}
}
