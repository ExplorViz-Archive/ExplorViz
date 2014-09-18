package explorviz.visualization.clustering

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import java.util.ArrayList
import java.util.List

class Clustering {

	var static List<Component> clusteredComponents
	var static int minClasses = 10
	
	def static List<Component> doSyntheticClustering(Application application) {
		clusteredComponents = application.components
		recursiveLookup(clusteredComponents.get(0))
		return clusteredComponents
	}
	
	def static void recursiveLookup(Component newcomponent) {
		if (newcomponent.clazzes.size >= minClasses) {
			for (subcomponent : newcomponent.children) {
				recursiveLookup(subcomponent)
			}
			newcomponent.children.add(clusterClasses(newcomponent.clazzes))
			newcomponent.clazzes.clear
		} else {
			for (child : newcomponent.children) {
				recursiveLookup(child)
			}
		}
	}
	
	def static Component clusterClasses(List<Clazz> clazzes) {
		var List<ClusterData> clusterdata = new ArrayList<ClusterData>
		for (clazz : clazzes) {
			clusterdata.add(new ClusterData(clazz)) 
		}
		return SingleLink::doSingleLink(clusterdata)
	}
	
	def static openClusteringDialog() {
		ClusteringJS::openDialog()
	}
	
}