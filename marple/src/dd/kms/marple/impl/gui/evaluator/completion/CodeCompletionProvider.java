package dd.kms.marple.impl.gui.evaluator.completion;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.CodeCompletion;
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
	private final ParserMediator		parserMediator;
	private final CompletionsFactory	completionsFactory;

	private ListCellRenderer<Object>	renderer;

	public CodeCompletionProvider(ParserMediator parserMediator, @Nullable Consumer<Throwable> exceptionConsumer) {
		this.parserMediator = parserMediator;
		this.completionsFactory = new CompletionsFactory(this, exceptionConsumer);
		setListCellRenderer(new CompletionRenderer());
	}

	List<CodeCompletion> provideCodeCompletions(String text, int caretPosition) throws ParseException {
		return parserMediator.provideCodeCompletions(text, caretPosition);
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
		return GuiCommons.isCaretPositionValid(text, caretPosition)
			? completionsFactory.getCompletions(text, caretPosition)
			: ImmutableList.of();
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
		return GuiCommons.isCaretPositionValid(text, caretPosition)
			? completionsFactory.getParameterizedCompletions(text, caretPosition)
			: ImmutableList.of();
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
