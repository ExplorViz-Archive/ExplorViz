package explorviz.visualization.clustering

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.Logging

class Clustering {

	var static int MIN_CLASS_AMOUNT_FOR_CLUSTERING = 10

	def static void doSyntheticClustering(Application application) {
		recursiveLookup(application.components.get(0), application)
	}

	def static void recursiveLookup(Component component, Application application) {
		for (child : component.children) {
			recursiveLookup(child, application)
		}
		
		if (component.clazzes.size >= MIN_CLASS_AMOUNT_FOR_CLUSTERING) {
			Logging::log("clustering..." + component.name)
			component.children.add(clusterClasses(component.clazzes, application))
			component.clazzes.clear
		}
	}

	def static Component clusterClasses(List<Clazz> clazzes, Application application) {
		var List<ClusterData> clusterdata = new ArrayList<ClusterData>
		for (clazz : clazzes) {
			clusterdata.add(new ClusterData(clazz))
		}
		
		//SingleLink::doSingleLink(clusterdata, application)
		CompleteLink::doCompleteLink(clusterdata, application)
		
	}

	def static void openClusteringDialog() {
		ClusteringJS::openDialog()
	}

}
