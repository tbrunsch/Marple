package com.AMS.jBEAM.javaParser;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

abstract class AbstractJavaEntityParser
{
	private static final Map<String, Class<?>>	PRIMITIVE_CLASSES_BY_NAME	= ReflectionUtils.getPrimitiveClasses().stream()
																				.collect(Collectors.toMap(
																							clazz -> clazz.getName(),
																							clazz -> clazz
																						)
																				);
	private static final List<JavaClassInfo>	PRIMITIVE_CLASS_INFOS		= PRIMITIVE_CLASSES_BY_NAME.keySet().stream().map(JavaClassInfo::new).collect(Collectors.toList());

	final JavaParserPool	parserPool;
	final ObjectInfo		thisInfo;

	AbstractJavaEntityParser(JavaParserPool parserPool, ObjectInfo thisInfo) {
		this.parserPool = parserPool;
		this.thisInfo = thisInfo;
	}

	abstract ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses);

	ParseResultIF parse(final JavaTokenStream tokenStream, ObjectInfo currentContextInfo, final List<Class<?>> expectedResultClasses) {
		return doParse(tokenStream.clone(), currentContextInfo, expectedResultClasses);
	}

	private Class<?> getClass(Object object, Class<?> declaredClass) {
		if (parserPool.getEvaluationMode() == EvaluationMode.DUCK_TYPING && object != null) {
			Class<?> clazz = object.getClass();
			return declaredClass.isPrimitive()
					? ReflectionUtils.getPrimitiveClass(clazz)
					: clazz;
		} else {
			return declaredClass;
		}
	}

	Class<?> getClass(ObjectInfo objectInfo) {
		return getClass(objectInfo.getObject(), objectInfo.getDeclaredClass());
	}

	ObjectInfo getFieldInfo(ObjectInfo contextInfo, Field field) throws NullPointerException {
		final Object fieldValue;
		if (parserPool.getEvaluationMode() == EvaluationMode.NONE) {
			fieldValue = null;
		} else {
			Object contextObject = (field.getModifiers() & Modifier.STATIC) != 0 ? null : contextInfo.getObject();
			try {
				field.setAccessible(true);
				fieldValue = field.get(contextObject);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected IllegalAccessException: " + e.getMessage());
			}
		}
		Class<?> fieldClass = getClass(fieldValue, field.getType());
		return new ObjectInfo(fieldValue, fieldClass);
	}

	ObjectInfo getMethodReturnInfo(ObjectInfo contextInfo, Method method, List<ObjectInfo> argumentInfos) throws NullPointerException {
		final Object methodReturnValue;
		if (parserPool.getEvaluationMode() == EvaluationMode.NONE) {
			methodReturnValue = null;
		} else {
			Object contextObject = (method.getModifiers() & Modifier.STATIC) != 0 ? null : contextInfo.getObject();
			Object[] arguments = new Object[argumentInfos.size()];
			Class<?>[] argumentTypes = method.getParameterTypes();
			for (int i = 0; i < argumentTypes.length; i++) {
				Object argument = argumentInfos.get(i).getObject();
				arguments[i] = ReflectionUtils.convertTo(argument, argumentTypes[i], false);
			}
			try {
				method.setAccessible(true);
				methodReturnValue = method.invoke(contextObject, arguments);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected IllegalAccessException: " + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new IllegalStateException("Internal error: Unexpected InvocationTargetException: " + e.getMessage());
			}
		}
		Class<?> methodReturnClass = getClass(methodReturnValue, method.getReturnType());
		return new ObjectInfo(methodReturnValue, methodReturnClass);
	}

	ObjectInfo getArrayElementInfo(ObjectInfo arrayInfo, ObjectInfo indexInfo) throws NullPointerException {
		final Object arrayElementValue;
		if (parserPool.getEvaluationMode() == EvaluationMode.NONE) {
			arrayElementValue = null;
		} else {
			Object arrayObject = arrayInfo.getObject();
			Object indexObject = indexInfo.getObject();
			int index = ReflectionUtils.convertTo(indexObject, int.class, false);
			arrayElementValue = Array.get(arrayObject, index);
		}
		Class<?> arrayElementClass = getClass(arrayElementValue, getClass(arrayInfo).getComponentType());
		return new ObjectInfo(arrayElementValue, arrayElementClass);
	}

	ObjectInfo getCastInfo(ObjectInfo objectInfo, Class<?> targetClass) throws ClassCastException {
		Object castedValue = ReflectionUtils.convertTo(objectInfo.getObject(), targetClass, true);
		return new ObjectInfo(castedValue, targetClass);
	}

	CompletionSuggestions suggestFieldsAndMethods(JavaTokenStream tokenStream, List<Class<?>> expectedClasses) {
		int insertionBegin, insertionEnd;
		insertionBegin = insertionEnd = tokenStream.getPosition();
		return suggestFieldsAndMethods(thisInfo, expectedClasses, insertionBegin, insertionEnd);
	}

	CompletionSuggestions suggestFieldsAndMethods(ObjectInfo contextInfo, List<Class<?>> expectedClasses, final int insertionBegin, final int insertionEnd) {
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = new LinkedHashMap<>();

		Class<?> contextClass = getClass(contextInfo);

		List<Field> fields = parserPool.getInspectionDataProvider().getFields(contextClass, false);
		ratedSuggestions.putAll(ParseUtils.createRatedSuggestions(
				fields,
				field -> new CompletionSuggestionField(field, insertionBegin, insertionEnd),
				ParseUtils.rateFieldByClassesFunc(expectedClasses))
		);

		List<Method> methods = parserPool.getInspectionDataProvider().getMethods(contextClass, false);
		ratedSuggestions.putAll(ParseUtils.createRatedSuggestions(
				methods,
				method -> new CompletionSuggestionMethod(method, insertionBegin, insertionEnd),
				ParseUtils.rateMethodByClassesFunc(expectedClasses))
		);

		return new CompletionSuggestions(ratedSuggestions);
	}

	List<ParseResultIF> parseMethodArguments(JavaTokenStream tokenStream, List<? extends Executable> availableMethods) {
		List<ParseResultIF> methodArguments = new ArrayList<>();

		int position = tokenStream.getPosition();
		JavaToken characterToken = tokenStream.readCharacterUnchecked();
		boolean requestedCodeCompletionBeforeNextArgument = characterToken.isContainsCaret();

		assert characterToken.getValue().charAt(0) == '(';

		// The case requestedCodeCompletionBeforeNextArgument == true will be handled at the beginning of the first loop iteration
		if (!requestedCodeCompletionBeforeNextArgument) {
			if (!tokenStream.hasMore()) {
				methodArguments.add(new ParseError(tokenStream.getPosition(), "Expected argument or closing parenthesis ')'"));
				return methodArguments;
			}

			char nextCharacter = tokenStream.peekCharacter();
			if (nextCharacter == ')') {
				tokenStream.readCharacterUnchecked();
				return methodArguments;
			}
		}

		for (int argIndex = 0; ; argIndex++) {
			final int i = argIndex;

			availableMethods = availableMethods.stream().filter(method -> isArgumentIndexValid(method, i)).collect(Collectors.toList());
			List<Class<?>> expectedArgumentTypes_i = availableMethods.stream().map(method -> method.getParameterTypes()[i]).distinct().collect(Collectors.toList());

			if (expectedArgumentTypes_i.isEmpty()) {
				methodArguments.add(new ParseError(tokenStream.getPosition(), "No further arguments expected"));
				return methodArguments;
			}

			if (requestedCodeCompletionBeforeNextArgument) {
				// suggestions for argument i
				if (availableMethods.isEmpty()) {
					// no suggestions since no further arguments expected
					methodArguments.add(CompletionSuggestions.NONE);
				} else {
					methodArguments.add(suggestFieldsAndMethods(tokenStream, expectedArgumentTypes_i));
				}
				return methodArguments;
			}

			/*
			 * Parse expression for argument i
			 */
			ParseResultIF argumentParseResult_i = parserPool.getExpressionParser().parse(tokenStream, thisInfo, expectedArgumentTypes_i);
			methodArguments.add(argumentParseResult_i);
			switch (argumentParseResult_i.getResultType()) {
				case COMPLETION_SUGGESTIONS:
					// code completion inside "[]" => propagate completion suggestions
					return methodArguments;
				case PARSE_ERROR:
				case AMBIGUOUS_PARSE_RESULT:
					// always propagate errors
					return methodArguments;
				case PARSE_RESULT: {
					ParseResult parseResult = ((ParseResult) argumentParseResult_i);
					int parsedToPosition = parseResult.getParsedToPosition();
					tokenStream.moveTo(parsedToPosition);
					ObjectInfo argumentInfo = parseResult.getObjectInfo();
					availableMethods = availableMethods.stream().filter(method -> acceptsArgumentInfo(method, i, argumentInfo)).collect(Collectors.toList());
					break;
				}
				default:
					throw new IllegalStateException("Unsupported parse result type: " + argumentParseResult_i.getResultType());
			}

			position = tokenStream.getPosition();
			characterToken = tokenStream.readCharacterUnchecked();

			if (characterToken == null) {
				methodArguments.add(new ParseError(position, "Expected comma ',' or closing parenthesis ')'"));
				return methodArguments;
			}

			if (characterToken.getValue().charAt(0) == ')') {
				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after ')'
					methodArguments.add(CompletionSuggestions.NONE);
				}
				return methodArguments;
			}

			if (characterToken.getValue().charAt(0) != ',') {
				methodArguments.add(new ParseError(position, "Expected comma ',' or closing parenthesis ')'"));
				return methodArguments;
			}

			requestedCodeCompletionBeforeNextArgument = characterToken.isContainsCaret();
		}
	}

	private static boolean isArgumentIndexValid(Executable method, int argIndex) {
		return method.isVarArgs() || method.getParameterCount() > argIndex;
	}

	private boolean acceptsArgumentInfo(Executable method, int argIndex, ObjectInfo argInfo) {
		final Class<?> expectedArgumentType;
		int numArguments = method.getParameterCount();
		if (argIndex < numArguments) {
			expectedArgumentType = method.getParameterTypes()[argIndex];
		} else {
			if (method.isVarArgs()) {
				expectedArgumentType = method.getParameterTypes()[numArguments - 1];
			} else {
				return false;
			}
		}
		Class<?> argClass = getClass(argInfo);
		return ParseUtils.isConvertibleTo(argClass, expectedArgumentType);
	}

	<T extends Executable> List<T> getBestMatchingMethods(List<T> availableMethods, List<ObjectInfo> argumentInfos) {
		int[] methodMatchRating = availableMethods.stream()
			.mapToInt(method -> rateArgumentMatch(method, argumentInfos))
			.toArray();

		List<T> methods;

		int[][] allowedRatingsByPhase = {
			{ ParseUtils.CLASS_MATCH_FULL },
			{ ParseUtils.CLASS_MATCH_INHERITANCE, ParseUtils.CLASS_MATCH_PRIMITIVE_CONVERSION},
			{ ParseUtils.CLASS_MATCH_BOXED, ParseUtils.CLASS_MATCH_BOXED_AND_CONVERSION, ParseUtils.CLASS_MATCH_BOXED_AND_INHERITANCE }
		};

		for (boolean allowVariadicMethods : Arrays.asList(false, true)) {
			for (int[] allowedRatings : allowedRatingsByPhase) {
				methods = filterMethods(availableMethods, allowedRatings, allowVariadicMethods, methodMatchRating);
				if (!methods.isEmpty()) {
					return methods;
				}
			}
		}
		return Collections.emptyList();
	}

	private int rateArgumentMatch(Executable method, List<ObjectInfo> argumentInfos) {
		List<Class<?>> argumentTypes = Arrays.stream(method.getParameterTypes()).collect(Collectors.toList());

		if (method.isVarArgs()) {
			// adapt number of argument types
			int deltaNumArguments = argumentInfos.size() - argumentTypes.size();
			if (deltaNumArguments < -1) {
				return ParseUtils.CLASS_MATCH_NONE;
			} else if (deltaNumArguments == -1) {
				// variadic argument can be omitted
				argumentTypes = argumentTypes.subList(0, argumentTypes.size());
			} else {
				// variable number of variadic arguments
				int lastArgumentIndex = argumentTypes.size() - 1;
				assert lastArgumentIndex >= 0 : "Variadic methods have at least one declared argument type";
				Class<?> variadicArgumentType = argumentTypes.get(lastArgumentIndex).getComponentType();
				assert variadicArgumentType != null : "Last argument type of variadic methods is an array";
				argumentTypes.set(lastArgumentIndex, variadicArgumentType);
				while (argumentTypes.size() < argumentInfos.size()) {
					argumentTypes.add(variadicArgumentType);
				}
			}
		} else {
			if (argumentTypes.size() != argumentInfos.size()) {
				return ParseUtils.CLASS_MATCH_NONE;
			}
		}
		assert argumentTypes.size() == argumentInfos.size() : "Adaption of argument types failed";

		int worstArgumentClassMatchRating = ParseUtils.CLASS_MATCH_FULL;
		for (int i = 0; i < argumentInfos.size(); i++) {
			Class<?> expectedType = argumentTypes.get(i);
			Class<?> actualType = getClass(argumentInfos.get(i));
			int argumentClassMatchRating = ParseUtils.rateClassMatch(actualType, expectedType);
			worstArgumentClassMatchRating = Math.max(worstArgumentClassMatchRating, argumentClassMatchRating);
		}
		return worstArgumentClassMatchRating;
	}

	private static <T extends Executable> List<T> filterMethods(List<T> methods, int[] allowedRatings, boolean allowVariadicMethods, int[] methodMatchRating) {
		List<T> filteredMethods = new ArrayList<>();
		for (int i = 0; i < methods.size(); i++) {
			int rating = methodMatchRating[i];
			if (IntStream.of(allowedRatings).noneMatch(allowedRating -> rating == allowedRating)) {
				continue;
			}
			T method = methods.get(i);
			if (!allowVariadicMethods && method.isVarArgs()) {
				continue;
			}
			filteredMethods.add(method);
		}
		return filteredMethods;
	}

	ParseResultIF readClass(JavaTokenStream tokenStream, List<Class<?>> expectedResultClasses) {
		String className = "";
		Class<?> lastDetectedClass = null;
		int lastParsedToPosition = -1;
		while (true) {
			int identifierStartPosition = tokenStream.getPosition();
			JavaToken identifierToken;
			try {
				identifierToken = tokenStream.readIdentifier();
			} catch (JavaTokenStream.JavaTokenParseException e) {
				return lastDetectedClass == null
						? new ParseError(tokenStream.getPosition(), "Expected sub-package or class name")
						: new ParseResult(lastParsedToPosition, new ObjectInfo(lastDetectedClass));

			}
			className += identifierToken.getValue();
			if (identifierToken.isContainsCaret()) {
				return suggestClassesAndPackages(identifierStartPosition, tokenStream.getPosition(), className);
			}

			Class<?> detectedClass = detectClass(className);
			if (detectedClass == null && lastDetectedClass != null) {
				return new ParseResult(lastParsedToPosition, new ObjectInfo(lastDetectedClass));
			}
			lastDetectedClass = detectedClass;
			lastParsedToPosition = tokenStream.getPosition();

			JavaToken characterToken = tokenStream.readCharacterUnchecked();
			if (characterToken == null ||  characterToken.getValue().charAt(0) != '.') {
				return lastDetectedClass == null
						? new ParseError(lastParsedToPosition, "Expected sub-package or class name")
						: new ParseResult(lastParsedToPosition, new ObjectInfo(lastDetectedClass));
			}
			className += ".";
			if (characterToken.isContainsCaret()) {
				return suggestClassesAndPackages(tokenStream.getPosition(), tokenStream.getPosition(), className);
			}
		}
	}

	private Class<?> detectClass(String className) {
		return Stream.of(
					PRIMITIVE_CLASSES_BY_NAME.get(className),
					getClassImportedViaClassName(className),
					getClassImportedViaPackage(className),
					getClass(className)
				).filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	private Class<?> getClassImportedViaClassName(String className) {
		for (JavaClassInfo importedClass : getImportedClasses()) {
			String simpleName = importedClass.getSimpleNameWithoutLeadingDigits();
			if (className.equals(simpleName) || className.startsWith(simpleName + ".")) {
				// Replace simpleName by fully qualified imported name and replace '.' by '$' when separating inner classes
				String fullyQualifiedClassName = importedClass.getName()
													+ className.substring(simpleName.length()).replace('.', '$');
				return getClass(fullyQualifiedClassName);
			}
		}
		return null;
	}

	private Class<?> getClassImportedViaPackage(String className) {
		return getImportedPackages().stream()
				.map(pack -> pack.getName() + "." + className)
				.map(this::getClass)
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	private Class<?> getClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private CompletionSuggestions suggestClassesAndPackages(int startPosition, int endPosition, String classOrPackagePrefix) {

		ImmutableMap.Builder<CompletionSuggestionIF, Integer> suggestionBuilder = ImmutableMap.builder();

		// Imported classes
		Set<JavaClassInfo> importedClasses = getImportedClasses();
		suggestionBuilder.putAll(suggestClasses(importedClasses, startPosition, endPosition, classOrPackagePrefix, true));

		// Imported packages
		Set<Package> importedPackages = getImportedPackages();
		suggestionBuilder.putAll(suggestPackages(importedPackages, startPosition, endPosition, classOrPackagePrefix));

		// Classes that have not been imported
		Set<ClassPath.ClassInfo> classes;
		try {
			classes = ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses();
		} catch (IOException e) {
			classes = Collections.emptySet();
		}
		List<JavaClassInfo> knownNotImportedClasses = classes.stream()
			.map(classInfo -> new JavaClassInfo(classInfo.getName()))
			.filter(classInfo -> !importedClasses.contains(classInfo))
			.collect(Collectors.toList());
		suggestionBuilder.putAll(suggestClasses(knownNotImportedClasses, startPosition, endPosition, classOrPackagePrefix, false));

		// Packages that have not been imported
		List<Package> knownNotImportedPackages = Arrays.stream(Package.getPackages())
			.filter(pack -> !importedPackages.contains(pack))
			.collect(Collectors.toList());
		suggestionBuilder.putAll(suggestPackages(knownNotImportedPackages, startPosition, endPosition, classOrPackagePrefix));

		return new CompletionSuggestions(suggestionBuilder.build());
	}

	private static Map<CompletionSuggestionIF, Integer> suggestClasses(Collection<JavaClassInfo> classes, int insertionBegin, int insertionEnd, String classOrPackagePrefix, boolean allowUnqualifiedUsage) {
		String parentLeaf = getParentLeaf(classOrPackagePrefix);
		String parentPath = getParentPath(classOrPackagePrefix);
		List<JavaClassInfo> classesToConsider = new ArrayList<>();
		if (parentPath != null) {
			for (JavaClassInfo classInfo : classes) {
				if (Objects.equals(getParentPath(classInfo.getName()), parentPath)) {
					classesToConsider.add(classInfo);
				}
			}
		} else if (allowUnqualifiedUsage) {
			classesToConsider.addAll(classes);
		}
		return ParseUtils.createRatedSuggestions(
			classesToConsider,
			classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
			ParseUtils.rateClassByNameFunc(parentLeaf)
		);
	}

	private static Map<CompletionSuggestionIF, Integer> suggestPackages(Collection<Package> packages, int insertionBegin, int insertionEnd, String classOrPackagePrefix) {
		String parentLeaf = getParentLeaf(classOrPackagePrefix);
		String parentPath = getParentPath(classOrPackagePrefix);
		List<Package> packagesToConsider = new ArrayList<>();
		for (Package pack : packages) {
			if (Objects.equals(getParentPath(pack.getName()), parentPath)) {
				packagesToConsider.add(pack);
			}
		}
		return ParseUtils.createRatedSuggestions(
			packagesToConsider,
			pack -> new CompletionSuggestionPackage(pack, insertionBegin, insertionEnd),
			ParseUtils.ratePackageByNameFunc(parentLeaf)
		);
	}

	private Set<JavaClassInfo> getImportedClasses() {
		Set<JavaClassInfo> importedClasses = new LinkedHashSet<>();
		importedClasses.addAll(PRIMITIVE_CLASS_INFOS);
		importedClasses.add(new JavaClassInfo(thisInfo.getDeclaredClass().getName()));
		importedClasses.addAll(parserPool.getImports().getImportedClasses());
		return importedClasses;
	}

	private Set<Package> getImportedPackages() {
		Set<Package> importedPackages = new LinkedHashSet<>();
		importedPackages.add(thisInfo.getDeclaredClass().getPackage());
		importedPackages.add(Package.getPackage("java.lang"));
		importedPackages.addAll(parserPool.getImports().getImportedPackages());
		return importedPackages;
	}

	private static String getParentPath(String path) {
		int lastSeparatorIndex = Math.max(path.lastIndexOf('.'), path.lastIndexOf('$'));
		return lastSeparatorIndex < 0 ? null : path.substring(0, lastSeparatorIndex);
	}

	private static String getParentLeaf(String path) {
		int lastDotPosition = path.lastIndexOf('.');
		return path.substring(lastDotPosition + 1);
	}
}
