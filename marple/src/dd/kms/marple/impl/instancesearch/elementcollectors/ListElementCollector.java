package dd.kms.marple.impl.instancesearch.elementcollectors;

import dd.kms.marple.impl.instancesearch.InstancePath;

import java.util.ArrayList;
import java.util.List;

public class ListElementCollector extends AbstractElementCollector
{
	private static final ListElementCollector	COLLECTOR	= new ListElementCollector();

	/**
	 * Warning: not thread-safe
	 */
	public static List<InstancePath> collect(List<?> list, InstancePath parentPath) {
		COLLECTOR.list = list;
		COLLECTOR.parentPath = parentPath;
		return COLLECTOR.collect();
	}

	private List<?>			list;
	private InstancePath	parentPath;

	@Override
	void doCollect() {
		int size = list.size();
		children = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Object object = list.get(i);
			String name = ".get(" + i + ")";
			InstancePath childPath = new InstancePath(object, name, parentPath);
			children.add(childPath);
		}
	}
}
