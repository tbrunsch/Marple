package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.gui.highlight.ComponentHighlighter;

import java.awt.*;

public class HighlightComponentAction implements InspectionAction
{
	private final Component	component;

	public HighlightComponentAction(Component component) {
		this.component = component;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public String getName() {
		return "Highlight";
	}

	@Override
	public String getDescription() {
		return "Highlight the component";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		Runnable componentHighlighter = new ComponentHighlighter(component);
		new Thread(componentHighlighter).start();
	}
}
