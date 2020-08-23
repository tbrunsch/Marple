package dd.kms.marple.gui.evaluator.imports;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.evaluator.textfields.AbstractInputTextField;
import dd.kms.marple.gui.evaluator.textfields.PackageInputTextField;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.PackageInfo;

import java.util.Collection;
import java.util.List;

class PackageImportPanel extends AbstractImportPanel<PackageInfo>
{
	PackageImportPanel(InspectionContext inspectionContext) {
		super("Packages", inspectionContext);
	}

	@Override
	AbstractInputTextField<PackageInfo> createEvaluationTextField() {
		return new PackageInputTextField(inspectionContext);
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
