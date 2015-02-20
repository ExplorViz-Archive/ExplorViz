package explorviz.shared.model

import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.shared.model.helper.ELanguage
import java.util.ArrayList
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.plugin_client.capacitymanagement.execution.SyncObject
import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration

class Application extends DrawNodeEntity implements SyncObject{
	static public int nextId = 0
	@Accessors var int id

	@Accessors var boolean database

	@Accessors var ELanguage programmingLanguage

	@Accessors long lastUsage

	@Accessors Node parent

	 ScalingGroup scalinggroup

	@Accessors var List<Component> components = new ArrayList<Component>

	@Accessors var List<CommunicationClazz> communications = new ArrayList<CommunicationClazz>

	@Accessors val transient List<CommunicationAppAccumulator> communicationsAccumulated = new ArrayList<CommunicationAppAccumulator>

	@Accessors var List<Communication> incomingCommunications = new ArrayList<Communication>
	@Accessors var List<Communication> outgoingCommunications = new ArrayList<Communication>
	
	/** new attributes since control-center */
	@Accessors var boolean lockedUntilExecutionActionFinished = false;
	@Accessors var String pid;
	@Accessors var String startScript;
	@Accessors var int waitTimeForStarting;
	
	@Accessors var double rootCauseRating
	@Accessors var boolean isRankingPositive = true
	@Accessors var double temporaryRating = -1;
	
	new(){
		super()
		id = nextId
		nextId++
	}
	
	
		
	override void destroy() {
		components.forEach[it.destroy()]
		communicationsAccumulated.forEach[it.destroy()]
		super.destroy()
	}

	def void clearAllPrimitiveObjects() {
		components.forEach[it.clearAllPrimitiveObjects()]

	//		communicationsAccumulated.forEach[it.clearAllPrimitiveObjects()] done in extra method
	}

	def void unhighlight() {
		components.forEach[it.unhighlight]
	}

	def void openAllComponents() {
		components.forEach[it.openAllComponents()]
	}
	
	/** new methods since control-center */
	
	override isLockedUntilExecutionActionFinished() {
		return lockedUntilExecutionActionFinished;
	}
	
	override setLockedUntilExecutionActionFinished(boolean locked) {
		lockedUntilExecutionActionFinished = locked;
	}

	def setScalinggroup(ScalingGroup scalinggroup){
		this.scalinggroup = scalinggroup;
		scalinggroup.addApplication(this);
	}
	
	/**
	 * Sets the scalingGroup without informing the load balancer
	 */
	def setDummyScalinggroup(ScalingGroup scalinggroup){
		this.scalinggroup = scalinggroup;
	}
	
	def getScalinggroup(){
		return this.scalinggroup;
	}
	/**
	 * Sets the root cause rating of this element to a failure state.
	 */
	def void setRootCauseRatingToFailure() {
		rootCauseRating = RanCorrConfiguration.RootCauseRatingFailureState;
	}

}
