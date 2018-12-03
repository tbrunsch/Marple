package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.EvaluationMode;
import com.AMS.jBEAM.javaParser.tokenizer.Operator;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class BinaryOperatorResultProvider
{
	private static final Set<Class<?>>	INTEGRAL_PRIMITIVE_CLASSES			= ImmutableSet.of(char.class, byte.class, short.class, int.class, long.class);
	private static final Set<Class<?>>	FLOATING_POINT_PRIMITIVE_CLASSES	= ImmutableSet.of(float.class, double.class);
	private static final Set<Class<?>>	NUMERIC_PRIMITIVE_CLASSES			= ImmutableSet.<Class<?>>builder().addAll(INTEGRAL_PRIMITIVE_CLASSES).addAll(FLOATING_POINT_PRIMITIVE_CLASSES).build();

	private static final Table<Operator, Class<?>, OperatorInfo> 	NUMERIC_OPERATOR_INFO_BY_OPERATOR_AND_TYPE 				= HashBasedTable.create();
	private static final Table<Operator, Class<?>, OperatorInfo>	SHIFT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE				= HashBasedTable.create();
	private static final Table<Operator, Class<?>, OperatorInfo>	NUMERIC_COMPARISON_OPERATOR_INFO_BY_OPERATOR_AND_TYPE	= HashBasedTable.create();
	private static final Table<Operator, Class<?>, OperatorInfo>	BIT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE					= HashBasedTable.create();
	private static final Map<Operator, OperatorInfo> 				LOGICAL_OPERATOR_INFO_BY_OPERATOR						= new HashMap<>();

	private static <S, T> void addNumericOperatorImplementation(Operator operator, Class<S> operandClass, Class<T> resultClass, BiFunction<S, S, T> implementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (o1, o2) -> implementation.apply(ReflectionUtils.convertTo(o1, operandClass, false), ReflectionUtils.convertTo(o2, operandClass, false));
		NUMERIC_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.put(operator, operandClass, new OperatorInfo(resultClass, wrappedImplementation));
	}

	private static <S, T> void addNumericOperatorImplementations(Class<S> operandClass, Class<T> resultClass, BiFunction<S, S, T> mulImpl, BiFunction<S, S, T> divImpl, BiFunction<S, S, T> addImpl, BiFunction<S, S, T> subImpl) {
		addNumericOperatorImplementation(Operator.MUL, 				operandClass, resultClass, mulImpl);
		addNumericOperatorImplementation(Operator.DIV, 				operandClass, resultClass, divImpl);
		addNumericOperatorImplementation(Operator.ADD_OR_CONCAT, 	operandClass, resultClass, addImpl);
		addNumericOperatorImplementation(Operator.SUB, 				operandClass, resultClass, subImpl);
	}

	private static <S, T> void addNumericOperatorImplementations(Class<S> operandClass, Class<T> resultClass, BiFunction<S, S, T> mulImpl, BiFunction<S, S, T> divImpl, BiFunction<S, S, T> addImpl, BiFunction<S, S, T> subImpl, BiFunction<S, S, T> modImpl) {
		addNumericOperatorImplementations(operandClass, resultClass, mulImpl, divImpl, addImpl, subImpl);
		addNumericOperatorImplementation(Operator.MOD,				operandClass, resultClass, modImpl);
	}

	private static <S> void addShiftOperatorImplementation(Operator operator, Class<S> lhsClass, BiFunction<S, Long, S> implementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (o1, o2) -> implementation.apply(ReflectionUtils.convertTo(o1, lhsClass, false), ReflectionUtils.convertTo(o2, long.class, false));
		Class<?> resultClass = lhsClass;
		SHIFT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.put(operator, lhsClass, new OperatorInfo(resultClass, wrappedImplementation));
	}

	private static void addShiftOperatorImplementations(Operator operator, BiFunction<Integer, Long, Integer> intImpl, BiFunction<Long, Long, Long> longImpl) {
		addShiftOperatorImplementation(operator, int.class,		intImpl);
		addShiftOperatorImplementation(operator, long.class,	longImpl);
	}

	private static <S> void addNumericComparisonImplementation(Operator operator, Class<S> operandClass, BiFunction<S, S, Boolean> implementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (o1, o2) -> implementation.apply(ReflectionUtils.convertTo(o1, operandClass, false), ReflectionUtils.convertTo(o2, operandClass, false));
		NUMERIC_COMPARISON_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.put(operator, operandClass, new OperatorInfo(boolean.class, wrappedImplementation));
	}

	private static void addNumericComparisonImplementations(Operator operator, BiFunction<Character, Character, Boolean> charImpl, BiFunction<Byte, Byte, Boolean> byteImpl, BiFunction<Short, Short, Boolean> shortImpl, BiFunction<Integer, Integer, Boolean> intImpl, BiFunction<Long, Long, Boolean> longImpl, BiFunction<Float, Float, Boolean> floatImpl, BiFunction<Double, Double, Boolean> doubleImpl) {
		addNumericComparisonImplementation(operator,	char.class,		charImpl);
		addNumericComparisonImplementation(operator,	byte.class,		byteImpl);
		addNumericComparisonImplementation(operator,	short.class,	shortImpl);
		addNumericComparisonImplementation(operator,	int.class,		intImpl);
		addNumericComparisonImplementation(operator,	long.class,		longImpl);
		addNumericComparisonImplementation(operator,	float.class,	floatImpl);
		addNumericComparisonImplementation(operator,	double.class,	doubleImpl);
	}

	private static <S, T> void addBitOperatorImplementation(Operator operator, Class<S> operandClass, Class<T> resultClass, BiFunction<S, S, T> implementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (o1, o2) -> implementation.apply(ReflectionUtils.convertTo(o1, operandClass, false), ReflectionUtils.convertTo(o2, operandClass, false));
		BIT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.put(operator, operandClass, new OperatorInfo(resultClass, wrappedImplementation));
	}

	private static <S, T> void addBitOperatorImplementations(Class<S> operandClass, Class<T> resultClass, BiFunction<S, S, T> andImpl, BiFunction<S, S, T> xorImpl, BiFunction<S, S, T> orImpl) {
		addBitOperatorImplementation(Operator.BITWISE_AND, 	operandClass, resultClass, andImpl);
		addBitOperatorImplementation(Operator.BITWISE_XOR, 	operandClass, resultClass, xorImpl);
		addBitOperatorImplementation(Operator.BITWISE_OR, 	operandClass, resultClass, orImpl);
	}

	private static void addLogicalOperator(Operator operator, BiFunction<Boolean, Boolean, Boolean> implementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (o1, o2) -> implementation.apply(ReflectionUtils.convertTo(o1, boolean.class, false), ReflectionUtils.convertTo(o2, boolean.class, false));
		LOGICAL_OPERATOR_INFO_BY_OPERATOR.put(operator, new OperatorInfo(boolean.class, wrappedImplementation));
	}

	static {
		addNumericOperatorImplementations(char.class, 	int.class,		(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b,	(a, b) -> a % b);
		addNumericOperatorImplementations(byte.class, 	int.class,		(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b,	(a, b) -> a % b);
		addNumericOperatorImplementations(short.class, 	int.class,		(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b,	(a, b) -> a % b);
		addNumericOperatorImplementations(int.class, 	int.class,		(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b,	(a, b) -> a % b);
		addNumericOperatorImplementations(long.class,	long.class,		(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b,	(a, b) -> a % b);
		addNumericOperatorImplementations(float.class, 	float.class,	(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b);
		addNumericOperatorImplementations(double.class,	double.class,	(a, b) -> a * b,	(a, b) -> a / b,	(a, b) -> a + b,	(a, b) -> a - b);

		addShiftOperatorImplementations(Operator.LEFT_SHIFT,			(a, b) -> a << b,	(a, b) -> a << b);
		addShiftOperatorImplementations(Operator.RIGHT_SHIFT,			(a, b) -> a >> b,	(a, b) -> a >> b);
		addShiftOperatorImplementations(Operator.UNSIGNED_RIGHT_SHIFT,	(a, b) -> a >>> b,	(a, b) -> a >>> b);

		addNumericComparisonImplementations(Operator.LESS_THAN, 				(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b);
		addNumericComparisonImplementations(Operator.LESS_THAN_OR_EQUAL_TO, 	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b);
		addNumericComparisonImplementations(Operator.GREATER_THAN, 				(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a < b);
		addNumericComparisonImplementations(Operator.GREATER_THAN_OR_EQUAL_TO,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a <= b);

		// For "==" and "!=" we must explicitly unbox to avoid comparison of references
		addNumericComparisonImplementations(Operator.EQUAL_TO,		(a, b) -> a.charValue() == b.charValue(), (a, b) -> a.byteValue() == b.byteValue(), (a, b) -> a.shortValue() == b.shortValue(), (a, b) -> a.intValue() == b.intValue(), (a, b) -> a.longValue() == b.longValue(), (a, b) -> a.floatValue() == b.floatValue(), (a, b) -> a.doubleValue() == b.doubleValue());
		addNumericComparisonImplementations(Operator.NOT_EQUAL_TO,	(a, b) -> a.charValue() != b.charValue(), (a, b) -> a.byteValue() != b.byteValue(), (a, b) -> a.shortValue() != b.shortValue(), (a, b) -> a.intValue() != b.intValue(), (a, b) -> a.longValue() != b.longValue(), (a, b) -> a.floatValue() != b.floatValue(), (a, b) -> a.doubleValue() != b.doubleValue());

		addBitOperatorImplementations(char.class, 	int.class,		(a, b) -> a & b,	(a, b) -> a ^ b,	(a, b) -> a | b);
		addBitOperatorImplementations(byte.class, 	int.class,		(a, b) -> a & b,	(a, b) -> a ^ b,	(a, b) -> a | b);
		addBitOperatorImplementations(short.class, 	int.class,		(a, b) -> a & b,	(a, b) -> a ^ b,	(a, b) -> a | b);
		addBitOperatorImplementations(int.class, 	int.class,		(a, b) -> a & b,	(a, b) -> a ^ b,	(a, b) -> a | b);
		addBitOperatorImplementations(long.class,	long.class,		(a, b) -> a & b,	(a, b) -> a ^ b,	(a, b) -> a | b);

		addLogicalOperator(Operator.LOGICAL_AND,	(a, b) -> a && b);
		addLogicalOperator(Operator.LOGICAL_OR,		(a, b) -> a || b);
	}

	private final EvaluationMode evaluationMode;

	public BinaryOperatorResultProvider(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	public ObjectInfo getMultiplicationInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericOperator(lhs, rhs, Operator.MUL);
	}

	public ObjectInfo getDivisionInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericOperator(lhs, rhs, Operator.DIV);
	}

	public ObjectInfo getModuloInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericOperator(lhs, rhs, Operator.MOD);
	}

	public ObjectInfo getAddOrConcatInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return getClass(lhs) == String.class || getClass(rhs) == String.class
				? concat(lhs, rhs)
				: applyNumericOperator(lhs, rhs, Operator.ADD_OR_CONCAT);
	}

	public ObjectInfo getSubtractionInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericOperator(lhs, rhs, Operator.SUB);
	}

	public ObjectInfo getLeftShiftInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyShiftOperator(lhs, rhs, Operator.LEFT_SHIFT);
	}

	public ObjectInfo getRightShiftInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyShiftOperator(lhs, rhs, Operator.RIGHT_SHIFT);
	}

	public ObjectInfo getUnsignedRightShiftInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyShiftOperator(lhs, rhs, Operator.UNSIGNED_RIGHT_SHIFT);
	}

	public ObjectInfo getLessThanInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericComparisonOperator(lhs, rhs, Operator.LESS_THAN);
	}

	public ObjectInfo getLessThanOrEqualToInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericComparisonOperator(lhs, rhs, Operator.LESS_THAN_OR_EQUAL_TO);
	}

	public ObjectInfo getGreaterThanInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericComparisonOperator(lhs, rhs, Operator.GREATER_THAN);
	}

	public ObjectInfo getGreaterThanOrEqualToInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyNumericComparisonOperator(lhs, rhs, Operator.GREATER_THAN_OR_EQUAL_TO);
	}

	public ObjectInfo getEqualToInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		Class<?> lhsClass = getClass(lhs);
		Class<?> rhsClass = getClass(rhs);
		return lhsClass.isPrimitive() || rhsClass.isPrimitive()
				? applyNumericComparisonOperator(lhs, rhs, Operator.EQUAL_TO)
				: new ObjectInfo(lhs.getObject() == rhs.getObject(), boolean.class);
	}

	public ObjectInfo getNotEqualToInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		Class<?> lhsClass = getClass(lhs);
		Class<?> rhsClass = getClass(rhs);
		return lhsClass.isPrimitive() || rhsClass.isPrimitive()
				? applyNumericComparisonOperator(lhs, rhs, Operator.NOT_EQUAL_TO)
				: new ObjectInfo(lhs.getObject() != rhs.getObject(), boolean.class);
	}

	public ObjectInfo getBitwiseAndInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyBitOperator(lhs, rhs, Operator.BITWISE_AND);
	}

	public ObjectInfo getBitwiseXorInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyBitOperator(lhs, rhs, Operator.BITWISE_XOR);
	}

	public ObjectInfo getBitwiseOrInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyBitOperator(lhs, rhs, Operator.BITWISE_OR);
	}

	public ObjectInfo getLogicalAndInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyLogicalOperator(lhs, rhs, Operator.LOGICAL_AND);
	}

	public ObjectInfo getLogicalOrInfo(ObjectInfo lhs, ObjectInfo rhs) throws OperatorException {
		return applyLogicalOperator(lhs, rhs, Operator.LOGICAL_OR);
	}

	public ObjectInfo getAssignmentInfo(ObjectInfo lhs, ObjectInfo rhs) {
		// TODO
		return null;
	}

	private Class<?> getClass(ObjectInfo objectInfo) {
		return ObjectInfoProvider.getClass(objectInfo, evaluationMode);
	}

	private ObjectInfo applyOperatorInfo(ObjectInfo lhs, ObjectInfo rhs, OperatorInfo operatorInfo) throws OperatorException {
		if (operatorInfo == null) {
			throw new OperatorException("Operator not defined on '" + getClass(lhs) + "' and '" + getClass(rhs) + "'");
		}
		Class<?> resultClass = operatorInfo.getResultClass();
		Object result = null;
		if (evaluationMode != EvaluationMode.NONE) {
			BiFunction<Object, Object, Object> operation = operatorInfo.getOperation();
			result = operation.apply(lhs.getObject(), rhs.getObject());
		}
		return new ObjectInfo(result, resultClass);
	}

	private ObjectInfo applyNumericOperator(ObjectInfo lhs, ObjectInfo rhs, Operator numericOperator) throws OperatorException {
		Class<?> commonNumericPrimitiveClass = getCommonNumericPrimitiveClass(lhs, rhs);
		OperatorInfo operatorInfo = NUMERIC_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.get(numericOperator, commonNumericPrimitiveClass);
		return applyOperatorInfo(lhs, rhs, operatorInfo);
	}

	private ObjectInfo concat(ObjectInfo lhs, ObjectInfo rhs) {
		Class<?> resultClass = String.class;
		String result = null;
		if (evaluationMode != EvaluationMode.NONE) {
			String lhsAsString = getStringRepresentation(lhs.getObject());
			String rhsAsString = getStringRepresentation(rhs.getObject());
			result = lhsAsString + rhsAsString;
		}
		return new ObjectInfo(result, resultClass);
	}

	private ObjectInfo applyShiftOperator(ObjectInfo lhs, ObjectInfo rhs, Operator shiftOperator) throws OperatorException {
		Class<?> lhsClass = getClass(lhs);
		Class<?> rhsClass = getClass(rhs);
		Class<?> primitiveLhsClass = getPrimitiveClass(lhsClass);
		Class<?> primitiveRhsClass = getPrimitiveClass(rhsClass);
		if (!isIntegral(primitiveLhsClass) || !isIntegral(primitiveRhsClass)) {
			throw new OperatorException("Operator cannot be applied to '" + lhsClass + "' and '" + rhsClass + "'");
		}
		lhsClass = primitiveLhsClass == long.class ? long.class : int.class;
		OperatorInfo operatorInfo = SHIFT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.get(shiftOperator, lhsClass);
		return applyOperatorInfo(lhs, rhs, operatorInfo);
	}

	private ObjectInfo applyNumericComparisonOperator(ObjectInfo lhs, ObjectInfo rhs, Operator comparisonOperator) throws OperatorException {
		Class<?> commonNumericPrimitiveClass = getCommonNumericPrimitiveClass(lhs, rhs);
		OperatorInfo operatorInfo = NUMERIC_COMPARISON_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.get(comparisonOperator, commonNumericPrimitiveClass);
		return applyOperatorInfo(lhs, rhs, operatorInfo);
	}

	private ObjectInfo applyBitOperator(ObjectInfo lhs, ObjectInfo rhs, Operator bitOperator) throws OperatorException {
		Class<?> commonNumericPrimitiveClass = getCommonNumericPrimitiveClass(lhs, rhs);
		if (!isIntegral(commonNumericPrimitiveClass)) {
			throw new OperatorException("Operator can only be applied to integral types");
		}
		OperatorInfo operatorInfo = BIT_OPERATOR_INFO_BY_OPERATOR_AND_TYPE.get(bitOperator, commonNumericPrimitiveClass);
		return applyOperatorInfo(lhs, rhs, operatorInfo);
	}

	private ObjectInfo applyLogicalOperator(ObjectInfo lhs, ObjectInfo rhs, Operator logicalOperator) throws OperatorException {
		Class<?> lhsClass = getClass(lhs);
		Class<?> rhsClass = getClass(rhs);
		Class<?> primitiveLhsClass = getPrimitiveClass(lhsClass);
		Class<?> primitiveRhsClass = getPrimitiveClass(rhsClass);
		if (primitiveLhsClass != boolean.class || primitiveRhsClass != boolean.class) {
			throw new OperatorException("Operator cannot be applied to '" + lhsClass + "' and '" + rhsClass + "'");
		}
		OperatorInfo operatorInfo = LOGICAL_OPERATOR_INFO_BY_OPERATOR.get(logicalOperator);
		return applyOperatorInfo(lhs, rhs, operatorInfo);
	}

	private static Class<?> getPrimitiveClass(Class<?> clazz) throws OperatorException {
		if (clazz.isPrimitive()) {
			return clazz;
		}
		Class<?> primitiveClass = ReflectionUtils.getPrimitiveClass(clazz);
		if (primitiveClass == null) {
			throw new OperatorException("Class '" + clazz + "' is neither a primitive nor a boxed class");
		}
		return primitiveClass;
	}

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

	private static boolean isIntegral(Class<?> primitiveClass) {
		return INTEGRAL_PRIMITIVE_CLASSES.contains(primitiveClass);
	}

	private static boolean isNumeric(Class<?> primitiveClass) {
		return NUMERIC_PRIMITIVE_CLASSES.contains(primitiveClass);
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

	private static class OperatorInfo
	{
		private final Class<?>								resultClass;
		private final BiFunction<Object, Object, Object>	operation;

		OperatorInfo(Class<?> resultClass, BiFunction<Object, Object, Object> operation) {
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
