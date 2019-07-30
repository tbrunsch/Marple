package dd.kms.marple.gui.actionprovidertree;

import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.gui.actionproviders.AbstractActionProviderMouseMotionListener;

import java.awt.event.MouseEvent;

public class ActionProviderTreeMouseMotionListener extends AbstractActionProviderMouseMotionListener
{
	@Override
	protected ActionProvider getActionProvider(MouseEvent e) {
		return ActionProviderTreeNodes.getActionProvider(e);
	}
}
