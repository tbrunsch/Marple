package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.utils.wrappers.ExecutableInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.util.List;

public class ClassMethodParser extends AbstractMethodParser<TypeToken<?>>
{
	public ClassMethodParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
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
		return parserContext.getInspectionDataProvider().getMethodInfos(contextType, true);
	}
}
