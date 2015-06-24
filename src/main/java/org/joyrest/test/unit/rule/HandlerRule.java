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
import org.joyrest.exception.configuration.ExceptionConfiguration;
import org.joyrest.test.unit.HandlerUnitTest;
import org.joyrest.test.unit.annotation.TestedExceptionHandler;
import org.joyrest.test.unit.easymock.Injector;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class HandlerRule implements TestRule {

	private final HandlerUnitTest test;

	private ExceptionConfiguration configuration = null;

	private boolean annotationInitialized = false;

	public HandlerRule(HandlerUnitTest test) {
		this.test = test;
	}

	@Override
	public Statement apply(Statement original, Description description) {
		return new HandlerStatement(original);
	}

	private class HandlerStatement extends Statement {

		private final Statement originalStatement;

		public HandlerStatement(Statement originalStatement) {
			this.originalStatement = originalStatement;
		}

		@Override
		public void evaluate() throws Throwable {
			initHandlerFromAnnotation();

			if (!annotationInitialized)
				if (containsTestSubject(test.getClass()))
					EasyMockSupport.injectMocks(test);

			originalStatement.evaluate();
		}

		private void initHandlerFromAnnotation() {
			TestedExceptionHandler annotation = test.getClass().getAnnotation(TestedExceptionHandler.class);

			if (nonNull(annotation)) {
				configuration = getHandler(annotation.value());

				Injector.injectMocks(test, configuration);
				annotationInitialized = true;
			}
		}

		private boolean containsTestSubject(Class<?> clazz) {
			return Arrays.stream(clazz.getDeclaredFields())
				.anyMatch(field -> nonNull(field.getAnnotation(TestSubject.class)));
		}

		private ExceptionConfiguration getHandler(Class<? extends ExceptionConfiguration> clazz) {
			try {
				return clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(
						format("Error occurred during initialization class '%s'.", clazz.getCanonicalName()), e);
			}
		}
	}

	public void setConfiguration(ExceptionConfiguration configuration) {
		this.configuration = configuration;
	}

	public boolean isAnnotationInitialized() {
		return annotationInitialized;
	}

	public ExceptionConfiguration getConfiguration() {
		return configuration;
	}

}
