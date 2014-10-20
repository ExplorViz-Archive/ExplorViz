package explorviz.shared.experiment

import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors

/**
 * @author Santje Finke
 * 
 */
class Step implements IsSerializable {
	@Accessors String source = ""
	@Accessors String dest = ""
	@Accessors boolean doubleClick = false
	@Accessors boolean rightClick = false
	@Accessors boolean leftClick = false
	@Accessors boolean hover = false
	@Accessors boolean connection = false
	@Accessors boolean backToLandscape = false
	@Accessors boolean timeshift = false
	@Accessors boolean requiresButton = false
	@Accessors boolean codeview = false
	@Accessors boolean choosetrace = false
	@Accessors boolean startanalysis = false
	@Accessors boolean pauseanalysis = false
	@Accessors boolean nextanalysis = false
	@Accessors boolean leaveanalysis = false
	
	/**
	 * @param source the name of the component the connection starts at
	 * @param dest name of the component the connection ends at
	 * @param rightClick to complete the step, right click the communication
	 * @param leftClick to complete the step, left click the communication
	 * @param hover to complete the step, hover over the communication
	 */
	new(String source, String dest, boolean hover, boolean leftClick){
		this.source = source
		this.dest = dest
		this.rightClick = rightClick
		this.leftClick = leftClick
		this.hover = hover
		this.connection = true
	}
	
		/**
	 * @param source name of the component
	 * @param doubleClick to complete the step, doubleclick the component
	 * @param rightClick to complete the step, rightClick the component
	 * @param leftClick to complete the step, leftClick the component
	 * @param hover to complete the step, hover over the component
	 */
	new(String source, boolean doubleClick, boolean rightClick, boolean leftClick, boolean hover){
		this.source = source
		this.doubleClick = doubleClick
		this.rightClick = rightClick
		this.leftClick = leftClick
		this.hover = hover
	}
	
	/**
	 * creates a tutorial step that doesn't react to interaction but provides a "continue" button
	 * pointing on a communication
	 * @param source name of the component the communication starts at
	 * @param dest name of the component the communication ends at
	 */
	new(String source, String dest){
		this.source = source
		this.dest = dest
		this.connection = true
		this.requiresButton = true
	}
	
	/**
	 * creates a tutorial step that either reacts to an action defined by a keyword,
	 * or doesn't react to an action and instead provides a "next" button. This depends on the 
	 * given argument
	 * @param keyword can be either a keyword to demand an action on a dialog or
	 * be the name of a component. If it isn't a keyword, an arrow will point at the given
	 * component and a next-button will be shown
	 * Possible keywords are: codeview, choosetrace, startanalysis, pauseanalysis, nextanalysis,
	 * landscape, timeshift, leaveanalysis
	 */	
	new(String keyword){
		if(keyword.equals("codeview")){
			codeview = true
		}else if(keyword.equals("choosetrace")){
			choosetrace = true
		}else if(keyword.equals("startanalysis")){
			startanalysis = true
		}else if(keyword.equals("pauseanalysis")){
			pauseanalysis = true
		}else if(keyword.equals("nextanalysis")){
			nextanalysis = true
		}else if(keyword.equals("leaveanalysis")){
			leaveanalysis = true
		}else if(keyword.equals("landscape")){
			backToLandscape = true
		}else if(keyword.equals("timeshift")){
			timeshift = true
		}else{
			this.source = source
			this.requiresButton = true
		}
	}

	new(){
		backToLandscape = false
	}
		
}