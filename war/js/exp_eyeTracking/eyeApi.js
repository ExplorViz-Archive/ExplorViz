var trackerStatus = "disconnected";
var connection;

/**
 * Starts connection to local server which communicates with the eyetracking hardware to get the data
 * 
 */
function startEyetracker(){
	connection = new WebSocket('ws://127.0.0.1:5441');

	connection.onopen = function () {
	  connection.send('{"requestType" : "trackerStatus"}');
	};

	// Log errors
	connection.onerror = function (error) {
	  console.error(error);
	};

	// Log messages from the server
	connection.onmessage = function (e) {
		var resp = JSON.parse(e.data);
		switch(resp.responseType){
			case "trackerStatus":
				if(trackerStatus != resp.status){
					trackerStatus = resp.status;
					$( document ).trigger( "trackerStatusChanged", trackerStatus);
				}
				break;
			case "gazeData":
				trackerStatus = resp.status;
				$( document ).trigger( "newGazeData", {"x" : resp.calX, "y" : resp.calY, "time" : resp.time});
				break;
			default:
				console.log('invalid request type: ' + e.data);
		}
	}
};

/**
 * Stops communication with local server for eye tracking data
 */
function stopEyetracker(){
	if(connection != null){
		connection.close();
		connection = null;
	}
}

/**
 * Gets status of eyeTracker
 */
function getEyetrackerStatus(){
	if(connection == null)
		return "noWebSocketConnection";

	return trackerStatus;
 }

/**
 * Starts calibration of eye tracker (calibration is not in use right now) 
 */
 function startEyetrackerCallibration(){
	 if(connection == null){
		 console.log("no websocket Connection");
		 return;
	 }
	 connection.send('{"requestType" : "startCalibration"}');
 }
 
 /**
  * Calibration point is send to server, to calibrate eye tracker
  * @param point Javascript object with an attribute x and y
  */
 function addEyetrackerCallibrationPoint(point){
	 if(connection == null){
		 console.log("no websocket Connection");
		 return;
	 }
	 connection.send('{"requestType" : "addCalibrationPoint", "calX" : "'
						+point.x +'", "calY" : "'+point.y+'"}');
 }
 
 /**
  * Ends calibration of eye tracker
  */
 function endEyetrackerCallibration(){
	 if(connection == null){
		 console.log("no websocket Connection");
		 return;
	 }
	 connection.send('{"requestType" : "endCalibration"}');
 }
