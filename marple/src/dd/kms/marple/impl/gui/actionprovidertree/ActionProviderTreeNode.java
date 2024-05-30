package dd.kms.marple.impl.gui.actionprovidertree;

import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.Actions;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.MouseEvent;

public interface ActionProviderTreeNode
{
	@Nullable ActionProvider getActionProvider(JTree tree, MouseEvent mouseEvent);

	default String getTrimmedText() {
		return Actions.trimDisplayText(getFullText());
	}

	String getFullText();
}
