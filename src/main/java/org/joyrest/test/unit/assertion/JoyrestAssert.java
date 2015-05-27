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
package org.joyrest.test.unit.assertion;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.joyrest.routing.entity.CollectionType;
import org.joyrest.routing.entity.Type;

import java.util.Collection;
import java.util.Optional;

public class JoyrestAssert {

	public static void assertType(Type<?> expected, Object entity) {
		assertType(expected.getType(), entity);
	}

	public static void assertType(Class<?> expected, Object entity) {
		requireNonNull(entity, "Entity cannot be null.");

		Object type = entity;
		if (entity instanceof Optional)
			type = ((Optional<?>) entity).get();

		if (!expected.isAssignableFrom(type.getClass()))
			throw new ClassCastException(
				format("Object cannot be cast from '%s' to the expected class '%s'",
					type.getClass().getCanonicalName(), expected.getCanonicalName()));
	}

	public static void assertType(CollectionType<?> expected, Object entity) {
		requireNonNull(entity, "Entity cannot be null.");
		requireNonNull(expected, "Expected type cannot be null.");

		Object type = entity;
		if (entity instanceof Optional)
			type = ((Optional<?>) entity).get();

		Class<?> expectedType = expected.getType();
		Class<?> expectedParam = expected.getParam();

		expectedType.isAssignableFrom(type.getClass());

		@SuppressWarnings("unchecked")
		Collection<Object> collection = (Collection<Object>) type;
		for (Object object: collection)
			if (!expectedParam.isAssignableFrom(object.getClass()))
				throw new ClassCastException(
					format("Object in a given collection cannot be cast from '%s' to the expected class '%s'",
						object.getClass().getCanonicalName(), expectedParam.getCanonicalName()));
	}
}
