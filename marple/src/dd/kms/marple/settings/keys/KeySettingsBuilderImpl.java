package dd.kms.marple.settings.keys;

import java.awt.event.KeyEvent;

class KeySettingsBuilderImpl implements KeySettingsBuilder
{
	private KeyRepresentation	inspectionKey			= new KeyRepresentation(KeyEvent.VK_I, 		KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
	private KeyRepresentation	evaluationKey			= new KeyRepresentation(KeyEvent.VK_F8,		KeyEvent.ALT_MASK);
	private KeyRepresentation	searchKey				= new KeyRepresentation(KeyEvent.VK_F,		KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
	private KeyRepresentation	debugSupportKey			= new KeyRepresentation(KeyEvent.VK_D,		KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
	private KeyRepresentation	codeCompletionKey		= new KeyRepresentation(KeyEvent.VK_SPACE,	KeyEvent.CTRL_MASK);
	private KeyRepresentation	showMethodArgumentsKey	= new KeyRepresentation(KeyEvent.VK_P,		KeyEvent.CTRL_MASK);

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
