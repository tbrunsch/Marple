package dd.kms.marple;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.marple.api.DebugSupport;
import dd.kms.marple.api.ObjectInspectionFramework;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettingsBuilder;
import dd.kms.marple.api.settings.evaluation.NamedObject;
import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class TestUtils
{
	static void setupInspectionFramework(JFrame testFrame) throws ClassNotFoundException {
		Variable variable1 = Variable.create("testFrame", JFrame.class, testFrame, true, false);
		Variable variable2 = Variable.create("testData", TestData.class, new TestData(), true, true);

		String importPackage1 = "javax.swing";
		String importPackage2 = "dd.kms.marple";
		String importPackage3 = DebugSupport.class.getPackage().getName();

		String importClass1 = "com.google.common.collect.ImmutableList";
		String importClass2 = "com.google.common.collect.ImmutableSet";

		ObjectTreeNode customHierarchyRoot = new FileNode(new File(System.getProperty("user.home")));

		ParserSettings parserSettings = ParserSettingsBuilder.create()
			.minimumAccessModifier(AccessModifier.PRIVATE)
			.importPackages(ImmutableSet.of(importPackage1, importPackage2, importPackage3))
			.importClassesByName(ImmutableSet.of(importClass1, importClass2))
			.considerAllClassesForClassCompletions(true)
			.customHierarchyRoot(customHierarchyRoot)
			.build();

		EvaluationSettings evaluationSettings = EvaluationSettingsBuilder.create()
			.suggestExpressionToEvaluate(DefaultMutableTreeNode.class, "this.getUserObject()")
			.addRelatedObjectsProvider(JTable.class, table ->
				Arrays.asList(
					new NamedObject("Model", table.getModel()),
					new NamedObject("Column model", table.getColumnModel()),
					new NamedObject("Cell editor", table.getCellEditor()),
					new NamedObject("Cell selection enabled", table.getCellSelectionEnabled()),
					new NamedObject("Num rows", table.getRowHeight())
				)
			)
			.addRelatedObjectsProvider(DefaultMutableTreeNode.class, node -> Collections.singletonList(new NamedObject("Root", node.getRoot())))
			.build();

		CustomAction triggerBreakpointAction = CustomAction.create(
			"Trigger breakpoint",
			DebugSupport.class.getSimpleName() + ".triggerBreakpoint(this)",
			Object.class,
			new KeyRepresentation(KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, KeyEvent.VK_B)
		);
		CustomActionSettings customActionSettings = CustomActionSettings.of(triggerBreakpointAction);

		InspectionSettings inspectionSettings = ObjectInspectionFramework.createInspectionSettingsBuilder()
			.evaluationSettings(evaluationSettings)
			.customActionSettings(customActionSettings)
			.build();
		ExpressionEvaluator evaluator = inspectionSettings.getEvaluator();
		evaluator.setParserSettings(parserSettings);
		evaluator.setVariables(ImmutableList.of(variable1, variable2));

		CompletableFuture.runAsync(ObjectInspectionFramework::preloadClasses);
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
		public @Nullable Object getUserObject() {
			return file;
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
