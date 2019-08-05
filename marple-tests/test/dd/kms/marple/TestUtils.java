package dd.kms.marple;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import dd.kms.marple.settings.InspectionSettings;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.ParserSettingsUtils;
import dd.kms.zenodot.settings.Variable;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class TestUtils
{
	static void setupInspectionFramework(JFrame testFrame) throws ClassNotFoundException {
		Variable variable1 = ParserSettingsUtils.createVariable("testFrame", testFrame, false);
		Variable variable2 = ParserSettingsUtils.createVariable("testData", new TestData(), true);

		String importPackage1 = "javax.swing";
		String importPackage2 = "dd.kms.marple";

		String importClass1 = "com.google.common.collect.ImmutableList";
		String importClass2 = "com.google.common.collect.ImmutableSet";

		ObjectTreeNode customHierarchyRoot = new PrimeNode(0);

		ParserSettings parserSettings = ParserSettingsUtils.createBuilder()
			.variables(ImmutableList.of(variable1, variable2))
			.minimumAccessLevel(AccessModifier.PRIVATE)
			.importPackagesByName(ImmutableSet.of(importPackage1, importPackage2))
			.importClassesByName(ImmutableSet.of(importClass1, importClass2))
			.customHierarchyRoot(customHierarchyRoot)
			.build();

		InspectionSettings inspectionSettings = ObjectInspectionFramework.createInspectionSettingsBuilder().build();
		inspectionSettings.getEvaluator().setParserSettings(parserSettings);

		ObjectInspectionFramework.register("Test", inspectionSettings);

		DebugSupport.SLOT_0 = "Use these slots for data exchange between Marple and your debugger.";
		DebugSupport.SLOT_1 = 42;

		DebugSupport.setSlotValue("demo",			"Yes!");
		DebugSupport.setSlotValue("variableImport",	42);
		DebugSupport.setSlotValue("successful",		1.41);
		DebugSupport.setSlotValue("expectedValues",	Arrays.asList("Yes!", 42, 1.41));
	}

	private static class PrimeNode implements ObjectTreeNode
	{
		// Use single-element int[] instead of an int to demonstrate action providers
		private final int[]		numbers;

		private List<PrimeNode> primeChildren	= null;

		PrimeNode(int number) {
			this.numbers = new int[]{ number };
		}

		@Override
		public String getName() {
			return String.valueOf(getNumber());
		}

		@Override
		public Iterable<? extends ObjectTreeNode> getChildNodes() {
			if (primeChildren == null) {
				primeChildren = createChildren();
			}
			return primeChildren;
		}

		@Override
		public @Nullable
		Object getUserObject() {
			return numbers;
		}

		private int getNumber() {
			return numbers[0];
		}

		private List<PrimeNode> createChildren() {
			int base = 10*getNumber();
			List<Integer> potentialSummands = base == 0 ? Lists.newArrayList(2, 3, 5, 7) : Lists.newArrayList(1, 3, 7, 9);
			List<PrimeNode> children = new ArrayList<>();
			for (int summand : potentialSummands) {
				int number = base + summand;
				if (IntMath.isPrime(number)) {
					PrimeNode child = new PrimeNode(number);
					children.add(child);
				}
			}
			return children;
		}
	}
}
