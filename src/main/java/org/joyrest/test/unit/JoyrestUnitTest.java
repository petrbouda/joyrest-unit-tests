package org.joyrest.test.unit;

import static org.joyrest.exception.type.RestException.notFoundSupplier;

import java.util.Set;

import org.joyrest.model.http.HttpMethod;
import org.joyrest.model.request.InternalRequest;
import org.joyrest.routing.ControllerConfiguration;
import org.joyrest.routing.InternalRoute;
import org.joyrest.routing.PathComparator;
import org.joyrest.routing.matcher.RequestMatcher;
import org.joyrest.stream.BiStream;
import org.joyrest.test.unit.model.MockRequest;
import org.joyrest.test.unit.model.MockResponse;
import org.joyrest.test.unit.rule.ControllerInitRule;
import org.junit.ClassRule;

public abstract class JoyrestUnitTest {

	@ClassRule
	public static ControllerInitRule joyrest = new ControllerInitRule();

	private PathComparator pathComparator = new PathComparator();

	protected void get(MockRequest req, MockResponse resp) {
		Set<InternalRoute> routes = joyrest.getRoutes();
		req.setMethod(HttpMethod.GET);
		req.setPath(joyrest.getGlobalPath());
		InternalRoute route = resolveRoute(routes, req);
		route.execute(req, resp);
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

	public void configure() {

    }

	public void setGlobalPath(String globalPath) {
		joyrest.setGlobalPath(globalPath);
	}

	public void setController(ControllerConfiguration controller) {
		joyrest.setController(controller);
	}

}
