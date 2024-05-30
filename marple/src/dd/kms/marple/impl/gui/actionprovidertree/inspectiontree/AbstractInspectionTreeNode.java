package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

abstract class AbstractInspectionTreeNode implements InspectionTreeNode
{
	private final @Nullable String	displayKey;
	private final Object			object;
	private final InspectionContext	context;
	private final List<ViewOption>	alternativeViews;

	private List<InspectionTreeNode>	cachedChildren;

	abstract List<? extends InspectionTreeNode> doGetChildren();

	AbstractInspectionTreeNode(@Nullable String displayKey, Object object, InspectionContext context, List<ViewOption> alternativeViews) {
		this.displayKey = displayKey;
		this.object = object;
		this.context = context;
		this.alternativeViews = ImmutableList.copyOf(alternativeViews);
	}

	String getDisplayKey() {
		return displayKey;
	}

	Object getObject() {
		return object;
	}

	InspectionContext getContext() {
		return context;
	}

	@Override
	public ActionProvider getActionProvider(JTree tree, MouseEvent e) {
		ActionProviderBuilder actionProviderBuilder = new ActionProviderBuilder(toString(), object, context)
			.suggestVariableName(displayKey);

		InspectionTreeNodes.addAlternativeViewActions(actionProviderBuilder, this, alternativeViews, tree, e);

		return actionProviderBuilder.build();
	}

	@Override
	public int getChildIndex(Object child) {
		return cachedChildren.indexOf(child);
	}

	@Override
	public List<InspectionTreeNode> getChildren() {
		if (cachedChildren == null) {
			cachedChildren = new LinkedList<>();
			cachedChildren.addAll(doGetChildren());
		}
		return cachedChildren;
	}

	@Override
	public final String toString() {
		return getTrimmedText();
	}
}
