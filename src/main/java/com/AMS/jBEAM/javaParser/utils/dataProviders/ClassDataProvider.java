package com.AMS.jBEAM.javaParser.utils.dataProviders;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.settings.Imports;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.wrappers.ClassInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
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

	private static final ClassPath				CLASS_PATH;
	private static final Set<String>			TOP_LEVEL_CLASS_NAMES;
	private static final Set<String>			PACKAGE_NAMES;

	static {
		ClassPath classPath;
		Set<String> topLevelClassNames;
		try {
			classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
			topLevelClassNames = classPath.getTopLevelClasses()
				.stream()
				.map(ClassPath.ClassInfo::getName)
				.collect(Collectors.toSet());
		} catch (IOException e) {
			classPath = null;
			topLevelClassNames = ImmutableSet.of();
		}
		CLASS_PATH = classPath;

		TOP_LEVEL_CLASS_NAMES = topLevelClassNames;

		Set<String> packageNames = new LinkedHashSet<>();
		for (String className : TOP_LEVEL_CLASS_NAMES) {
			for (String packageName = getParent(className); packageName != null; packageName = getParent(packageName)) {
				packageNames.add(packageName);
			}
		}
		PACKAGE_NAMES = ImmutableSet.copyOf(packageNames);
	}

	private final ParserContext	parserContext;
	private final Imports		imports;

	public ClassDataProvider(ParserContext parserContext) {
		this.parserContext = parserContext;
		this.imports = parserContext.getSettings().getImports();
	}

	public ParseResultIF readClass(TokenStream tokenStream) {
		ClassReader reader = new ClassReader(parserContext, imports, tokenStream);
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

	private static int lastIndexOfPathSeparator(String path) {
		return Math.max(path.lastIndexOf('.'), path.lastIndexOf('$'));
	}

	private static String getParent(String path) {
		int lastSeparatorIndex = lastIndexOfPathSeparator(path);
		return lastSeparatorIndex < 0 ? null : path.substring(0, lastSeparatorIndex);
	}

	private static String getLeaf(String path) {
		int lastSeparatorIndex = lastIndexOfPathSeparator(path);
		return path.substring(lastSeparatorIndex + 1);
	}

	private static class ClassReader
	{
		private final ParserContext	parserContext;
		private final Imports 		imports;
		private final TokenStream	tokenStream;

		private String				packageOrClassName		= "";
		private int					identifierStartPosition	= -1;

		ClassReader(ParserContext parserContext, Imports imports, TokenStream tokenStream) {
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
			return TOP_LEVEL_CLASS_NAMES.stream().anyMatch(name -> name.startsWith(packagePrefix));
		}

		private Map<CompletionSuggestionIF, Integer> suggestUnqualifiedClasses(String classPrefix, int insertionBegin, int insertionEnd, Set<ClassInfo> classes, Set<ClassInfo> suggestedClasses) {
			if (lastIndexOfPathSeparator(classPrefix) >= 0) {
				// class is fully qualified, so no match
				return ImmutableMap.of();
			}
			Sets.SetView<ClassInfo> newSuggestedClasses = Sets.difference(classes, suggestedClasses);
			classes.addAll(newSuggestedClasses);
			return ParseUtils.createRatedSuggestions(
				newSuggestedClasses,
				classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
				rateClassByNameFunc(classPrefix)
			);
		}

		private static Map<CompletionSuggestionIF, Integer> suggestQualifiedClasses(String classPrefixWithPackage, int insertionBegin, int insertionEnd, Set<ClassInfo> suggestedClasses) {
			String packageName = getParent(classPrefixWithPackage);
			if (packageName == null) {
				// class is not fully qualified, so no match
				return ImmutableMap.of();
			}
			String prefix = packageName + ".";
			int lastSeparatorIndex = packageName.length();
			List<ClassInfo> newSuggestedClasses = new ArrayList<>();
			for (String className : TOP_LEVEL_CLASS_NAMES) {
				if (lastIndexOfPathSeparator(className) != lastSeparatorIndex) {
					continue;
				}
				if (!className.startsWith(prefix)) {
					continue;
				}
				ClassInfo clazz = new ClassInfo(className);
				if (suggestedClasses.contains(clazz)) {
					continue;
				}
				newSuggestedClasses.add(clazz);
			}
			suggestedClasses.addAll(newSuggestedClasses);
			String classPrefix = getLeaf(classPrefixWithPackage);
			return ParseUtils.createRatedSuggestions(
				newSuggestedClasses,
				classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
				rateClassByNameFunc(classPrefix)
			);
		}

		private static Map<CompletionSuggestionIF, Integer> suggestPackages(String packagePrefix, int insertionBegin, int insertionEnd) {
			String parentPackage = getParent(packagePrefix);
			int lastSeparatorIndex = lastIndexOfPathSeparator(packagePrefix);
			List<String> suggestedPackageNames = new ArrayList<>();
			for (String packageName : PACKAGE_NAMES) {
				if (lastIndexOfPathSeparator(packageName) != lastSeparatorIndex) {
					continue;
				}
				if (parentPackage != null && !packageName.startsWith(parentPackage)) {
					continue;
				}
				suggestedPackageNames.add(packageName);
			}
			String subpackagePrefix = getLeaf(packagePrefix);
			return ParseUtils.createRatedSuggestions(
				suggestedPackageNames,
				packageName -> new CompletionSuggestionPackage(packageName, insertionBegin, insertionEnd),
				ratePackageByNameFunc(subpackagePrefix)
			);
		}

		private CompletionSuggestions suggestClassesAndPackages(int insertionBegin, int insertionEnd, String classOrPackagePrefix) {
			ImmutableMap.Builder<CompletionSuggestionIF, Integer> suggestionBuilder = ImmutableMap.builder();

			Set<ClassInfo> importedClasses = getImportedClasses();
			Set<Package> importedPackages = getImportedPackages();
			Set<ClassInfo> topLevelClassesInPackages = getTopLevelClassesInPackages(importedPackages);

			Set<ClassInfo> suggestedClasses = new HashSet<>();

			suggestionBuilder.putAll(suggestUnqualifiedClasses(classOrPackagePrefix, insertionBegin, insertionEnd, importedClasses, suggestedClasses));
			suggestionBuilder.putAll(suggestUnqualifiedClasses(classOrPackagePrefix, insertionBegin, insertionBegin, topLevelClassesInPackages, suggestedClasses));
			suggestionBuilder.putAll(suggestQualifiedClasses(classOrPackagePrefix, insertionBegin, insertionEnd, suggestedClasses));
			suggestionBuilder.putAll(suggestPackages(classOrPackagePrefix, insertionBegin, insertionEnd));

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

		private Set<ClassInfo> getTopLevelClassesInPackages(Collection<Package> packages) {
			Set<ClassInfo> classes = new HashSet<>();
			for (Package pack : Iterables.filter(packages, Objects::nonNull)) {
				String packageName = pack.getName();
				if (packageName == null) {
					continue;
				}
				for (ClassPath.ClassInfo classInfo : CLASS_PATH.getTopLevelClasses(packageName)) {
					classes.add(new ClassInfo(classInfo.getName()));
				}
			}
			return classes;
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
	private static int ratePackageByName(String packageName, String expectedName) {
		int lastDotIndex = packageName.lastIndexOf('.');
		String subpackageName = packageName.substring(lastDotIndex + 1);
		// transformation required to make it comparable to rated fields and methods
		return (ParseUtils.TYPE_MATCH_NONE + 1)*ParseUtils.rateStringMatch(subpackageName, expectedName) + ParseUtils.TYPE_MATCH_NONE;
	}

	private static ToIntFunction<String> ratePackageByNameFunc(String expectedName) {
		return packageName -> ratePackageByName(packageName, expectedName);
	}
}
