/*
* inspired by http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
* and added a priority queue
*/
package explorviz.visualization.layout.datastructures.graph

import explorviz.visualization.engine.Logging
import explorviz.visualization.engine.math.Vector3f
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.SortedSet
import java.util.TreeSet

class DijkstraAlgorithm {
	var Graph<Vector3fNode> graph
	val HashSet<Vector3fNode> settledNodes = new HashSet<Vector3fNode>()
	val HashMap<Vector3fNode, Vector3fNode> predecessors = new HashMap<Vector3fNode, Vector3fNode>()
	val HashMap<Vector3fNode, Integer> distance = new HashMap<Vector3fNode, Integer>()
	val SortedSet<Vector3fNode> set = new TreeSet<Vector3fNode>(
		new Comparator<Vector3fNode>() {
			override compare(Vector3fNode o1, Vector3fNode o2) {
				val distanceToTarget = o1.sub(currentTarget).length <=> o2.sub(currentTarget).length
				if (distanceToTarget == 0) {
					val pathDistance = distance.get(o1) <=> distance.get(o2)
					if (pathDistance == 0) {
						o1.sub(currentStart).length <=> o2.sub(currentStart).length
					} else {
						pathDistance
					}
				} else {
					distanceToTarget
				}
			}

			override equals(Object obj) {
				throw new UnsupportedOperationException("")
			}

		});
	val GraphPriorityQueue<Vector3fNode> priorityQueue = new GraphPriorityQueue<Vector3fNode>(set);

	Vector3fNode currentStart
	Vector3fNode currentTarget

	new(Graph<Vector3fNode> pGraph) {
		graph = pGraph
	}

	def Integer getDistance(Vector3fNode source, Vector3fNode target) {
		for (Edge<Vector3fNode> edge : graph.edges) {

			if (edge.hasVertex(source) && edge.hasVertex(target)) {
				return ((source as Vector3f).distanceTo(target as Vector3f) as int) + 1
			}

		}

		throw new RuntimeException("Should not happen");
	}

	def Integer getShortestDistance(Vector3fNode destination) {
		var Integer d = distance.get(destination);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
   * This method returns the path from the source to the selected target and
   * empty if no path exists
   */
	def List<Vector3fNode> getPath(Vector3fNode target) {
		val List<Vector3fNode> path = new ArrayList<Vector3fNode>();
		var Vector3fNode step = target;

		if (predecessors.get(step) == null) {
			return path;
		}
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}

		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}

	def List<Vector3fNode> dijkstra(Vector3fNode start, Vector3fNode target) {
		settledNodes.clear
		predecessors.clear
		distance.clear
		priorityQueue.clear
		
		currentStart = start
		currentTarget = target

		for (Vector3fNode node : graph.vertices) {
			distance.put(node, Integer.MAX_VALUE)
		}

		distance.put(start, 0)
		priorityQueue.add(start);

		var long startT = System::currentTimeMillis();
		var int i = 0
		var foundAPath = (target.sub(start).length <= 0.001f)

		while (!priorityQueue.empty && !foundAPath) {
			var Vector3fNode node = priorityQueue.poll;
			settledNodes.add(node);
			foundAPath = evaluateNeighbours(node, target);
			i++
		}
		getPath(target)
	}

	def boolean evaluateNeighbours(Vector3fNode source, Vector3fNode target) {
		var List<Vector3fNode> neighbors = graph.getNeighborsFast(source)

		if (neighbors != null) {
			for (Vector3fNode neighbor : neighbors) {
				if (!settledNodes.contains(neighbor)) {
					val shortestSummed = getShortestDistance(source) + getDistance(source, neighbor)

					if (getShortestDistance(neighbor) > shortestSummed) {
						distance.put(neighbor, shortestSummed);
						predecessors.put(neighbor, source);
						priorityQueue.add(neighbor);
					}
					if (target.sub(neighbor).length <= 0.001f) {
						return true
					}
				}
			}
		}
		return false
	}

}
