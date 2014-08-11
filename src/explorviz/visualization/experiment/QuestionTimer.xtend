package explorviz.visualization.experiment

import com.google.gwt.user.client.Timer

class QuestionTimer extends Timer {
	long interval = 480000 //default time in ms to turn timer red (8min)
	long startTime = 0
	int minutes = 8
	String red = "class='question-timer-warning'"
			
	new(int time){
		interval = time*60*1000 //convert min to ms
		minutes = time
	}
	
	def setTime(long start){
		startTime = start
	}
	
	override run(){
		//change label
		var long time = System.currentTimeMillis
		var long sec = (time - startTime)/1000 //time used in seconds
		var long min = sec/60 //whole minutes that have passed
		sec = sec - min*60 //decrease second amount by minutes
		var timeLabel = min+":"
		if(sec<10){
			timeLabel = timeLabel+"0"+sec
		}else{
			timeLabel = timeLabel + sec
		}
		var String label
		if(time < startTime+interval){
			label = "<p> Elapsed time: <b>"+timeLabel+"</b> (of "+minutes+" minutes)</p>" 
		}else{ //over time limit
			label = "<p> Elapsed time: <span "+red+">"+timeLabel+" (overtime)</span></p>"
		}
		ExperimentJS.setTimer(label)
	}
	
	def setMaxTime(int time) {
		interval = time*60*1000 //convert min to ms
		minutes = time
	}
	
}