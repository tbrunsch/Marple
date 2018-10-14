package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.InspectionLinkIF;
import com.AMS.jBEAM.objectInspection.InspectionUtils;
import com.AMS.jBEAM.objectInspection.swing.SwingObjectInspector;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SwingComponentInspectionPanel extends JPanel
{
    private final JScrollPane   scrollPane;
    private final JTree         tree;

    public SwingComponentInspectionPanel(Component component, List<Object> subComponentHierarchy) {
        super(new GridBagLayout());

        List<InspectionLinkIF> componentInspectionHierarchy = createComponentInspectionHierarchy(component, subComponentHierarchy);
        tree = SwingInspectionUtils.createInspectionLinkTree(componentInspectionHierarchy);
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        scrollPane  = new JScrollPane(tree);
        add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    private static List<InspectionLinkIF> createComponentInspectionHierarchy(Component component, List<Object> subComponentHierarchy) {
        List<Object> componentHierarchy = new ArrayList<>();
        for (Component curComponent = component; curComponent != null; curComponent = curComponent.getParent()) {
            componentHierarchy.add(0, curComponent);
        }
        componentHierarchy.addAll(subComponentHierarchy);
        return createInspectionLinkHierarchy(componentHierarchy);
    }

    private static List<InspectionLinkIF> createInspectionLinkHierarchy(List<Object> hierarchy) {
        Map<Object, List<Field>> fieldsByObject = new HashMap<>();
        // Traverse hierarchy from child to parent because usually children are members of parents and not vice versa
        for (int i = hierarchy.size() - 1; i >= 0; i--) {
            Object object = hierarchy.get(i);
            fieldsByObject.put(object, new ArrayList<>());
            addAll(fieldsByObject, InspectionUtils.findFieldValues(object, fieldsByObject.keySet()));
        }
        return hierarchy.stream()
                .map(object -> createInspectionLink(object, fieldsByObject.get(object)))
                .collect(Collectors.toList());
    }

    private static <S, T> void addAll(Map<S, List<T>> aggregated, Map<S, List<T>> toAdd) {
        for (S key : toAdd.keySet()) {
            List<T> valueList = aggregated.get(key);
            valueList.addAll(toAdd.get(key));
        }
    }

    private static InspectionLinkIF createInspectionLink(Object object, List<Field> fields) {
        String linkText = object.getClass().getSimpleName();
        if (!fields.isEmpty()) {
            String fieldText = fields.stream()
                    .map(InspectionUtils::formatField)
                    .collect(Collectors.joining(", "));
            linkText += " (" + fieldText + ")";
        }
        return SwingObjectInspector.getInspector().createObjectInspectionLink(object, linkText);
    }
}
