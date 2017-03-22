startReplayModeJS = function(withEyeTrackingOverlay, eyeTrackingData){

	var video = document.getElementById("screenRecordVideoplayer");
	var canvas = document.getElementById('eyeTrackingReplayCanvas');
	var context = canvas.getContext('2d');
	var seekBar = document.getElementById('seek-bar');
	var loadedReplay = JSON.parse(eyeTrackingData);
	var gazeCopy = loadedReplay.eyeData.slice();
	var overlay;
	var calOverlay;
	var isEyeTrackingOverlay = false;
	var lastGaze = gazeCopy[0];
	var drawNoCircle = false;
	var i = 0;

	$("#eyeTrackingReplayCanvas").hide();
	
	document.getElementById('play-pause').addEventListener('click', playPause, false);
	document.getElementById('eyeTrackingData').addEventListener('click', playPauseEyeTracking, false);

	document.getElementById('seek-bar').addEventListener("change", function() {
		var time = video.duration * (seekBar.value / 100);
		video.currentTime = time;
		gazeCopy = loadedReplay.eyeData.slice();
	});


	video.addEventListener("timeupdate", function() {
		//due to streaming, the seekbar will only show the true progress after fully loading the videoData
		var videoDuration = video.duration;
		if(!isFinite(videoDuration)) {
			videoDuration = 1800; //default duration to 30 minutes *60 for seconds
		}
		var value = (100 / videoDuration) * video.currentTime;
		seekBar.value = value;
	});


	video.addEventListener('play', function(){
		draw(this,context);
	},false);
	
	video.addEventListener('ended', function(){
		//reset controls
		var playButton = document.getElementById("play-pause");
		playButton.innerHTML = '<span class="glyphicon glyphicon-play"></span>';
		gazeCopy = loadedReplay.eyeData.slice();
	},false);
	
	function playPause(){
		var playButton = document.getElementById("play-pause");
		if (video.paused == true) {
				video.play();
				playButton.innerHTML = '<span class="glyphicon glyphicon-pause"></span>';
		} else {
				video.pause();
				playButton.innerHTML = '<span class="glyphicon glyphicon-play"></span>';
				gazeCopy = loadedReplay.eyeData.slice();
		}
	}
	
	function playPauseEyeTracking() {
		var playButton = document.getElementById("eyeTrackingData");
		if (isEyeTrackingOverlay) {
			isEyeTrackingOverlay = false;
			$("#eyeTrackingReplayCanvas").hide();
			playButton.innerHTML = '<span class="glyphicon glyphicon-eye-close"></span>';
		} else {
			isEyeTrackingOverlay = true;
			$("#eyeTrackingReplayCanvas").show();
			playButton.innerHTML = '<span class="glyphicon glyphicon-eye-open"></span>';
		}
	}
	
	function draw(v,c) {	//we define, that if array entries timestamp differs from each other more than 210 ms, the user looked outside the display
		  if(v.paused || v.ended){
			return true;
		  }
		  	var currentGaze = gazeCopy[0];
		  	if(!drawNoCircle) {
		  		gazeCopy.shift();
		  	} else {
		  		drawNoCircle = false;
		  	}
		  	if(currentGaze == undefined)
				return false;
		  	
		  	var userOutsideDisplay = (gazeCopy[0][2] - currentGaze[2]) >= 210;
			
			//shift to the correct eyeTrackingData that should be replayed 
			//property currentTime of video shows progress of video in seconds
			var videoTime = loadedReplay.videostart + (v.currentTime * 1000); 
			if(currentGaze[2]-videoTime >= 40 && !userOutsideDisplay) {
				gazeCopy = loadedReplay.eyeData.slice();
				currentGaze = gazeCopy.shift();
				console.log("eyeTracking reset " + i);
			}
			
			while(currentGaze[2] < videoTime){
				var nearVideotime = Math.abs(videoTime - currentGaze[2]);
				if((gazeCopy[0][2] - currentGaze[2]) >= 210 && nearVideotime <= 3000) {
					console.log("user " + i + " outside display at " + nearVideotime + " " + gazeCopy[0][2]+ " vs "+ currentGaze[2]);
					drawNoCircle = true;
					break;
				}
				currentGaze = gazeCopy.shift();
				if(currentGaze == undefined)
				return false;
			}
				
			//resolution of screen
			var width = video.width; //1280;
			var height = video.height; //720
			
			//compute the x and y coordinates for the eyeTracking circle
			var x = currentGaze[0];
			var y = currentGaze[1];
			
			//normalize coordinates, and react to special cases at start and end of eyeTracking data
			if(!lastGaze) {
				lastGaze = currentGaze;
			}
			var nextGaze = gazeCopy[1];
			if(!nextGaze) {
				nextGaze = currentGaze;
			}
			x = (lastGaze[0] + x + nextGaze[0]);
			y = (lastGaze[1] + y + nextGaze[1]);
			x=x/3.0;
			y=y/3.0;
			
			//draw inside canvas
			c.clearRect(0, 0, width, height);
			if(drawNoCircle) {
					//TODO make better
			} else {
				c.globalAlpha = 0.5;
				c.beginPath();
				c.arc(x * width, y * height, 30, 0, 2 * Math.PI, false);
				c.fillStyle = 'red';
				c.fill();
			}

			lastGaze = currentGaze;
			i++;
			setTimeout(draw,35,v,c);
	}

};

/**
 * Creates class for handling recording screen and tracking the eye of a user
 * 
 * @param eyeTracking boolean deciding if the eye should be tracked
 * @param screenRecord boolean deciding if the the screen should be recorded
 * @param userID id of the user for saving data on server
 */
