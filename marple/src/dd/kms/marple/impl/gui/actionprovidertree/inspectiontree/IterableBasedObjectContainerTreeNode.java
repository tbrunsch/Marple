package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.actions.Actions;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Node that represents both, lists and arrays
 */
class IterableBasedObjectContainerTreeNode extends AbstractInspectionTreeNode
{
	private static final int	UNKNOWN_SIZE	= -1;

	private final @Nullable String				fieldName;
	private final ObjectInfo					containerInfo;
	private final Iterable<?>					keys;
	private final @Nullable Function<Object, ?>	elementAccessor;
	private final InspectionContext				context;

	private int size;

	IterableBasedObjectContainerTreeNode(int childIndex, @Nullable String displayKey, ObjectInfo containerInfo, Iterable<?> keys, @Nullable Function<Object, ?> elementAccessor, InspectionContext context) {
		super(childIndex);
		this.fieldName = displayKey;
		this.containerInfo = containerInfo;
		this.keys = keys;
		this.elementAccessor = elementAccessor;
		this.context = context;

		this.size = keys instanceof Collection<?> ? ((Collection<?>) keys).size() : UNKNOWN_SIZE;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		int numChildren = 0;
		int entryIndex = 0;
		for (Object key : keys) {
			if (!ReflectionUtils.isObjectInspectable(key) && withValueNodes()) {
				Object value = elementAccessor.apply(key);
				InspectionTreeNode keyValueNode = createKeyValueNode(numChildren++, key, value);
				childBuilder.add(keyValueNode);
			} else {
				InspectionTreeNode keyNode = createKeyNode(numChildren++, entryIndex, key);
				childBuilder.add(keyNode);
				if (withValueNodes()) {
					Object value = elementAccessor.apply(key);
					InspectionTreeNode valueNode = createValueNode(numChildren++, entryIndex, value);
					childBuilder.add(valueNode);
				}
			}
			entryIndex++;
		}
		if (size == UNKNOWN_SIZE) {
			size = entryIndex;
		}
		return childBuilder.build();
	}

	@Override
	public ActionProvider getActionProvider() {
		return new ActionProviderBuilder(toString(), containerInfo, context)
			.suggestVariableName(fieldName)
			.build();
	}

	@Override
	public String toString() {
		String sizeText = size == UNKNOWN_SIZE ? "?" : Integer.toString(size);
		String valueDisplayText = Actions.trimName(context.getDisplayText(containerInfo)) + " size = " + sizeText;
		return fieldName == null ? valueDisplayText : fieldName + " = " + valueDisplayText;
	}

	private InspectionTreeNode createKeyNode(int childIndex, int entryIndex, Object key) {
		String displayText = getKeyNodeDisplayText(entryIndex);
		ObjectInfo keyInfo = getObjectInfo(key);
		return InspectionTreeNodes.create(childIndex, displayText, keyInfo, true, context);
	}

	private InspectionTreeNode createValueNode(int childIndex, int entryIndex, Object value) {
		String displayText = getValueNodeDisplayText(entryIndex);
		ObjectInfo valueInfo = getObjectInfo(value);
		return InspectionTreeNodes.create(childIndex, displayText, valueInfo, true, context);
	}

	private InspectionTreeNode createKeyValueNode(int childIndex, Object key, Object value) {
		ObjectInfo keyInfo = getObjectInfo(key);
		ObjectInfo valueInfo = getObjectInfo(value);
		String displayText = getKeyValueNodeDisplayText(keyInfo);
		return InspectionTreeNodes.create(childIndex, displayText, valueInfo, true, context);
	}

	private ObjectInfo getObjectInfo(Object keyOrValue) {
		TypeInfo typeInfo = keyOrValue == null
				? InfoProvider.NO_TYPE
				: ReflectionUtils.getRuntimeTypeInfo(containerInfo).resolveType(keyOrValue.getClass());
		return InfoProvider.createObjectInfo(keyOrValue, typeInfo);
	}

	private String getKeyNodeDisplayText(int entryIndex) {
		return withValueNodes() ? "[key" + entryIndex + "]" : "[entry" + entryIndex + "]";
	}

	private String getValueNodeDisplayText(int entryIndex) {
		return "[value" + entryIndex + "]";
	}

	private String getKeyValueNodeDisplayText(ObjectInfo keyInfo) {
		return "[" + context.getDisplayText(keyInfo) + "]";
	}

	private boolean withValueNodes() {
		return elementAccessor != null;
	}
}
