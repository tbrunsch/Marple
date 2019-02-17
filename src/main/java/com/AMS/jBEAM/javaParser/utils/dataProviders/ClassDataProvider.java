package com.AMS.jBEAM.javaParser.utils.dataProviders;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.parsers.ParseExpectation;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.settings.Imports;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.wrappers.ClassInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.util.*;
import java.util.function.ToIntFunction;
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
	private static final List<ClassInfo>		PRIMITIVE_CLASS_INFOS		= PRIMITIVE_CLASSES_BY_NAME.keySet().stream().map(ClassInfo::new).collect(Collectors.toList());

	private final ParserContext	parserContext;
	private final Imports		imports;

	public ClassDataProvider(ParserContext parserContext) {
		this.parserContext = parserContext;
		this.imports = parserContext.getSettings().getImports();
	}

	public ParseResultIF readTopLevelClass(TokenStream tokenStream) {
		TopLevelClassReader reader = new TopLevelClassReader(parserContext, imports, tokenStream);
		return reader.read();
	}

	public ParseResultIF readInnerClass(TokenStream tokenStream, TypeToken<?> contextType) {
		Class<?> contextClass = contextType.getRawType();
		int startPosition = tokenStream.getPosition();

		if (tokenStream.isCaretAtPosition()) {
			return suggestInnerClasses("", contextClass, startPosition, startPosition);
		}

		Token identifierToken;
		try {
			identifierToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected inner class name", ParseError.ErrorType.WRONG_PARSER);
		}

		String innerClassName = identifierToken.getValue();

		if (identifierToken.isContainsCaret()) {
			return suggestInnerClasses(innerClassName, contextClass, startPosition, tokenStream.getPosition());
		}

		Optional<Class<?>> firstClassMatch = Arrays.stream(contextClass.getDeclaredClasses())
			.filter(clazz -> clazz.getSimpleName().equals(innerClassName))
			.findFirst();
		if (!firstClassMatch.isPresent()) {
			return new ParseError(startPosition, "Unknown inner class '" + innerClassName + "'", ParseError.ErrorType.WRONG_PARSER);
		}

		Class<?> innerClass = firstClassMatch.get();
		TypeToken<?> innerClassType = contextType.resolveType(innerClass);
		return new ClassParseResult(tokenStream.getPosition(), innerClassType);
	}

	private CompletionSuggestions suggestInnerClasses(String expectedName, Class<?> contextClass, int insertionBegin, int insertionEnd) {
		List<ClassInfo> classesToConsider = Arrays.stream(contextClass.getDeclaredClasses())
											.map(clazz -> new ClassInfo(clazz.getName()))
											.collect(Collectors.toList());
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
				classesToConsider,
				classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
				rateClassByNameFunc(expectedName)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private static class TopLevelClassReader
	{
		private final ParserContext	parserContext;
		private final Imports 		imports;
		private final TokenStream	tokenStream;

		private String				packageOrClassName		= "";
		private int					identifierStartPosition	= -1;

		TopLevelClassReader(ParserContext parserContext, Imports imports, TokenStream tokenStream) {
			this.parserContext = parserContext;
			this.imports = imports;
			this.tokenStream = tokenStream;
		}

		ParseResultIF read() {
			while (true) {
				identifierStartPosition = tokenStream.getPosition();
				Token identifierToken;
				try {
					identifierToken = tokenStream.readIdentifier();
				} catch (TokenStream.JavaTokenParseException e) {
					return new ParseError(identifierStartPosition, "Expected sub-package or class name", ParseError.ErrorType.SYNTAX_ERROR);
				}
				packageOrClassName += identifierToken.getValue();
				if (identifierToken.isContainsCaret()) {
					return suggestClassesAndPackages(identifierStartPosition, tokenStream.getPosition(), packageOrClassName);
				}

				Class<?> detectedClass = detectClass(packageOrClassName);
				if (detectedClass != null) {
					return new ClassParseResult(tokenStream.getPosition(), TypeToken.of(detectedClass));
				}

				Token characterToken = tokenStream.readCharacterUnchecked();
				if (characterToken == null ||  characterToken.getValue().charAt(0) != '.') {
					return new ParseError(identifierStartPosition, "Unknown class name '" + packageOrClassName + "'", ParseError.ErrorType.SEMANTIC_ERROR);
				}

				if (!packageExists(packageOrClassName)) {
					return new ParseError(tokenStream.getPosition(), "Unknown class or package '" + packageOrClassName + "'", ParseError.ErrorType.SEMANTIC_ERROR);
				}

				packageOrClassName += ".";
				if (characterToken.isContainsCaret()) {
					return suggestClassesAndPackages(tokenStream.getPosition(), tokenStream.getPosition(), packageOrClassName);
				}
			}
		}

		private static boolean packageExists(String packageName) {
			String packagePrefix = packageName + ".";
			try {
				ImmutableSet<ClassPath.ClassInfo> classes = ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClasses();
				return classes.stream().anyMatch(classInfo -> classInfo.getName().startsWith(packagePrefix));
			} catch (IOException e) {
				return false;
			}
		}

		private static Map<CompletionSuggestionIF, Integer> suggestClasses(Collection<ClassInfo> classes, Collection<Package> importedPackages, int insertionBegin, int insertionEnd, String classOrPackagePrefix, boolean allowUnqualifiedUsage) {


//			TODO: importedPackages auswerten

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
				rateClassByNameFunc(parentLeaf)
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
				ratePackageByNameFunc(parentLeaf)
			);
		}

		private static String getParentPath(String path) {
			int lastSeparatorIndex = Math.max(path.lastIndexOf('.'), path.lastIndexOf('$'));
			return lastSeparatorIndex < 0 ? null : path.substring(0, lastSeparatorIndex);
		}

		private static String getParentLeaf(String path) {
			int lastDotPosition = path.lastIndexOf('.');
			return path.substring(lastDotPosition + 1);
		}

		private CompletionSuggestions suggestClassesAndPackages(int insertionBegin, int insertionEnd, String classOrPackagePrefix) {
			ImmutableMap.Builder<CompletionSuggestionIF, Integer> suggestionBuilder = ImmutableMap.builder();

			Set<ClassInfo> importedClasses = getImportedClasses();
			Set<Package> importedPackages = getImportedPackages();

			// Imported classes
			suggestionBuilder.putAll(suggestClasses(importedClasses, importedPackages, insertionBegin, insertionEnd, classOrPackagePrefix, true));

			// Imported packages
			suggestionBuilder.putAll(suggestPackages(importedPackages, insertionBegin, insertionEnd, classOrPackagePrefix));

			// Classes that have not been imported
			Set<ClassPath.ClassInfo> classes;
			try {
				classes = ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClasses();
			} catch (IOException e) {
				classes = Collections.emptySet();
			}
			List<ClassInfo> knownNotImportedClasses = classes.stream()
					.map(classInfo -> new ClassInfo(classInfo.getName()))
					.filter(classInfo -> !importedClasses.contains(classInfo))
					.collect(Collectors.toList());
			suggestionBuilder.putAll(suggestClasses(knownNotImportedClasses, importedPackages, insertionBegin, insertionEnd, classOrPackagePrefix, false));

			// Packages that have not been imported
			List<Package> knownNotImportedPackages = Arrays.stream(Package.getPackages())
					.filter(pack -> !importedPackages.contains(pack))
					.collect(Collectors.toList());
			suggestionBuilder.putAll(suggestPackages(knownNotImportedPackages, insertionBegin, insertionEnd, classOrPackagePrefix));

			return new CompletionSuggestions(insertionBegin, suggestionBuilder.build());
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
			for (ClassInfo importedClass : getImportedClasses()) {
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
			} catch (ClassNotFoundException | NoClassDefFoundError e) {
				return null;
			}
		}

		private Class<?> getThisClass() {
			ObjectInfo thisInfo = parserContext.getThisInfo();
			TypeToken<?> thisType = parserContext.getObjectInfoProvider().getType(thisInfo);
			return thisType == null ? null : thisType.getRawType();
		}

		private Set<ClassInfo> getImportedClasses() {
			Set<ClassInfo> importedClasses = new LinkedHashSet<>();
			importedClasses.addAll(PRIMITIVE_CLASS_INFOS);
			Class<?> thisClass = getThisClass();
			if (thisClass != null) {
				importedClasses.add(new ClassInfo(thisClass.getName()));
			}
			importedClasses.addAll(imports.getImportedClasses());
			return importedClasses;
		}

		private Set<Package> getImportedPackages() {
			Set<Package> importedPackages = new LinkedHashSet<>();
			Class<?> thisClass = getThisClass();
			if (thisClass != null) {
				importedPackages.add(thisClass.getPackage());
			}
			importedPackages.add(Package.getPackage("java.lang"));
			importedPackages.addAll(imports.getImportedPackages());
			return importedPackages;
		}
	}

	/*
	 * Class Suggestions
	 */
	private static int rateClassByName(ClassInfo classInfo, String expectedSimpleClassName) {
		// transformation required to make it comparable to rated fields and methods
		return (ParseUtils.TYPE_MATCH_NONE + 1)*ParseUtils.rateStringMatch(classInfo.getSimpleNameWithoutLeadingDigits(), expectedSimpleClassName) + ParseUtils.TYPE_MATCH_NONE;
	}

	private static ToIntFunction<ClassInfo> rateClassByNameFunc(final String simpleClassName) {
		return classInfo -> rateClassByName(classInfo, simpleClassName);
	}

	public static String getClassDisplayText(ClassInfo classInfo) {
		return classInfo.getName();
	}

	/*
	 * Package Suggestions
	 */
	private static int ratePackageByName(Package pack, String expectedName) {
		String packageName = pack.getName();
		int lastDotIndex = packageName.lastIndexOf('.');
		String subpackageName = packageName.substring(lastDotIndex + 1);
		// transformation required to make it comparable to rated fields and methods
		return (ParseUtils.TYPE_MATCH_NONE + 1)*ParseUtils.rateStringMatch(subpackageName, expectedName) + ParseUtils.TYPE_MATCH_NONE;
	}

	private static ToIntFunction<Package> ratePackageByNameFunc(final String packageName) {
		return pack -> ratePackageByName(pack, packageName);
	}

	public static String getPackageDisplayText(Package pack) {
		return pack.getName();
	}
}
