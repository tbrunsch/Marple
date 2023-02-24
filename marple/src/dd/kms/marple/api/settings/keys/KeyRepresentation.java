package dd.kms.marple.api.settings.keys;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyRepresentation
{
	private static final Map<Integer, String> MODIFIER_TO_STRING	= ImmutableMap.<Integer, String>builder()
		.put(KeyEvent.CTRL_MASK,			"Ctrl")
		.put(KeyEvent.CTRL_DOWN_MASK,		"Ctrl")
		.put(KeyEvent.ALT_MASK,				"Alt")
		.put(KeyEvent.ALT_DOWN_MASK,		"Alt")
		.put(KeyEvent.ALT_GRAPH_MASK,		"AltGr")
		.put(KeyEvent.ALT_GRAPH_DOWN_MASK,	"AltGr")
		.put(KeyEvent.SHIFT_MASK,			"Shift")
		.put(KeyEvent.SHIFT_DOWN_MASK,		"Shift")
		.build();

	private static List<String> modifiersToStrings(int modifiers) {
		return MODIFIER_TO_STRING.keySet().stream()
			.filter(modifier -> (modifiers & modifier) != 0)
			.map(MODIFIER_TO_STRING::get)
			.distinct()
			.collect(Collectors.toList());
	}

	private static String keyCodeToString(int keyCode) {
		for (Field field : KeyEvent.class.getFields()) {
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
				continue;
			}
			String name = field.getName();
			if (!name.startsWith("VK_")) {
				continue;
			}
			if (field.getType() != int.class) {
				continue;
			}
			try {
				int value = (Integer) field.get(null);
				if (value == keyCode) {
					return extractKeyName(name);
				}
			} catch (IllegalAccessException e) {
				continue;
			}
		}
		return "<missing description>";
	}

	private static String extractKeyName(String keyFieldName) {
		assert keyFieldName.startsWith("VK_");
		String keyNameUpperUnderscore = keyFieldName.substring(3);
		return Joiner.on(" ").join(
			Iterables.transform(
				Splitter.on("_").split(keyNameUpperUnderscore),
				part -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, part)
			)
		);
	}

	private final int	modifiers;
	private final int	keyCode;

	public KeyRepresentation(int modifiers, int keyCode) {
		this.modifiers = modifiers;
		this.keyCode = keyCode;
	}

	public int getModifiers() {
		return modifiers;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public boolean matches(KeyRepresentation expectedKey) {
		return expectedKey != null && keyCode == expectedKey.keyCode && modifiers == expectedKey.modifiers;
	}

	public KeyStroke asKeyStroke() {
		return KeyStroke.getKeyStroke(keyCode, modifiers);
	}

	@Override
	public String toString() {
		return Joiner.on(" + ").join(
			Iterables.concat(
				modifiersToStrings(modifiers),
				ImmutableList.of(keyCodeToString(keyCode))
			)
		);
	}
}
