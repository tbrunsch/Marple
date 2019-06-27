package dd.kms.marple.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.actions.Actions;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.common.UniformView;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Node that represents both, lists and arrays
 */
class IterableBasedObjectContainerTreeNode extends AbstractInspectionTreeNode
{
	private static final int	UNKNOWN_SIZE	= -1;

	private final @Nullable String				fieldName;
	private final Object 						container;
	private final TypeInfo						typeInfo;
	private final Iterable<?>					keys;
	private final @Nullable Function<Object, ?>	elementAccessor;
	private final InspectionContext				inspectionContext;

	private int size;

	IterableBasedObjectContainerTreeNode(int childIndex, @Nullable String displayKey, Object container, TypeInfo typeInfo, Iterable<?> keys, @Nullable Function<Object, ?> elementAccessor, InspectionContext inspectionContext) {
		super(childIndex);
		this.fieldName = displayKey;
		this.container = container;
		this.typeInfo = typeInfo;
		this.keys = keys;
		this.elementAccessor = elementAccessor;
		this.inspectionContext = inspectionContext;

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
		return new ActionProviderBuilder(toString(), container, inspectionContext)
			.suggestVariableName(fieldName)
			.build();
	}

	@Override
	public String toString() {
		String sizeText = size == UNKNOWN_SIZE ? "?" : Integer.toString(size);
		String valueDisplayText = Actions.trimName(inspectionContext.getDisplayText(container)) + " size = " + sizeText;
		return fieldName == null ? valueDisplayText : fieldName + " = " + valueDisplayText;
	}

	private InspectionTreeNode createKeyNode(int childIndex, int entryIndex, Object key) {
		String displayText = getKeyNodeDisplayText(entryIndex);
		TypeInfo typeInfo = key == null ? InfoProvider.NO_TYPE : this.typeInfo.resolveType(key.getClass());
		return InspectionTreeNodes.create(childIndex, displayText, key, typeInfo, true, inspectionContext);
	}

	private InspectionTreeNode createValueNode(int childIndex, int entryIndex, Object value) {
		String displayText = getValueNodeDisplayText(entryIndex);
		TypeInfo typeInfo = value == null ? InfoProvider.NO_TYPE : this.typeInfo.resolveType(value.getClass());
		return InspectionTreeNodes.create(childIndex, displayText, value, typeInfo, true, inspectionContext);
	}

	private InspectionTreeNode createKeyValueNode(int childIndex, Object key, Object value) {
		String displayText = getKeyValueNodeDisplayText(key, value);
		TypeInfo typeInfo = value == null ? InfoProvider.NO_TYPE : this.typeInfo.resolveType(value.getClass());
		return InspectionTreeNodes.create(childIndex, displayText, value, typeInfo, true, inspectionContext);
	}

	private String getKeyNodeDisplayText(int entryIndex) {
		return withValueNodes() ? "[key" + entryIndex + "]" : "[entry" + entryIndex + "]";
	}

	private String getValueNodeDisplayText(int entryIndex) {
		return "[value" + entryIndex + "]";
	}

	private String getKeyValueNodeDisplayText(Object key, Object value) {
		return "[" + inspectionContext.getDisplayText(key) + "]";
	}

	private boolean withValueNodes() {
		return elementAccessor != null;
	}
}
