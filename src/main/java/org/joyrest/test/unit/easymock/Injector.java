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
package org.joyrest.test.unit.easymock;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.internal.Injection;
import org.easymock.internal.InjectionPlan;
import org.easymock.internal.InjectionTarget;

public class Injector {

	public static void injectMocks(EasyMockSupport test, Object handler) {
		requireNonNull(test);

		InjectionPlan injectionPlan = new InjectionPlan();
		createMocksForAnnotations(test, injectionPlan);
		injectMocksOnClass(handler, injectionPlan);

		// Check for unsatisfied qualified injections only after having scanned all TestSubjects and their superclasses
		for (Injection injection : injectionPlan.getQualifiedInjections())
			if (!injection.isMatched())
				throw new RuntimeException(
						format("Unsatisfied qualifier: '%s'", injection.getAnnotation().fieldName()));

	}

	private static void createMocksForAnnotations(EasyMockSupport test, InjectionPlan injectionPlan) {
		for (Field field : test.getClass().getDeclaredFields()) {
			Mock annotation = field.getAnnotation(Mock.class);
			if (isNull(annotation))
				continue;

			Class<?> type = field.getType();
			String name = annotation.name();
			// Empty string means we are on the default value which we means no name (aka null) from the EasyMock point of view
			name = (name.isEmpty() ? null : name);

			Object mock = test.createMock(name, annotation.type(), type);
			field.setAccessible(true);
			try {
				field.set(test, mock);
			} catch (final IllegalAccessException e) {
				throw new RuntimeException("Error occurred during setting a mock into the field.", e);
			}

			injectionPlan.addInjection(new Injection(mock, annotation));
		}
	}

	private static void injectMocksOnClass(Object controller, InjectionPlan injectionPlan) {
		List<Field> fields = injectByName(controller.getClass(), controller, injectionPlan.getQualifiedInjections());
		injectByType(controller, fields, injectionPlan.getUnqualifiedInjections());
	}

	private static List<Field> injectByName(Class<?> controllerClazz, Object controller, List<Injection> qualifiedInjections) {

		List<Field> fields = asList(controllerClazz.getDeclaredFields());
		for (Injection injection : qualifiedInjections) {
			Field field = getFieldByName(controllerClazz, injection.getQualifier());
			InjectionTarget target = injectionTargetWithField(field);
			if (isNull(target))
				continue;

			if (target.accepts(injection)) {
				target.inject(controller, injection);
				fields.remove(target.getTargetField());
			}
		}

		return fields;
	}

	private static void injectByType(Object obj, List<Field> fields, List<Injection> injections) {
		for (Field field : fields) {
			InjectionTarget target = injectionTargetWithField(field);
			Injection toAssign = findUniqueAssignable(injections, target);

			if (nonNull(target) && nonNull(toAssign))
				target.inject(obj, toAssign);
		}
	}

	private static Field getFieldByName(final Class<?> clazz, final String fieldName) {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (final NoSuchFieldException e) {
			return null;
		} catch (final SecurityException e) {
			return null;
		}
	}

	private static InjectionTarget injectionTargetWithField(Field field) {
		// Skip final or static fields
		if (field == null || (field.getModifiers() & (Modifier.STATIC + Modifier.FINAL)) != 0)
			return null;

		return new InjectionTarget(field);
	}

	private static Injection findUniqueAssignable(List<Injection> injections, InjectionTarget target) {
		Injection toAssign = null;
		for (Injection injection : injections) {
			if (target.accepts(injection)) {
				if (nonNull(toAssign))
					throw new RuntimeException(
							format("At least two mocks can be assigned to '%s': %s and %s",
									target.getTargetField(), toAssign.getMock(), injection.getMock()));

				toAssign = injection;
			}
		}
		return toAssign;
	}
}
