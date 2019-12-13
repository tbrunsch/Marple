package dd.kms.marple.instancesearch;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.instancesearch.settings.SearchSettings;
import dd.kms.zenodot.utils.wrappers.InfoProvider;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InstancePathFinder
{
	private final Consumer<InstancePath>	pathConsumer;
	private InspectionContext				inspectionContext;

	private final Object					lock				= new Object();

	private ProcessingState					processingState		= ProcessingState.NOT_RUNNING;

	private InstanceBreadthFirstSearch		instanceSearch = null;

	public InstancePathFinder(Consumer<InstancePath> pathConsumer) {
		this.pathConsumer = pathConsumer;
	}

	public void setInspectionContext(InspectionContext inspectionContext) {
		this.inspectionContext = inspectionContext;
	}

	public void search(InstancePath sourcePath, Predicate<Object> targetFilter, SearchSettings settings) {
		Object sourceObject = sourcePath.getLastNodeObject();
		synchronized (lock) {
			if (processingState != ProcessingState.NOT_RUNNING && processingState != ProcessingState.FINISHED || sourceObject == null) {
				return;
			}
		}
		instanceSearch = null;
		try {
			BooleanSupplier stopFlagSupplier = () -> processingState == ProcessingState.WAITING_FOR_TERMINATION;

			changeProcessingState(ProcessingState.SEARCHING);
			instanceSearch = new InstanceBreadthFirstSearch(targetFilter, pathConsumer, stopFlagSupplier, settings, this::getDisplayString);
			instanceSearch.search(sourcePath);
		} finally {
			changeProcessingState(ProcessingState.FINISHED);
			instanceSearch = null;
		}
	}

	public void reset() {
		changeProcessingState(ProcessingState.NOT_RUNNING);
	}

	public String getStatusText() {
		synchronized (lock) {
			String status = processingState.toString();
			if (instanceSearch != null) {
				int numVisitedObjects = instanceSearch.getNumberOfVisitedNodes();
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
				: inspectionContext.getDisplayText(InfoProvider.createObjectInfo(object));
	}

	private void changeProcessingState(ProcessingState newState) {
		synchronized (lock) {
			final ProcessingState[] validPredecessorStates;
			switch (newState) {
				case NOT_RUNNING:
					validPredecessorStates = new ProcessingState[]{ ProcessingState.FINISHED };
					break;
				case SEARCHING:
					validPredecessorStates = new ProcessingState[]{ ProcessingState.NOT_RUNNING };
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
		NOT_RUNNING				("Not running"),
		SEARCHING				("Searching..."),
		WAITING_FOR_TERMINATION	("Waiting for termination..."),
		FINISHED				("Finished");

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
