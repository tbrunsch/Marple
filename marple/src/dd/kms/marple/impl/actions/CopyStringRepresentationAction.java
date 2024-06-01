package dd.kms.marple.impl.actions;

import com.google.common.base.Preconditions;
import dd.kms.marple.api.actions.InspectionAction;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class CopyStringRepresentationAction implements InspectionAction
{
	private final Object object;

	public CopyStringRepresentationAction(Object object) {
		Preconditions.checkNotNull(object);
		this.object = object;
	}

	@Override
	public String getName() {
		return "Copy string representation";
	}

	@Override
	public String getDescription() {
		return "Copy '" + getStringToCopy() + "'";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		StringSelection stringSelection = new StringSelection(getStringToCopy());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private String getStringToCopy() {
		return object.toString();
	}
}
