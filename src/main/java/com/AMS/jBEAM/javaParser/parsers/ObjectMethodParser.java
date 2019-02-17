package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.utils.wrappers.ExecutableInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.util.List;

public class ObjectMethodParser extends AbstractMethodParser<ObjectInfo>
{
	public ObjectMethodParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
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
	List<ExecutableInfo> getMethodInfos(ObjectInfo contextInfo) {
		TypeToken<?> contextType = parserContext.getObjectInfoProvider().getType(contextInfo);
		return parserContext.getInspectionDataProvider().getMethodInfos(contextType, false);
	}
}
