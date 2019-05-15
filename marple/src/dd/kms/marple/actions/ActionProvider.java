package dd.kms.marple.actions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ActionProvider
{
	public static ActionProvider of(String displayText, List<InspectionAction> actions) {
		return new ActionProvider(displayText, actions);
	}

	private final String					displayText;
	private final List<InspectionAction>	actions;

	private ActionProvider(String displayText, List<InspectionAction> actions) {
		this.displayText = displayText;
		this.actions = actions.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	public List<InspectionAction> getActions() {
		return actions;
	}

	@Override
	public String toString() {
		return displayText;
	}
}
