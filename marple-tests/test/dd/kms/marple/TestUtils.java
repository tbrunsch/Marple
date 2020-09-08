package dd.kms.marple;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.marple.settings.InspectionSettings;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsUtils;
import dd.kms.zenodot.api.settings.Variable;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class TestUtils
{
	static void setupInspectionFramework(JFrame testFrame) throws ClassNotFoundException {
		Variable variable1 = ParserSettingsUtils.createVariable("testFrame", testFrame, false);
		Variable variable2 = ParserSettingsUtils.createVariable("testData", new TestData(), true);

		String importPackage1 = "javax.swing";
		String importPackage2 = "dd.kms.marple";

		String importClass1 = "com.google.common.collect.ImmutableList";
		String importClass2 = "com.google.common.collect.ImmutableSet";

		ObjectTreeNode customHierarchyRoot = new FileNode(new File(System.getProperty("user.home")));

		ParserSettings parserSettings = ParserSettingsUtils.createBuilder()
			.variables(ImmutableList.of(variable1, variable2))
			.minimumAccessModifier(AccessModifier.PRIVATE)
			.importPackagesByName(ImmutableSet.of(importPackage1, importPackage2))
			.importClassesByName(ImmutableSet.of(importClass1, importClass2))
			.considerAllClassesForClassCompletions(true)
			.customHierarchyRoot(customHierarchyRoot)
			.build();

		InspectionSettings inspectionSettings = ObjectInspectionFramework.createInspectionSettingsBuilder().build();
		inspectionSettings.getEvaluator().setParserSettings(parserSettings);

		ObjectInspectionFramework.register(inspectionSettings);

		DebugSupport.SLOTS[0] = "Use these slots for data exchange between Marple and your debugger.";
		DebugSupport.SLOTS[1] = 42;

		DebugSupport.setSlotValue("demo",			"Yes!");
		DebugSupport.setSlotValue("variableImport",	42);
		DebugSupport.setSlotValue("successful",		1.41);
		DebugSupport.setSlotValue("expectedValues",	Arrays.asList("Yes!", 42, 1.41));
	}

	private static class FileNode implements ObjectTreeNode
	{
		private final File file;

		private List<FileNode> children	= null;

		FileNode(File file) {
			this.file = file;
		}

		@Override
		public String getName() {
			return getFile().getName();
		}

		@Override
		public Iterable<? extends ObjectTreeNode> getChildNodes() {
			if (children == null) {
				children = createChildren();
			}
			return children;
		}

		@Override
		public @Nullable ObjectInfo getUserObject() {
			return InfoProvider.createObjectInfo(file);
		}

		private File getFile() {
			return file;
		}

		private List<FileNode> createChildren() {
			if (!file.isDirectory()) {
				return ImmutableList.of();
			}
			File[] files = file.listFiles();
			if (files == null) {
				return ImmutableList.of();
			}
			ImmutableList.Builder<FileNode> builder = ImmutableList.builder();
			for (boolean isDirectory : new boolean[]{ true, false }) {
				builder.addAll(Arrays.stream(files)
					.filter(file -> file.isDirectory() == isDirectory)
					.map(FileNode::new)
					.collect(Collectors.toList()));
			}
			return builder.build();
		}
	}
}
