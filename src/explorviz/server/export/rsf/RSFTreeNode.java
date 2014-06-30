package explorviz.server.export.rsf;

import java.util.ArrayList;
import java.util.List;

class RSFTreeNode {
	private final List<RSFTreeNode> children = new ArrayList<RSFTreeNode>();
	private final String name;
	private int id;

	public RSFTreeNode(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<RSFTreeNode> getChildren() {
		return children;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public RSFTreeNode insertIntoHierarchy(final String[] elements) {
		if (elements.length == 0) {
			return this;
		}

		final RSFTreeNode child = seekOrCreateChild(elements[0]);
		final String[] lessElements = new String[elements.length - 1];

		for (int i = 1; i < elements.length; i++) {
			lessElements[i - 1] = elements[i];
		}

		return child.insertIntoHierarchy(lessElements);
	}

	public RSFTreeNode seekOrCreateChild(final String childName) {
		for (final RSFTreeNode child : getChildren()) {
			if (child.name.equals(childName)) {
				return child;
			}
		}

		final RSFTreeNode newChild = new RSFTreeNode(childName);
		// ExtraVis expects sorted list...
		int insertIndex = Integer.MAX_VALUE;
		for (int i = 0; i < getChildren().size(); i++) {
			final RSFTreeNode child = getChildren().get(i);

			if (child.name.compareTo(childName) >= 1) {
				insertIndex = i;
				break;
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

		for (final RSFTreeNode child : children) {
			maxDepth = Math.max(maxDepth, child.getMaxHierarchyDepth());
		}

		return maxDepth + 1;
	}

	public String toString() {
		return name;
	}
}