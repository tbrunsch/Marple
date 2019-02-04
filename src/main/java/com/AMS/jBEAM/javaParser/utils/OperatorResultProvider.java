package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import com.AMS.jBEAM.javaParser.tokenizer.BinaryOperator;
import com.AMS.jBEAM.javaParser.tokenizer.UnaryOperator;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OperatorResultProvider
{
	private static final Set<Class<?>>	INTEGRAL_PRIMITIVE_CLASSES			= ImmutableSet.of(char.class, byte.class, short.class, int.class, long.class);
	private static final Set<Class<?>>	FLOATING_POINT_PRIMITIVE_CLASSES	= ImmutableSet.of(float.class, double.class);
	private static final Set<Class<?>>	NUMERIC_PRIMITIVE_CLASSES			= ImmutableSet.<Class<?>>builder().addAll(INTEGRAL_PRIMITIVE_CLASSES).addAll(FLOATING_POINT_PRIMITIVE_CLASSES).build();

	private static final Table<UnaryOperator, Class<?>, UnaryOperatorInfo>		UNARY_OPERATOR_WITH_ASSIGNMENT_INFO_BY_OPERATOR_AND_TYPE	= HashBasedTable.create();
	private static final Table<UnaryOperator, Class<?>, UnaryOperatorInfo>		SIGN_OPERATOR_INFO_BY_OPERATOR_AND_TYPE						= HashBasedTable.create();

	private static final Table<BinaryOperator, Class<?>, BinaryOperatorInfo> 	NUMERIC_OPERATOR_INFO_BY_OPERATOR_AND_TYPE 					= HashBasedTable.create();
	private static final Table<BinaryOperator, Class<?>, BinaryOperatorInfo>	SHIFT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE					= HashBasedTable.create();
	private static final Table<BinaryOperator, Class<?>, BinaryOperatorInfo>	NUMERIC_COMPARISON_OPERATOR_INFO_BY_OPERATOR_AND_TYPE		= HashBasedTable.create();
	private static final Table<BinaryOperator, Class<?>, BinaryOperatorInfo>	BIT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE						= HashBasedTable.create();
	private static final Map<BinaryOperator, BinaryOperatorInfo> 				LOGICAL_OPERATOR_INFO_BY_OPERATOR							= new HashMap<>();

	private static <S> void addUnaryOperatorWithAssignmentImplementation(UnaryOperator operator, Class<S> operandClass, Function<S, S> implementation) {
		Function<Object, Object> wrappedImplementation = o -> implementation.apply(ReflectionUtils.convertTo(o, operandClass, false));
		Class<?> resultClass = operandClass;
		UNARY_OPERATOR_WITH_ASSIGNMENT_INFO_BY_OPERATOR_AND_TYPE.put(operator, operandClass, new UnaryOperatorInfo(resultClass, wrappedImplementation));
	}

	private static void addUnaryOperatorWithAssignmentImplementations(UnaryOperator operator, Function<Character, Character> charImpl, Function<Byte, Byte> byteImpl, Function<Short, Short> shortImpl, Function<Integer, Integer> intImpl, Function<Long, Long> longImpl) {
		addUnaryOperatorWithAssignmentImplementation(operator,	char.class,		charImpl);
		addUnaryOperatorWithAssignmentImplementation(operator,	byte.class,		byteImpl);
		addUnaryOperatorWithAssignmentImplementation(operator,	short.class,	shortImpl);
		addUnaryOperatorWithAssignmentImplementation(operator,	int.class,		intImpl);
		addUnaryOperatorWithAssignmentImplementation(operator,	long.class,		longImpl);
	}

	private static <S, T> void addSignOperatorImplementation(UnaryOperator operator, Class<S> operandClass, Class<T> resultClass, Function<S, T> implementation) {
		Function<Object, Object> wrappedImplementation = o -> implementation.apply(ReflectionUtils.convertTo(o, operandClass, false));
		SIGN_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.put(operator, operandClass, new UnaryOperatorInfo(resultClass, wrappedImplementation));
	}

	private static void addSignOperatorImplementations(UnaryOperator operator, Function<Byte, Integer> byteImpl, Function<Short, Integer> shortImpl, Function<Integer, Integer> intImpl, Function<Long, Long> longImpl, Function<Float, Float> floatImpl, Function<Double, Double> doubleImpl) {
		addSignOperatorImplementation(operator,	byte.class,		int.class,		byteImpl);
		addSignOperatorImplementation(operator,	short.class,	int.class,		shortImpl);
		addSignOperatorImplementation(operator,	int.class,		int.class,		intImpl);
		addSignOperatorImplementation(operator,	long.class,		long.class,		longImpl);
		addSignOperatorImplementation(operator,	float.class,	float.class,	floatImpl);
		addSignOperatorImplementation(operator,	double.class,	double.class,	doubleImpl);
	}

	private static <S, T> void addNumericOperatorImplementation(BinaryOperator operator, Class<S> operandClass, Class<T> resultClass, BiFunction<S, S, T> implementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (o1, o2) -> implementation.apply(ReflectionUtils.convertTo(o1, operandClass, false), ReflectionUtils.convertTo(o2, operandClass, false));
		NUMERIC_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.put(operator, operandClass, new BinaryOperatorInfo(resultClass, wrappedImplementation));
	}

	private static <S, T> void addNumericOperatorImplementations(Class<S> operandClass, Class<T> resultClass, BiFunction<S, S, T> mulImpl, BiFunction<S, S, T> divImpl, BiFunction<S, S, T> addImpl, BiFunction<S, S, T> subImpl) {
		addNumericOperatorImplementation(BinaryOperator.MULTIPLY, 		operandClass, resultClass, mulImpl);
		addNumericOperatorImplementation(BinaryOperator.DIVIDE, 		operandClass, resultClass, divImpl);
		addNumericOperatorImplementation(BinaryOperator.ADD_OR_CONCAT, 	operandClass, resultClass, addImpl);
		addNumericOperatorImplementation(BinaryOperator.SUBTRACT, 		operandClass, resultClass, subImpl);
	}

	private static <S, T> void addNumericOperatorImplementations(Class<S> operandClass, Class<T> resultClass, BiFunction<S, S, T> mulImpl, BiFunction<S, S, T> divImpl, BiFunction<S, S, T> addImpl, BiFunction<S, S, T> subImpl, BiFunction<S, S, T> modImpl) {
		addNumericOperatorImplementations(operandClass, resultClass, mulImpl, divImpl, addImpl, subImpl);
		addNumericOperatorImplementation(BinaryOperator.MODULO,			operandClass, resultClass, modImpl);
	}

	private static <S, T> void addShiftOperatorImplementation(BinaryOperator operator, Class<S> lhsClass, Class<T> resultClass, BiFunction<S, Long, T> implementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (o1, o2) -> implementation.apply(ReflectionUtils.convertTo(o1, lhsClass, false), ReflectionUtils.convertTo(o2, long.class, false));
		SHIFT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.put(operator, lhsClass, new BinaryOperatorInfo(resultClass, wrappedImplementation));
	}

	private static void addShiftOperatorImplementations(BinaryOperator operator, BiFunction<Character, Long, Integer> charImpl, BiFunction<Byte, Long, Integer> byteImpl, BiFunction<Short, Long, Integer> shortImpl, BiFunction<Integer, Long, Integer> intImpl, BiFunction<Long, Long, Long> longImpl) {
		addShiftOperatorImplementation(operator, char.class,	int.class, 	charImpl);
		addShiftOperatorImplementation(operator, byte.class,	int.class, 	byteImpl);
		addShiftOperatorImplementation(operator, short.class,	int.class, 	shortImpl);
		addShiftOperatorImplementation(operator, int.class,		int.class, 	intImpl);
		addShiftOperatorImplementation(operator, long.class,	long.class,	longImpl);
	}

	private static <S> void addNumericComparisonImplementation(BinaryOperator operator, Class<S> operandClass, BiFunction<S, S, Boolean> implementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (o1, o2) -> implementation.apply(ReflectionUtils.convertTo(o1, operandClass, false), ReflectionUtils.convertTo(o2, operandClass, false));
		NUMERIC_COMPARISON_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.put(operator, operandClass, new BinaryOperatorInfo(boolean.class, wrappedImplementation));
	}

	private static void addNumericComparisonImplementations(BinaryOperator operator, BiFunction<Character, Character, Boolean> charImpl, BiFunction<Byte, Byte, Boolean> byteImpl, BiFunction<Short, Short, Boolean> shortImpl, BiFunction<Integer, Integer, Boolean> intImpl, BiFunction<Long, Long, Boolean> longImpl, BiFunction<Float, Float, Boolean> floatImpl, BiFunction<Double, Double, Boolean> doubleImpl) {
		addNumericComparisonImplementation(operator,	char.class,		charImpl);
		addNumericComparisonImplementation(operator,	byte.class,		byteImpl);
		addNumericComparisonImplementation(operator,	short.class,	shortImpl);
		addNumericComparisonImplementation(operator,	int.class,		intImpl);
		addNumericComparisonImplementation(operator,	long.class,		longImpl);
		addNumericComparisonImplementation(operator,	float.class,	floatImpl);
		addNumericComparisonImplementation(operator,	double.class,	doubleImpl);
	}

	private static <S, T> void addBitOperatorImplementation(BinaryOperator operator, Class<S> operandClass, Class<T> resultClass, BiFunction<S, S, T> implementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (o1, o2) -> implementation.apply(ReflectionUtils.convertTo(o1, operandClass, false), ReflectionUtils.convertTo(o2, operandClass, false));
		BIT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.put(operator, operandClass, new BinaryOperatorInfo(resultClass, wrappedImplementation));
	}

	private static <S, T> void addBitOperatorImplementations(Class<S> operandClass, Class<T> resultClass, BiFunction<S, S, T> andImpl, BiFunction<S, S, T> xorImpl, BiFunction<S, S, T> orImpl) {
		addBitOperatorImplementation(BinaryOperator.BITWISE_AND, 	operandClass, resultClass, andImpl);
		addBitOperatorImplementation(BinaryOperator.BITWISE_XOR, 	operandClass, resultClass, xorImpl);
		addBitOperatorImplementation(BinaryOperator.BITWISE_OR, 	operandClass, resultClass, orImpl);
	}

	private static void addLogicalOperator(BinaryOperator operator, BiFunction<Boolean, Boolean, Boolean> implementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (o1, o2) -> implementation.apply(ReflectionUtils.convertTo(o1, boolean.class, false), ReflectionUtils.convertTo(o2, boolean.class, false));
		LOGICAL_OPERATOR_INFO_BY_OPERATOR.put(operator, new BinaryOperatorInfo(boolean.class, wrappedImplementation));
	}

	static {
		addUnaryOperatorWithAssignmentImplementations(UnaryOperator.INCREMENT,	a -> (char) (a + 1), a -> (byte) (a + 1), a -> (short) (a + 1), a -> a + 1, a -> a + 1);
		addUnaryOperatorWithAssignmentImplementations(UnaryOperator.DECREMENT,	a -> (char) (a - 1), a -> (byte) (a - 1), a -> (short) (a - 1), a -> a - 1, a -> a - 1);

		addSignOperatorImplementations(UnaryOperator.PLUS, 	a -> +a, 	a -> +a, 	a -> +a, 	a -> +a, 	a -> +a, 	a -> +a);
		addSignOperatorImplementations(UnaryOperator.MINUS, a -> -a, 	a -> -a, 	a -> -a, 	a -> -a, 	a -> -a, 	a -> -a);

		addNumericOperatorImplementations(char.class, 	int.class,		(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b,	(a, b) -> a % b);
		addNumericOperatorImplementations(byte.class, 	int.class,		(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b,	(a, b) -> a % b);
		addNumericOperatorImplementations(short.class, 	int.class,		(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b,	(a, b) -> a % b);
		addNumericOperatorImplementations(int.class, 	int.class,		(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b,	(a, b) -> a % b);
		addNumericOperatorImplementations(long.class,	long.class,		(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b,	(a, b) -> a % b);
		addNumericOperatorImplementations(float.class, 	float.class,	(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b);
		addNumericOperatorImplementations(double.class,	double.class,	(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b);

		addShiftOperatorImplementations(BinaryOperator.LEFT_SHIFT,				(a, b) -> (int) (a << b),	(a, b) -> (int) (a << b),	(a, b) -> (int) (a << b),	(a, b) -> a << b,	(a, b) -> a << b);
		addShiftOperatorImplementations(BinaryOperator.RIGHT_SHIFT,				(a, b) -> (int) (a >> b),	(a, b) -> (int) (a >> b),	(a, b) -> (int) (a >> b),	(a, b) -> a >> b,	(a, b) -> a >> b);
		addShiftOperatorImplementations(BinaryOperator.UNSIGNED_RIGHT_SHIFT,	(a, b) -> (int) (a >>> b),	(a, b) -> (int) (a >>> b),	(a, b) -> (int) (a >>> b),	(a, b) -> a >>> b,	(a, b) -> a >>> b);

		addNumericComparisonImplementations(BinaryOperator.LESS_THAN, 					(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b);
		addNumericComparisonImplementations(BinaryOperator.LESS_THAN_OR_EQUAL_TO, 		(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b);
		addNumericComparisonImplementations(BinaryOperator.GREATER_THAN, 				(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a < b);
		addNumericComparisonImplementations(BinaryOperator.GREATER_THAN_OR_EQUAL_TO,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a <= b);

		// For "==" and "!=" we must explicitly unbox to avoid comparison of references
		addNumericComparisonImplementations(BinaryOperator.EQUAL_TO,		(a, b) -> a.charValue() == b.charValue(), (a, b) -> a.byteValue() == b.byteValue(), (a, b) -> a.shortValue() == b.shortValue(), (a, b) -> a.intValue() == b.intValue(), (a, b) -> a.longValue() == b.longValue(), (a, b) -> a.floatValue() == b.floatValue(), (a, b) -> a.doubleValue() == b.doubleValue());
		addNumericComparisonImplementations(BinaryOperator.NOT_EQUAL_TO,	(a, b) -> a.charValue() != b.charValue(), (a, b) -> a.byteValue() != b.byteValue(), (a, b) -> a.shortValue() != b.shortValue(), (a, b) -> a.intValue() != b.intValue(), (a, b) -> a.longValue() != b.longValue(), (a, b) -> a.floatValue() != b.floatValue(), (a, b) -> a.doubleValue() != b.doubleValue());

		addBitOperatorImplementations(char.class, 	int.class,		(a, b) -> a & b,	(a, b) -> a ^ b,	(a, b) -> a | b);
		addBitOperatorImplementations(byte.class, 	int.class,		(a, b) -> a & b,	(a, b) -> a ^ b,	(a, b) -> a | b);
		addBitOperatorImplementations(short.class, 	int.class,		(a, b) -> a & b,	(a, b) -> a ^ b,	(a, b) -> a | b);
		addBitOperatorImplementations(int.class, 	int.class,		(a, b) -> a & b,	(a, b) -> a ^ b,	(a, b) -> a | b);
		addBitOperatorImplementations(long.class,	long.class,		(a, b) -> a & b,	(a, b) -> a ^ b,	(a, b) -> a | b);

		addLogicalOperator(BinaryOperator.LOGICAL_AND,	(a, b) -> a && b);
		addLogicalOperator(BinaryOperator.LOGICAL_OR,	(a, b) -> a || b);
	}

	private final EvaluationMode evaluationMode;

	public OperatorResultProvider(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	/*
	 * Unary Operators
	 */
	public ObjectInfo getIncrementInfo(ObjectInfo objectInfo) throws OperatorException {
		return applyUnaryOperatorWithAssignment(objectInfo, UnaryOperator.INCREMENT);
	}

	public ObjectInfo getDecrementInfo(ObjectInfo objectInfo) throws OperatorException {
		return applyUnaryOperatorWithAssignment(objectInfo, UnaryOperator.DECREMENT);
	}

	public ObjectInfo getPlusInfo(ObjectInfo objectInfo) throws OperatorException {
		return applySignOperator(objectInfo, UnaryOperator.PLUS);
	}

	public ObjectInfo getMinusInfo(ObjectInfo objectInfo) throws OperatorException {
		return applySignOperator(objectInfo, UnaryOperator.MINUS);
	}

	public ObjectInfo getLogicalNotInfo(ObjectInfo objectInfo) throws OperatorException {
		Class<?> clazz = getClass(objectInfo);
		Class<?> primitiveClass = getPrimitiveClass(clazz);
		if (primitiveClass != boolean.class) {
			throw new OperatorException("Operator cannot be applied to '" + clazz + "'");
		}
		Object result = evaluationMode == EvaluationMode.NONE ? ObjectInfo.INDETERMINATE : !((boolean) objectInfo.getObject());
		TypeToken<?> resultType = TypeToken.of(boolean.class);
		return new ObjectInfo(result, resultType);
	}

	public ObjectInfo getBitwiseNotInfo(ObjectInfo objectInfo) throws OperatorException {
		Class<?> clazz = getClass(objectInfo);
		Class<?> primitiveClass = getPrimitiveClass(clazz);
		if (!isIntegral(primitiveClass)) {
			throw new OperatorException("Operator cannot be applied to '" + clazz + "'");
		}
		TypeToken<?> resultType = primitiveClass == long.class ? TypeToken.of(long.class) : TypeToken.of(int.class);
		Object result = ObjectInfo.INDETERMINATE;
		if (evaluationMode != EvaluationMode.NONE) {
			Object object = objectInfo.getObject();
			if (primitiveClass == long.class) {
				result = ~ReflectionUtils.convertTo(object, long.class, false).longValue();
			} else {
				result = ~ReflectionUtils.convertTo(object, int.class, false).intValue();
			}
		}
		return new ObjectInfo(result, resultType);
	}

	/*
	 * Binary Operators
	 */
	public ObjectInfo getMultiplicationInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericOperator(lhs, rhs, BinaryOperator.MULTIPLY);
	}

	public ObjectInfo getDivisionInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericOperator(lhs, rhs, BinaryOperator.DIVIDE);
	}

	public ObjectInfo getModuloInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericOperator(lhs, rhs, BinaryOperator.MODULO);
	}

	public ObjectInfo getAddOrConcatInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return getClass(lhs) == String.class || getClass(rhs) == String.class
				? concat(lhs, rhs)
				: applyNumericOperator(lhs, rhs, BinaryOperator.ADD_OR_CONCAT);
	}

	public ObjectInfo getSubtractionInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericOperator(lhs, rhs, BinaryOperator.SUBTRACT);
	}

	public ObjectInfo getLeftShiftInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyShiftOperator(lhs, rhs, BinaryOperator.LEFT_SHIFT);
	}

	public ObjectInfo getRightShiftInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyShiftOperator(lhs, rhs, BinaryOperator.RIGHT_SHIFT);
	}

	public ObjectInfo getUnsignedRightShiftInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyShiftOperator(lhs, rhs, BinaryOperator.UNSIGNED_RIGHT_SHIFT);
	}

	public ObjectInfo getLessThanInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericComparisonOperator(lhs, rhs, BinaryOperator.LESS_THAN);
	}

	public ObjectInfo getLessThanOrEqualToInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericComparisonOperator(lhs, rhs, BinaryOperator.LESS_THAN_OR_EQUAL_TO);
	}

	public ObjectInfo getGreaterThanInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericComparisonOperator(lhs, rhs, BinaryOperator.GREATER_THAN);
	}

	public ObjectInfo getGreaterThanOrEqualToInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericComparisonOperator(lhs, rhs, BinaryOperator.GREATER_THAN_OR_EQUAL_TO);
	}

	public ObjectInfo getEqualToInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		Class<?> lhsClass = getClass(lhs);
		Class<?> rhsClass = getClass(rhs);
		if (isPrimitive(lhsClass) || isPrimitive(rhsClass)) {
			return applyNumericComparisonOperator(lhs, rhs, BinaryOperator.EQUAL_TO);
		}
		Object result = evaluationMode == EvaluationMode.NONE ? ObjectInfo.INDETERMINATE : lhs.getObject() == rhs.getObject();
		TypeToken<?> resultType = TypeToken.of(boolean.class);
		return new ObjectInfo(result, resultType);
	}

	public ObjectInfo getNotEqualToInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		Class<?> lhsClass = getClass(lhs);
		Class<?> rhsClass = getClass(rhs);
		if (isPrimitive(lhsClass) || isPrimitive(rhsClass)) {
			return applyNumericComparisonOperator(lhs, rhs, BinaryOperator.NOT_EQUAL_TO);
		}
		Object result = evaluationMode == EvaluationMode.NONE ? ObjectInfo.INDETERMINATE : lhs.getObject() != rhs.getObject();
		TypeToken<?> resultType = TypeToken.of(boolean.class);
		return new ObjectInfo(result, resultType);
	}

	public ObjectInfo getBitwiseAndInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyBitOperator(lhs, rhs, BinaryOperator.BITWISE_AND);
	}

	public ObjectInfo getBitwiseXorInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyBitOperator(lhs, rhs, BinaryOperator.BITWISE_XOR);
	}

	public ObjectInfo getBitwiseOrInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyBitOperator(lhs, rhs, BinaryOperator.BITWISE_OR);
	}

	public ObjectInfo getLogicalAndInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyLogicalOperator(lhs, rhs, BinaryOperator.LOGICAL_AND);
	}

	public ObjectInfo getLogicalOrInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyLogicalOperator(lhs, rhs, BinaryOperator.LOGICAL_OR);
	}

	public ObjectInfo getAssignmentInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		ObjectInfo.ValueSetterIF lhsValueSetter = lhs.getValueSetter();
		if (lhsValueSetter == null) {
			throw new OperatorException("Cannot assign values to non-lvalues or final fields");
		}
		TypeToken<?> declaredLhsType = lhs.getDeclaredType();
		TypeToken<?> rhsType = getType(rhs);
		if (ParseUtils.rateTypeMatch(rhsType, declaredLhsType) == ParseUtils.TYPE_MATCH_NONE) {
			throw new OperatorException("Cannot assign value of type '" + rhsType + "' to left-hand side. Expected an instance of class '" + declaredLhsType + "'");
		}
		TypeToken<?> declaredResultType = declaredLhsType;
		Object resultObject = ObjectInfo.INDETERMINATE;
		if (evaluationMode != EvaluationMode.NONE) {
			try {
				resultObject = rhs.getObject();
				lhsValueSetter.setObject(resultObject);
			} catch (IllegalArgumentException e) {
				throw new OperatorException("Could assign value of type '" + rhsType + "' to left-hand side.");
			}
		}
		return new ObjectInfo(resultObject, declaredResultType);
	}

	/*
	 * Utility Methods
	 */
	private TypeToken<?> getType(ObjectInfo objectInfo) {
		return ObjectInfoProvider.getType(objectInfo, evaluationMode);
	}

	private Class<?> getClass(ObjectInfo objectInfo) {
		TypeToken<?> type = getType(objectInfo);
		return type == null ? null : type.getRawType();
	}

	private static boolean isIntegral(Class<?> primitiveClass) {
		return INTEGRAL_PRIMITIVE_CLASSES.contains(primitiveClass);
	}

	private static boolean isNumeric(Class<?> primitiveClass) {
		return NUMERIC_PRIMITIVE_CLASSES.contains(primitiveClass);
	}

	private static boolean isPrimitive(Class<?> clazz) {
		return clazz != null && clazz.isPrimitive();
	}

	private static Class<?> getPrimitiveClass(Class<?> clazz) throws OperatorException {
		if (clazz == null) {
			throw new OperatorException("null is not a primitive");
		}
		if (clazz.isPrimitive()) {
			return clazz;
		}
		Class<?> primitiveClass = ReflectionUtils.getPrimitiveClass(clazz);
		if (primitiveClass == null) {
			throw new OperatorException("Class '" + clazz + "' is neither a primitive nor a boxed class");
		}
		return primitiveClass;
	}

	/*
	 * Utility Methods for Unary Operators
	 */
	private Class<?> getNumericPrimitiveClass(ObjectInfo objectInfo) throws OperatorException {
		Class<?> clazz = getClass(objectInfo);
		Class<?> primitiveClass = getPrimitiveClass(clazz);
		if (isNumeric(primitiveClass)) {
			return primitiveClass;
		}
		throw new OperatorException("Expected numeric class, but found '" + clazz + "'");
	}

	private ObjectInfo applyUnaryOperatorInfo(ObjectInfo objectInfo, UnaryOperatorInfo operatorInfo) throws OperatorException {
		if (operatorInfo == null) {
			throw new OperatorException("Operator not defined on '" + getClass(objectInfo) + "'");
		}
		TypeToken<?> resultType = TypeToken.of(operatorInfo.getResultClass());
		Object result = ObjectInfo.INDETERMINATE;
		if (evaluationMode != EvaluationMode.NONE) {
			Function<Object, Object> operation = operatorInfo.getOperation();
			result = operation.apply(objectInfo.getObject());
		}
		return new ObjectInfo(result, resultType);
	}

	private ObjectInfo applyUnaryOperatorWithAssignment(ObjectInfo objectInfo, UnaryOperator operator) throws OperatorException {
		ObjectInfo.ValueSetterIF valueSetter = objectInfo.getValueSetter();
		if (valueSetter == null) {
			throw new OperatorException("Cannot assign values to non-lvalues or final fields");
		}
		Class<?> clazz = getClass(objectInfo);
		Class<?> primitiveClass = getPrimitiveClass(clazz);
		UnaryOperatorInfo operatorInfo = UNARY_OPERATOR_WITH_ASSIGNMENT_INFO_BY_OPERATOR_AND_TYPE.get(operator, primitiveClass);
		ObjectInfo operatorResult = applyUnaryOperatorInfo(objectInfo, operatorInfo);
		if (evaluationMode != EvaluationMode.NONE) {
			try {
				Object resultObject = operatorResult.getObject();
				valueSetter.setObject(resultObject);
			} catch (IllegalArgumentException e) {
				throw new OperatorException("Illegal argument exception when applying operator: " + e.getMessage());
			}
		}
		return operatorResult;
	}

	private ObjectInfo applySignOperator(ObjectInfo objectInfo, UnaryOperator signOperator) throws OperatorException {
		Class<?> numericPrimitiveClass = getNumericPrimitiveClass(objectInfo);
		UnaryOperatorInfo operatorInfo = SIGN_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.get(signOperator, numericPrimitiveClass);
		return applyUnaryOperatorInfo(objectInfo, operatorInfo);
	}

	/*
	 * Utility Methods for Binary Operators
	 */
	private static Class<?> getCommonPrimitiveClass(Class<?> class1, Class<?> class2) throws OperatorException {
		Class<?> primitiveClass1 = getPrimitiveClass(class1);
		Class<?> primitiveClass2 = getPrimitiveClass(class2);
		if (ReflectionUtils.isPrimitiveConvertibleTo(primitiveClass1, primitiveClass2, false)) {
			return primitiveClass2;
		} else if (ReflectionUtils.isPrimitiveConvertibleTo(primitiveClass2, primitiveClass1, false)) {
			return primitiveClass1;
		} else {
			throw new OperatorException("Operator cannot be applied to '" + class1 + "' and '" + class2 + "'");
		}
	}

	private Class<?> getCommonNumericPrimitiveClass(ObjectInfo objectInfo1, ObjectInfo objectInfo2) throws OperatorException {
		Class<?> class1 = getClass(objectInfo1);
		Class<?> class2 = getClass(objectInfo2);
		Class<?> commonPrimitiveClass = getCommonPrimitiveClass(class1, class2);
		if (isNumeric(commonPrimitiveClass)) {
			return commonPrimitiveClass;
		}
		throw new OperatorException("Expected numeric classes, but found '" + class1 + "' and '" + class2 + "'");
	}

	private static String getStringRepresentation(Object object) {
		if (object != null) {
			String s = object.toString();
			if (s != null) {
				return s;
			}
		}
		return "null";
	}

	private ObjectInfo applyBinaryOperatorInfo(ObjectInfo lhs, ObjectInfo rhs, BinaryOperatorInfo operatorInfo) throws OperatorException {
		if (operatorInfo == null) {
			throw new OperatorException("Operator not defined on '" + getClass(lhs) + "' and '" + getClass(rhs) + "'");
		}
		TypeToken<?> resultType = TypeToken.of(operatorInfo.getResultClass());
		Object result = ObjectInfo.INDETERMINATE;
		if (evaluationMode != EvaluationMode.NONE) {
			BiFunction<Object, Object, Object> operation = operatorInfo.getOperation();
			result = operation.apply(lhs.getObject(), rhs.getObject());
		}
		return new ObjectInfo(result, resultType);
	}

	private ObjectInfo applyNumericOperator(ObjectInfo lhs, ObjectInfo rhs, BinaryOperator numericOperator) throws OperatorException {
		Class<?> commonNumericPrimitiveClass = getCommonNumericPrimitiveClass(lhs, rhs);
		BinaryOperatorInfo operatorInfo = NUMERIC_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.get(numericOperator, commonNumericPrimitiveClass);
		return applyBinaryOperatorInfo(lhs, rhs, operatorInfo);
	}

	private ObjectInfo concat(ObjectInfo lhs, ObjectInfo rhs) {
		TypeToken<?> resultType = TypeToken.of(String.class);
		Object result = ObjectInfo.INDETERMINATE;
		if (evaluationMode != EvaluationMode.NONE) {
			String lhsAsString = getStringRepresentation(lhs.getObject());
			String rhsAsString = getStringRepresentation(rhs.getObject());
			result = lhsAsString + rhsAsString;
		}
		return new ObjectInfo(result, resultType);
	}

	private ObjectInfo applyShiftOperator(ObjectInfo lhs, ObjectInfo rhs, BinaryOperator shiftOperator) throws OperatorException {
		Class<?> lhsClass = getClass(lhs);
		Class<?> rhsClass = getClass(rhs);
		Class<?> primitiveLhsClass = getPrimitiveClass(lhsClass);
		Class<?> primitiveRhsClass = getPrimitiveClass(rhsClass);
		if (!isIntegral(primitiveLhsClass) || !isIntegral(primitiveRhsClass)) {
			throw new OperatorException("Operator cannot be applied to '" + lhsClass + "' and '" + rhsClass + "'");
		}
		BinaryOperatorInfo operatorInfo = SHIFT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.get(shiftOperator, primitiveLhsClass);
		return applyBinaryOperatorInfo(lhs, rhs, operatorInfo);
	}

	private ObjectInfo applyNumericComparisonOperator(ObjectInfo lhs, ObjectInfo rhs, BinaryOperator comparisonOperator) throws OperatorException {
		Class<?> commonNumericPrimitiveClass = getCommonNumericPrimitiveClass(lhs, rhs);
		BinaryOperatorInfo operatorInfo = NUMERIC_COMPARISON_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.get(comparisonOperator, commonNumericPrimitiveClass);
		return applyBinaryOperatorInfo(lhs, rhs, operatorInfo);
	}

	private ObjectInfo applyBitOperator(ObjectInfo lhs, ObjectInfo rhs, BinaryOperator bitOperator) throws OperatorException {
		Class<?> commonNumericPrimitiveClass = getCommonNumericPrimitiveClass(lhs, rhs);
		if (!isIntegral(commonNumericPrimitiveClass)) {
			throw new OperatorException("Operator can only be applied to integral types");
		}
		BinaryOperatorInfo operatorInfo = BIT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.get(bitOperator, commonNumericPrimitiveClass);
		return applyBinaryOperatorInfo(lhs, rhs, operatorInfo);
	}

	private ObjectInfo applyLogicalOperator(ObjectInfo lhs, ObjectInfo rhs, BinaryOperator logicalOperator) throws OperatorException {
		Class<?> lhsClass = getClass(lhs);
		Class<?> rhsClass = getClass(rhs);
		Class<?> primitiveLhsClass = getPrimitiveClass(lhsClass);
		Class<?> primitiveRhsClass = getPrimitiveClass(rhsClass);
		if (primitiveLhsClass != boolean.class || primitiveRhsClass != boolean.class) {
			throw new OperatorException("Operator cannot be applied to '" + lhsClass + "' and '" + rhsClass + "'");
		}
		BinaryOperatorInfo operatorInfo = LOGICAL_OPERATOR_INFO_BY_OPERATOR.get(logicalOperator);
		return applyBinaryOperatorInfo(lhs, rhs, operatorInfo);
	}

	private static class UnaryOperatorInfo
	{
		private final Class<?>					resultClass;
		private final Function<Object, Object>	operation;

		private UnaryOperatorInfo(Class<?> resultClass, Function<Object, Object> operation) {
			this.resultClass = resultClass;
			this.operation = operation;
		}

		Class<?> getResultClass() {
			return resultClass;
		}

		Function<Object, Object> getOperation() {
			return operation;
		}
	}

	private static class BinaryOperatorInfo
	{
		private final Class<?>								resultClass;
		private final BiFunction<Object, Object, Object>	operation;

		BinaryOperatorInfo(Class<?> resultClass, BiFunction<Object, Object, Object> operation) {
			this.resultClass = resultClass;
			this.operation = operation;
		}

		Class<?> getResultClass() {
			return resultClass;
		}

		BiFunction<Object, Object, Object> getOperation() {
			return operation;
		}
	}

	public static class OperatorException extends Exception
	{
		private OperatorException(String message) {
			super(message);
		}
	}
}
