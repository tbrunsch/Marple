package com.AMS.jBEAM.javaParser;

public class ParserSettings
{
	private final ImportsIF			imports;
	private final VariablePoolIF	variablePool;
	private final AccessLevel		minimumAccessLevel;
	private final EvaluationMode	evaluationModeCodeCompletion;
	private final EvaluationMode	evaluationModeCodeEvaluation;

	public ParserSettings(ImportsIF imports, VariablePoolIF variablePool, AccessLevel minimumAccessLevel, EvaluationMode evaluationModeCodeCompletion, EvaluationMode evaluationModeCodeEvaluation) {
		this.imports = imports;
		this.variablePool = variablePool;
		this.minimumAccessLevel = minimumAccessLevel;
		this.evaluationModeCodeCompletion = evaluationModeCodeCompletion;
		this.evaluationModeCodeEvaluation = evaluationModeCodeEvaluation;
	}

	public ImportsIF getImports() {
		return imports;
	}

	public VariablePoolIF getVariablePool() {
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
}
