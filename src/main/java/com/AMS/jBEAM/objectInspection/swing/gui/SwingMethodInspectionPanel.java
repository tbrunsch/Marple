package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.InspectionLinkIF;
import com.AMS.jBEAM.javaParser.ReflectionUtils;
import com.AMS.jBEAM.objectInspection.ObjectInspector;
import com.AMS.jBEAM.objectInspection.common.AccessModifier;
import com.AMS.jBEAM.objectInspection.swing.gui.table.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class SwingMethodInspectionPanel extends JPanel
{
	private final ListBasedTable<Method> table;

	public SwingMethodInspectionPanel(final Object object) {
		super(new GridBagLayout());

		List<Method> methods = ReflectionUtils.getMethods(object.getClass());
		List<ColumnDescriptionIF<Method>> columnDescriptions = createColumnDescriptionsFor(object);

		table = new ListBasedTable<>(methods, columnDescriptions);
		JTable internalTable = table.getInternalTable();
		internalTable.getColumnModel().getColumn(1).setCellRenderer(new RunnableRenderer());
		internalTable.setDefaultRenderer(AccessModifier.class, new AccessModifierRenderer());

		add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}

	private static List<ColumnDescriptionIF<Method>> createColumnDescriptionsFor(final Object object) {
		return Arrays.asList(
			new ColumnDescription<>("Return Type",	String.class,			method -> method.getReturnType().getSimpleName(),		new TableValueFilter_Wildcard()),
			new ColumnDescription<>("Name",			Object.class,			method -> getMethodNameLink(method, object),			new TableValueFilter_Wildcard()),
			new ColumnDescription<>("Arguments",		String.class,			method -> getArgumentsAsString(method),					new TableValueFilter_Wildcard()),
			new ColumnDescription<>("Class",			String.class,			method -> method.getDeclaringClass().getSimpleName(),	new TableValueFilter_Selection()),
			new ColumnDescription<>("Modifier",		AccessModifier.class,	method -> getAccessModifier(method),					new TableValueFilter_Selection())
		);
	}

	private static Object getMethodNameLink(Method method, Object object) {
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length > 0) {
			// currently no link because parameterized
			return methodName;
		}
		return new MethodLink(method, object);
	}

	private static String getArgumentsAsString(Method method) {
		return Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "));
	}

	private static AccessModifier getAccessModifier(Method method) {
		int modifiers = method.getModifiers();
		return AccessModifier.getValue(modifiers);
	}

	private static class MethodLink implements Runnable
	{
		private final Method method;
		private final Object object;

		MethodLink(Method method, Object object) {
			this.method = method;
			this.object = object;
		}

		@Override
		public String toString() {
			return method.getName();
		}

		@Override
		public void run() {
			method.setAccessible(true);
			try {
				Object returnValue = method.invoke(object);
				if (method.getReturnType().equals(Void.TYPE)) {
					return;
				} else if (returnValue == null) {
					JOptionPane.showMessageDialog(null, "null");
				} else {
					InspectionLinkIF objectInspectionLink = ObjectInspector.getInspector().createObjectInspectionLink(returnValue);
					objectInspectionLink.run();
				}
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Caught the following exception:\n\n" + e.getMessage());
			}
		}
	}
}
