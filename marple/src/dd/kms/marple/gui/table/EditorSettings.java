package dd.kms.marple.gui.table;

import java.util.List;

public interface EditorSettings<T>
{
	void setElementValue(List<T> list, int elementIndex, Object value);
}
