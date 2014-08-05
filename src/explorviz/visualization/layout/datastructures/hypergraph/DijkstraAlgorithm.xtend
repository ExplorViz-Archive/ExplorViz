/*
* inspired by http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
* and added a priority queue
*/
package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.visualization.engine.Logging
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList
import java.util.SortedSet
import java.util.TreeSet
import explorviz.visualization.engine.math.Vector3f

class DijkstraAlgorithm<V> {
	var Graph<V> graph
	val HashSet<V> settledNodes = new HashSet<V>()
	val HashMap<V, V> predecessors = new HashMap<V, V>()
	val HashMap<V, Integer> distance = new HashMap<V, Integer>()
	val SortedSet<V> set = new TreeSet<V>(new Comparator() {
			override int compare(Object o1, Object o2) {
				return Integer.compare(distance.get(o1 as V), distance.get(o2 as V))
			}

			override equals(Object obj) {
				throw new UnsupportedOperationException("TODO: auto-generated method stub")
			}

		});
	val GraphPriorityQueue<V> p = new GraphPriorityQueue<V>(set);
//	val GraphPriorityQueue<V> p = new GraphPriorityQueue<V>(1,
//		new Comparator<V>() {
//			override int compare(Object o1, Object o2) {
//				return Integer.compare(distance.get(o1 as V), distance.get(o2 as V))
//			}
//
//			override equals(Object obj) {
//				throw new UnsupportedOperationException("TODO: auto-generated method stub")
//			}
//
//		});

	new(Graph<V> pGraph) {
		settledNodes.clear
		predecessors.clear
		distance.clear
		p.clear
		graph = pGraph
		graph.adjMatrix.clear
		graph.createAdjacencyMatrix
	}
	
	def Integer getDistance(V source, V target) {
	 
	    for (Edge<V> edge : graph.edges) {
	 
		      if (edge.hasVertex(source) && edge.hasVertex(target)) {
		        return ((source as Vector3f).distanceTo(target as Vector3f) as int)+1
		      }
	 
	    }
	 
    	throw new RuntimeException("Should not happen");
	  	}

	def Integer getShortestDistance(V destination) {
		var Integer d = distance.get(destination);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
   * This method returns the path from the source to the selected target and
   * NULL if no path exists
   */
	def LinkedList<V> getPath(V target) {
		val LinkedList<V> path = new LinkedList<V>();
		var V step = target;

		if (predecessors.get(step) == null) {
			return null;
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

	def void dijkstra(V start) {

		for (V v : graph.vertices) { // fuer jeden Knoten
			distance.put(v, Integer.MAX_VALUE)
		}

		distance.put(start, 0)
		p.add(start);

		while (!p.empty) {
//			Logging.log("size: "+p.size)
			var V node = p.poll;
			settledNodes.add(node);
			evaluateNeighbours(node);
		}
	}

	def void evaluateNeighbours(V source) {
		var ArrayList<V> adjacentNodes = graph.getNeighborsFast(source)
//		Logging.log("neighbors: "+ adjacentNodes)
		if(adjacentNodes != null) {
		for (V target : adjacentNodes) {
			if (!settledNodes.contains(target)) {
				if (getShortestDistance(target) > getShortestDistance(source) + getDistance(source,target)) {
					distance.put(target, getShortestDistance(source) + getDistance(source,target));
					predecessors.put(target, source);
					p.add(target);
				}
			}
		}
		}
	}

}
