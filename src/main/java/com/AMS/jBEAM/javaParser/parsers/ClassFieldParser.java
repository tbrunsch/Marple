package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.utils.wrappers.FieldInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.util.List;

/**
 * Parses a sub expression starting with a field {@code <field>}, assuming the context {@code <context class>.<field>}
 */
public class ClassFieldParser extends AbstractFieldParser<TypeToken<?>>
{
	public ClassFieldParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	boolean contextCausesNullPointerException(TypeToken<?> contextType) {
		return false;
	}

	@Override
	Object getContextObject(TypeToken<?> context) {
		return null;
	}

	@Override
	List<FieldInfo> getFieldInfos(TypeToken<?> contextType) {
		return parserToolbox.getInspectionDataProvider().getFieldInfos(contextType, true);
	}
}
