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
package org.joyrest.test.unit;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.function.Function;

import org.easymock.EasyMockSupport;
import org.joyrest.exception.configuration.ExceptionConfiguration;
import org.joyrest.exception.handler.InternalExceptionHandler;
import org.joyrest.test.unit.annotation.TestedExceptionHandler;
import org.joyrest.test.unit.model.MockRequest;
import org.joyrest.test.unit.model.MockResponse;
import org.joyrest.test.unit.rule.HandlerRule;
import org.junit.Rule;

public abstract class HandlerUnitTest extends EasyMockSupport {

	@Rule
	public HandlerRule holder = new HandlerRule(this);

	private Map<Class<? extends Exception>, InternalExceptionHandler> handlers = null;

	public void configure() {

	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void call(Class<?> clazz, MockRequest req, MockResponse resp, Exception ex) {
		initialize();

		InternalExceptionHandler handler = resolveHandler(clazz);
		handler.execute(req, resp, ex);
	}

	public void initialize() {
		if (!holder.isAnnotationInitialized())
			configure();

		ExceptionConfiguration configuration = holder.getConfiguration();
		if (isNull(configuration))
			throw new RuntimeException(format("There is no defined any tested configuration. Use '%s' annotation " +
				" on the given test class or setter in 'configure' method.", TestedExceptionHandler.class.getCanonicalName()));

		configuration.initialize();
		handlers = configuration.getExceptionHandlers().stream()
			.collect(toMap(InternalExceptionHandler::getExceptionClass, Function.identity()));
	}

	public InternalExceptionHandler resolveHandler(Class<?> clazz) {
		InternalExceptionHandler handler = handlers.get(clazz);
		
		if (isNull(handler))
			throw new RuntimeException(format("There is no defined handler with class '%s'", clazz));

		return handler;
	}

	protected <REQ, RESP> void handle(MockRequest<REQ> req, MockResponse<RESP> resp, Exception ex) {
		call(ex.getClass(), req, resp, ex);
	}

	protected <REQ, RESP, T extends Exception> void handle(Class<T> clazz, MockRequest<REQ> req, MockResponse<RESP> resp, T ex) {
		call(clazz, req, resp, ex);
	}

	public void setConfiguration(ExceptionConfiguration configuration) {
		this.holder.setConfiguration(configuration);
	}
}
