package dd.kms.marple.actions;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.evaluator.ExpressionEvaluators;
import dd.kms.marple.gui.common.WindowManager;
import dd.kms.marple.gui.evaluator.VariablePanel;
import dd.kms.zenodot.settings.ParserSettingsUtils;
import dd.kms.zenodot.settings.Variable;

import java.util.Set;
import java.util.stream.Collectors;

public class AddVariableAction implements InspectionAction
{
	private final String			suggestedName;
	private final Object			value;
	private final InspectionContext	inspectionContext;

	public AddVariableAction(String suggestedName, Object value, InspectionContext inspectionContext) {
		this.value = value;
		this.inspectionContext = inspectionContext;
		this.suggestedName = suggestedName;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
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
		variablesBuilder.addAll(ExpressionEvaluators.getVariables(inspectionContext));
		variablesBuilder.add(ParserSettingsUtils.createVariable(name, value, false));
		ExpressionEvaluators.setVariables(variablesBuilder.build(), inspectionContext);
		WindowManager.showInFrame(VariablePanel.WINDOW_TITLE, this::createVariablePanel, variablePanel -> variablePanel.editVariableName(name), VariablePanel::updateContent);
	}

	private String createVariableName(String suggestedName) {
		Set<String> variableNames = ExpressionEvaluators.getVariables(inspectionContext).stream().map(Variable::getName).collect(Collectors.toSet());
		String name = suggestedName;
		int index = 1;
		while (variableNames.contains(name)) {
			name = suggestedName + (++index);
		}
		return name;
	}

	private VariablePanel createVariablePanel() {
		return new VariablePanel(inspectionContext);
	}
}
