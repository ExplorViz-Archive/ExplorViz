package explorviz.server.export;

import static org.junit.Assert.*;

import org.junit.Test;

public class TreeNodeTest {

	@Test
	public void testInsertIntoHierarchy() {
		final TreeNode root = new TreeNode("Root");
		final String[] elements = "com.test.explorviz.Main".split("\\.");
		root.insertIntoHierarchy(elements);
		root.insertIntoHierarchy(elements);

		assertEquals(1, root.getChildren().size());
		final TreeNode first = root.getChildren().get(0);
		assertEquals("com", first.getName());

		assertEquals(1, first.getChildren().size());
		final TreeNode second = first.getChildren().get(0);
		assertEquals("test", second.getName());

		assertEquals(1, second.getChildren().size());
		final TreeNode third = second.getChildren().get(0);
		assertEquals("explorviz", third.getName());

		assertEquals(1, third.getChildren().size());
		final TreeNode fourth = third.getChildren().get(0);
		assertEquals("Main", fourth.getName());

		assertEquals(4, root.getMaxHierarchyDepth());
	}
}
