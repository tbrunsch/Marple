package dd.kms.marple.gui.evaluator.completion;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.ParameterChoicesProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class CodeCompletionProvider implements CompletionProvider
{
	private final CompletionsFactory	completionsFactory;

	private ListCellRenderer<Object>	renderer;

	public CodeCompletionProvider(CompletionSuggestionProvider suggestionProvider, @Nullable Consumer<Throwable> exceptionConsumer) {
		completionsFactory = new CompletionsFactory(this, suggestionProvider, exceptionConsumer);
		setListCellRenderer(new CompletionRenderer());
	}

	@Override
	public void clearParameterizedCompletionParams() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAlreadyEnteredText(JTextComponent textComponent) {
		return "";
	}

	@Override
	public List<Completion> getCompletions(JTextComponent textComponent) {
		String text = textComponent.getText();
		int caretPosition = textComponent.getCaretPosition();
		return completionsFactory.getCompletions(text, caretPosition);
	}

	@Override
	public ParameterChoicesProvider getParameterChoicesProvider() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Completion> getCompletionsAt(JTextComponent textComponent, Point point) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ParameterizedCompletion> getParameterizedCompletions(JTextComponent textComponent) {
		String text = textComponent.getText();
		int caretPosition = textComponent.getCaretPosition();
		return completionsFactory.getParameterizedCompletions(text, caretPosition);
	}

	@Override
	public char getParameterListEnd() {
		return ')';
	}

	@Override
	public String getParameterListSeparator() {
		return ", ";
	}

	@Override
	public char getParameterListStart() {
		return '(';
	}

	@Override
	public CompletionProvider getParent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAutoActivateOkay(JTextComponent textComponent) {
		return true;
	}

	@Override
	public ListCellRenderer<Object> getListCellRenderer() {
		return renderer;
	}

	@Override
	public void setListCellRenderer(ListCellRenderer<Object> listCellRenderer) {
		renderer = listCellRenderer;
	}

	@Override
	public void setParameterizedCompletionParams(char c, String s, char c1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setParent(CompletionProvider completionProvider) {
		throw new UnsupportedOperationException();
	}

}
