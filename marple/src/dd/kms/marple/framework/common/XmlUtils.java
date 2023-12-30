package dd.kms.marple.framework.common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class XmlUtils
{
	public static Element createChildElement(Element node, String childName) {
		Document document = node.getOwnerDocument();
		Element child = document.createElement(childName);
		node.appendChild(child);
		return child;
	}

	public static List<Element> getChildElements(Element node, String childName) {
		NodeList childNodes = node.getChildNodes();
		int numChildren = childNodes.getLength();
		List<Element> childElements = new ArrayList<>();
		for (int i = 0; i < numChildren; i++) {
			Node child = childNodes.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && Objects.equals(child.getNodeName(), childName)) {
				childElements.add((Element) child);
			}
		}
		return childElements;
	}

	public static Element getUniqueChild(Element node, String childName) throws ParseException {
		List<Element> childElements = getChildElements(node, childName);
		if (childElements.size() == 1) {
			return childElements.get(0);
		}
		throw new ParseException(node.getNodeName() + " does not have a unique child '" + childName + "'");
	}

	public static <T> void writeValueToChild(Element node, String childName, T value, Formatter<T> formatter) {
		Element child = createChildElement(node, childName);
		String stringRepresentation = formatter.format(value);
		if (stringRepresentation != null) {
			child.setTextContent(stringRepresentation);
		}
	}

	public static <T> T readValueFromChild(Element node, String childName, Parser<T> parser) throws ParseException {
		Element child = getUniqueChild(node, childName);
		String stringRepresentation = child.getTextContent();
		stringRepresentation = stringRepresentation.trim();
		return parser.parse(stringRepresentation);
	}

	public static <T extends Enum<T>> void writeEnumAttribute(Element node, T value) {
		String valueName = value.name();
		node.setAttribute("value", valueName);
	}

	public static <T extends Enum<T>> T readEnumAttribute(Element node, Class<T> enumClass) throws ParseException {
		String valueName = node.getAttribute("value");
		if (valueName == null) {
			throw new ParseException("Missing attribute 'value'");
		}
		T[] enumConstants = enumClass.getEnumConstants();
		for (T enumConstant : enumConstants) {
			if (Objects.equals(enumConstant.name(), valueName)) {
				return enumConstant;
			}
		}
		throw new ParseException("Unknown enum value '" + enumClass.getSimpleName() + "." + valueName + "'");
	}

	public static <T> void writeList(Element root, String listNodeName, String listElementName, Collection<T> list, StructWriter<T> elementWriter) {
		Element listNode = createChildElement(root, listNodeName);
		for (T element : list) {
			Element listElementNode = createChildElement(listNode, listElementName);
			elementWriter.writeStruct(listElementNode, element);
		}
	}

	public static <T> List<T> readList(Element root, String listNodeName, String listElementName, StructReader<T> elementReader) throws ParseException {
		Element listNode = getUniqueChild(root, listNodeName);
		List<Element> childNodes = getChildElements(listNode, listElementName);
		List<T> list = new ArrayList<>(childNodes.size());
		for (Element childNode : childNodes) {
			T element = elementReader.readStruct(childNode);
			list.add(element);
		}
		return list;
	}

	public static String formatInt(int value) {
		return String.valueOf(value);
	}

	public static int parseInt(String s) throws ParseException {
		if (s != null) {
			s = s.trim();
		}
		if (s != null && !s.isEmpty()) {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				/* handled later */
			}
		}
		throw new ParseException("Could not parse '" + s + "' as integer");
	}

	@FunctionalInterface
	public interface Formatter<T>
	{
		String format(T value);
	}

	@FunctionalInterface
	public interface Parser<T>
	{
		T parse(String valueAsString) throws ParseException;
	}

	@FunctionalInterface
	public interface StructWriter<S>
	{
		void writeStruct(Element node, S struct);
	}

	@FunctionalInterface
	public interface StructReader<S>
	{
		S readStruct(Element node) throws ParseException;
	}

	public static class ParseException extends IOException
	{
		ParseException(String message) {
			super(message);
		}
	}
}
