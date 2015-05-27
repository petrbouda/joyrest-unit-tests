package org.joyrest.test.unit.assertion;

import static org.joyrest.routing.entity.ResponseCollectionType.RespList;
import static org.joyrest.routing.entity.ResponseType.Resp;
import static org.joyrest.test.unit.assertion.JoyrestAssert.assertType;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JoyrestAssertTest {

	@Test
	public void testAssertType() throws Exception {
		assertType(Resp(String.class), "Test");
	}

	@Test(expected = ClassCastException.class)
	public void testAssertTypeException() throws Exception {
		assertType(Resp(String.class), 1);
	}

	@Test(expected = NullPointerException.class)
	public void testAssertTypeNullPointerException() throws Exception {
		assertType(Resp(String.class), null);
	}

	@Test
	public void testAssertTypeOptional() throws Exception {
		assertType(Resp(String.class), Optional.of("Test"));
	}

	@Test(expected = ClassCastException.class)
	public void testAssertTypeOptionalException() throws Exception {
		assertType(Resp(String.class), Optional.of(1));
	}

	@Test
	public void testAssertCollectionType() throws Exception {
		List<String> list = Arrays.asList("First", "Second", "Third");
		assertType(RespList(String.class), list);
	}

	@Test(expected = ClassCastException.class)
	public void testAssertCollectionTypeException() throws Exception {
		List<Object> list = Arrays.asList("First", "Second", 1);
		assertType(RespList(String.class), list);
	}

	@Test(expected = NullPointerException.class)
	public void testAssertCollectionTypeNullPointerException() throws Exception {
		assertType(RespList(String.class), null);
	}

	@Test
	public void testAssertCollectionTypeOptional() throws Exception {
		List<String> list = Arrays.asList("First", "Second", "Third");
		assertType(RespList(String.class), Optional.of(list));
	}
}