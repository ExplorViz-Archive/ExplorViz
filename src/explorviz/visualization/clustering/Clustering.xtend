package explorviz.visualization.clustering

import explorviz.shared.model.Application

class Clustering {
	public static boolean doClassnameClustering = false
	
	def static doSyntheticClustering(Application application) {
	}
	
	def static openClusteringDialog() {
		ClusteringJS::openDialog()
	}
}