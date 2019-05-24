package dd.kms.marple.instancesearch;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import dd.kms.zenodot.common.FieldScanner;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

class ClassGraphGenerationBreadthFirstSearch extends AbstractBreadthFirstSearch<Class<?>, Class<?>>
{
	private final MutableGraph<Class<?>>	classGraph;
	private final BooleanSupplier			stopFlagSupplier;

	ClassGraphGenerationBreadthFirstSearch(BooleanSupplier stopFlagSupplier) {
		this.stopFlagSupplier = stopFlagSupplier;
		classGraph = GraphBuilder
			.directed()
			.allowsSelfLoops(false)
			.build();
	}

	MutableGraph<Class<?>> getClassGraph() {
		return classGraph;
	}

	@Override
	boolean isStopExecution() {
		return stopFlagSupplier.getAsBoolean();
	}

	@Override
	Iterable<Class<?>> getChildren(Class<?> parentClass) {
		if (parentClass.isArray()) {
			Class<?> componentType = parentClass.getComponentType();
			return ImmutableSet.of(componentType);
		}
		if (Iterable.class.isAssignableFrom(parentClass)) {
			return ImmutableSet.of(Object.class);
		}
		if (Map.class.isAssignableFrom(parentClass)) {
			return ImmutableSet.of(Object.class);
		}
		if (Multimap.class.isAssignableFrom(parentClass)) {
			return ImmutableSet.of(Object.class);
		}
		List<Field> fields = new FieldScanner().getFields(parentClass, false);
		return fields.stream().map(Field::getType).filter(type -> type != parentClass).collect(Collectors.toSet());
	}

	@Override
	boolean shouldProcessDiscoveredEdge(@Nullable Class<?> from, Class<?> to) {
		if (from != null) {
			classGraph.putEdge(from, to);
		}
		return true;
	}

	@Override
	Class<?> getLastNode(Class<?> clazz) {
		return clazz;
	}
}
