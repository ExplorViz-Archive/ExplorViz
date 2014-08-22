package explorviz.visualization.layout.datastructures.graph

import java.util.PriorityQueue
import java.util.SortedSet
import java.util.Comparator

class GraphPriorityQueue<V> extends PriorityQueue<V> {
	
	new(SortedSet<V> set) {
		super(set)
	}
	
	new(int i, Comparator<V> comparator) {
		super(i,comparator)
	}
	
	override boolean add(V e) {
        var boolean isAdded = false;
        if(!super.contains(e))
        {
            isAdded = super.add(e);
        }
        return isAdded;
    }
}