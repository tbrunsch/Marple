package dd.kms.marple.instancesearch;

import com.google.common.graph.Graph;

import java.util.function.BooleanSupplier;

class ClassBreadthFirstSearch extends AbstractBreadthFirstSearch<Class<?>, Class<?>>
{
	private final Graph<Class<?>>	classSearchGraph;
	private final BooleanSupplier	stopExecutionFlagSupplier;

	ClassBreadthFirstSearch(Graph<Class<?>> classSearchGraph, BooleanSupplier stopExecutionFlagSupplier) {
		this.classSearchGraph = classSearchGraph;
		this.stopExecutionFlagSupplier = stopExecutionFlagSupplier;
	}

	@Override
	boolean isStopExecution() {
		return stopExecutionFlagSupplier.getAsBoolean();
	}

	@Override
	Iterable<Class<?>> getChildren(Class<?> parentPathInfo) {
		return classSearchGraph.successors(parentPathInfo);
	}

	@Override
	boolean shouldProcessDiscoveredEdge(Class<?> from, Class<?> to) {
		return true;
	}

	@Override
	Class<?> getLastNode(Class<?> clazz) {
		return clazz;
	}
}
