package dd.kms.marple.swing.gui;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.marple.InspectionLinkIF;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.swing.SwingObjectInspector;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SwingComponentInspectionPanel extends JPanel
{
	private final JScrollPane	scrollPane;
	private final JTree			tree;

	public SwingComponentInspectionPanel(List<Object> componentHierarchy) {
		super(new GridBagLayout());

		List<InspectionLinkIF> componentInspectionHierarchy = createInspectionLinkHierarchy(componentHierarchy);
		tree = SwingInspectionUtils.createInspectionLinkTree(componentInspectionHierarchy);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		scrollPane  = new JScrollPane(tree);
		add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}

	private static List<InspectionLinkIF> createInspectionLinkHierarchy(List<Object> componentHierarchy) {
		Multimap<Object, Field> fieldsByObject = ArrayListMultimap.create();
		Set<Object> objectsToFind = new HashSet<>();

		// Traverse hierarchy from child to parent because usually children are members of parents and not vice versa
		for (int i = componentHierarchy.size() - 1; i >= 0; i--) {
			Object object = componentHierarchy.get(i);
			objectsToFind.add(object);
			fieldsByObject.putAll(ReflectionUtils.findFieldValues(object, objectsToFind));
		}

		List<InspectionLinkIF> inspectionLinkHierarchy = new ArrayList<>(componentHierarchy.size());
		for (int i = 0; i < componentHierarchy.size(); i++) {
			Object component = componentHierarchy.get(i);
			Collection<Field> fields = fieldsByObject.get(component);
			InspectionLinkIF inspectionLink = createComponentInspectionLink(component, componentHierarchy.subList(0, i+1), fields);
			inspectionLinkHierarchy.add(inspectionLink);
		}
		return inspectionLinkHierarchy;
	}

	private static InspectionLinkIF createComponentInspectionLink(Object component, List<Object> componentHierarchy, Collection<Field> fields) {
		String linkText = component.getClass().getSimpleName();
		if (!fields.isEmpty()) {
			String fieldText = fields.stream()
					.map(field -> field.getDeclaringClass().getSimpleName() + "." + field.getName())
					.collect(Collectors.joining(", "));
			linkText += " (" + fieldText + ")";
		}
		return SwingObjectInspector.getInspector().createComponentInspectionLink(componentHierarchy, linkText);
	}
}
