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
import static org.joyrest.exception.type.RestException.notFoundSupplier;
import static org.joyrest.model.http.HttpMethod.*;
import static org.joyrest.utils.PathUtils.getPathParams;

import java.util.Set;

import org.easymock.EasyMockSupport;
import org.joyrest.model.http.HttpMethod;
import org.joyrest.model.request.InternalRequest;
import org.joyrest.routing.ControllerConfiguration;
import org.joyrest.routing.InternalRoute;
import org.joyrest.routing.PathComparator;
import org.joyrest.routing.PathCorrector;
import org.joyrest.routing.matcher.RequestMatcher;
import org.joyrest.stream.BiStream;
import org.joyrest.test.unit.annotation.TestedController;
import org.joyrest.test.unit.model.MockRequest;
import org.joyrest.test.unit.model.MockResponse;
import org.joyrest.test.unit.rule.JoyrestRule;
import org.junit.Rule;

public abstract class JoyrestUnitTest extends EasyMockSupport {

	@Rule
	public JoyrestRule holder = new JoyrestRule(this);

	private Set<InternalRoute> routes = null;

	private static final PathComparator pathComparator = new PathComparator();

	private static final PathCorrector pathCorrector = new PathCorrector();

	public void configure() {

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void call(HttpMethod method, String path, MockRequest req, MockResponse resp) {
		initialize();

		req.setMethod(method);
		req.setPath(createPath(holder.getControllerPath(), pathCorrector.apply(path)));
		InternalRoute route = resolveRoute(routes, req);
		req.setPathParams(getPathParams(route, req.getPathParts()));
		route.execute(req, resp);
	}

	public void initialize() {
		if (!holder.isAnnotationInitialized())
			configure();

		ControllerConfiguration controller = holder.getController();
		if (isNull(controller))
			throw new RuntimeException(format("There is no defined any tested controller. Use '%s' annotation " +
					"on the given test class or setter in 'configure' method.", TestedController.class.getCanonicalName()));

		controller.initialize();
		routes = controller.getRoutes();
	}

	private static String createPath(String globalPath, String path) {
		if (isNull(globalPath) && isNull(path))
			return "/";

		else if (isNull(globalPath))
			return path;

		else if (isNull(path))
			return globalPath;

		return globalPath + path;
	}

	public InternalRoute resolveRoute(Set<InternalRoute> routes, InternalRequest<?> request) {
		return BiStream.of(routes.stream(), request)
			.throwIfNull(pathComparator, notFoundSupplier(format(
					"There is no route suitable for path [%s]",
					request.getPath())))

			.throwIfNull(RequestMatcher::matchHttpMethod, notFoundSupplier(format(
					"There is no route suitable for path [%s], method [%s]",
					request.getPath(), request.getMethod())))

			.findAny().get();
	}

	protected <REQ, RESP> void get(MockRequest<REQ> req, MockResponse<RESP> resp) {
		call(GET, null, req, resp);
	}

	protected <REQ, RESP> void get(String path, MockRequest<REQ> req, MockResponse<RESP> resp) {
		call(GET, path, req, resp);
	}

	protected <REQ, RESP> void post(MockRequest<REQ> req, MockResponse<RESP> resp) {
		call(POST, null, req, resp);
	}

	protected <REQ, RESP> void post(String path, MockRequest<REQ> req, MockResponse<RESP> resp) {
		call(POST, path, req, resp);
	}

	protected <REQ, RESP> void put(MockRequest<REQ> req, MockResponse<RESP> resp) {
		call(PUT, null, req, resp);
	}

	protected <REQ, RESP> void put(String path, MockRequest<REQ> req, MockResponse<RESP> resp) {
		call(PUT, path, req, resp);
	}

	protected <REQ, RESP> void delete(MockRequest<REQ> req, MockResponse<RESP> resp) {
		call(DELETE, null, req, resp);
	}

	protected <REQ, RESP> void delete(String path, MockRequest<REQ> req, MockResponse<RESP> resp) {
		call(DELETE, path, req, resp);
	}

	public void setGlobalPath(String globalPath) {
		this.holder.setControllerPath(pathCorrector.apply(globalPath));
	}

	public void setController(ControllerConfiguration controller) {
		this.holder.setController(controller);
	}
}
