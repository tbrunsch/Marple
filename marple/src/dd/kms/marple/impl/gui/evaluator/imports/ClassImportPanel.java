package dd.kms.marple.impl.gui.evaluator.imports;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.AbstractInputTextField;
import dd.kms.marple.impl.gui.evaluator.textfields.ClassInputTextField;
import dd.kms.zenodot.api.settings.ParserSettings;

import java.util.Collection;
import java.util.List;

class ClassImportPanel extends AbstractImportPanel<Class<?>>
{
	ClassImportPanel(InspectionContext context) {
		super("Classes", context);
	}

	@Override
	AbstractInputTextField<Class<?>> createEvaluationTextField() {
		return new ClassInputTextField(context);
	}

	@Override
	Collection<Class<?>> getImports() {
		return getParserSettings().getImports().getImportedClasses();
	}

	@Override
	void setImports(List<Class<?>> imports) {
		ParserSettings parserSettings = getParserSettings().builder().importClasses(imports).build();
		setParserSettings(parserSettings);
	}
}
