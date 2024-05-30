package dd.kms.marple.impl.gui.evaluator.textfields;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.ObjectInspectionFramework;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.api.settings.keys.KeyFunction;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.impl.gui.evaluator.completion.CodeCompletionDecorators;
import dd.kms.marple.impl.gui.evaluator.completion.ParserMediator;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.Imports;
import dd.kms.zenodot.api.settings.ParserSettings;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractInputTextField<T> extends JTextField implements ParserMediator
{
	private static volatile boolean	PRELOADED_CLASSES	= false;

	static List<CodeCompletion> filterCompletions(List<CodeCompletion> completions) {
		return completions.stream()
			.filter(completion -> completion.getRating().getNameMatch() != StringMatch.NONE)
			.collect(Collectors.toList());
	}

	private final ExpressionEvaluator	expressionEvaluator;
	private final List<Class<?>>		temporarilyImportedClasses	= new ArrayList<>();

	private Consumer<T>					evaluationResultConsumer	= result -> {};
	private Consumer<Throwable>			exceptionConsumer			= e -> {};

	/*
	 * Cached Data
	 */
	private String						cachedText					= null;
	private int							cachedCaretPosition			= -1;
	private List<CodeCompletion>		cachedRatedSuggestions		= ImmutableList.of();
	private ParseException				cachedParseException		= null;

	AbstractInputTextField(InspectionContext context) {
		this.expressionEvaluator = context.getEvaluator();

		if (!PRELOADED_CLASSES) {
			PRELOADED_CLASSES = true;
			CompletableFuture.runAsync(ObjectInspectionFramework::preloadClasses);
		}

		KeySettings keySettings = context.getSettings().getKeySettings();


		CodeCompletionDecorators.decorate(
			this,
			this,
			keySettings.getKey(KeyFunction.CODE_COMPLETION),
			keySettings.getKey(KeyFunction.SHOW_METHOD_ARGUMENTS),
			expressionEvaluator.getExpressionHistory()
		);
	}

	abstract List<CodeCompletion> doProvideCompletions(String text, int caretPosition) throws ParseException;
	abstract T evaluate(String text) throws ParseException;

	public void setEvaluationResultConsumer(Consumer<T> evaluationResultConsumer) {
		this.evaluationResultConsumer = evaluationResultConsumer;
	}

	/**
	 * The exception consumer will also be called with {@code null} to notify the consumer
	 * that currently there is no exception.
	 */
	public void setExceptionConsumer(Consumer<Throwable> exceptionConsumer) {
		this.exceptionConsumer = exceptionConsumer;
	}

	public void addInputVerifier() {
		setInputVerifier(new ParserVerifier());
	}

	public T evaluateText() throws ParseException {
		return evaluate(getText());
	}

	ParserSettings getParserSettings() {
		ParserSettings parserSettings = expressionEvaluator.getParserSettings();
		if (temporarilyImportedClasses.isEmpty()) {
			return parserSettings;
		}
		Set<Class<?>> importedClasses = new LinkedHashSet<>();
		importedClasses.addAll(parserSettings.getImports().getImportedClasses());
		importedClasses.addAll(temporarilyImportedClasses);
		return parserSettings.builder()
				.importClasses(importedClasses)
				.build();
	}

	List<Variable> getVariables() {
		return expressionEvaluator.getVariables();
	}

	void setVariables(List<Variable> variables) {
		expressionEvaluator.setVariables(variables);
	}

	@Override
	public List<CodeCompletion> provideCodeCompletions(String text, int caretPosition) throws ParseException {
		if (!Objects.equals(text, cachedText) || caretPosition != cachedCaretPosition) {
			cachedText = text;
			cachedCaretPosition = caretPosition;
			try {
				cachedRatedSuggestions = doProvideCompletions(text, caretPosition);
				cachedParseException = null;
			} catch (ParseException e) {
				cachedParseException = e;
			}
		}
		if (cachedParseException != null) {
			throw cachedParseException;
		}
		return cachedRatedSuggestions;
	}

	@Override
	public void consumeText(String text) {
		try {
			T evaluationResult = evaluate(text);
			evaluationResultConsumer.accept(evaluationResult);
		} catch (Throwable t) {
			consumeException(t);
		}
	}

	@Override
	public void consumeException(Throwable t) {
		exceptionConsumer.accept(t);
	}

	@Override
	public boolean isClassImported(String unqualifiedClassName) {
		Imports imports = getParserSettings().getImports();
		Set<Class<?>> importedClasses = imports.getImportedClasses();
		for (Class<?> importedClass : importedClasses) {
			if (importedClass.getSimpleName().equals(unqualifiedClassName)) {
				return true;
			}
		}
		Set<String> importedPackages = imports.getImportedPackages();
		for (String importedPackage : importedPackages) {
			try {
				String qualifiedClassName = importedPackage + "." + unqualifiedClassName;
				/* Class<?> ignored =*/ Class.forName(qualifiedClassName);
				return true;
			} catch (ClassNotFoundException e) {
				/* class not imported from this package */
			}
		}
		return false;
	}

	@Override
	public boolean isClassImportedTemporarily(String normalizedClassName) {
		return temporarilyImportedClasses.stream()
			.anyMatch(c -> Objects.equals(c.getName(), normalizedClassName));
	}

	@Override
	public void importClassTemporarily(Class<?> clazz) {
		temporarilyImportedClasses.add(clazz);
		clearCache();

	}

	@Override
	public void removeClassFromTemporaryImports(Class<?> clazz) {
		temporarilyImportedClasses.remove(clazz);
		clearCache();
	}

	private void clearCache() {
		cachedText = null;
		cachedCaretPosition = -1;
		cachedRatedSuggestions = ImmutableList.of();
		cachedParseException = null;
	}

	private class ParserVerifier extends InputVerifier
	{
		@Override
		public boolean verify(JComponent input) {
			if (!(input instanceof JTextComponent)) {
				return false;
			}
			JTextComponent textInput = (JTextComponent) input;
			String text = textInput.getText();
			if (Strings.isNullOrEmpty(text)) {
				// allow leaving the text component if no text is entered
				return true;
			}
			try {
				evaluate(text);
				return true;
			} catch (ParseException e) {
				return false;
			}
		}
	}
}
