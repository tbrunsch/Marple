package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class InvokeMethodAction implements InspectionAction
{
	private final Object				object;
	private final Method				method;
	private final Consumer<Object>		returnValueConsumer;
	private final Consumer<Exception>	exceptionConsumer;

	public InvokeMethodAction(Object object, Method method, Consumer<Object> returnValueConsumer, Consumer<Exception> exceptionConsumer) {
		this.object = object;
		this.method = method;
		this.returnValueConsumer = returnValueConsumer;
		this.exceptionConsumer = exceptionConsumer;
	}

	@Override
	public String getName() {
		return "Invoke";
	}

	@Override
	public String getDescription() {
		return "Invoke method '" + method.getName() + "' and inspect the result in the inspection dialog";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		try {
			method.setAccessible(true);
			Object returnValue = method.invoke(object);
			if (!method.getReturnType().equals(Void.TYPE)) {
				returnValueConsumer.accept(returnValue);
			}
		} catch (Exception e) {
			exceptionConsumer.accept(e);
		}
	}
}
