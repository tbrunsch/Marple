package dd.kms.marple.gui.evaluator.imports;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.evaluator.textfields.AbstractInputTextField;
import dd.kms.marple.gui.evaluator.textfields.ClassInputTextField;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.ClassInfo;

import java.util.Collection;
import java.util.List;

class ClassImportPanel extends AbstractImportPanel<ClassInfo>
{
	ClassImportPanel(InspectionContext inspectionContext) {
		super("Classes", inspectionContext);
	}

	@Override
	AbstractInputTextField<ClassInfo> createEvaluationTextField() {
		return new ClassInputTextField(inspectionContext);
	}

	@Override
	Collection<ClassInfo> getImports() {
		return getParserSettings().getImports().getImportedClasses();
	}

	@Override
	void setImports(List<ClassInfo> imports) {
		ParserSettings parserSettings = getParserSettings().builder().importClasses(imports).build();
		setParserSettings(parserSettings);
	}
}
