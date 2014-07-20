package explorviz.visualization.interaction

class TraceReplayer {
	
	def static replayInit(long traceId) {
		var tableInformation = "<tbody>"
		
		tableInformation += "<tr><td>Index:</td><td>0</td></tr>"
		tableInformation += "<tr><td>Caller:</td><td>I</td></tr>"
		tableInformation += "<tr><td>Callee:</td><td>U</td></tr>"
		tableInformation += "<tr><td>Method:</td><td>getMe(..)</td></tr>"
		tableInformation += "<tr><td>Requests:</td><td>21</td></tr>"
		tableInformation += "<tr><td>Avg. Responsetime:</td><td>10 msec</td></tr>"
		
		tableInformation += "</tbody>"
		
		TraceReplayerJS::openDialog(traceId.toString(), tableInformation)
	}
	
	def static play() {
		
	}
	
	def static pause() {
		
	}
	
	def static previous() {
		
	}
	
	def static next() {
		
	}
}