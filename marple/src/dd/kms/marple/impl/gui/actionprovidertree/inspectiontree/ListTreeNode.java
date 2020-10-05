package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.actions.Actions;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

/**
 * Node that represents both, lists and arrays
 */
class ListTreeNode extends AbstractInspectionTreeNode
{
	static final int	RANGE_SIZE_BASE	= 10;

	private final @Nullable String	displayKey;
	private final ObjectInfo		containerInfo;
	private final List<?>			list;
	private final InspectionContext	context;

	ListTreeNode(@Nullable String displayKey, ObjectInfo containerInfo, List<?> list, InspectionContext context) {
		this.displayKey = displayKey;
		this.containerInfo = containerInfo;
		this.list = list;
		this.context = context;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		return InspectionTreeNodes.getChildren(new ChildIterator(), 10);
	}

	private InspectionTreeNode createRangeNode(int rangeBeginIndex, int rangeEndIndex) {
		return ListIndexRangeTreeNode.createRangeNode(containerInfo, list, rangeBeginIndex, rangeEndIndex, context);
	}

	@Override
	public ActionProvider getActionProvider() {
		return new ActionProviderBuilder(toString(), containerInfo, context)
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
		String valueDisplayText = context.getDisplayText(containerInfo);
		if (trimmed) {
			valueDisplayText = Actions.trimDisplayText(valueDisplayText);
		}
		valueDisplayText += (" size = " + list.size());
		return displayKey == null ? valueDisplayText : displayKey + " = " + valueDisplayText;
	}

	private class ChildIterator implements Iterator<List<InspectionTreeNode>>
	{
		private int	rangeSize					= 1;
		private int	rangeBeginIndex				= 0;
		private int	firstIndexWithLargerRange	= RANGE_SIZE_BASE;

		@Override
		public boolean hasNext() {
			return rangeBeginIndex < list.size();
		}

		@Override
		public List<InspectionTreeNode> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			int rangeEndIndex = Math.min(rangeBeginIndex + rangeSize, list.size());
			InspectionTreeNode child = createRangeNode(rangeBeginIndex, rangeEndIndex);
			rangeBeginIndex = rangeEndIndex;
			if (rangeBeginIndex == firstIndexWithLargerRange) {
				firstIndexWithLargerRange *= RANGE_SIZE_BASE;
				rangeSize *= RANGE_SIZE_BASE;
			}
			return ImmutableList.of(child);
		}
	}
}
