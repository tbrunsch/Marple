package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.evaluator.ExpressionEvaluators;
import dd.kms.zenodot.settings.ParserSettingsBuilder;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

class ImportPanel extends JPanel
{
	private static final Insets	DEFAULT_INSETS	= new Insets(5, 5, 5, 5);

	private final CustomImportPanel	packagesPanel;
	private final CustomImportPanel	classesPanel;

	private final InspectionContext	inspectionContext;

	ImportPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.inspectionContext = inspectionContext;

		packagesPanel = new CustomImportPanel("Packages", getPackageNames(), this::isPackageName, this::setPackageNames, inspectionContext);
		classesPanel = new CustomImportPanel("Classes", getClassNames(), this::isClassName, this::setClassNames, inspectionContext);

		add(packagesPanel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(classesPanel,	new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
	}

	public void updateContent() {
		packagesPanel.updateContent(getPackageNames());
		classesPanel.updateContent(getClassNames());
	}

	/*
	 * Packages
	 */
	private boolean isPackageName(String s) {
		return Package.getPackage(s) != null;
	}

	private Collection<String> getPackageNames() {
		return ExpressionEvaluators.getValue(settings -> settings.getImports().getImportedPackageNames(), inspectionContext);
	}

	private void setPackageNames(List<String> packageNames) {
		Set<String> filteredPackageNames = packageNames.stream().filter(this::isPackageName).collect(Collectors.toSet());
		ExpressionEvaluators.setValue(filteredPackageNames, ParserSettingsBuilder::importPackages, inspectionContext);
	}

	/*
	 * Classes
	 */
	private boolean isClassName(String s) {
		try {
			return Class.forName(s) != null;
		} catch (Exception e) {
			return false;
		}
	}

	private Collection<String> getClassNames() {
		Set<ClassInfo> importedClasses = inspectionContext.getEvaluator().getParserSettings().getImports().getImportedClasses();
		return importedClasses.stream().map(ClassInfo::getNormalizedName).collect(Collectors.toList());
	}

	private void setClassNames(List<String> classNames) {
		Set<String> filteredClassNames = classNames.stream().filter(this::isClassName).collect(Collectors.toSet());
		ExpressionEvaluators.setValue(filteredClassNames, ParserSettingsBuilder::importClasses, inspectionContext);
	}}
