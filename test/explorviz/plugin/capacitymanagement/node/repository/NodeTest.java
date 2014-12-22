package explorviz.plugin.capacitymanagement.node.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import explorviz.plugin.capacitymanagement.node.repository.Node;

public class NodeTest {

	private static final double DELTA = 0.01;

	@Test
	public void testGetAverageCPUUtilization() throws Exception {
		final Node node = new Node("0", "inst0", "host0", 4, null);
		assertFalse(node.hasSufficientCPUUilizationHistoryEntries());
		assertEquals(-1, node.getAverageCPUUtilization(), DELTA);

		node.addCPUUtilizationHistoryEntry(0);
		assertEquals(0, node.getAverageCPUUtilization(), DELTA);
		assertFalse(node.hasSufficientCPUUilizationHistoryEntries());

		node.addCPUUtilizationHistoryEntry(1);
		assertEquals(0.5, node.getAverageCPUUtilization(), DELTA);
		assertFalse(node.hasSufficientCPUUilizationHistoryEntries());

		node.addCPUUtilizationHistoryEntry(0);
		assertEquals(0.3333, node.getAverageCPUUtilization(), DELTA);
		assertFalse(node.hasSufficientCPUUilizationHistoryEntries());

		node.addCPUUtilizationHistoryEntry(0.5);
		assertEquals(0.375, node.getAverageCPUUtilization(), DELTA);
		assertTrue(node.hasSufficientCPUUilizationHistoryEntries());

		node.addCPUUtilizationHistoryEntry(0.3);
		assertEquals(0.45, node.getAverageCPUUtilization(), DELTA);
		assertTrue(node.hasSufficientCPUUilizationHistoryEntries());
	}

}
