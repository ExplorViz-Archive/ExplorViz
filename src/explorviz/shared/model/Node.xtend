package explorviz.shared.model

import explorviz.shared.model.helper.DrawNodeEntity
import java.util.ArrayList
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.plugin_client.attributes.IPluginKeys
import explorviz.plugin_client.capacitymanagement.execution.SyncObject

class Node extends DrawNodeEntity implements SyncObject {
	@Accessors String ipAddress
	
	@Accessors double cpuUtilization
	@Accessors long freeRAM
	@Accessors long usedRAM
	
	@Accessors long lastSeenTimestamp
	
	@Accessors List<Application> applications = new ArrayList<Application>
	
	@Accessors var boolean visible = true

	@Accessors NodeGroup parent
	
	@Accessors var boolean LockedUntilExecutionActionFinished = false;
	
	@Accessors String id;
	@Accessors String hostname;
	@Accessors String flavor;
	@Accessors String image;
	
	var int runningApplications = 0;
	
	
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
	
	def addCPUUtilizationHistoryEntry(double entry){
		//Why u no use CapMan?
		
	}
	
	def boolean hasSufficientCPUUilizationHistoryEntries(){
		//TODO for Capacity Planning
		//implement method DONE?
		if (!(this.getGenericData(IPluginKeys::CAPMAN_CPU_UTIL_HISTORY) == null)) {
			return true
		} else return false
	}
	
	def  void removeApplication(int id){
		for(Application n: applications){
			if(n.getId() == id){
				applications.remove(n);
				n.destroy();
				return;
			}
		}
	}
	
	def  void addApplication(Application app){
		applications.add(app);
	}
	
	override void destroy() {
		applications.forEach[it.destroy()]
		super.destroy()
	}	
	
	def int readRunningApplications(){
		return runningApplications;
	}
	
	def void incrementRunningApplications(){
		runningApplications++;
	}
	
		def void decrementRunningApplications(){
		runningApplications--;
	}
}
