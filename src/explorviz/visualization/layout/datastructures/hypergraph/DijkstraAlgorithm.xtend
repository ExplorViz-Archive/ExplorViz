package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.visualization.engine.Logging
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList
import java.util.List
import java.util.Map
import java.util.PriorityQueue
import java.util.Set

class DijkstraAlgorithm<V> {
    var Graph<V> graph
  	val Set<V> settledNodes = new HashSet<V>()
  	val Set<V> unSettledNodes = new HashSet<V>()
  	val Map<V, V> predecessors = new HashMap<V,V>()
  	val Map<V, Integer> distance = new HashMap<V, Integer>()
  	val PriorityQueue<V> p = new PriorityQueue<V>(10, new Comparator() {
					override int compare(Object o1, Object o2) {
						return Integer.compare(distance.get(o1), distance.get(o2))
					}
					override equals(Object obj) {
						throw new UnsupportedOperationException("TODO: auto-generated method stub")
					}
					
});
  	
  	new (Graph<V> pGraph) {
    // create a copy of the array so that we can operate on this array
    	graph = pGraph
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
    Logging.log("menno")
    path.add(step);
    while (predecessors.get(step) != null) {
      step = predecessors.get(step);
      path.add(step);
    }
    // Put it into the correct order
    Collections.reverse(path);
    return path;
  }
  
  def void dijkstra (V start) {

    for (V v : graph.vertices){   // fuer jeden Knoten
      distance.put(v, Integer.MAX_VALUE)	
    }
    
    distance.put(start, 0)
	p.add(start);

 	while (!p.empty) {
	     Logging.log("size: "+p.size)
	      var V node = p.poll;
	      settledNodes.add(node);
	      evaluateNeighbours(node);
    }
  }
  
  def void evaluateNeighbours(V source) {
	   if(source == null) {
	   	Logging.log("fu")
	   }
	    var List<V> adjacentNodes = graph.getNeighbors(source)
		    for (V target : adjacentNodes) {
		      if (!settledNodes.contains(target)) {
//		      Logging.log("und hier")
			      if (getShortestDistance(target) > getShortestDistance(source) + 1) {
			        distance.put(target, getShortestDistance(source)+1);
					predecessors.put(target, source);
								      p.add(target);					
			      }		      
		      }
		    }
		    
		    Logging.log("komme raus")
    }
  
}