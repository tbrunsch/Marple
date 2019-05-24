package dd.kms.marple.instancesearch.elementcollectors;

import dd.kms.marple.instancesearch.InstancePath;

import java.util.ArrayList;
import java.util.List;

public class IterableElementCollector extends AbstractElementCollector
{
	private static final IterableElementCollector	COLLECTOR	= new IterableElementCollector();

	/**
	 * Warning: not thread-safe
	 */
	public static List<InstancePath> collect(Iterable<?> iterable, InstancePath parentPath) {
		COLLECTOR.iterable = iterable;
		COLLECTOR.parentPath = parentPath;
		return COLLECTOR.collect();
	}

	private Iterable<?>		iterable;
	private InstancePath	parentPath;

	@Override
	void doCollect() {
		children = new ArrayList<>();
		int i = 0;
		for (Object object : iterable) {
			String name = "{" + i + "}";
			InstancePath childPath = new InstancePath(object, name, parentPath);
			children.add(childPath);
			i++;
		}
	}
}
