package dd.kms.marple.impl.instancesearch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.instancesearch.elementcollectors.*;
import dd.kms.marple.impl.instancesearch.settings.SearchSettings;
import dd.kms.zenodot.api.common.FieldScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

class InstanceBreadthFirstSearch extends AbstractBreadthFirstSearch<Integer, InstancePath>
{
	private final Predicate<Object>			searchFilter;
	private final Consumer<InstancePath>	pathConsumer;
	private final BooleanSupplier			stopExecutionFlagSupplier;
	private final SearchSettings			settings;

	private final Function<Object, String>	toStringFunction;

	InstanceBreadthFirstSearch(Predicate<Object> searchFilter, Consumer<InstancePath> pathConsumer, BooleanSupplier stopExecutionFlagSupplier, SearchSettings settings, Function<Object, String> toStringFunction) {
		super(settings.getEffectiveMaxSearchDepth());
		this.searchFilter = searchFilter;
		this.pathConsumer = pathConsumer;
		this.stopExecutionFlagSupplier = stopExecutionFlagSupplier;
		this.settings = settings;
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
		if (!settings.isSearchOnlyPureFields()) {
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
		}

		/*
		 * Collect regular fields
		 */
		List<Field> fields = new FieldScanner().getFields(object.getClass(), false);
		List<InstancePath> children = new ArrayList<>();
		for (Field field : fields) {
			if (settings.isSearchOnlyNonStaticFields() && Modifier.isStatic(field.getModifiers())) {
				continue;
			}
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
			return settings.isExtendPathsBeyondAcceptedInstances();
		}
		if (lastNodeObject == null) {
			return false;
		}
		Class<?> childObjectClass = lastNodeObject.getClass();
		return settings.getClassFilter().test(childObjectClass);
	}

	@Override
	Integer getLastNode(InstancePath pathInfo) {
		return System.identityHashCode(pathInfo.getLastNodeObject());
	}
}
