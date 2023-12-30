package dd.kms.marple.framework.common;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class UniformDocumentListener implements DocumentListener
{
	public static DocumentListener create(Runnable onDocumentChanged) {
		return new UniformDocumentListener() {
			@Override
			protected void onDocumentChanged() {
				onDocumentChanged.run();
			}
		};
	}

	protected abstract void onDocumentChanged();

	@Override
	public void insertUpdate(DocumentEvent e) {
		onDocumentChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		onDocumentChanged();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		onDocumentChanged();
	}
}
