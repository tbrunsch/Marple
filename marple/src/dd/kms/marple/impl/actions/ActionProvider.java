package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ActionProvider
{
	public static ActionProvider of(String displayText, List<InspectionAction> actions, Supplier<InspectionAction> defaultActionSupplier) {
		return new ActionProvider(displayText, actions, defaultActionSupplier);
	}

	private final String						displayText;
	private final List<InspectionAction>		actions;
	private final Supplier<InspectionAction>	defaultActionSupplier;

	private ActionProvider(String displayText, List<InspectionAction> actions, Supplier<InspectionAction> defaultActionSupplier) {
		this.displayText = displayText;
		this.actions = actions.stream().filter(Objects::nonNull).collect(Collectors.toList());
		this.defaultActionSupplier = defaultActionSupplier;
	}

	public List<InspectionAction> getActions() {
		return actions;
	}

	public Optional<InspectionAction> getDefaultAction() {
		InspectionAction defaultAction = defaultActionSupplier.get();
		return Optional.ofNullable(defaultAction);
	}

	@Override
	public String toString() {
		return displayText;
	}
}
