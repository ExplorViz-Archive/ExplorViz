package explorviz.visualization.clustering

import explorviz.shared.model.Application
import java.util.ArrayList
import explorviz.shared.model.Component
import java.util.List
import explorviz.shared.model.CommunicationClazz

class Clustering {
	//public static boolean doClassnameClustering = false
	var static List<Component> componentslist = new ArrayList<Component>
	var static List<CommunicationClazz> classeslist = new ArrayList<CommunicationClazz>
	
	def static doSyntheticClustering(Application application) {
		
		//val double[] methodCalls = newDoubleArrayOfSize(5)
		//val double[] activeInstances = newDoubleArrayOfSize(5)
		//val String[] classnames = newArrayOfSize(5)	
		
		componentslist = application.components
		classeslist = application.communications
		

		System.out.println(classeslist)
		System.out.println(componentslist)
		
		// if single-link is chosen, do single-link clustering
		//SingleLink::doSingleLink(methodCalls, activeInstances, classnames)
		
		
	}
	
	def static openClusteringDialog() {
		ClusteringJS::openDialog()
	}
	
	def static void main(String[] args) {

	}
}