EyeTrackScreenRecordExperiment = function(eyeTracking, screenRecord, userID, questionnaire, save) {
	var recorded_Data = [];
	var videoData;
	var started = false;
	var recordRTC;

	var currenCallibrationPoint;
	var callibrationPoints;

	var videoStartTime;
	
	initExperimentData();
	
	function initExperimentData(){
		if(started)
			return;
		started = true;
		recorded_Data = [];
		videoData = null;
		if(screenRecord) {	//TODO change order?
			startScreenRecord();
		}
		if(eyeTracking) {	//TODO change  order?
			startEyetracker();
		}
		$( document ).on(
			"newGazeData",
			function( eventObject, arg1 ) {
			   recorded_Data.push([arg1.x, arg1.y, arg1.time]);
			}
		);
		$( document ).on(
			"stopExperiment",
			function() {
				stopExperiment();
			}
		);
	}
	
	function stopExperiment(){
		if(screenRecord) {
			stopScreenRecord();
		}
		if(eyeTracking) {
			stopEyetracker();
		}
	}
	
	//WebRTC ScreenRecord

	function stopScreenRecord() {
		var pathToService = "/explorviz/uploadfileservice";
		
		if(recordRTC != null) {
			recordRTC.stopRecording(function() {				
				writeExperimentToDisk();
				
				var blob = recordRTC.getBlob();

			    var file = new File([blob], questionnaire + "_"  + userID +'.mp4', {
			        type: 'video/mp4'
			    });
			    
			    var formData = new FormData();
			    formData.append('uploadFormElement', file);
			    
			    window.beforeunload = "Please wait a bit more before closing this window. An upload of your answers to the server is still in progress.";
			    
			    $.ajax({
					type : "POST",
					data : formData,
					url : pathToService,
					processData : false,
					contentType: false,
					success : function(response) {
						console.log(response);
						tryToFinish(true);
						permitClosingWindow();
					},
					error : function(response) {
						recordRTC.save(questionnaire + "_"  + userID +'.mp4');
						console.log(response);
						tryToFinish(false);
						permitClosingWindow();
					}
				});
			    
			    function permitClosingWindow() {
			    	window.beforeunload = null;
			    }
			    
			    function tryToFinish(success) {
			    	if(success) {
			    		$.event.trigger({
				    		type: "uploadFinishedSuccessful"
				    	});
			    	} else {
			    		$.event.trigger({
				    		type: "uploadFinishedFailure"
				    	});
			    	}
			    }

			});
		}
	};

	function startScreenRecord() {
		console.log("startScreenRecord");
		getScreenId(function (error, sourceId, screen_constraints) {
			navigator.webkitGetUserMedia(screen_constraints, (function (stream) {
				recordRTC = RecordRTC(stream, {
					type: 'video', // audio or video or gif or canvas
					mimeType: 'video/mp4',
					width: screen.width, //1920
					height: screen.height, //1080
					frameInterval: 5	//default is 10, set minimum interval (in milliseconds) between each time we push a frame to the videorecorder
				});

				recordRTC.initRecorder();

				recordRTC.startRecording();
				videoStartTime = new Date();
			}), //onFailure 
			(function () {
				console.log("Failure in getScreenId");
			}));
		});

	}
	
	function writeExperimentToDisk(f) {
		if(eyeTracking){
		  var data = {'videostart' : videoStartTime.getTime(),
					  'eyeData': recorded_Data};

		  var json = JSON.stringify(data);
		  
		  save(json);
		}
	}
	
	
};

//helper function to trigger stopExperiment Event
triggerStopExperiment = function () {
	$.event.trigger({
		type: "stopExperiment"
	});
};

triggerQuestionnaireFinished = function () {
	$.event.trigger({
		type: "questionnaireFinished"
	});
};

uploadAndQuestionnaireFinished = function(finalFinishCallback) {
	var isQuestionnaireFinished = false;
	var isUploadFinished = false;
	var response;
	
	function setQuestionnaireFinished() {
		isQuestionnaireFinished = true;
		tryToFinish();
	}
	
	function setUploadFinished() {
		isUploadFinished = true;
		tryToFinish();
	}
	
	$( document ).on(
			"uploadFinishedSuccessful",
			function() {
				response = "Upload successful."
				setUploadFinished();
			}
		);
	
	$( document ).on(
			"uploadFinishedFailure",
			function() {
				response = "Error during upload."
				setUploadFinished();
			}
		);
	
	$( document ).on(
			"questionnaireFinished",
			function() {
				setQuestionnaireFinished();
			}
		);
	
	function tryToFinish() {
		if(isUploadFinished && isQuestionnaireFinished) {
			if("Error during upload." == response) {
				swal({
					  title: "Error",
					  text: "There was a problem with the upload of your answers. Please contact your adviser.",
					  type: "warning",
					  showCancelButton: false,
					  confirmButtonClass: "btn-danger",
					  confirmButtonText: "Okay",
					  closeOnConfirm: true
					},
					function(onConfirm){
						finalFinishCallback();
					});
			} else {
				finalFinishCallback();
			}
			
		} else if(isQuestionnaireFinished) {
			swal({
				title : "Thank you for taking part in this experiment",
				text : "Please wait a little more for your answers to be uploaded to the server. Sorry for the inconvenience.",
				type : "warning",
				showCancelButton : false,
				showLoaderOnConfirm : true,
			}, function(onConfirm) {
				tryToFinish();
				return false;
			}
			);
		}
	}
};

showMainQuestionsStartDialog = function(continueFunction) {
	swal(
			{
				title : "Primary",
				text : "This starts the main questions",
				type : "info",
				closeOnConfirm : true,
			},
			function(isConfirm) {
				if(isConfirm)
					continueFunction();
			});
};

