package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable
import java.util.Set
import java.util.HashSet
import org.eclipse.xtend.lib.annotations.Accessors

class RuntimeInformation implements IsSerializable {
	@Accessors int calledTimes
	@Accessors float overallTraceDuration
	@Accessors int requests
	@Accessors float averageResponseTime
	
	@Accessors Set<Integer> orderIndexes = new HashSet<Integer> 
}