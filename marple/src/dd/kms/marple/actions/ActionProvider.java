package dd.kms.marple.actions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

public class ActionProvider
{
	public static ActionProvider of(String displayText, InspectionAction... actions) {
		return of(displayText, Arrays.asList(actions));
	}

	public static ActionProvider of(String displayText, List<InspectionAction> actions) {
		return new ActionProvider(displayText, actions);
	}

	private final String					displayText;
	private final List<InspectionAction>	actions;

	private ActionProvider(String displayText, List<InspectionAction> actions) {
		this.displayText = displayText;
		Preconditions.checkArgument(!actions.isEmpty());
		this.actions = ImmutableList.copyOf(actions);
	}

	public List<InspectionAction> getActions() {
		return actions;
	}

	@Override
	public String toString() {
		return displayText;
	}
}
