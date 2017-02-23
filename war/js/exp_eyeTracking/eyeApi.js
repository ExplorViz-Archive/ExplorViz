var trackerStatus = "disconnected";
var connection;

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


function stopEyetracker(){
	if(connection != null){
		connection.close();
		connection = null;
	}
}
function getEyetrackerStatus(){
	if(connection == null)
		return "noWebSocketConnection";

	return trackerStatus;
 }

 function startEyetrackerCallibration(){
	 if(connection == null){
		 console.log("no websocket Connection");
		 return;
	 }
	 connection.send('{"requestType" : "startCalibration"}');
 }
 function addEyetrackerCallibrationPoint(point){
	 if(connection == null){
		 console.log("no websocket Connection");
		 return;
	 }
	 connection.send('{"requestType" : "addCalibrationPoint", "calX" : "'
						+point.x +'", "calY" : "'+point.y+'"}');
 }
 function endEyetrackerCallibration(){
	 if(connection == null){
		 console.log("no websocket Connection");
		 return;
	 }
	 connection.send('{"requestType" : "endCalibration"}');
 }
