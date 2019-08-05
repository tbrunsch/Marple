package dd.kms.marple;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class DebugSupport
{
	private static final Pattern SLOT_NAME_PATTERN  = Pattern.compile("^[_\\$A-Za-z][_\\$A-Za-z0-9]*$");

	private final int _a = 1;
	private final int _$a_b$_ = 1;

	/**
	 * Set a breakpoint inside this method if you need features of your
	 * debugger that Marple lacks.
	 *
	 * Marple lets you export data into unnamed and named slots (see below)
	 * and gives you a convenient way to trigger this method as long as you
	 * do not configure a different breakpoint trigger method. You can then
	 * analyze the objects encountered by Marple with your debugger.
	 */
	private static int	BREAKPOINT_DUMMY	= 1;

	public static void triggerBreakpoint() {
		/*
		 * Dummy code that will hopefully not be optimized away by the compiler
		 *
		 * Put your breakpoint somewhere here.
		 */
		BREAKPOINT_DUMMY = -BREAKPOINT_DUMMY;
		if (BREAKPOINT_DUMMY != 0) {
			BREAKPOINT_DUMMY = 1;
		}
	}

	/**
	 * Fixed, unnamed slots for data exchange between Marple and a debugger
	 *
	 * Use these slots to store references to objects you have encountered
	 * during your analysis with Marple if you want to analyze them with
	 * your debugger.
	 *
	 * You can also set these slots programmatically from your debugger to
	 * access them from Marple.
	 */
	public static Object	SLOT_0;
	public static Object	SLOT_1;
	public static Object	SLOT_2;
	public static Object	SLOT_3;
	public static Object	SLOT_4;

	/**
	 * Named slots for data exchange between Marple and a debugger
	 *
	 * Same use cases as for unnamed slots, but preferable if you need to keep
	 * track of several objects and using generic names becomes too confusing.
	 *
	 * Additionally, Marple lets you transfer its variables to named slots and
	 * vice versa, so the data transfer is much more convenient than with multiple
	 * unnamed slots.
	 */
	private static final Map<String, Object>	NAMED_SLOTS	= Maps.newLinkedHashMap();

	public static void clearNamedSlots() {
		NAMED_SLOTS.clear();
	}

	public static void deleteSlot(String slotName) {
		NAMED_SLOTS.remove(slotName);
	}

	public static Collection<String> getSlotNames() {
		return Collections.unmodifiableSet(NAMED_SLOTS.keySet());
	}

	public static Object getSlotValue(String slotName) {
		return NAMED_SLOTS.get(slotName);
	}

	public static boolean setSlotValue(String slotName, Object value) {
		if (!isSlotNameValid(slotName)) {
			return false;
		}
		NAMED_SLOTS.put(slotName, value);
		return true;
	}

	public static boolean renameSlot(String oldSlotName, String newSlotName) {
		if (!NAMED_SLOTS.containsKey(oldSlotName)) {
			return false;
		}
		if (Objects.equals(oldSlotName, newSlotName)) {
			return true;
		}
		if (NAMED_SLOTS.containsKey(newSlotName)) {
			return false;
		}
		if (!isSlotNameValid(newSlotName)) {
			return false;
		}
		Object value = getSlotValue(oldSlotName);
		deleteSlot(oldSlotName);
		setSlotValue(newSlotName, value);
		return true;
	}

	private static boolean isSlotNameValid(String slotName) {
		return slotName != null && SLOT_NAME_PATTERN.matcher(slotName).matches();
	}
}
