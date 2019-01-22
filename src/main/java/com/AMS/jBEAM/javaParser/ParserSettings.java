package com.AMS.jBEAM.javaParser;

public class ParserSettings
{
	private final Imports			imports;
	private final VariablePool		variablePool;
	private final AccessLevel		minimumAccessLevel;
	private final EvaluationMode	evaluationModeCodeCompletion;
	private final EvaluationMode	evaluationModeCodeEvaluation;

	public ParserSettings(Imports imports, VariablePool variablePool, AccessLevel minimumAccessLevel, EvaluationMode evaluationModeCodeCompletion, EvaluationMode evaluationModeCodeEvaluation) {
		this.imports = imports;
		this.variablePool = variablePool;
		this.minimumAccessLevel = minimumAccessLevel;
		this.evaluationModeCodeCompletion = evaluationModeCodeCompletion;
		this.evaluationModeCodeEvaluation = evaluationModeCodeEvaluation;
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
}
