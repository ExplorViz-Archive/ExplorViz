package explorviz.shared.model

import explorviz.shared.model.helper.DrawNodeEntity
import java.util.ArrayList
import java.util.List

class Node extends DrawNodeEntity {
	@Property String ipAddress

	@Property double cpuUtilization
	@Property long freeRAM
	@Property long usedRAM

	@Property List<Application> applications = new ArrayList<Application>

	@Property var boolean visible = true

	@Property NodeGroup parent

	public def String getDisplayName() {
		if (this.parent.opened) {
			if (this.ipAddress != null && !this.ipAddress.empty && !this.ipAddress.startsWith("<")) {
				this.ipAddress
			} else {
				this.name
			}
		} else {
			this.parent.name
		}
	}

	override void destroy() {
		applications.forEach[it.destroy()]
		super.destroy()
	}
}
