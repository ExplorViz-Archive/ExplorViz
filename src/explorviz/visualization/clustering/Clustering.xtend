package explorviz.visualization.clustering

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import java.util.ArrayList
import java.util.List

/**
 *
 * @author Mirco Barzel
 *
 */
class Clustering {
	var static boolean ENABLED = false
	var static GenericClusterLink CLUSTER_METHOD = new CompleteLink()

	var static int MIN_CLASS_AMOUNT_FOR_CLUSTERING = 10

	def static void doSyntheticClustering(Application application) {
		if (ENABLED)
			recursiveLookup(application.components.get(0), application)
	}

	def static void recursiveLookup(Component component, Application application) {
		for (child : component.children) {
			recursiveLookup(child, application)
		}
		
		if (component.clazzes.size >= MIN_CLASS_AMOUNT_FOR_CLUSTERING && !component.isSynthetic) {
			Clustering::clusterClasses(component.clazzes, component, application)
		}
	}

	def static void clusterClasses(List<Clazz> clazzes, Component parentComponent, Application application) {
		var List<ClusterData> clusterdata = new ArrayList<ClusterData>
		for (clazz : clazzes) {
			clusterdata.add(new ClusterData(clazz))
		}
		
		CLUSTER_METHOD.doGenericClustering(clusterdata, parentComponent, application)
	}
}
