/**
 * function object to add essential functionality to a modal holding a video player, the screen Recording of a user during an experiment
 * and overlays a canvas to draw a transparent red circle where the users eyes looked, if there are eye tracking data.
 * 
 * @param withEyeTrackingOverlay, boolean to check whether eyeTracking data is there
 * @param eyeTrackingData, JSON String holding eye tracking data
 */
startReplayModeJS = function(withEyeTrackingOverlay, eyeTrackingData){

	var video = document.getElementById("screenRecordVideoplayer");
	var canvas = document.getElementById('eyeTrackingReplayCanvas');
	var context = canvas.getContext('2d');
	var seekBar = document.getElementById('seek-bar');
	var loadedReplay = null;
	var gazeCopy = null;
	var lastGaze = null;
	if(withEyeTrackingOverlay) {
		loadedReplay = JSON.parse(eyeTrackingData);
		gazeCopy = loadedReplay.eyeData.slice();
		lastGaze = gazeCopy[0];
	}
	var overlay;
	var calOverlay;
	var isEyeTrackingOverlay = false;
	
	var drawNoCircle = false;
	var i = 0;

	$("#eyeTrackingReplayCanvas").hide();
	
	//hardcoded ids of play/pause button and showing eye tracking or not
	document.getElementById('play-pause').addEventListener('click', playPause, false);
	document.getElementById('eyeTrackingData').addEventListener('click', playPauseEyeTracking, false);

	document.getElementById('seek-bar').addEventListener("change", function() {
		var time = video.duration * (seekBar.value / 100);
		video.currentTime = time;
		
		if(withEyeTrackingOverlay) {
			gazeCopy = loadedReplay.eyeData.slice();
		}
	});

	//workaround of chrome bug with no duration
	video.currentTime = 1800;

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
		if(withEyeTrackingOverlay){
			draw(this,context);
		}
	},false);
	
	video.addEventListener('ended', function(){
		//reset controls
		if(withEyeTrackingOverlay) {
			gazeCopy = loadedReplay.eyeData.slice();
		}
		var playButton = document.getElementById("play-pause");
		playButton.innerHTML = '<span class="glyphicon glyphicon-play"></span>';
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
				
			//resolution of video
			var width = video.width;
			var height = video.height;
			
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
			if(!drawNoCircle) {
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
 * @param userID id of the user for saving data on server with different filenames
 * @param questionnairePrefix String to save the data in the correct spot on the server
 * @param save is a callback function to save the eyeTracking data with an RPC call to the server
 */
EyeTrackScreenRecordExperiment = function(eyeTracking, screenRecord, userID, questionnairePrefix, save) {
	var recorded_Data = [];
	var videoData;
	var started = false;
	var recordRTC;

	var currenCallibrationPoint;
	var callibrationPoints;

	var videoStartTime;
	
	initExperimentData();
	
	//starts recording of eyetracking data and screen recording, as well as an event handler for stopping experiment
	function initExperimentData(){
		//init variables and start experiment
		if(started)
			return;
		started = true;		
		recorded_Data = [];
		videoData = null;
		if(screenRecord) {
			startScreenRecord();
		}
		if(eyeTracking) {
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
	
	//stops recording of eyeTracking data and screen Recording
	function stopExperiment(){
		if(screenRecord) {
			stopScreenRecord();
		}
		if(eyeTracking) {
			stopEyetracker();
		}
	}

	//uploads screen Recording to hardcoded server (pathToServide) and calls function to upload eyetracking data as well
	//handles also that the window should not be closed and user logged out until upload is finished (by triggering an event for another function object)  
	function stopScreenRecord() {
		var pathToService = "/explorviz/uploadfileservice";
		
		if(recordRTC != null) {
			recordRTC.stopRecording(function() {
				if(eyeTracking){
					uploadEyeTrackingDataWithCallback();
				}
					
				var blob = recordRTC.getBlob();

			    var file = new File([blob], questionnairePrefix + "_"  + userID +'.mp4', {
			        type: 'video/mp4'
			    });
			    
			    var formData = new FormData();
			    formData.append('uploadFormElement', file);
			    
			    //stop closing of window
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
						//in case of error during upload, save screen recording local in downloads-folder
						recordRTC.save(questionnairePrefix + "_"  + userID +'.mp4');
						console.log(response);
						tryToFinish(false);
						permitClosingWindow();
					}
				});
			    
			    //name says all
			    function permitClosingWindow() {
			    	window.beforeunload = null;
			    }
			    
			    //trigger event for another function object: upload is finished (in success or failure)
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
		} else {
			console.log("Error: screen was never recorded.")
		}
	};

	//start the screen recording
	function startScreenRecord() {
		getScreenId(function (error, sourceId, screen_constraints) {
			if(error == "not-installed") {
				console.log("Error: not correct chrome extensions installed");
			}
			console.log(screen_constraints.video);
			navigator.webkitGetUserMedia(screen_constraints, (function (stream) {
				recordRTC = RecordRTC(stream, {
					type: 'video',
					mimeType: 'video/mp4',
					width: screen.width,
					height: screen.height,
					frameInterval: 5	//default is 10, set minimum interval (in milliseconds) between each time we push a frame to the videorecorder
				});

				//set function calls for chrome extension and js-libs
				recordRTC.initRecorder();
				recordRTC.startRecording();
				videoStartTime = new Date();
			}), //onFailure 
			(function (e) {
				console.log(e);
			}));
		});

	}
	
	//upload eyetracking data to server with callback from of a GWT function
	function uploadEyeTrackingDataWithCallback() {
		if(eyeTracking){
		  var data = {'videostart' : videoStartTime.getTime(),
					  'eyeData': recorded_Data,
					  'height' : screen.height,
					  'width' : screen.width};

		  var json = JSON.stringify(data);
		  //GWT function callback
		  save(json);
		}
	}
	
	
};

/**
 * helper function to trigger stopExperiment Event from GWT
 */
triggerStopExperiment = function () {
	$.event.trigger({
		type: "stopExperiment"
	});
};

/**
 * trigger questionnaireFinished from inside GWT
 */
triggerQuestionnaireFinished = function () {
	$.event.trigger({
		type: "questionnaireFinished"
	});
};

/**
 * Handling of loose ends before outlogging of user after experiment, questionnaire and upload needs to be finished beforehand
 * Notices per events
 * @param finalFinishCallback callback from GWT for closing experiment dialog and logging user out  
 */
uploadAndQuestionnaireFinished = function(finalFinishCallback) {
	var isQuestionnaireFinished = false;
	var isUploadFinished = false;
	var response;

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
	
	function setQuestionnaireFinished() {
		isQuestionnaireFinished = true;
		tryToFinish();
	}
	
	function setUploadFinished() {
		isUploadFinished = true;
		tryToFinish();
	}
	
	//checking whether questionnaire and upload are finished, handled accordingly when they are not (
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

/**
 * Short between alert before starting the main questions, to postpone starting the time limit of answering the questions
 * @param continueFunction GWT callback function to start the main questions in the questionnaire 
 */
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

