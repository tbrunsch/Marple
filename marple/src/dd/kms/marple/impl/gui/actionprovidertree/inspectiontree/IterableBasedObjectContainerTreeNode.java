package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.Actions;
import dd.kms.marple.impl.common.ReflectionUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

class IterableBasedObjectContainerTreeNode extends AbstractInspectionTreeNode
{
	private static final int	MAX_NUM_CHILDREN	= 100;

	private static final int	UNKNOWN_SIZE		= -1;

	private final Iterable<?>					keys;
	private final @Nullable Function<Object, ?>	elementAccessor;
	private final int							size;

	IterableBasedObjectContainerTreeNode(@Nullable String fieldName, Object container, Iterable<?> keys, @Nullable Function<Object, ?> elementAccessor, InspectionContext context, List<ViewOption> alternativeViews) {
		super(fieldName, container, context, alternativeViews);
		this.keys = keys;
		this.elementAccessor = elementAccessor;
		this.size = keys instanceof Collection<?> ? ((Collection<?>) keys).size() : UNKNOWN_SIZE;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		return InspectionTreeNodes.getChildren(new ChildIterator(), MAX_NUM_CHILDREN);
	}

	private InspectionTreeNode createKeyNode(int entryIndex, Object key) {
		String displayText = getKeyNodeDisplayText(entryIndex);
		return InspectionTreeNodes.create(displayText, key, getContext());
	}

	private InspectionTreeNode createValueNode(int entryIndex, Object value) {
		String displayText = getValueNodeDisplayText(entryIndex);
		return InspectionTreeNodes.create(displayText, value, getContext());
	}

	private InspectionTreeNode createKeyValueNode(Object key, Object value) {
		String displayText = getKeyValueNodeDisplayText(key);
		return InspectionTreeNodes.create(displayText, value, getContext());
	}

	private String getKeyNodeDisplayText(int entryIndex) {
		return withValueNodes() ? "[key" + entryIndex + "]" : "[entry" + entryIndex + "]";
	}

	private String getValueNodeDisplayText(int entryIndex) {
		return "[value" + entryIndex + "]";
	}

	private String getKeyValueNodeDisplayText(Object key) {
		return "[" + getContext().getDisplayText(key) + "]";
	}

	private boolean withValueNodes() {
		return elementAccessor != null;
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
		String sizeText = size == UNKNOWN_SIZE ? "?" : Integer.toString(size);
		valueDisplayText += (" size = " + sizeText);
		String fieldName = getDisplayKey();
		return fieldName == null ? valueDisplayText : fieldName + " = " + valueDisplayText;
	}

	private class ChildIterator implements Iterator<List<InspectionTreeNode>>
	{
		private final Iterator<?>	keyIterator = keys.iterator();

		private int entryIndex	= 0;

		@Override
		public boolean hasNext() {
			return keyIterator.hasNext();
		}

		@Override
		public List<InspectionTreeNode> next() {
			Object key = keyIterator.next();
			ImmutableList.Builder<InspectionTreeNode> builder = ImmutableList.builder();
			if (!ReflectionUtils.isObjectInspectable(key) && withValueNodes()) {
				Object value = elementAccessor.apply(key);
				InspectionTreeNode keyValueNode = createKeyValueNode(key, value);
				builder.add(keyValueNode);
			} else {
				InspectionTreeNode keyNode = createKeyNode(entryIndex, key);
				builder.add(keyNode);
				if (withValueNodes()) {
					Object value = elementAccessor.apply(key);
					InspectionTreeNode valueNode = createValueNode(entryIndex, value);
					builder.add(valueNode);
				}
			}
			entryIndex++;
			return builder.build();
		}
	}
}
