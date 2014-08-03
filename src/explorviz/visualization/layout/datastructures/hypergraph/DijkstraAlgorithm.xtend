package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.visualization.engine.Logging
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList
import java.util.List
import java.util.Map
import java.util.Set

class DijkstraAlgorithm<V> {
    var Graph<V> graph
  	val Set<V> settledNodes = new HashSet<V>()
  	val Set<V> unSettledNodes = new HashSet<V>()
  	val Map<V, V> predecessors = new HashMap<V,V>()
  	val Map<V, Integer> distance = new HashMap<V, Integer>()
  	
  	new (Graph<V> pGraph) {
    // create a copy of the array so that we can operate on this array
    	graph = pGraph
  	}
  	
  	def void execute(V source) {
	    distance.put(source, 0);
	    unSettledNodes.add(source);
	    while (unSettledNodes.size() > 0) {
	      var V node = getMinimum(unSettledNodes);
	      settledNodes.add(node);
	      unSettledNodes.remove(node);
	      findMinimalDistances(node);
	    }
  	}
  	
  	def void findMinimalDistances(V vertex) {
	    var List<V> adjacentNodes = graph.getNeighbors(vertex)
	    for (V target : adjacentNodes) {
	      if (getShortestDistance(target) > getShortestDistance(vertex) + getDistance(vertex, target)) {
	        distance.put(target, getShortestDistance(vertex)+getDistance(vertex, target));
	        predecessors.put(target, vertex);
	        unSettledNodes.add(target);
	      }
	    }
	
	  }
	  
	def Integer getDistance(V source, V target) {
	    for (Edge<V> edge : graph.edges) {
		      if (edge.source == source && edge.target == target) {
		        return edge.weight;
		      }
	    }
    	throw new RuntimeException("Should not happen");
  	}
  
  	def V getMinimum(Set<V> vertices) {
	    var V minimum = null;
	    for (V vertex : vertices) {
		      if (minimum == null) {
		        minimum = vertex;
		      } else {
		        if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
		          minimum = vertex;
		        }
		      }
	    }
	    return minimum;
 	}
  
   def boolean isSettled(V vertex) {
    return settledNodes.contains(vertex);
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
      Logging.log("so haben wir aber nicht gewettet")
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
}