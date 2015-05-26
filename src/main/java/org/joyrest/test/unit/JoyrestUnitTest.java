package org.joyrest.test.unit;

import static java.util.Objects.isNull;
import static org.joyrest.exception.type.RestException.notFoundSupplier;
import static org.joyrest.model.http.HttpMethod.*;
import static org.joyrest.utils.PathUtils.getPathParams;

import java.util.Set;

import org.joyrest.model.http.HttpMethod;
import org.joyrest.model.request.InternalRequest;
import org.joyrest.routing.ControllerConfiguration;
import org.joyrest.routing.InternalRoute;
import org.joyrest.routing.PathComparator;
import org.joyrest.routing.PathCorrector;
import org.joyrest.routing.matcher.RequestMatcher;
import org.joyrest.stream.BiStream;
import org.joyrest.test.unit.model.MockRequest;
import org.joyrest.test.unit.model.MockResponse;
import org.joyrest.test.unit.rule.ControllerInitRule;
import org.junit.ClassRule;

public abstract class JoyrestUnitTest {

	@ClassRule
	public final static ControllerInitRule joyrest = new ControllerInitRule();

	private final PathComparator pathComparator = new PathComparator();

	private final PathCorrector pathCorrector = new PathCorrector();

	public void configure() {

	}

	protected void call(HttpMethod method, String path, MockRequest req, MockResponse resp) {
		Set<InternalRoute> routes = joyrest.getRoutes();
		InternalRoute route = resolveRoute(routes, req);
		req.setMethod(method);
		req.setPath(createPath(joyrest.getGlobalPath(), pathCorrector.apply(path)));
		req.setPathParams(getPathParams(route, req.getPathParts()));
		route.execute(req, resp);
	}

	protected void get(MockRequest req, MockResponse resp) {
		call(GET, null, req, resp);
	}

	protected void get(String path, MockRequest req, MockResponse resp) {
		call(GET, path, req, resp);
	}

	protected void post(MockRequest req, MockResponse resp) {
		call(POST, null, req, resp);
	}

	protected void post(String path, MockRequest req, MockResponse resp) {
		call(POST, path, req, resp);
	}

	protected void put(MockRequest req, MockResponse resp) {
		call(PUT, null, req, resp);
	}

	protected void put(String path, MockRequest req, MockResponse resp) {
		call(PUT, path, req, resp);
	}

	protected void delete(MockRequest req, MockResponse resp) {
		call(DELETE, null, req, resp);
	}

	protected void delete(String path, MockRequest req, MockResponse resp) {
		call(DELETE, path, req, resp);
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
			.throwIfNull(pathComparator, notFoundSupplier(String.format(
					"There is no route suitable for path [%s]",
					request.getPath())))

			.throwIfNull(RequestMatcher::matchHttpMethod, notFoundSupplier(String.format(
					"There is no route suitable for path [%s], method [%s]",
					request.getPath(), request.getMethod())))

			.findAny().get();
	}

	public void setGlobalPath(String globalPath) {
		joyrest.setGlobalPath(pathCorrector.apply(globalPath));
	}

	public void setController(ControllerConfiguration controller) {
		joyrest.setController(controller);
	}

}
