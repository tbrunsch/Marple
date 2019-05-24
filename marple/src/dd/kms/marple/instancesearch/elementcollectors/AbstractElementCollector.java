package dd.kms.marple.instancesearch.elementcollectors;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.instancesearch.InstancePath;

import java.util.ConcurrentModificationException;
import java.util.List;

abstract class AbstractElementCollector
{
	protected List<InstancePath> children;

	abstract void doCollect();

	public final List<InstancePath> collect() {
		children = null;
		try {
			doCollect();
		} catch (ConcurrentModificationException e) {
			/*
			 * Can happen, e.g., when structures are scanned that are used to represent the scan result => difficult to avoid,
			 * but not relevant for the search.
			 */
		} catch (Exception e) {
			System.err.println(e);
		}
		return children == null ? ImmutableList.of() : children;
	}
}
