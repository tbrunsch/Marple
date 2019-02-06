package com.AMS.jBEAM.javaParser.settings;

import com.AMS.jBEAM.javaParser.debug.ParserLoggerIF;
import com.AMS.jBEAM.javaParser.debug.ParserNullLogger;
import com.AMS.jBEAM.javaParser.utils.ClassInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ParserSettingsBuilder
{
	private final ImmutableSet.Builder<ClassInfo> 						importClassesBuilder			= ImmutableSet.builder();
	private final ImmutableSet.Builder<Package> 						importPackagesBuilder			= ImmutableSet.builder();

	private final ImmutableMap.Builder<String, VariablePool.ValueData>	variablesBuilder 				= ImmutableMap.builder();

	private AccessLevel													minimumAccessLevel				= AccessLevel.PUBLIC;

	private EvaluationMode												evaluationModeCodeCompletion	= EvaluationMode.NONE;
	private EvaluationMode												evaluationModeCodeEvaluation	= EvaluationMode.STRONGLY_TYPED;

	private ObjectTreeNodeIF											customHierarchyRoot				= LeafObjectTreeNode.EMPTY;

	private ParserLoggerIF												logger							= new ParserNullLogger();

	public ParserSettingsBuilder importClass(ClassInfo classInfo) {
		importClassesBuilder.add(classInfo);
		return this;
	}

	public ParserSettingsBuilder importPackage(Package pack) {
		importPackagesBuilder.add(pack);
		return this;
	}

	public ParserSettingsBuilder addVariable(Variable variable) {
		variablesBuilder.put(variable.getName(), new VariablePool.ValueData(variable.getValue(), variable.isUseHardReferenceInPool()));
		return this;
	}

	public ParserSettingsBuilder minimumAccessLevel(AccessLevel minimumAccessLevel) {
		this.minimumAccessLevel = minimumAccessLevel;
		return this;
	}

	public ParserSettingsBuilder evaluationModeCodeCompletion(EvaluationMode evaluationModeCodeCompletion) {
		this.evaluationModeCodeCompletion = evaluationModeCodeCompletion;
		return this;
	}

	public ParserSettingsBuilder evaluationModeCodeEvaluation(EvaluationMode evaluationModeCodeEvaluation) {
		this.evaluationModeCodeEvaluation = evaluationModeCodeEvaluation;
		return this;
	}

	public ParserSettingsBuilder customHierarchyRoot(ObjectTreeNodeIF customHierarchyRoot) {
		this.customHierarchyRoot = customHierarchyRoot;
		return this;
	}

	public ParserSettingsBuilder logger(ParserLoggerIF logger) {
		this.logger = logger;
		return this;
	}

	public ParserSettings build() {
		Imports imports = new Imports(importClassesBuilder.build(), importPackagesBuilder.build());
		VariablePool variablePool = new VariablePool(variablesBuilder.build());
		return new ParserSettings(imports, variablePool, minimumAccessLevel, evaluationModeCodeCompletion, evaluationModeCodeEvaluation, customHierarchyRoot, logger);
	}
}
