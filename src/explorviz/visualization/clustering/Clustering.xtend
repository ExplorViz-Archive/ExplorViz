package explorviz.visualization.clustering

import explorviz.shared.model.Application
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

	var static int MIN_CLASS_AMOUNT_FOR_CLUSTERING = 32

	def static void doSyntheticClustering(Application application) {
		if (ENABLED)
			CLUSTER_METHOD.clusterNameCounter = 1;
			recursiveLookup(application.components.get(0), application)
	}

	def static void recursiveLookup(Component component, Application application) {
		if (component.clazzes.size >= MIN_CLASS_AMOUNT_FOR_CLUSTERING && !component.synthetic) {
			Clustering::clusterClasses(component, application)
		}
		
		for (child : component.children) {
			recursiveLookup(child, application)
		}
	}

	def static void clusterClasses(Component parentComponent, Application application) {
		var List<ClusterData> clusterdata = new ArrayList<ClusterData>
		for (clazz : parentComponent.clazzes) {
			clusterdata.add(new ClusterData(clazz))
		}
		
		CLUSTER_METHOD.doGenericClustering(clusterdata, parentComponent, application)
	}
}
