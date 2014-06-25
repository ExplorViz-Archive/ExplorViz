package explorviz.server.export;

import java.util.ArrayList;
import java.util.List;

class TreeNode {
	private final List<TreeNode> children = new ArrayList<TreeNode>();
	private final String name;
	private int id;

	public TreeNode(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public TreeNode insertIntoHierarchy(final String[] elements) {
		if (elements.length == 0) {
			return this;
		}

		final TreeNode child = seekOrCreateChild(elements[0]);
		final String[] lessElements = new String[elements.length - 1];

		for (int i = 1; i < elements.length; i++) {
			lessElements[i - 1] = elements[i];
		}

		return child.insertIntoHierarchy(lessElements);
	}

	public TreeNode seekOrCreateChild(final String childName) {
		for (final TreeNode child : getChildren()) {
			if (child.name.equals(childName)) {
				return child;
			}
		}

		final TreeNode newChild = new TreeNode(childName);
		// ExtraVis expects sorted list...
		int insertIndex = Integer.MAX_VALUE;
		for (int i = 0; i < getChildren().size(); i++) {
			final TreeNode child = getChildren().get(i);

			if (child.name.compareTo(childName) >= 1) {
				insertIndex = i;
			}
		}

		if (insertIndex == Integer.MAX_VALUE) {
			getChildren().add(newChild);
		} else {
			getChildren().add(insertIndex, newChild);
		}

		return newChild;
	}

	public int getMaxHierarchyDepth() {
		if (children.isEmpty()) {
			return 0;
		}

		int maxDepth = 0;

		for (final TreeNode child : children) {
			maxDepth = Math.max(maxDepth, child.getMaxHierarchyDepth());
		}

		return maxDepth + 1;
	}

	public String toString() {
		return name;
	}
}