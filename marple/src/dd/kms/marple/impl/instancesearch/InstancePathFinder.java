package dd.kms.marple.impl.instancesearch;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.instancesearch.settings.SearchSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InstancePathFinder
{
	private final Consumer<InstancePath>	pathConsumer;
	private InspectionContext				context;

	private final Object					lock				= new Object();

	private volatile ProcessingState		processingState		= ProcessingState.NOT_RUNNING;
	private int								pathCounter;

	private InstanceSearch					instanceSearch;

	public InstancePathFinder(Consumer<InstancePath> pathConsumer) {
		this.pathConsumer = path -> {
			pathCounter++;
			pathConsumer.accept(path);
		};
	}

	public void setInspectionContext(InspectionContext context) {
		this.context = context;
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
			instanceSearch = new InstanceSearch(targetFilter, pathConsumer, stopFlagSupplier, settings, this::getDisplayString);
			pathCounter = 0;
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
			StringBuilder statusBuilder = new StringBuilder(processingState.toString());
			List<String> additionalInformation = new ArrayList<>();
			if (instanceSearch != null) {
				int numVisitedObjects = instanceSearch.getNumberOfVisitedNodes();
				additionalInformation.add(numVisitedObjects + " objects visited");
			}
			if (processingState != ProcessingState.NOT_RUNNING) {
				String pathSingularPlural = pathCounter == 1 ? "path" : "paths";
				additionalInformation.add(pathCounter + " " + pathSingularPlural + " found");
			}
			if (additionalInformation.isEmpty()) {
				return statusBuilder.toString();
			}
			return statusBuilder
				.append(" (")
				.append(additionalInformation.stream().collect(Collectors.joining(", ")))
				.append(")")
				.toString();
		}
	}

	public ProcessingState getProcessingState() {
		return processingState;
	}

	public void stop() {
		changeProcessingState(ProcessingState.WAITING_FOR_TERMINATION);
	}

	private String getDisplayString(Object object) {
		return context != null
			? context.getDisplayText(object)
			: (object != null ? object.toString() : "null");
	}

	private void changeProcessingState(ProcessingState newState) {
		synchronized (lock) {
			final ProcessingState validPredecessorState;
			switch (newState) {
				case NOT_RUNNING:
					validPredecessorState = ProcessingState.FINISHED;
					break;
				case SEARCHING:
					validPredecessorState = ProcessingState.NOT_RUNNING;
					break;
				case WAITING_FOR_TERMINATION:
					validPredecessorState = ProcessingState.SEARCHING;
					break;
				case FINISHED:
					processingState = newState;
					return;
				default:
					throw new IllegalArgumentException("Unsupported state: " + newState);
			}
			if (validPredecessorState == processingState) {
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
