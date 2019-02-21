package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.utils.wrappers.ExecutableInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.util.List;

public class ClassMethodParser extends AbstractMethodParser<TypeToken<?>>
{
	public ClassMethodParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
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
	List<ExecutableInfo> getMethodInfos(TypeToken<?> contextType) {
		return parserToolbox.getInspectionDataProvider().getMethodInfos(contextType, true);
	}
}
