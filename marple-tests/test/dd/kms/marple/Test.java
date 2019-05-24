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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Test
{
	public static void main(String[] args) {
		JFrame testFrame = new TestFrame();

		TestUtils.setupInspectionFramework(testFrame);

		testFrame.pack();
		testFrame.setVisible(true);
	}
}
