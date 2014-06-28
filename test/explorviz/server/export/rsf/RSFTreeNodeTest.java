package explorviz.server.export.rsf;

import static org.junit.Assert.*;

import org.junit.Test;

import explorviz.server.export.rsf.RSFTreeNode;

public class RSFTreeNodeTest {

	@Test
	public void testInsertIntoHierarchy() {
		final RSFTreeNode root = new RSFTreeNode("Root");
		final String[] elements = "com.test.explorviz.Main".split("\\.");
		root.insertIntoHierarchy(elements);
		root.insertIntoHierarchy(elements);

		assertEquals(1, root.getChildren().size());
		final RSFTreeNode first = root.getChildren().get(0);
		assertEquals("com", first.getName());

		assertEquals(1, first.getChildren().size());
		final RSFTreeNode second = first.getChildren().get(0);
		assertEquals("test", second.getName());

		assertEquals(1, second.getChildren().size());
		final RSFTreeNode third = second.getChildren().get(0);
		assertEquals("explorviz", third.getName());

		assertEquals(1, third.getChildren().size());
		final RSFTreeNode fourth = third.getChildren().get(0);
		assertEquals("Main", fourth.getName());

		assertEquals(4, root.getMaxHierarchyDepth());
	}
}
