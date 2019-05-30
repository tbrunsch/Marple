package dd.kms.marple.gui.evaluator.completion;

import com.google.common.base.Strings;
import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.settings.ParserSettings;

import javax.swing.*;
import javax.swing.text.JTextComponent;

public class ExpressionVerifiers
{
	private static final InputVerifier	PACKAGE_NAME_VERIFIER	= new PackageNameVerifier();
	private static final InputVerifier	CLASS_NAME_VERIFIER		= new ClassNameVerifier();

	public static void addPackageNameVerifier(JTextComponent component) {
		component.setInputVerifier(PACKAGE_NAME_VERIFIER);
	}

	public static void addClassNameVerifier(JTextComponent component) {
		component.setInputVerifier(CLASS_NAME_VERIFIER);
	}

	public static void addExpressionVerifier(JTextComponent component, Object thisValue, InspectionContext inspectionContext) {
		component.setInputVerifier(new ExpressionVerifier(thisValue, inspectionContext));
	}

	public static boolean isPackageName(String s) {
		// TODO: Use parser
		return Package.getPackage(s) != null;
	}

	public static boolean isClassName(String s) {
		// TODO: Use parser
		try {
			return Class.forName(s) != null;
		} catch (Throwable t) {
			return false;
		}
	}

	private static class PackageNameVerifier extends InputVerifier
	{
		@Override
		public boolean verify(JComponent input) {
			if (!(input instanceof JTextComponent)) {
				return false;
			}
			JTextComponent textComponent = (JTextComponent) input;
			String text = textComponent.getText();
			return Strings.isNullOrEmpty(text) || isPackageName(text);
		}
	}

	private static class ClassNameVerifier extends InputVerifier
	{
		@Override
		public boolean verify(JComponent input) {
			if (!(input instanceof JTextComponent)) {
				return false;
			}
			JTextComponent textComponent = (JTextComponent) input;
			String text = textComponent.getText();
			return Strings.isNullOrEmpty(text) || isClassName(text);
		}
	}

	private static class ExpressionVerifier extends InputVerifier
	{
		private final Object			thisValue;
		private final InspectionContext	inspectionContext;

		ExpressionVerifier(Object thisValue, InspectionContext inspectionContext) {
			this.thisValue = thisValue;
			this.inspectionContext = inspectionContext;
		}

		@Override
		public boolean verify(JComponent input) {
			if (!(input instanceof JTextComponent)) {
				return false;
			}
			JTextComponent textInput = (JTextComponent) input;
			String text = textInput.getText();
			if (Strings.isNullOrEmpty(text)) {
				return true;
			}
			ParserSettings parserSettings = inspectionContext.getEvaluator().getParserSettings();
			try {
				JavaParser parser = new JavaParser(text, thisValue, parserSettings);
				parser.evaluate();
				return true;
			} catch (ParseException e) {
				return false;
			}
		}
	}
}
