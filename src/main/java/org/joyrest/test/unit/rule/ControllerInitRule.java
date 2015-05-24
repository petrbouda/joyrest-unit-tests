package org.joyrest.test.unit.rule;

import static java.lang.String.format;
import static java.util.Objects.isNull;

import java.util.Set;

import org.joyrest.routing.ControllerConfiguration;
import org.joyrest.routing.InternalRoute;
import org.joyrest.test.unit.JoyrestUnitTest;
import org.joyrest.test.unit.annotation.TestedController;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ControllerInitRule implements TestRule {

	private Set<InternalRoute> routes = null;

	private String globalPath = "";

	private ControllerConfiguration controller = null;

	@Override
	public Statement apply(Statement statement, Description description) {
		JoyrestUnitTest test = initializeTestClass(description.getClassName());
		test.configure();

		if (isNull(controller)) {
			controller = getControllerFromAnnotation(description);
			
			if (isNull(controller))
				throw new RuntimeException(format("There is no defined any tested controller. Use '%s' annotation " +
								"on the given test class or setter in 'configure' method.",
						TestedController.class.getCanonicalName()));
		}
		
		controller.initialize();
		this.routes = controller.getRoutes();

		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				statement.evaluate();
			}
		};
	}

	private ControllerConfiguration getControllerFromAnnotation(Description description) {
		TestedController tested = description.getAnnotation(TestedController.class);

		if (isNull(tested))
			throw new RuntimeException(
					format("There is no defined any tested controller. Use '%s' annotation on the given test class.",
							TestedController.class.getCanonicalName()));

		Class<?> clazz = tested.value();

		if (isNull(clazz))
			throw new RuntimeException("There is no defined any tested controller. Tested controller cannot be null.");

		if (!ControllerConfiguration.class.isAssignableFrom(clazz))
			throw new RuntimeException(
					format("Tested controller class is not an instance of the interface '%s'.",
							ControllerConfiguration.class.getCanonicalName()));

		return initializeConfiguration(clazz);
	}

	public Set<InternalRoute> getRoutes() {
		return routes;
	}


	public void setGlobalPath(String globalPath) {
		this.globalPath = globalPath;
	}

	public void setController(ControllerConfiguration controller) {
		this.controller = controller;
	}

	public String getGlobalPath() {
		return globalPath;
	}

	public ControllerConfiguration getController() {
		return controller;
	}

	private ControllerConfiguration initializeConfiguration(Class<?> clazz) {
		try {
			return (ControllerConfiguration) clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(
					format("Error occurred during initialization class '%s'.",
							clazz.getCanonicalName()), e);
		}
	}

	private JoyrestUnitTest initializeTestClass(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			return (JoyrestUnitTest) clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(
					format("Error occurred during initialization class '%s'.",
							className), e);
		}
	}
}
