package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.utils.wrappers.FieldInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.util.List;

/**
 * Parses a sub expression starting with a field {@code <field>}, assuming the context
 * <ul>
 *     <li>{@code <context instance>.<field>} or</li>
 *     <li>{@code <field>} (like {@code <context instance>.<field>} for {@code <context instance> = this})</li>
 * </ul>
 */
public class ObjectFieldParser extends AbstractFieldParser<ObjectInfo>
{
	public ObjectFieldParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	boolean contextCausesNullPointerException(ObjectInfo contextInfo) {
		return contextInfo.getObject() == null;
	}

	@Override
	Object getContextObject(ObjectInfo contextInfo) {
		return contextInfo.getObject();
	}

	@Override
	List<FieldInfo> getFieldInfos(ObjectInfo contextInfo) {
		TypeToken<?> contextType = parserToolbox.getObjectInfoProvider().getType(contextInfo);
		return parserToolbox.getInspectionDataProvider().getFieldInfos(contextType, false);
	}
}
