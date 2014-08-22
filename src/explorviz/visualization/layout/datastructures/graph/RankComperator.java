package explorviz.visualization.layout.datastructures.graph;

import java.util.Comparator;

import explorviz.shared.model.helper.Draw3DNodeEntity;

public class RankComperator implements Comparator<Draw3DNodeEntity> {
	Graphzahn graph;

	public RankComperator(final Graphzahn pGraph) {
		graph = pGraph;
	}

	@Override
	public int compare(final Draw3DNodeEntity o1, final Draw3DNodeEntity o2) {
		if (Integer.compare(graph.getRank(o1), graph.getRank(o2)) < 0) {
			return 1;
		} else {
			return -1;
		}
	}
}
