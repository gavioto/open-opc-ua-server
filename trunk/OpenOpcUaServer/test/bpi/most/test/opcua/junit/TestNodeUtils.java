package bpi.most.test.opcua.junit;

import static org.junit.Assert.*;

import org.junit.Test;

import bpi.most.opcua.server.core.util.NodeUtils;

public class TestNodeUtils {

	@Test
	public void testPrimitiveTypes() {
		int i = 23;
		float f = 23.2f;
		Object o;

		o = i;
		assertTrue(NodeUtils.isBuiltinType(o.getClass()));
		o = f;
		assertTrue(NodeUtils.isBuiltinType(o.getClass()));
	}

}
