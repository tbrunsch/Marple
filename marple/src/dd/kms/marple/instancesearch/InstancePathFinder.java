package dd.kms.marple.instancesearch;

import com.google.common.collect.Sets;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import dd.kms.marple.InspectionContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InstancePathFinder
{
	private final Consumer<InstancePath>		pathConsumer;
	private InspectionContext					inspectionContext;

	private final Object						lock				= new Object();

	private ProcessingState						processingState		= ProcessingState.NOT_RUNNING;

	private AbstractBreadthFirstSearch<?, ?>	currentSearch		= null;

	public InstancePathFinder(Consumer<InstancePath> pathConsumer) {
		this.pathConsumer = pathConsumer;
	}

	public void setInspectionContext(InspectionContext inspectionContext) {
		this.inspectionContext = inspectionContext;
	}

	public void search(InstancePath sourcePath, Class<?> targetClass, Predicate<Object> targetFilter) {
		Object sourceObject = sourcePath.getLastNodeObject();
		synchronized (lock) {
			if (processingState != ProcessingState.NOT_RUNNING && processingState != ProcessingState.FINISHED || sourceObject == null) {
				return;
			}
		}
		currentSearch = null;
		try {
			BooleanSupplier stopFlagSupplier = () -> processingState == ProcessingState.WAITING_FOR_TERMINATION;

			changeProcessingState(ProcessingState.SEARCHING_CLASSES);
			MutableGraph<Class<?>> classSearchGraph = ClassSearchGraphGenerator.createClassSearchGraph(sourceObject.getClass(), targetClass, stopFlagSupplier);

			changeProcessingState(ProcessingState.EXCLUDING_CLASSES);
			ClassBreadthFirstSearch classBreadthFirstSearch = new ClassBreadthFirstSearch(Graphs.transpose(classSearchGraph), stopFlagSupplier);
			currentSearch = classBreadthFirstSearch;
			Set<Class<?>> relevantClasses = classBreadthFirstSearch.search(targetClass);
			Set<Class<?>> classesToExcludeFromSearch = Sets.newHashSet(Sets.difference(classSearchGraph.nodes(), relevantClasses));

			changeProcessingState(ProcessingState.SEARCHING_INSTANCES);
			InstanceBreadthFirstSearch instanceBreadthFirstSearch = new InstanceBreadthFirstSearch(classesToExcludeFromSearch, targetFilter, pathConsumer, stopFlagSupplier, this::getDisplayString);
			currentSearch = instanceBreadthFirstSearch;
			instanceBreadthFirstSearch.search(sourcePath);
		} finally {
			changeProcessingState(ProcessingState.FINISHED);
			currentSearch = null;
		}
	}

	public void reset() {
		changeProcessingState(ProcessingState.NOT_RUNNING);
	}

	public String getStatusText() {
		synchronized (lock) {
			String status = processingState.toString();
			if (currentSearch != null) {
				int numVisitedObjects = currentSearch.getNumberOfVisitedNodes();
				status += (" (" + numVisitedObjects + " objects visited)");
			}
			return status;
		}
	}

	public ProcessingState getProcessingState() {
		return processingState;
	}

	public void stop() {
		changeProcessingState(ProcessingState.WAITING_FOR_TERMINATION);
	}

	private String getDisplayString(Object object) {
		return inspectionContext == null
				? (object == null ? "null" : object.toString())
				: inspectionContext.getDisplayText(object);
	}

	private void changeProcessingState(ProcessingState newState) {
		synchronized (lock) {
			final ProcessingState[] validPredecessorStates;
			switch (newState) {
				case NOT_RUNNING:
					validPredecessorStates = new ProcessingState[]{ ProcessingState.FINISHED };
					break;
				case SEARCHING_CLASSES:
					validPredecessorStates = new ProcessingState[]{ ProcessingState.NOT_RUNNING };
					break;
				case EXCLUDING_CLASSES:
					validPredecessorStates = new ProcessingState[]{ ProcessingState.SEARCHING_CLASSES };
					break;
				case SEARCHING_INSTANCES:
					validPredecessorStates = new ProcessingState[]{ ProcessingState.EXCLUDING_CLASSES };
					break;
				case WAITING_FOR_TERMINATION:
					if (processingState != ProcessingState.NOT_RUNNING) {
						processingState = newState;
					}
					return;
				case FINISHED:
					processingState = newState;
					return;
				default:
					throw new IllegalArgumentException("Unsupported state: " + newState);
			}
			if (Arrays.asList(validPredecessorStates).contains(processingState)) {
				processingState = newState;
			}
		}
	}

	public enum ProcessingState
	{
		NOT_RUNNING				("not running"),
		SEARCHING_CLASSES		("Step 1 of 3: Performing class based search..."),
		EXCLUDING_CLASSES		("Step 2 of 3: Excluding classes..."),
		SEARCHING_INSTANCES		("Step 3 of 3: Searching instances..."),
		WAITING_FOR_TERMINATION	("Waiting for termination..."),
		FINISHED				("finished");

		private final String displayString;

		ProcessingState(String displayString) {
			this.displayString = displayString;
		}

		@Override
		public String toString() {
			return displayString;
		}
	}
}
