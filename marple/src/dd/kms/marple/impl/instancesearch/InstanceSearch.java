package dd.kms.marple.impl.instancesearch;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.instancesearch.elementcollectors.ArrayElementCollector;
import dd.kms.marple.impl.instancesearch.elementcollectors.CollectionElementCollector;
import dd.kms.marple.impl.instancesearch.elementcollectors.MapElementCollector;
import dd.kms.marple.impl.instancesearch.elementcollectors.MultimapElementCollector;
import dd.kms.marple.impl.instancesearch.settings.SearchSettings;
import dd.kms.zenodot.api.common.FieldScanner;

class InstanceSearch
{
	private final int						maxSearchDepth;

	private final Set<Integer>				visitedNodeIdentityHashes	= Sets.newHashSet();
	private final Queue<InstancePath>		nodesToConsider				= Lists.newLinkedList();

	// redundant variable to avoid concurrency problems
	private int								numVisitedNodes;

	private final Predicate<Object>			searchFilter;
	private final Consumer<InstancePath>	nodeConsumer;
	private final BooleanSupplier			stopExecutionFlagSupplier;
	private final SearchSettings			settings;

	private final Function<Object, String>	toStringFunction;

	InstanceSearch(Predicate<Object> searchFilter, Consumer<InstancePath> nodeConsumer, BooleanSupplier stopExecutionFlagSupplier, SearchSettings settings, Function<Object, String> toStringFunction) {
		maxSearchDepth = settings.getEffectiveMaxSearchDepth();
		this.searchFilter = searchFilter;
		this.nodeConsumer = nodeConsumer;
		this.stopExecutionFlagSupplier = stopExecutionFlagSupplier;
		this.settings = settings;
		this.toStringFunction = toStringFunction;
	}

	void search(InstancePath startPathInfo) {
		visitedNodeIdentityHashes.clear();
		nodesToConsider.clear();
		numVisitedNodes = 0;

		int curDepth = 0;
		int countUntilHigherDepth = 1;
		onDiscoveredEdge(null, startPathInfo);
		while (!nodesToConsider.isEmpty() && !stopExecutionFlagSupplier.getAsBoolean() && curDepth < maxSearchDepth) {
			InstancePath node = nodesToConsider.poll();
			Iterable<InstancePath> children = getChildren(node);
			for (InstancePath child : children) {
				onDiscoveredEdge(node, child);
			}

			countUntilHigherDepth--;
			if (countUntilHigherDepth == 0) {
				countUntilHigherDepth = nodesToConsider.size();
				curDepth++;
			}
		}
	}

	int getNumberOfVisitedNodes() {
		return numVisitedNodes;
	}

	private void onDiscoveredEdge(@Nullable InstancePath from, InstancePath to) {
		if (!shouldProcessDiscoveredNode(to)) {
			return;
		}
		Integer identityHash = System.identityHashCode(to.getLastNodeObject());
		if (visitedNodeIdentityHashes.add(identityHash)) {
			nodesToConsider.add(to);
			numVisitedNodes++;
		}
	}

	private Iterable<InstancePath> getChildren(InstancePath parent) {
		Object object = parent.getLastNodeObject();
		if (!ReflectionUtils.isObjectInspectable(object) || object == this || object instanceof InstancePath) {
			return ImmutableList.of();
		}
		if (!settings.isSearchOnlyPureFields()) {
			if (object.getClass().isArray()) {
				return ArrayElementCollector.collect(object, parent);
			}
			if (object instanceof Collection<?>) {
				return CollectionElementCollector.collect((Collection<?>) object, parent);
			}
			if (object instanceof Map<?, ?>) {
				return MapElementCollector.collect((Map<?, ?>) object, parent, toStringFunction);
			}
			if (object instanceof Multimap<?, ?>) {
				return MultimapElementCollector.collect((Multimap<?, ?>) object, parent, toStringFunction);
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
				InstancePath child = new InstancePath(field.get(object), "." + field.getName(), parent);
				children.add(child);
			} catch (IllegalAccessException ignored) {
				/* we only discover what can be discovered */
			}
		}
		return children;
	}

	private boolean shouldProcessDiscoveredNode(InstancePath node) {
		Object lastNodeObject = node.getLastNodeObject();
		if (searchFilter.test(lastNodeObject)) {
			nodeConsumer.accept(node);
			return settings.isExtendPathsBeyondAcceptedInstances();
		}
		if (lastNodeObject == null) {
			return false;
		}
		Class<?> childObjectClass = lastNodeObject.getClass();
		return settings.getClassFilter().test(childObjectClass);
	}
}
