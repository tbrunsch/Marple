package dd.kms.marple.instancesearch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.instancesearch.elementcollectors.*;
import dd.kms.zenodot.common.FieldScanner;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.*;

class InstanceBreadthFirstSearch extends AbstractBreadthFirstSearch<Integer, InstancePath>
{
	private final Set<Class<?>>				classesToExcludeFromSearch;
	private final Predicate<Object>			searchFilter;
	private final Consumer<InstancePath>	pathConsumer;
	private final BooleanSupplier			stopExecutionFlagSupplier;
	private final boolean					extendPathsBeyondAcceptedInstances;

	private final Function<Object, String>	toStringFunction;

	InstanceBreadthFirstSearch(Set<Class<?>> classesToExcludeFromSearch, Predicate<Object> searchFilter, Consumer<InstancePath> pathConsumer, BooleanSupplier stopExecutionFlagSupplier, boolean extendPathsBeyondAcceptedInstances, Function<Object, String> toStringFunction) {
		this.classesToExcludeFromSearch = classesToExcludeFromSearch;
		this.searchFilter = searchFilter;
		this.pathConsumer = pathConsumer;
		this.stopExecutionFlagSupplier = stopExecutionFlagSupplier;
		this.extendPathsBeyondAcceptedInstances = extendPathsBeyondAcceptedInstances;
		this.toStringFunction = toStringFunction;
	}

	@Override
	boolean isStopExecution() {
		return stopExecutionFlagSupplier.getAsBoolean();
	}

	@Override
	Iterable<InstancePath> getChildren(InstancePath parentPath) {
		Object object = parentPath.getLastNodeObject();
		if (!ReflectionUtils.isObjectInspectable(object) || object == this || object instanceof InstancePath) {
			return ImmutableList.of();
		}
		if (object.getClass().isArray()) {
			return ArrayElementCollector.collect(object, parentPath);
		}
		if (object instanceof List<?>) {
			return ListElementCollector.collect((List<?>) object, parentPath);
		}
		if (object instanceof Iterable<?>) {
			return IterableElementCollector.collect((Iterable<?>) object, parentPath);
		}
		if (object instanceof Map<?, ?>) {
			return MapElementCollector.collect((Map<?, ?>) object, parentPath, toStringFunction);
		}
		if (object instanceof Multimap<?, ?>) {
			return MultimapElementCollector.collect((Multimap<?, ?>) object, parentPath, toStringFunction);
		}

		/*
		 * Collect regular fields
		 */
		List<Field> fields = new FieldScanner().getFields(object.getClass(), false);
		List<InstancePath> children = new ArrayList<>();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				InstancePath child = new InstancePath(field.get(object), "." + field.getName(), parentPath);
				children.add(child);
			} catch (IllegalAccessException e) {
				continue;
			}
		}
		return children;
	}

	@Override
	boolean shouldProcessDiscoveredEdge(InstancePath predecessorPath, InstancePath path) {
		Object lastNodeObject = path.getLastNodeObject();
		if (searchFilter.test(lastNodeObject)) {
			pathConsumer.accept(path);
			return extendPathsBeyondAcceptedInstances;
		}
		if (lastNodeObject == null) {
			return false;
		}
		Class<?> childObjectClass = lastNodeObject.getClass();
		if (classesToExcludeFromSearch.contains(childObjectClass)) {
			return false;
		}
		return true;
	}

	@Override
	Integer getLastNode(InstancePath pathInfo) {
		return System.identityHashCode(pathInfo.getLastNodeObject());
	}
}
