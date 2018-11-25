package com.AMS.jBEAM.javaParser;

public class JavaParserSettings
{
	private final Imports			imports;
	private final EvaluationMode	evaluationModeCodeCompletion;
	private final EvaluationMode	evaluationModeCodeEvaluation;

	public JavaParserSettings(Imports imports, EvaluationMode evaluationModeCodeCompletion, EvaluationMode evaluationModeCodeEvaluation) {
		this.imports = imports;
		this.evaluationModeCodeCompletion = evaluationModeCodeCompletion;
		this.evaluationModeCodeEvaluation = evaluationModeCodeEvaluation;
	}

	public Imports getImports() {
		return imports;
	}

	public EvaluationMode getEvaluationModeCodeCompletion() {
		return evaluationModeCodeCompletion;
	}

	public EvaluationMode getEvaluationModeCodeEvaluation() {
		return evaluationModeCodeEvaluation;
	}
}
