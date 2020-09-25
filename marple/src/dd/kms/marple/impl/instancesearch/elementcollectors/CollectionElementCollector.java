package dd.kms.marple.impl.instancesearch.elementcollectors;

import dd.kms.marple.impl.instancesearch.InstancePath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionElementCollector extends AbstractElementCollector
{
	private static final CollectionElementCollector COLLECTOR	= new CollectionElementCollector();

	/**
	 * Warning: not thread-safe
	 */
	public static List<InstancePath> collect(Collection<?> collection, InstancePath parentPath) {
		COLLECTOR.collection = collection;
		COLLECTOR.parentPath = parentPath;
		return COLLECTOR.collect();
	}

	private Collection<?>	collection;
	private InstancePath	parentPath;

	@Override
	void doCollect() {
		boolean isList = collection instanceof List;
		children = new ArrayList<>(collection.size());
		int i = 0;
		for (Object object : collection) {
			String name = isList ? ".get(" + i + ")" : "{" + i + "}";
			InstancePath childPath = new InstancePath(object, name, parentPath);
			children.add(childPath);
			i++;
		}
	}
}
