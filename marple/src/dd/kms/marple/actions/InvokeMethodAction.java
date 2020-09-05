package dd.kms.marple.actions;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.util.function.Consumer;

public class InvokeMethodAction implements InspectionAction
{
	private final ObjectInfo			objectInfo;
	private final ExecutableInfo		methodInfo;
	private final Consumer<ObjectInfo>	returnValueConsumer;
	private final Consumer<Exception>	exceptionConsumer;

	public InvokeMethodAction(ObjectInfo objectInfo, ExecutableInfo methodInfo, Consumer<ObjectInfo> returnValueConsumer, Consumer<Exception> exceptionConsumer) {
		this.objectInfo = objectInfo;
		this.methodInfo = methodInfo;
		this.returnValueConsumer = returnValueConsumer;
		this.exceptionConsumer = exceptionConsumer;
	}

	@Override
	public boolean isDefaultAction() {
		return true;
	}

	@Override
	public String getName() {
		return "Invoke";
	}

	@Override
	public String getDescription() {
		return "Invoke method '" + methodInfo.getName() + "' and inspect the result in the inspection dialog";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		try {
			ObjectInfo returnValueInfo = ReflectionUtils.OBJECT_INFO_PROVIDER.getExecutableReturnInfo(objectInfo.getObject(), methodInfo, ImmutableList.of());
			if (!methodInfo.getReturnType().getRawType().equals(Void.TYPE)) {
				returnValueConsumer.accept(returnValueInfo);
			}
		} catch (Exception e) {
			exceptionConsumer.accept(e);
		}
	}
}
