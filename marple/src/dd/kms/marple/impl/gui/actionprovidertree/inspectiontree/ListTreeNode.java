package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.actions.Actions;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Node that represents both, lists and arrays
 */
class ListTreeNode extends AbstractInspectionTreeNode
{
	private final @Nullable String	displayKey;
	private final Object 			container;
	private final List<?>			list;
	private final InspectionContext context;

	ListTreeNode(@Nullable String displayKey, Object container, List<?> list, InspectionContext context) {
		this.displayKey = displayKey;
		this.container = container;
		this.list = list;
		this.context = context;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		return InspectionTreeNodes.getListElementNodes(list, 0, list.size(), 0, context);
	}

	@Override
	public ActionProvider getActionProvider(JTree tree, MouseEvent e) {
		return new ActionProviderBuilder(toString(), container, context)
			.suggestVariableName(displayKey)
			.build();
	}

	@Override
	public String getTrimmedText() {
		return getText(true);
	}

	@Override
	public String getFullText() {
		return getText(false);
	}

	private String getText(boolean trimmed) {
		String valueDisplayText = context.getDisplayText(container);
		if (trimmed) {
			valueDisplayText = Actions.trimDisplayText(valueDisplayText);
		}
		valueDisplayText += (" size = " + list.size());
		return displayKey == null ? valueDisplayText : displayKey + " = " + valueDisplayText;
	}
}
