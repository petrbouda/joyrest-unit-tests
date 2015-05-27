package org.joyrest.test.unit.rule;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import java.util.Arrays;

import org.easymock.EasyMockSupport;
import org.easymock.TestSubject;
import org.joyrest.routing.ControllerConfiguration;
import org.joyrest.test.unit.JoyrestUnitTest;
import org.joyrest.test.unit.annotation.TestedController;
import org.joyrest.test.unit.easymock.JoyrestInjector;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class JoyrestRule implements TestRule {

	private final JoyrestUnitTest test;

	private String globalPath = "";

	private ControllerConfiguration controller = null;

	private boolean annonationInitialized = false;

	public JoyrestRule(JoyrestUnitTest test) {
		this.test = test;
	}

	@Override
	public Statement apply(Statement original, Description description) {
		return new JoyrestStatement(original);
	}

	private class JoyrestStatement extends Statement {

		private final Statement originalStatement;

		public JoyrestStatement(Statement originalStatement) {
			this.originalStatement = originalStatement;
		}

		@Override
		public void evaluate() throws Throwable {
			initControllerFromAnnotation();

			if (!annonationInitialized)
				if (containsTestSubject(test.getClass()))
					EasyMockSupport.injectMocks(test);

			originalStatement.evaluate();
		}

		private void initControllerFromAnnotation() {
			TestedController annotation = test.getClass().getAnnotation(TestedController.class);

			if (nonNull(annotation)) {
				globalPath = annotation.globalPath();
				controller = getController(annotation.value());

				JoyrestInjector.injectMocks(test, controller);
				annonationInitialized = true;
			}
		}

		private boolean containsTestSubject(Class<?> clazz) {
			return Arrays.stream(clazz.getDeclaredFields())
				.anyMatch(field -> nonNull(field.getAnnotation(TestSubject.class)));
		}

		private ControllerConfiguration getController(Class<? extends ControllerConfiguration> clazz) {
			try {
				return clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(
						format("Error occurred during initialization class '%s'.", clazz.getCanonicalName()), e);
			}
		}
	}

	public void setGlobalPath(String globalPath) {
		this.globalPath = globalPath;
	}

	public void setController(ControllerConfiguration controller) {
		this.controller = controller;
	}

	public boolean isAnnonationInitialized() {
		return annonationInitialized;
	}

	public String getGlobalPath() {
		return globalPath;
	}

	public ControllerConfiguration getController() {
		return controller;
	}

}
