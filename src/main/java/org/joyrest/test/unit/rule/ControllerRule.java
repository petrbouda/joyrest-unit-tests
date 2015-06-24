/*
 * Copyright 2015 Petr Bouda
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyrest.test.unit.rule;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import java.util.Arrays;

import org.easymock.EasyMockSupport;
import org.easymock.TestSubject;
import org.joyrest.routing.ControllerConfiguration;
import org.joyrest.test.unit.ControllerUnitTest;
import org.joyrest.test.unit.annotation.TestedController;
import org.joyrest.test.unit.easymock.Injector;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ControllerRule implements TestRule {

	private final ControllerUnitTest test;

	private String controllerPath = "";

	private ControllerConfiguration controller = null;

	private boolean annotationInitialized = false;

	public ControllerRule(ControllerUnitTest test) {
		this.test = test;
	}

	@Override
	public Statement apply(Statement original, Description description) {
		return new ControllerStatement(original);
	}

	private class ControllerStatement extends Statement {

		private final Statement originalStatement;

		public ControllerStatement(Statement originalStatement) {
			this.originalStatement = originalStatement;
		}

		@Override
		public void evaluate() throws Throwable {
			initControllerFromAnnotation();

			if (!annotationInitialized)
				if (containsTestSubject(test.getClass()))
					EasyMockSupport.injectMocks(test);

			originalStatement.evaluate();
		}

		private void initControllerFromAnnotation() {
			TestedController annotation = test.getClass().getAnnotation(TestedController.class);

			if (nonNull(annotation)) {
				controllerPath = annotation.controllerPath();
				controller = getController(annotation.value());

				Injector.injectMocks(test, controller);
				annotationInitialized = true;
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

	public void setControllerPath(String controllerPath) {
		this.controllerPath = controllerPath;
	}

	public void setController(ControllerConfiguration controller) {
		this.controller = controller;
	}

	public boolean isAnnotationInitialized() {
		return annotationInitialized;
	}

	public String getControllerPath() {
		return controllerPath;
	}

	public ControllerConfiguration getController() {
		return controller;
	}

}
