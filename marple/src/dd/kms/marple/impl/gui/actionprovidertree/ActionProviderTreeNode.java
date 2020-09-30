package dd.kms.marple.impl.gui.actionprovidertree;

import javax.annotation.Nullable;

import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.Actions;

public interface ActionProviderTreeNode
{
	@Nullable ActionProvider getActionProvider();

	default String getTrimmedText() {
		return Actions.trimDisplayText(getFullText());
	}

	String getFullText();
}
