package com.AMS.jBEAM.javaParser.settings;

import com.AMS.jBEAM.javaParser.debug.ParserLoggerIF;

public class ParserSettings
{
	private final Imports			imports;
	private final VariablePool		variablePool;
	private final AccessLevel		minimumAccessLevel;
	private final EvaluationMode	evaluationModeCodeCompletion;
	private final EvaluationMode	evaluationModeCodeEvaluation;
	private final ObjectTreeNodeIF	customHierarchyRoot;

	private final ParserLoggerIF	logger;

	ParserSettings(Imports imports, VariablePool variablePool, AccessLevel minimumAccessLevel, EvaluationMode evaluationModeCodeCompletion, EvaluationMode evaluationModeCodeEvaluation, ObjectTreeNodeIF customHierarchyRoot, ParserLoggerIF logger) {
		this.imports = imports;
		this.variablePool = variablePool;
		this.minimumAccessLevel = minimumAccessLevel;
		this.evaluationModeCodeCompletion = evaluationModeCodeCompletion;
		this.evaluationModeCodeEvaluation = evaluationModeCodeEvaluation;
		this.customHierarchyRoot = customHierarchyRoot;
		this.logger = logger;
	}

	public Imports getImports() {
		return imports;
	}

	public VariablePool getVariablePool() {
		return variablePool;
	}

	public AccessLevel getMinimumAccessLevel() {
		return minimumAccessLevel;
	}

	public EvaluationMode getEvaluationModeCodeCompletion() {
		return evaluationModeCodeCompletion;
	}

	public EvaluationMode getEvaluationModeCodeEvaluation() {
		return evaluationModeCodeEvaluation;
	}

	public ObjectTreeNodeIF getCustomHierarchyRoot() {
		return customHierarchyRoot;
	}

	public ParserLoggerIF getLogger() {
		return logger;
	}
}
