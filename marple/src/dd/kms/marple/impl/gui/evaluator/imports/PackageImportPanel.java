package dd.kms.marple.impl.gui.evaluator.imports;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.AbstractInputTextField;
import dd.kms.marple.impl.gui.evaluator.textfields.PackageInputTextField;
import dd.kms.zenodot.api.settings.ParserSettings;

import java.util.Collection;
import java.util.List;

class PackageImportPanel extends AbstractImportPanel<String>
{
	PackageImportPanel(InspectionContext context) {
		super("Packages", context);
	}

	@Override
	AbstractInputTextField<String> createEvaluationTextField() {
		return new PackageInputTextField(context);
	}

	@Override
	Collection<String> getImports() {
		return getParserSettings().getImports().getImportedPackages();
	}

	@Override
	void setImports(List<String> imports) {
		ParserSettings parserSettings = getParserSettings().builder().importPackages(imports).build();
		setParserSettings(parserSettings);
	}
}
