package explorviz.shared.model

import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.plugin_client.capacitymanagement.execution.SyncObject
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.renderer.ColorDefinitions
import java.util.ArrayList
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.Collections

class NodeGroup extends DrawNodeEntity implements SyncObject{
	@Accessors List<Node> nodes = new ArrayList<Node>

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
	
	def void setStartAndEndIpRangeAsName() {
		val ipAddresses = getAllIpAddresses
		Collections.sort(ipAddresses)
		if (ipAddresses.size() >= 2) {
			name = ipAddresses.get(0) + " - " + ipAddresses.get(ipAddresses.size() - 1)
			return
		} else if (ipAddresses.size() == 1) {
			name = ipAddresses.get(0)
		} else {
			name =  "none"
		}

	}	
	
	private def List<String> getAllIpAddresses() {
		val ipAddresses = new ArrayList<String>()
		for (node : nodes) {
			ipAddresses.add(node.getIpAddress())
		}
		ipAddresses;
	}	
	
	def setAllChildrenVisibility(boolean visiblity) {
        for (node : nodes)
    	   node.visible = visiblity
    }
    
	override void destroy() {
	   for (node : nodes)
		   node.destroy
		   
	    super.destroy()
	}
	
	/** New functionnalities since control-center: */
	
	def int getNodeCount(){
		return nodes.size();
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