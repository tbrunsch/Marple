package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.Imports;
import com.AMS.jBEAM.javaParser.JavaParserContext;
import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassDataProvider
{
	private static final Map<String, Class<?>>	PRIMITIVE_CLASSES_BY_NAME	= ReflectionUtils.getPrimitiveClasses().stream()
		.collect(Collectors.toMap(
				clazz -> clazz.getName(),
				clazz -> clazz
				)
		);
	private static final List<ClassInfo>	PRIMITIVE_CLASS_INFOS		= PRIMITIVE_CLASSES_BY_NAME.keySet().stream().map(ClassInfo::new).collect(Collectors.toList());

	private final JavaParserContext parserContext;
	private final Imports 			imports;

	public ClassDataProvider(JavaParserContext parserContext, Imports imports) {
		this.parserContext = parserContext;
		this.imports = imports;
	}

	public ParseResultIF readClass(TokenStream tokenStream, List<Class<?>> expectedResultClasses, boolean includePrimitiveClasses, boolean returnClassInsteadOfCompletionsIfAvailable) {
		String className = "";
		Class<?> lastDetectedClass = null;
		int lastParsedToPosition = -1;
		while (true) {
			int identifierStartPosition = tokenStream.getPosition();
			Token identifierToken;
			try {
				identifierToken = tokenStream.readIdentifier();
			} catch (TokenStream.JavaTokenParseException e) {
				return lastDetectedClass == null
						? new ParseError(tokenStream.getPosition(), "Expected sub-package or class name", ParseError.ErrorType.SYNTAX_ERROR)
						: new ParseResult(lastParsedToPosition, new ObjectInfo(null, lastDetectedClass));

			}
			className += identifierToken.getValue();
			if (identifierToken.isContainsCaret()) {
				return lastDetectedClass == null || !returnClassInsteadOfCompletionsIfAvailable
						? suggestClassesAndPackages(identifierStartPosition, tokenStream.getPosition(), className, includePrimitiveClasses)
						: new ParseResult(lastParsedToPosition, new ObjectInfo(null, lastDetectedClass));
			}

			Class<?> detectedClass = detectClass(className, includePrimitiveClasses);
			if (detectedClass == null && lastDetectedClass != null) {
				return new ParseResult(lastParsedToPosition, new ObjectInfo(null, lastDetectedClass));
			}
			lastDetectedClass = detectedClass;
			lastParsedToPosition = tokenStream.getPosition();

			Token characterToken = tokenStream.readCharacterUnchecked();
			if (characterToken == null ||  characterToken.getValue().charAt(0) != '.') {
				return lastDetectedClass == null
						? new ParseError(lastParsedToPosition, "Expected sub-package or class name", ParseError.ErrorType.SYNTAX_ERROR)
						: new ParseResult(lastParsedToPosition, new ObjectInfo(null, lastDetectedClass));
			}
			className += (detectedClass == null ? "." : "$");
			if (characterToken.isContainsCaret()) {
				return lastDetectedClass == null || !returnClassInsteadOfCompletionsIfAvailable
						? suggestClassesAndPackages(tokenStream.getPosition(), tokenStream.getPosition(), className, false)
						: new ParseResult(lastParsedToPosition, new ObjectInfo(null, lastDetectedClass));
			}
		}
	}

	private Class<?> detectClass(String className, boolean includePrimitiveClasses) {
		return Stream.of(
			includePrimitiveClasses ? PRIMITIVE_CLASSES_BY_NAME.get(className) : null,
			getClassImportedViaClassName(className, includePrimitiveClasses),
			getClassImportedViaPackage(className),
			getClass(className)
		).filter(Objects::nonNull)
		.findFirst().orElse(null);
	}

	private Class<?> getClassImportedViaClassName(String className, boolean includePrimitiveClasses) {
		for (ClassInfo importedClass : getImportedClasses(includePrimitiveClasses)) {
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

	private CompletionSuggestions suggestClassesAndPackages(int insertionBegin, int insertionEnd, String classOrPackagePrefix, boolean includePrimitiveClasses) {

		ImmutableMap.Builder<CompletionSuggestionIF, Integer> suggestionBuilder = ImmutableMap.builder();

		// Imported classes
		Set<ClassInfo> importedClasses = getImportedClasses(includePrimitiveClasses);
		suggestionBuilder.putAll(suggestClasses(importedClasses, insertionBegin, insertionEnd, classOrPackagePrefix, true));

		// Imported packages
		Set<Package> importedPackages = getImportedPackages();
		suggestionBuilder.putAll(suggestPackages(importedPackages, insertionBegin, insertionEnd, classOrPackagePrefix));

		// Classes that have not been imported
		Set<ClassPath.ClassInfo> classes;
		try {
			classes = ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses();
		} catch (IOException e) {
			classes = Collections.emptySet();
		}
		List<ClassInfo> knownNotImportedClasses = classes.stream()
				.map(classInfo -> new ClassInfo(classInfo.getName()))
				.filter(classInfo -> !importedClasses.contains(classInfo))
				.collect(Collectors.toList());
		suggestionBuilder.putAll(suggestClasses(knownNotImportedClasses, insertionBegin, insertionEnd, classOrPackagePrefix, false));

		// Packages that have not been imported
		List<Package> knownNotImportedPackages = Arrays.stream(Package.getPackages())
				.filter(pack -> !importedPackages.contains(pack))
				.collect(Collectors.toList());
		suggestionBuilder.putAll(suggestPackages(knownNotImportedPackages, insertionBegin, insertionEnd, classOrPackagePrefix));

		return new CompletionSuggestions(suggestionBuilder.build());
	}

	private static Map<CompletionSuggestionIF, Integer> suggestClasses(Collection<ClassInfo> classes, int insertionBegin, int insertionEnd, String classOrPackagePrefix, boolean allowUnqualifiedUsage) {
		String parentLeaf = getParentLeaf(classOrPackagePrefix);
		String parentPath = getParentPath(classOrPackagePrefix);
		List<ClassInfo> classesToConsider = new ArrayList<>();
		if (parentPath != null) {
			for (ClassInfo classInfo : classes) {
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

	private Set<ClassInfo> getImportedClasses(boolean includePrimitiveClasses) {
		Set<ClassInfo> importedClasses = new LinkedHashSet<>();
		if (includePrimitiveClasses) {
			importedClasses.addAll(PRIMITIVE_CLASS_INFOS);
		}
		Class<?> thisClass = parserContext.getThisInfo().getDeclaredClass();
		if (thisClass != null) {
			importedClasses.add(new ClassInfo(thisClass.getName()));
		}
		importedClasses.addAll(imports.getImportedClasses());
		return importedClasses;
	}

	private Set<Package> getImportedPackages() {
		Set<Package> importedPackages = new LinkedHashSet<>();
		Class<?> thisClass = parserContext.getThisInfo().getDeclaredClass();
		if (thisClass != null) {
			importedPackages.add(thisClass.getPackage());
		}
		importedPackages.add(Package.getPackage("java.lang"));
		importedPackages.addAll(imports.getImportedPackages());
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
