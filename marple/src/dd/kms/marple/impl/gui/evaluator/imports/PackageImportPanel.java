package dd.kms.marple.impl.gui.evaluator.imports;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.AbstractInputTextField;
import dd.kms.marple.impl.gui.evaluator.textfields.PackageInputTextField;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.PackageInfo;

import java.util.Collection;
import java.util.List;

class PackageImportPanel extends AbstractImportPanel<PackageInfo>
{
	PackageImportPanel(InspectionContext context) {
		super("Packages", context);
	}

	@Override
	AbstractInputTextField<PackageInfo> createEvaluationTextField() {
		return new PackageInputTextField(context);
	}

	@Override
	Collection<PackageInfo> getImports() {
		return getParserSettings().getImports().getImportedPackages();
	}

	@Override
	void setImports(List<PackageInfo> imports) {
		ParserSettings parserSettings = getParserSettings().builder().importPackages(imports).build();
		setParserSettings(parserSettings);
	}
}
