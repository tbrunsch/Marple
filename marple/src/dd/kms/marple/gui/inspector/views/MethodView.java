package dd.kms.marple.gui.inspector.views;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.gui.ObjectView;
import dd.kms.marple.gui.table.*;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.MethodScanner;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodView extends JPanel implements ObjectView
{
	private static final String	NAME	= "Methods";

	private final ListBasedTable<Method>	table;

	private final Object					object;
	private final InspectionContext			inspectionContext;

	public MethodView(Object object, InspectionContext inspectionContext) {
		super(new GridBagLayout());
		this.inspectionContext = inspectionContext;
		this.object = object;

		setName(NAME);

		List<Method> methods = new MethodScanner().getMethods(object.getClass());
		List<ColumnDescription<Method>> columnDescriptions = createColumnDescriptions();

		table = new ListBasedTable<>(methods, columnDescriptions);
		JTable internalTable = table.getInternalTable();
		internalTable.getColumnModel().getColumn(1).setCellRenderer(new ActionProviderRenderer());
		internalTable.setDefaultRenderer(AccessModifier.class, new AccessModifierRenderer());

		add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}

	@Override
	public String getViewName() {
		return NAME;
	}

	@Override
	public Component getViewComponent() {
		return this;
	}

	@Override
	public Object getViewSettings() {
		/* currently there are no settings */
		return null;
	}

	@Override
	public void applyViewSettings(Object settings) {
		/* currently there are no settings */
	}

	private List<ColumnDescription<Method>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<Method>("Return Type",	String.class, 			method -> method.getReturnType().getSimpleName()	).valueFilter(TableValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Method>("Name",		Object.class, 			method -> getMethodActionProvider(method)			).valueFilter(TableValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Method>("Arguments",	String.class, 			method -> getArgumentsAsString(method)				).valueFilter(TableValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Method>("Class",		String.class, 			method -> method.getDeclaringClass().getSimpleName()).valueFilter(TableValueFilters.createSelectionFilter(inspectionContext)).build(),
			new ColumnDescriptionBuilder<Method>("Modifier",	AccessModifier.class,	method -> getAccessModifier(method)					).valueFilter(TableValueFilters.createMinimumAccessLevelFilter()).build()
		);
	}

	private ActionProvider getMethodActionProvider(Method method) {
		String methodName = method.getName();
		String argumentList = getArgumentsAsString(method);
		Class<?>[] parameterTypes = method.getParameterTypes();
		ImmutableList.Builder<InspectionAction> actionsBuilder = ImmutableList.builder();
		if (parameterTypes.length == 0) {
			actionsBuilder.add(createInvokeMethodAction(method));
		}
		actionsBuilder.add(inspectionContext.createEvaluateExpressionAction(methodName + "(" + argumentList + ")", object));
		return ActionProvider.of(methodName, actionsBuilder.build());
	}

	private static String getArgumentsAsString(Method method) {
		return Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "));
	}

	private static AccessModifier getAccessModifier(Method method) {
		int modifiers = method.getModifiers();
		return AccessModifier.getValue(modifiers);
	}

	private InspectionAction createInvokeMethodAction(Method method) {
		return inspectionContext.createInvokeMethodAction(object, method, this::handleMethodReturnValue, this::displayMethodInvokationException);
	}

	private void handleMethodReturnValue(Object returnValue) {
		if (returnValue == null) {
			JOptionPane.showMessageDialog(null, "null");
			return;
		}
		InspectionAction inspectObjectAction = inspectionContext.createInspectObjectAction(returnValue);
		inspectObjectAction.perform();
	}

	private void displayMethodInvokationException(Exception e) {
		// TODO: Better handling
		JOptionPane.showMessageDialog(null, "Caught the following exception:\n\n" + e.getMessage());
	}
}
