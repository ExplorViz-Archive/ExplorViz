package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable
import java.util.Set
import java.util.HashSet

class RuntimeInformation implements IsSerializable {
	@Property int calledTimes
	@Property float overallTraceDuration
	@Property int requests
	@Property float averageResponseTime
	
	@Property Set<Integer> orderIndexes = new HashSet<Integer> 
}