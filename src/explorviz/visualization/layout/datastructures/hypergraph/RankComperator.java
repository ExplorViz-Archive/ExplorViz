package explorviz.visualization.layout.datastructures.hypergraph;

import java.util.Comparator;

import explorviz.shared.model.helper.Draw3DNodeEntity;

public class RankComperator implements Comparator<Draw3DNodeEntity> {
	Graph<Draw3DNodeEntity> graph;

	public RankComperator(final Graph<Draw3DNodeEntity> graph) {
		this.graph = graph;
	}

	@Override
	public int compare(final Draw3DNodeEntity o1, final Draw3DNodeEntity o2) {
		return Integer.compare(graph.getRank(o1), graph.getRank(o2));
	}
}
