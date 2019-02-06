package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.settings.LeafObjectTreeNode;
import com.AMS.jBEAM.javaParser.settings.ObjectTreeNodeIF;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

class CustomHierarchy
{
	private final static Date		DATE_VALUE								= new Date(System.currentTimeMillis());
	private final static String		ACTIVITY_VALUE							= "Writing tests";

	final static DataItem			DATE									= new DataItem("Date", "Date", DATE_VALUE);
	final static DataItem			ACTIVITY								= new DataItem("Activity", "String", ACTIVITY_VALUE);
	final static Component			EXCEL_IMPORTER							= new Component("Excel Importer", "Importer", DATE, ACTIVITY);

	private final static double		TOTAL_PRODUCTIVITY_VALUE				= 5.5;
	private final static double		RELATIVE_PRODUCTIVITY_VALUE				= 5.5 / 8;
	private final static double		RELATIVE_PRODUCTIVITY_POTENTIAL_VALUE	= 2.5 / 8;

	final static DataItem			TOTAL_PRODUCTIVITY						= new DataItem("Total Productivity (h)", "double", TOTAL_PRODUCTIVITY_VALUE);
	final static DataItem			RELATIVE_PRODUCTIVITY					= new DataItem("Relative Productivity", "double", RELATIVE_PRODUCTIVITY_VALUE);
	final static DataItem			RELATIVE_PRODUCTIVITY_POTENTIAL			= new DataItem("Relative Productivity Potential", "double", RELATIVE_PRODUCTIVITY_POTENTIAL_VALUE);
	final static Component			PRODUCTIVITY_CALCULATION				= new Component("Productivity Calculation", "Calculation", TOTAL_PRODUCTIVITY, RELATIVE_PRODUCTIVITY, RELATIVE_PRODUCTIVITY_POTENTIAL);

	final static ComponentManager	COMPONENT_MANAGER						= new ComponentManager(EXCEL_IMPORTER, PRODUCTIVITY_CALCULATION);

	final static ObjectTreeNodeIF	ROOT									= new ObjectTreeNodeIF() {
																				@Override
																				public String getName() {
																					return "root";
																				}

																				@Override
																				public List<ObjectTreeNodeIF> getChildNodes() {
																					return Arrays.asList(
																							new LeafObjectTreeNode("Component Manager", COMPONENT_MANAGER),
																							new ComponentNode(EXCEL_IMPORTER),
																							new ComponentNode(PRODUCTIVITY_CALCULATION)
																					);
																				}

																				@Override
																				public Object getUserObject() {
																					return null;
																				}
																			};

	static class DataItem
	{
		final String	name;
		final String	dataType;
		final Object	value;

		DataItem(String name, String dataType, Object value) {
			this.name = name;
			this.dataType = dataType;
			this.value = value;
		}
	}

	static class Component
	{
		final String			name;
		final String			componentType;
		final List<DataItem>	dataItems;

		Component(String name, String componentType, DataItem... dataItems) {
			this.name = name;
			this.componentType = componentType;
			this.dataItems = Arrays.asList(dataItems);
		}
	}

	static class ComponentManager
	{
		final List<Component>	components;

		ComponentManager(Component... components) {
			this.components = Arrays.asList(components);
		}
	}

	private static class ComponentNode implements ObjectTreeNodeIF
	{
		private final Component component;

		ComponentNode(Component component) { this.component = component; }

		@Override
		public String getName() {
			return component.name;
		}

		@Override
		public List<ObjectTreeNodeIF> getChildNodes() {
			return component.dataItems.stream().map(dataItem -> new LeafObjectTreeNode(dataItem.name, dataItem)).collect(Collectors.toList());
		}

		@Override
		public Object getUserObject() {
			return component;
		}
	}
}
