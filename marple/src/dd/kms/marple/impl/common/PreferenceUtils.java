package dd.kms.marple.impl.common;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.impl.common.XmlUtils.ParseException;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
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

	public static void readSettings(InspectionSettings settings) {
		Path preferencesFile = settings.getPreferencesFile();
		if (!checkPreferenceFile(preferencesFile, true)) {
			return;
		}
		try (InputStream stream = Files.newInputStream(preferencesFile)) {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(stream);
			Element root = document.getDocumentElement();
			readSettings(root, settings);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			/* nothing we can do */
		}
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
		writeMinimumAccessModifier(root, evaluator);
		writeImportedPackages(root, evaluator);
		writeImportedClasses(root, evaluator);
		writeCustomActions(root, customActionSettings);

	}

	private static void readSettings(Element root, InspectionSettings settings) throws ParseException {
		int majorVersion = XmlUtils.readValueFromChild(root, "MajorVersion", XmlUtils::parseInt);
		if (majorVersion != 1) {
			throw new ParseException("Major version " + majorVersion + " of settings file is not supported");
		}
		ExpressionEvaluator evaluator = settings.getEvaluator();
		CustomActionSettings customActionSettings = settings.getCustomActionSettings();
		readEvaluationMode(root, evaluator);
		readMinimumAccessModifier(root, evaluator);
		readImportedPackages(root, evaluator);
		readImportedClasses(root, evaluator);
		readCustomActions(root, customActionSettings);
	}

	private static void writeEvaluationMode(Element root, ExpressionEvaluator evaluator) {
		EvaluationMode evaluationMode = evaluator.getParserSettings().getEvaluationMode();
		XmlUtils.writeValueToChild(root, "EvaluationMode", evaluationMode, EVALUATION_MODE_STRING_REPRESENTATIONS::get);
	}

	private static void readEvaluationMode(Element root, ExpressionEvaluator evaluator) throws ParseException {
		EvaluationMode evaluationMode = XmlUtils.readValueFromChild(root, "EvaluationMode", EVALUATION_MODE_STRING_REPRESENTATIONS.inverse()::get);
		if (evaluationMode != null) {
			ParserSettings parserSettings = evaluator.getParserSettings().builder().evaluationMode(evaluationMode).build();
			evaluator.setParserSettings(parserSettings);
		}
	}

	private static void writeMinimumAccessModifier(Element root, ExpressionEvaluator evaluator) {
		AccessModifier minimumAccessModifier = evaluator.getParserSettings().getMinimumAccessModifier();
		XmlUtils.writeValueToChild(root, "MinimumAccessModifier", minimumAccessModifier, ACCESS_MODIFIER_STRING_REPRESENTATIONS::get);
	}

	private static void readMinimumAccessModifier(Element root, ExpressionEvaluator evaluator) throws ParseException {
		AccessModifier minimumAccessModifier = XmlUtils.readValueFromChild(root, "MinimumAccessModifier", ACCESS_MODIFIER_STRING_REPRESENTATIONS.inverse()::get);
		if (minimumAccessModifier != null) {
			ParserSettings parserSettings = evaluator.getParserSettings().builder().minimumAccessModifier(minimumAccessModifier).build();
			evaluator.setParserSettings(parserSettings);
		}
	}

	private static void writeImportedPackages(Element root, ExpressionEvaluator evaluator) {
		Collection<String> importedPackages = evaluator.getParserSettings().getImports().getImportedPackages();
		XmlUtils.writeList(root, "ImportedPackages", "Package", importedPackages, Node::setTextContent);
	}

	private static void readImportedPackages(Element root, ExpressionEvaluator evaluator) throws ParseException {
		Set<String> importedPackages = new LinkedHashSet<>(XmlUtils.readList(root, "ImportedPackages", "Package", Node::getTextContent));
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

	private static void readImportedClasses(Element root, ExpressionEvaluator evaluator) throws ParseException {
		List<String> importedClassNames = XmlUtils.readList(root, "ImportedClasses", "Class", Node::getTextContent);
		Set<Class<?>> importedClasses = new LinkedHashSet<>(importedClassNames.size());
		for (String importedClassName : importedClassNames) {
			try {
				Class<?> importedClass = Class.forName(importedClassName);
				importedClasses.add(importedClass);
			} catch (ClassNotFoundException e) {
				/* skip */
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

	private static void readCustomActions(Element root, CustomActionSettings customActionSettings) throws ParseException {
		List<CustomAction> customActions = XmlUtils.readList(root, "CustomActions", "Action", PreferenceUtils::readCustomAction);
		customActionSettings.setCustomActions(customActions);
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
}
