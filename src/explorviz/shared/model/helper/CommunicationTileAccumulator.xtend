package explorviz.shared.model.helper

import explorviz.shared.model.helper.DrawEdgeEntity
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.shared.model.Communication
import java.util.List
import java.util.ArrayList

class CommunicationTileAccumulator extends DrawEdgeEntity {
	@Accessors int requestsCache
	
	@Accessors val transient List<Communication> communications = new ArrayList<Communication>(4)
	
	@Accessors Point startPoint 
	@Accessors Point endPoint 

	override void destroy() {
		super.destroy()
	}
}
