package com.AMS.jBEAM.javaParser.common;

import com.AMS.jBEAM.javaParser.settings.LeafObjectTreeNode;
import com.AMS.jBEAM.javaParser.settings.ObjectTreeNodeIF;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CustomHierarchy
{
	public static final Date				DATE_VALUE								= new Date(System.currentTimeMillis());
	public static final String				ACTIVITY_VALUE							= "Writing tests";

	public static final DataItem			DATE									= new DataItem("Date", "Date", DATE_VALUE);
	public  static final DataItem			ACTIVITY								= new DataItem("Activity", "String", ACTIVITY_VALUE);
	public static final Component			EXCEL_IMPORTER							= new Component("Excel Importer", "Importer", DATE, ACTIVITY);

	public static final double				TOTAL_PRODUCTIVITY_VALUE				= 5.5;
	public static final double				RELATIVE_PRODUCTIVITY_VALUE				= 5.5 / 8;
	public static final double				RELATIVE_PRODUCTIVITY_POTENTIAL_VALUE	= 2.5 / 8;

	public static final DataItem			TOTAL_PRODUCTIVITY						= new DataItem("Total Productivity (h)", "double", TOTAL_PRODUCTIVITY_VALUE);
	public static final DataItem			RELATIVE_PRODUCTIVITY					= new DataItem("Relative Productivity", "double", RELATIVE_PRODUCTIVITY_VALUE);
	public static final DataItem			RELATIVE_PRODUCTIVITY_POTENTIAL			= new DataItem("Relative Productivity Potential", "double", RELATIVE_PRODUCTIVITY_POTENTIAL_VALUE);
	public static final Component			PRODUCTIVITY_CALCULATION				= new Component("Productivity Calculation", "Calculation", TOTAL_PRODUCTIVITY, RELATIVE_PRODUCTIVITY, RELATIVE_PRODUCTIVITY_POTENTIAL);

	public static final ComponentManager	COMPONENT_MANAGER						= new ComponentManager(EXCEL_IMPORTER, PRODUCTIVITY_CALCULATION);

	public static final ObjectTreeNodeIF	ROOT									= new ObjectTreeNodeIF() {
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

	public static class DataItem
	{
		private final String	name;
		private final String	dataType;
		private final Object	value;

		DataItem(String name, String dataType, Object value) {
			this.name = name;
			this.dataType = dataType;
			this.value = value;
		}

		public String getDataType() {
			return dataType;
		}

		public Object getValue() {
			return value;
		}
	}

	public static class Component
	{
		private final String			name;
		private final String			componentType;
		private final List<DataItem>	dataItems;

		Component(String name, String componentType, DataItem... dataItems) {
			this.name = name;
			this.componentType = componentType;
			this.dataItems = Arrays.asList(dataItems);
		}

		public String getComponentType() {
			return componentType;
		}

		public List<DataItem> getDataItems() {
			return dataItems;
		}
	}

	public static class ComponentManager
	{
		private final List<Component>	components;

		ComponentManager(Component... components) {
			this.components = Arrays.asList(components);
		}

		public List<Component> getComponents() {
			return components;
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
