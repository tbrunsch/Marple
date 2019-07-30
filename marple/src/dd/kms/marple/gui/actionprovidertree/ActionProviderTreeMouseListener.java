package dd.kms.marple.gui.actionprovidertree;

import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.gui.actionproviders.AbstractActionProviderMouseListener;

import java.awt.event.MouseEvent;

public class ActionProviderTreeMouseListener extends AbstractActionProviderMouseListener
{
	@Override
	protected ActionProvider getActionProvider(MouseEvent e) {
		return ActionProviderTreeNodes.getActionProvider(e);
	}
}
