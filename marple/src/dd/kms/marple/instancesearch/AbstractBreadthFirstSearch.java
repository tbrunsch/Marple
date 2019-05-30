package dd.kms.marple.instancesearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.Queue;
import java.util.Set;

/**
 * @param <N>	node class
 * @param <P>	path data class (may be same as N if only the final node of the path is of interest)
 */
abstract class AbstractBreadthFirstSearch<N, P>
{
	private final Set<N>	visitedNodes		= Sets.newHashSet();
	private final Queue<P>	pathInfosToConsider	= Lists.newLinkedList();

	// redundant variable to avoid concurrency problems
	private int				numVisitedNodes;

	abstract boolean isStopExecution();
	abstract Iterable<P> getChildren(P parentPathInfo);
	abstract boolean shouldProcessDiscoveredEdge(@Nullable P from, P to);
	abstract N getLastNode(P pathInfo);

	Set<N> search(P startPathInfo) {
		visitedNodes.clear();
		pathInfosToConsider.clear();
		numVisitedNodes = 0;

		onDiscoveredEdge(null, startPathInfo);
		while (!pathInfosToConsider.isEmpty() && !isStopExecution()) {
			P pathInfo = pathInfosToConsider.poll();
			Iterable<P> childPathInfos = getChildren(pathInfo);
			for (P childPathInfo : childPathInfos) {
				onDiscoveredEdge(pathInfo, childPathInfo);
			}
		}
		return visitedNodes;
	}

	int getNumberOfVisitedNodes() {
		return numVisitedNodes;
	}

	private void onDiscoveredEdge(@Nullable P from, P to) {
		if (!shouldProcessDiscoveredEdge(from, to)) {
			return;
		}
		N node = getLastNode(to);
		if (visitedNodes.contains(node)) {
			return;
		}
		visitedNodes.add(node);
		pathInfosToConsider.add(to);
		numVisitedNodes++;
	}
}