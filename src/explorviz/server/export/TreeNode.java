package explorviz.server.export;

import java.util.*;

class TreeNode implements Iterable<TreeNode> {

	private final Set<TreeNode> children = new TreeSet<TreeNode>();
	private final String name;

	public TreeNode(final String name) {
		this.name = name;
	}

	public void insertIntoHierarchy(final String[] elements) {
		if (elements.length == 0) {
			return;
		}

		final TreeNode child = new TreeNode(elements[0]);
		final String[] lessElements = new String[elements.length - 1];

		for (int i = 1; i < elements.length; i++) {
			lessElements[i - 1] = elements[i];
		}

		child.insertIntoHierarchy(lessElements);
		addChild(child);
	}

	public boolean addChild(final TreeNode n) {
		return children.add(n);
	}

	public boolean removeChild(final TreeNode n) {
		return children.remove(n);
	}

	public Iterator<TreeNode> iterator() {
		return children.iterator();
	}

	public String getName() {
		return name;
	}
}