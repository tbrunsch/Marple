package dd.kms.marple.impl.actions;

import com.google.common.collect.Iterables;
import dd.kms.marple.api.actions.InspectionAction;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ActionProvider
{
	public static ActionProvider of(String displayText, List<InspectionAction> actions, boolean executeDefaultAction) {
		return new ActionProvider(displayText, actions, executeDefaultAction);
	}

	private final String					displayText;
	private final List<InspectionAction>	actions;
	private final boolean					executeDefaultAction;

	private ActionProvider(String displayText, List<InspectionAction> actions, boolean executeDefaultAction) {
		this.displayText = displayText;
		this.actions = actions.stream().filter(Objects::nonNull).collect(Collectors.toList());
		this.executeDefaultAction = executeDefaultAction;
	}

	public List<InspectionAction> getActions() {
		return actions;
	}

	public Optional<InspectionAction> getDefaultAction() {
		List<InspectionAction> defaultActions = actions.stream()
			.filter(InspectionAction::isDefaultAction)
			.collect(Collectors.toList());
		return executeDefaultAction && defaultActions.size() == 1
				? Optional.of(Iterables.getOnlyElement(defaultActions))
				: Optional.empty();
	}

	@Override
	public String toString() {
		return displayText;
	}
}
