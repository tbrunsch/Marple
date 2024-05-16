package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.Actions;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Node that represents both, lists and arrays
 */
class ListTreeNode extends AbstractInspectionTreeNode
{
	private final List<?>			list;

	ListTreeNode(@Nullable String displayKey, Object container, List<?> list, InspectionContext context, List<ViewOption> alternativeViews) {
		super(displayKey, container, context, alternativeViews);
		this.list = list;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		return InspectionTreeNodes.getListElementNodes(list, 0, list.size(), 0, getContext());
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
		Object container = getObject();
		String valueDisplayText = getContext().getDisplayText(container);
		if (trimmed) {
			valueDisplayText = Actions.trimDisplayText(valueDisplayText);
		}
		valueDisplayText += (" size = " + list.size());
		String displayKey = getDisplayKey();
		return displayKey == null ? valueDisplayText : displayKey + " = " + valueDisplayText;
	}
}
