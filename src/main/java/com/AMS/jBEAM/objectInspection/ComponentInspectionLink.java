package com.AMS.jBEAM.objectInspection;

import java.util.List;

class ComponentInspectionLink extends AbstractInspectionLink
{
	private static Object getLastObjectInHierarchy(List<Object> componentHierarchy) {
		return componentHierarchy.get(componentHierarchy.size()-1);
	}

	private final List<Object> componentHierarchy;

	ComponentInspectionLink(List<Object> componentHierarchy) {
		this(componentHierarchy, getLastObjectInHierarchy(componentHierarchy).toString());
	}

	ComponentInspectionLink(List<Object> componentHierarchy, String linkText) {
		super(linkText);
		this.componentHierarchy = componentHierarchy;
	}

	@Override
	public Object getObjectToInspect() {
		return getLastObjectInHierarchy(componentHierarchy);
	}

	@Override
	void doInspect(ObjectInspector inspector) {
		Object objectToInspect = getObjectToInspect();
		inspector.beginInspection(objectToInspect);
		inspector.inspectComponent(componentHierarchy);
		inspector.inspectObject(objectToInspect);
		inspector.endInspection();
	}
}
