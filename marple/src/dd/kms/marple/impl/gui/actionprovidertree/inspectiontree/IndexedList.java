package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import java.util.AbstractList;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

class IndexedList<T> extends AbstractList<T>
{
	private final BiMap<Integer, T>	objectsByIndex	= HashBiMap.create();

	@Override
	public int size() {
		return objectsByIndex.size();
	}

	@Override
	public T get(int index) {
		return objectsByIndex.get(index);
	}

	@Override
	public boolean add(T child) {
		if (indexOf(child) < 0) {
			objectsByIndex.put(size(), child);
			return true;
		}
		return false;
	}

	@Override
	public int indexOf(Object o) {
		Integer index = objectsByIndex.inverse().get(o);
		return index == null ? -1 : index;
	}

	@Override
	public boolean remove(Object o) {
		int index = indexOf(o);
		if (index < 0) {
			return false;
		}
		int lastIndex = size() - 1;
		while (index < lastIndex) {
			objectsByIndex.put(index, objectsByIndex.get(++index));
		}
		objectsByIndex.remove(lastIndex);
		return true;
	}
}
