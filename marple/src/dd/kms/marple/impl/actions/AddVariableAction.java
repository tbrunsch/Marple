package dd.kms.marple.impl.actions;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.impl.gui.common.WindowManager;
import dd.kms.marple.impl.gui.evaluator.VariablePanel;

import java.util.Set;
import java.util.stream.Collectors;

public class AddVariableAction implements InspectionAction
{
	private final String			suggestedName;
	private final Object			value;
	private final InspectionContext	context;

	public AddVariableAction(String suggestedName, Object value, InspectionContext context) {
		this.value = value;
		this.context = context;
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
		String name = createVariableName(suggestedName == null ? "variable" : suggestedName);
		ImmutableList.Builder<Variable> variablesBuilder = ImmutableList.builder();
		ExpressionEvaluator evaluator = context.getEvaluator();
		variablesBuilder.addAll(evaluator.getVariables());
		variablesBuilder.add(Variable.create(name, value != null ? value.getClass() : Object.class, value, true, true));
		evaluator.setVariables(variablesBuilder.build());
		WindowManager.showInFrame(VariablePanel.WINDOW_TITLE, this::createVariablePanel, variablePanel -> variablePanel.editVariableName(name), VariablePanel::updateContent);
	}

	private String createVariableName(String suggestedName) {
		ExpressionEvaluator evaluator = context.getEvaluator();
		Set<String> variableNames = evaluator.getVariables().stream().map(Variable::getName).collect(Collectors.toSet());
		String name = suggestedName;
		int index = 1;
		while (variableNames.contains(name)) {
			name = suggestedName + (++index);
		}
		return name;
	}

	private VariablePanel createVariablePanel() {
		return new VariablePanel(context);
	}
}
