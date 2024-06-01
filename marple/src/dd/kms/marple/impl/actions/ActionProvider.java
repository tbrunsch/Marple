package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ActionProvider
{
	public static ActionProvider of(String displayText, List<InspectionAction> actions, @Nullable InspectionAction defaultAction) {
		return new ActionProvider(displayText, actions, defaultAction);
	}

	private final String					displayText;
	private final List<InspectionAction>	actions;
	@Nullable
	private final InspectionAction			defaultAction;

	private ActionProvider(String displayText, List<InspectionAction> actions, @Nullable InspectionAction defaultAction) {
		this.displayText = displayText;
		this.actions = actions.stream().filter(Objects::nonNull).collect(Collectors.toList());
		this.defaultAction = defaultAction;
	}

	public List<InspectionAction> getActions() {
		return actions;
	}

	public Optional<InspectionAction> getDefaultAction() {
		return Optional.ofNullable(defaultAction);
	}

	@Override
	public String toString() {
		return displayText;
	}
}
