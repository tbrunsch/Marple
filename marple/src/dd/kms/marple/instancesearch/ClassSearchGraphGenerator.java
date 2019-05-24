package dd.kms.marple.instancesearch;

import com.google.common.graph.MutableGraph;

import java.util.function.BooleanSupplier;

class ClassSearchGraphGenerator
{
	static MutableGraph<Class<?>> createClassSearchGraph(Class<?> sourceClass, Class<?> targetClass, BooleanSupplier stopFlagSupplier) {
		MutableGraph<Class<?>> classGraph = createClassGraph(sourceClass, stopFlagSupplier);

		// add cast edges
		for (Class<?> curClass = targetClass; curClass != null; curClass = curClass.getSuperclass()) {
			Class<?>[] interfaces = curClass.getInterfaces();
			for (Class<?> interfazz : interfaces) {
				if (interfazz != targetClass) {
					classGraph.putEdge(interfazz, targetClass);
				}
			}
			if (curClass != targetClass) {
				classGraph.putEdge(curClass, targetClass);
			}
		}

		return classGraph;
	}

	private static MutableGraph<Class<?>> createClassGraph(Class<?> sourceClass, BooleanSupplier stopFlagSupplier) {
		ClassGraphGenerationBreadthFirstSearch classGraphGenerationBreadthFirstSearch = new ClassGraphGenerationBreadthFirstSearch(stopFlagSupplier);
		classGraphGenerationBreadthFirstSearch.search(sourceClass);
		return classGraphGenerationBreadthFirstSearch.getClassGraph();
	}
}
