package explorviz.visualization.clustering

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import java.util.ArrayList
import java.util.List

class Clustering {

	var static int minClasses = 10
	
	def static Application doSyntheticClustering(Application application) {
		
		recursiveLookup(application.components.get(0))
		
		return application

	}
	
	def static void recursiveLookup(Component component) {
		if (component.clazzes.size >= minClasses) {
			for (subcomponent : component.children) {
				recursiveLookup(subcomponent)
			}
			component.children.add(clusterClasses(component.clazzes))
			component.clazzes.clear
		} else {
			for (child : component.children) {
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