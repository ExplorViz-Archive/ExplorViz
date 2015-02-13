package explorviz.shared.model

import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.plugin_client.capacitymanagement.execution.SyncObject
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.renderer.ColorDefinitions
import java.util.ArrayList
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors

class NodeGroup extends DrawNodeEntity implements SyncObject{
	@Accessors List<Node> nodes = new ArrayList<Node>
	@Accessors List<ScalingGroup> scalinggroups = new ArrayList<ScalingGroup>
	@Accessors System parent
	
	@Accessors var boolean visible = true
	
	public static val Vector4f plusColor = ColorDefinitions::nodeGroupPlusColor
	public static val Vector4f backgroundColor = ColorDefinitions::nodeGroupBackgroundColor
	
		var boolean opened
		
	/** New attributes since control-center */
	var boolean isLockedUntilExecutionActionFinished = false;
	var int hostnameCounter = 1;
	

	
	def boolean isOpened() {
	    opened
	}
	
	def void setOpened(boolean openedParam) {
	    if (openedParam) {
	       setAllChildrenVisibility(true)
	    } else {
	       setAllChildrenVisibility(false)
	       if (nodes.size() > 0) {
	          val firstNode = nodes.get(0)
    	      firstNode.visible = true
	       }
	    }
	    
	    this.opened = openedParam
	}
	
	def setAllChildrenVisibility(boolean visiblity) {
        nodes.forEach [
    	   it.visible = visiblity
    	]
    }
    
	override void destroy() {
		nodes.forEach [it.destroy()]
	    super.destroy()
	}
	
	/** New functionnalities since control-center: */
	
	def int getNodeCount(){
		return nodes.size();
	}
	
	def synchronized void  removeScalingGroup(String name) {
		 
			scalinggroups.remove(getScalingGroupByName(name));
		
	}
	
	def synchronized ScalingGroup getScalingGroupByName(String group) {
		
			for ( ScalingGroup scalingGroup : scalinggroups) {
				if (scalingGroup.getName().equalsIgnoreCase(group)) {
					return scalingGroup;
				}
		
			return null;
		}
	}
	
	def synchronized  void addScalingGroup (String groupname,  String applicationFolder,
			 String startApplicationScript, String terminateApplicationScript, int waitTimeForApplicationStartInMillis,
			 String loadReceiver, String dynamicScalingGroup, boolean enabled, NodeGroup parent) {
		
			scalinggroups.add(new ScalingGroup(groupname, applicationFolder, startApplicationScript, terminateApplicationScript,
					waitTimeForApplicationStartInMillis, 
					loadReceiver, dynamicScalingGroup, enabled, parent));
		
	}
	
	def synchronized void addScalingGroup(ScalingGroup group){
		scalinggroups.add(group);
	}
	
	override boolean isLockedUntilExecutionActionFinished(){
		return isLockedUntilExecutionActionFinished;
	}
	
	
	override void setLockedUntilExecutionActionFinished(boolean locked){
		isLockedUntilExecutionActionFinished = locked;
	}
	
	def synchronized void addNode(Node node){
		nodes.add(node);
	}
	
	def synchronized void removeNode(String ip){
		for(Node n: nodes){
			if(n.ipAddress.equals(ip)){
				nodes.remove(n);
				n.destroy();
				return;
			}
		}
	}
	
	def String generateNewUniqueHostname() {
		return  getName() +String.valueOf(hostnameCounter++);
	}


	
}