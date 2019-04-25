package dd.kms.marple;

import dd.kms.marple.settings.InspectionSettings;
import dd.kms.marple.settings.SecuritySettings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @param <C>	GUI component class
 * @param <K>	KeyStroke class
 * @param <P>	Point class
 */
public abstract class AbstractObjectInspectionFramework<C, K, P>
{
	private final Map<Object, InspectionContextImpl<C, K, P>> managedInspectionContexts	= new HashMap<>();

	private C	lastComponentUnderMouse;
	private P	lastMousePositionOnComponent;
	private P	lastMousePositionOnScreen;

	protected abstract void registerListeners();
	protected abstract void unregisterListeners();
	protected abstract P getMousePositionOnScreen();
	protected abstract boolean keyMatches(K actualKey, K expectedKey);

	public final void registerSettings(Object identifier, InspectionSettings<C, K, P> inspectionSettings) {
		if (managedInspectionContexts.isEmpty()) {
			registerListeners();
		}
		managedInspectionContexts.put(identifier, new InspectionContextImpl<>(inspectionSettings));
	}

	public final void unregisterSettings(Object identifier) {
		if (managedInspectionContexts.isEmpty()) {
			return;
		}
		managedInspectionContexts.remove(identifier);
		if (managedInspectionContexts.isEmpty()) {
			unregisterListeners();
		}
	}

	private boolean keyMatches(K key, InspectionSettings<C, K, P> settings) {
		return keyMatches(key, settings.getInspectionKey())
			|| keyMatches(key, settings.getEvaluationKey());
	}

	/*
	 * Event call backs
	 */
	protected void onKeyPressed(K key) {
		P mousePosOnScreen = getMousePositionOnScreen();
		if (!mousePosOnScreen.equals(lastMousePositionOnScreen)) {
			return;
		}

		InspectionContextImpl<C, K, P> context = managedInspectionContexts.values().stream()
			.filter(c -> keyMatches(key, c.getSettings()))
			.filter(c -> lastComponentUnderMouse == null || c.getSettings().getResponsibilityPredicate().test(lastComponentUnderMouse))
			.findFirst()
			.orElse(null);

		if (context == null) {
			return;
		}

		InspectionSettings<C, K, P> settings = context.getSettings();
		if (keyMatches(key, settings.getInspectionKey())) {
			if (userHasPermission(settings)) {
				context.performInspection(lastComponentUnderMouse, lastMousePositionOnComponent);
			}
		} else {
			assert keyMatches(key, settings.getEvaluationKey());
			if (userHasPermission(settings)) {
				context.performEvaluation();
			}
		}
	}

	private boolean userHasPermission(InspectionSettings<C, K, P> settings) {
		Optional<SecuritySettings> securitySettingsOptional = settings.getSecuritySettings();
		if (!securitySettingsOptional.isPresent()) {
			return true;
		}
		SecuritySettings securitySettings = securitySettingsOptional.get();
		try {
			String passwordHash = securitySettings.hashPassword(securitySettings.queryPassword());
			return Objects.equals(passwordHash, securitySettings.getPasswordHash());
		} catch (Exception e) {
			return false;
		}
	}

	protected void onMouseOverComponentAction(C component, P mousePosOnComponent) {
		lastComponentUnderMouse = component;
		lastMousePositionOnComponent = mousePosOnComponent;
		lastMousePositionOnScreen = getMousePositionOnScreen();
	}
}
