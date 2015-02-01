package explorviz.visualization.meta_monitoring

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.callbacks.VoidCallback

class MetaMonitoringManager {
	var static MetaMonitoringServiceAsync metaMonitoringService

	var static MONITORING_ENABLED = false
	var static alreadyInitialized = false

	def static init() {
		if (MONITORING_ENABLED && !alreadyInitialized) {
			metaMonitoringService = createAsyncService()
			AspectWeaver::weave
			alreadyInitialized = true
		}
	}

	def static private createAsyncService() {
		val MetaMonitoringServiceAsync metaMonitoringService = GWT::create(typeof(MetaMonitoringService))
		val endpoint = metaMonitoringService as ServiceDefTarget
		val moduleRelativeURL = GWT::getModuleBaseURL() + "metamonitoring"
		endpoint.serviceEntryPoint = moduleRelativeURL
		return metaMonitoringService
	}

	def static void sendRecordBundle(String bundle) {
		metaMonitoringService.sendRecordBundle(bundle, new VoidCallback())
	}
}
