package dd.kms.marple.framework.common;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.evaluation.AdditionalEvaluationSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.framework.common.XmlUtils.ParseException;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PreferenceUtils
{
	private static final BiMap<EvaluationMode, String>	EVALUATION_MODE_STRING_REPRESENTATIONS	= ImmutableBiMap.of(
		EvaluationMode.STATIC_TYPING, 	"Static typing",
		EvaluationMode.DYNAMIC_TYPING,	"Dynamic typing",
		EvaluationMode.MIXED,			"Mixed"
	);

	private static final BiMap<AccessModifier, String>	ACCESS_MODIFIER_STRING_REPRESENTATIONS	= ImmutableBiMap.of(
		AccessModifier.PRIVATE,			"Private",
		AccessModifier.PACKAGE_PRIVATE,	"Package private",
		AccessModifier.PROTECTED,		"Protected",
		AccessModifier.PUBLIC,			"Public"
	);

	public static void writeSettings(InspectionSettings settings) {
		Path preferencesFile = settings.getPreferencesFile();
		if (!checkPreferenceFile(preferencesFile, false)) {
			return;
		}
		try (OutputStream stream = Files.newOutputStream(preferencesFile)) {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.newDocument();
			Element root = document.createElement("Settings");
			document.appendChild(root);
			writeSettings(root, settings);
			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(stream);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(domSource, streamResult);
		} catch (ParserConfigurationException | IOException | TransformerException e) {
			/* nothing we can do */
		}
	}

	public static void readSettings(InspectionContext context) {
		InspectionSettings settings = context.getSettings();
		Path preferencesFile = settings.getPreferencesFile();
		if (!checkPreferenceFile(preferencesFile, true)) {
			return;
		}
		try (InputStream stream = Files.newInputStream(preferencesFile)) {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(stream);
			Element root = document.getDocumentElement();
			readSettings(root, context);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			/* nothing we can do */
		} finally {
			EvaluationSettings evaluationSettings = settings.getEvaluationSettings();
			Collection<AdditionalEvaluationSettings> additionalSettings = evaluationSettings.getAdditionalSettings().values();
			for (AdditionalEvaluationSettings additionalEvalSettings : additionalSettings) {
				additionalEvalSettings.applySettings(context);
			}
		}

	}

	public static void logParseExceptionAsWarning(ParseException parseException) {
		System.out.println("Warning while reading Marple preferences: " + parseException.getMessage());
	}

	private static boolean checkPreferenceFile(Path preferencesFile, boolean fileMustExist) {
		if (preferencesFile == null) {
			return false;
		}
		if (Files.isRegularFile(preferencesFile)) {
			return true;
		}
		return !fileMustExist && !Files.exists(preferencesFile);
	}

	private static void writeSettings(Element root, InspectionSettings settings) {
		XmlUtils.writeValueToChild(root, "MajorVersion", 1, XmlUtils::formatInt);
		XmlUtils.writeValueToChild(root, "MinorVersion", 0, XmlUtils::formatInt);

		ExpressionEvaluator evaluator = settings.getEvaluator();
		CustomActionSettings customActionSettings = settings.getCustomActionSettings();
		writeEvaluationMode(root, evaluator);
		writeMinimumAccessModifiers(root, evaluator);
		writeImportedPackages(root, evaluator);
		writeImportedClasses(root, evaluator);
		writeCustomActions(root, customActionSettings);
		writeAdditionalEvaluationSettings(root, settings.getEvaluationSettings().getAdditionalSettings());
	}

	private static void readSettings(Element root, InspectionContext context) {
		Integer majorVersion = XmlUtils.readValueFromChildOrNull(root, "MajorVersion", XmlUtils::parseInt);
		if (majorVersion == null) {
			logParseExceptionAsWarning(new ParseException("Cannot read major version"));
			return;
		}
		if (majorVersion != 1) {
			logParseExceptionAsWarning(new ParseException("Major version " + majorVersion + " of settings file is not supported"));
			return;
		}
		InspectionSettings settings = context.getSettings();
		ExpressionEvaluator evaluator = settings.getEvaluator();
		CustomActionSettings customActionSettings = settings.getCustomActionSettings();
		readEvaluationMode(root, evaluator);
		readMinimumAccessModifiers(root, evaluator);
		readImportedPackages(root, evaluator);
		readImportedClasses(root, evaluator);
		readCustomActions(root, customActionSettings);

		EvaluationSettings evaluationSettings = settings.getEvaluationSettings();
		Collection<AdditionalEvaluationSettings> additionalSettings = evaluationSettings.getAdditionalSettings().values();
		readAdditionalEvaluationSettings(root, additionalSettings);
	}

	private static void writeEvaluationMode(Element root, ExpressionEvaluator evaluator) {
		EvaluationMode evaluationMode = evaluator.getParserSettings().getEvaluationMode();
		XmlUtils.writeValueToChild(root, "EvaluationMode", evaluationMode, EVALUATION_MODE_STRING_REPRESENTATIONS::get);
	}

	private static void readEvaluationMode(Element root, ExpressionEvaluator evaluator) {
		EvaluationMode evaluationMode = XmlUtils.readValueFromChildOrNull(root, "EvaluationMode", EVALUATION_MODE_STRING_REPRESENTATIONS.inverse()::get);
		if (evaluationMode == null) {
			logParseExceptionAsWarning(new ParseException("Cannot read evaluation mode"));
			return;
		}
		ParserSettings parserSettings = evaluator.getParserSettings().builder().evaluationMode(evaluationMode).build();
		evaluator.setParserSettings(parserSettings);
	}

	private static void writeMinimumAccessModifiers(Element root, ExpressionEvaluator evaluator) {
		AccessModifier minimumFieldAccessModifier = evaluator.getParserSettings().getMinimumFieldAccessModifier();
		AccessModifier minimumMethodAccessModifier = evaluator.getParserSettings().getMinimumMethodAccessModifier();
		XmlUtils.writeValueToChild(root, "MinimumAccessModifier", minimumFieldAccessModifier, ACCESS_MODIFIER_STRING_REPRESENTATIONS::get);
		XmlUtils.writeValueToChild(root, "MinimumMethodAccessModifier", minimumMethodAccessModifier, ACCESS_MODIFIER_STRING_REPRESENTATIONS::get);
	}

	private static void readMinimumAccessModifiers(Element root, ExpressionEvaluator evaluator) {
		ParserSettingsBuilder parserSettingsBuilder = evaluator.getParserSettings().builder();

		AccessModifier minimumAccessModifier = XmlUtils.readValueFromChildOrNull(root, "MinimumAccessModifier", ACCESS_MODIFIER_STRING_REPRESENTATIONS.inverse()::get);
		if (minimumAccessModifier == null) {
			logParseExceptionAsWarning(new ParseException("Cannot read minimum access modifier"));
		} else {
			/*
			 * In older versions, "MinimumAccessModifier" represented both, the
			 * minimum access modifier for fields and for methods.
			 */
			parserSettingsBuilder.minimumFieldAccessModifier(minimumAccessModifier);
			parserSettingsBuilder.minimumMethodAccessModifier(minimumAccessModifier);
		}

		// Will be null for old preferences files
		AccessModifier minimumMethodAccessModifier = XmlUtils.readValueFromChildOrNull(root, "MinimumMethodAccessModifier", ACCESS_MODIFIER_STRING_REPRESENTATIONS.inverse()::get);
		if (minimumMethodAccessModifier != null) {
			parserSettingsBuilder.minimumMethodAccessModifier(minimumMethodAccessModifier);
		}

		ParserSettings parserSettings = parserSettingsBuilder.build();
		evaluator.setParserSettings(parserSettings);
	}

	private static void writeImportedPackages(Element root, ExpressionEvaluator evaluator) {
		Collection<String> importedPackages = evaluator.getParserSettings().getImports().getImportedPackages();
		XmlUtils.writeList(root, "ImportedPackages", "Package", importedPackages, Node::setTextContent);
	}

	private static void readImportedPackages(Element root, ExpressionEvaluator evaluator) {
		List<String> importedPackagesList = XmlUtils.readListOrNull(root, "ImportedPackages", "Package", Node::getTextContent);
		Set<String> importedPackages = new LinkedHashSet<>();
		if (importedPackagesList == null) {
			logParseExceptionAsWarning(new ParseException("Cannot read imported packages"));
		} else {
			importedPackages.addAll(importedPackagesList);
		}

		ParserSettings oldParserSettings = evaluator.getParserSettings();

		// combine stored and old imported packages
		importedPackages.addAll(oldParserSettings.getImports().getImportedPackages());

		ParserSettings parserSettings = oldParserSettings.builder().importPackages(importedPackages).build();
		evaluator.setParserSettings(parserSettings);
	}

	private static void writeImportedClasses(Element root, ExpressionEvaluator evaluator) {
		Set<Class<?>> importedClasses = evaluator.getParserSettings().getImports().getImportedClasses();
		List<String> importedClassNames = importedClasses.stream().map(Class::getName).collect(Collectors.toList());
		XmlUtils.writeList(root, "ImportedClasses", "Class", importedClassNames, Node::setTextContent);
	}

	private static void readImportedClasses(Element root, ExpressionEvaluator evaluator) {
		List<String> importedClassNames = XmlUtils.readListOrNull(root, "ImportedClasses", "Class", Node::getTextContent);
		Set<Class<?>> importedClasses = new LinkedHashSet<>();
		if (importedClassNames == null) {
			logParseExceptionAsWarning(new ParseException("Cannot read imported class names"));
		} else {
			for (String importedClassName : importedClassNames) {
				try {
					Class<?> importedClass = Class.forName(importedClassName);
					importedClasses.add(importedClass);
				} catch (ClassNotFoundException e) {
					/* skip */
				}
			}
		}

		ParserSettings oldParserSettings = evaluator.getParserSettings();

		// combine stored and old imported classes
		importedClasses.addAll(oldParserSettings.getImports().getImportedClasses());

		ParserSettings parserSettings = oldParserSettings.builder().importClasses(importedClasses).build();
		evaluator.setParserSettings(parserSettings);
	}

	private static void writeCustomActions(Element root, CustomActionSettings customActionSettings) {
		List<CustomAction> customActions = customActionSettings.getCustomActions();
		XmlUtils.writeList(root, "CustomActions", "Action", customActions, PreferenceUtils::writeCustomAction);
	}

	private static void readCustomActions(Element root, CustomActionSettings customActionSettings) {
		List<CustomAction> customActions = XmlUtils.readListOrNull(root, "CustomActions", "Action", PreferenceUtils::readCustomAction);
		if (customActions == null) {
			logParseExceptionAsWarning(new ParseException("Cannot read custom actions"));
		} else {
			customActionSettings.setCustomActions(customActions);
		}
	}

	private static void writeCustomAction(Element node, CustomAction customAction) {
		XmlUtils.writeValueToChild(node, "Name", customAction.getName(), s -> s);
		XmlUtils.writeValueToChild(node, "Expression", customAction.getActionExpression(), s -> s);
		XmlUtils.writeValueToChild(node, "Class", customAction.getThisClass(), Class::getName);
		writeKey(node, customAction.getKey());
	}

	private static CustomAction readCustomAction(Element node) throws ParseException {
		String name = XmlUtils.readValueFromChild(node, "Name", s -> s);
		String expression = XmlUtils.readValueFromChild(node, "Expression", s -> s);
		String className = XmlUtils.readValueFromChild(node, "Class", s -> s);
		Class<?> thisClass;
		try {
			thisClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new ParseException("Could not find class '" + className + "': " + e);
		}
		KeyRepresentation key = readKey(node);
		return CustomAction.create(name, expression, thisClass, key);
	}

	private static void writeKey(Element node, @Nullable KeyRepresentation key) {
		if (key == null) {
			return;
		}
		Element keyNode = XmlUtils.createChildElement(node, "Key");
		XmlUtils.writeValueToChild(keyNode, "Modifiers", key.getModifiers(), XmlUtils::formatInt);
		XmlUtils.writeValueToChild(keyNode, "KeyCode", key.getKeyCode(), XmlUtils::formatInt);
	}

	@Nullable
	private static KeyRepresentation readKey(Element node) throws ParseException {
		List<Element> keyNodes = XmlUtils.getChildElements(node, "Key");
		if (keyNodes.isEmpty()) {
			return null;
		}
		if (keyNodes.size() > 1) {
			throw new ParseException("Found multiple keys for one custom action");
		}
		Element keyNode = keyNodes.get(0);
		int modifiers = XmlUtils.readValueFromChild(keyNode, "Modifiers", XmlUtils::parseInt);
		int keyCode = XmlUtils.readValueFromChild(keyNode, "KeyCode", XmlUtils::parseInt);
		return new KeyRepresentation(modifiers, keyCode);
	}

	private static void writeAdditionalEvaluationSettings(Element root, Map<String, AdditionalEvaluationSettings> additionalEvaluationSettings) {
		Element additionalEvaluationSettingsElement = XmlUtils.createChildElement(root, "AdditionalEvaluationSettings");
		for (AdditionalEvaluationSettings additionalEvalSettings : additionalEvaluationSettings.values()) {
			String childName = additionalEvalSettings.getXmlElementName();
			if (childName == null) {
				continue;
			}
			Element settingsElement = XmlUtils.createChildElement(additionalEvaluationSettingsElement, childName);
			additionalEvalSettings.writeSettings(settingsElement);
		}
	}

	private static void readAdditionalEvaluationSettings(Element root, Collection<AdditionalEvaluationSettings> additionalEvaluationSettings) {
		Element additionalEvaluationSettingsElement = XmlUtils.getUniqueChildOrNull(root, "AdditionalEvaluationSettings");
		if (additionalEvaluationSettingsElement == null) {
			// no settings stored
			return;
		}
		for (AdditionalEvaluationSettings additionalEvalSettings : additionalEvaluationSettings) {
			String childName = additionalEvalSettings.getXmlElementName();
			if (childName == null) {
				continue;
			}
			Element settingsElement = XmlUtils.getUniqueChildOrNull(additionalEvaluationSettingsElement, childName);
			if (settingsElement == null) {
				// no settings stored
				continue;
			}
			try {
				additionalEvalSettings.readSettings(settingsElement);
			} catch (ParseException e) {
				logParseExceptionAsWarning(e);
			} catch (IOException e) {
				logParseExceptionAsWarning(new ParseException(e.getMessage()));
			}
		}
	}
}
