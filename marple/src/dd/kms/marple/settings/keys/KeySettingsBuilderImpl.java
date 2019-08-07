package dd.kms.marple.settings.keys;

import java.awt.event.KeyEvent;

class KeySettingsBuilderImpl implements KeySettingsBuilder
{
	private KeyRepresentation	inspectionKey			= new KeyRepresentation(KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK,	KeyEvent.VK_I);
	private KeyRepresentation	evaluationKey			= new KeyRepresentation(KeyEvent.ALT_MASK,							KeyEvent.VK_F8);
	private KeyRepresentation	searchKey				= new KeyRepresentation(KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK,	KeyEvent.VK_F);
	private KeyRepresentation	debugSupportKey			= new KeyRepresentation(KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK,	KeyEvent.VK_D);
	private KeyRepresentation	codeCompletionKey		= new KeyRepresentation(KeyEvent.CTRL_MASK,							KeyEvent.VK_SPACE);
	private KeyRepresentation	showMethodArgumentsKey	= new KeyRepresentation(KeyEvent.CTRL_MASK,							KeyEvent.VK_P);

	@Override
	public KeySettingsBuilder inspectionKey(KeyRepresentation inspectionKey) {
		this.inspectionKey = inspectionKey;
		return this;
	}

	@Override
	public KeySettingsBuilder evaluationKey(KeyRepresentation evaluationKey) {
		this.evaluationKey = evaluationKey;
		return this;
	}

	@Override
	public KeySettingsBuilder searchKey(KeyRepresentation searchKey) {
		this.searchKey = searchKey;
		return this;
	}

	@Override
	public KeySettingsBuilder debugSupportKey(KeyRepresentation debugSupportKey) {
		this.debugSupportKey = debugSupportKey;
		return this;
	}

	@Override
	public KeySettingsBuilder codeCompletionKey(KeyRepresentation codeCompletionKey) {
		this.codeCompletionKey = codeCompletionKey;
		return this;
	}

	@Override
	public KeySettingsBuilder showMethodArgumentsKey(KeyRepresentation showMethodArgumentsKey) {
		this.showMethodArgumentsKey = showMethodArgumentsKey;
		return this;
	}

	@Override
	public KeySettings build() {
		return new KeySettingsImpl(inspectionKey, evaluationKey, searchKey, debugSupportKey, codeCompletionKey, showMethodArgumentsKey);
	}
}
