package com.AMS.jBEAM.javaParser.classesForTest;

class DummyClass
{
	static final char FIRST_CHARACTER = 'D';

	static class InternalClassStage1
	{
		static double value = 5.0;

		static class InternalClassStage2 {
			static int i = 3;
		}
	}
}
