package dd.kms.marple.impl.instancesearch.elementcollectors;

import dd.kms.marple.impl.instancesearch.InstancePath;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayElementCollector extends AbstractElementCollector
{
	private static final ArrayElementCollector	COLLECTOR	= new ArrayElementCollector();

	/**
	 * Warning: not thread-safe
	 */
	public static List<InstancePath> collect(Object array, InstancePath parent) {
		COLLECTOR.array = array;
		COLLECTOR.parent = parent;
		return COLLECTOR.collect();
	}

	private Object			array;
	private InstancePath	parent;

	@Override
	void doCollect() {
		int size = Array.getLength(array);
		children = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Object object = Array.get(array, i);
			String name = "[" + i + "]";
			InstancePath child = new InstancePath(object, name, parent);
			children.add(child);
		}
	}
}
