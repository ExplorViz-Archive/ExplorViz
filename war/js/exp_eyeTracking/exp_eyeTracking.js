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
	var lastGaze;

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
	
	function draw(v,c) {
		  if(v.paused || v.ended){
			return true;
		  }
			

			var currentGaze = gazeCopy.shift();
			if(currentGaze == undefined)
				return false;
			
			//shift to the correct eyeTrackingData that should be replayed 
			//property currentTime of video shows progress of video in seconds
			var videoTime = loadedReplay.videostart + (v.currentTime * 1000); 
			while(currentGaze[2] < videoTime){	
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
			
			//normalize coordinates
			if(!lastGaze) {
				lastGaze = currentGaze;
			}
			var nextGaze = gazeCopy[1];
			if(!nextGaze) {
				nextGaze = currentGaze;
			}
			x = (lastGaze[0] + x + nextGaze[0])/3;
			y = (lastGaze[1] + x + nextGaze[1])/3;
			
			//draw inside canvas
			c.clearRect(0, 0, width, height);
			c.globalAlpha = 0.5;
			c.beginPath();
			c.arc(x * width, y * height, 30, 0, 2 * Math.PI, false);
			c.fillStyle = 'red';
			c.fill();
			
			lastGaze = currentGaze;
		  setTimeout(draw,20,v,c);
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
	//var n = d.getTime();
	
	initExperimentData();
	
	function initExperimentData(){
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
				console.log("stopExperiment-Listener");
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
		if(recordRTC != null) {
			recordRTC.stopRecording(function(videoURL) {
				//document.querySelector('video').src = videoURL;	//here the video set to the saved video, so it gets replayed

				recordRTC.save(questionnaire + "_"  + userID +'.mp4');	//startTime of video is unique enough
				writeExperimentToDisk();

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
					width: 1920,
					height: 1080,
					//width: 853,
					//height: 480,
					frameInterval: 60

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
		  var data = {'videostart' : videoStartTime.getTime(),
					  'eyeData': recorded_Data};

		  var json = JSON.stringify(data);
		  
		  save(json);
		  
		  /*var downloadLink = document.createElement('a');
		  downloadLink.download = data.videostart + userID + ".txt"; //ist ein extra Attribut der a-Klasse: ZB <a href="/images/myw3schoolsimage.jpg" download="w3logo">
		  // Chrome allows the link to be clicked without actually adding it to the DOM.
		  downloadLink.href = window.URL.createObjectURL(blob);

		  downloadLink.click();*/
	}
	
	
};

//helper function to trigger stopExperiment Event
triggerStopExperiment = function () {
	$.event.trigger({
		type: "stopExperiment"
	});
};

startFileUploadDialogToServerJS = function(showSwalResponse) {
	var uploadForm = "<form id='uploadScreenRecordFile' action='/uploadfileservice' method='post' enctype='multipart/form-data'>"
		+ "<input type='file' id='uploadFormElement'></input></form>";

	swal({
	title : "HTML <small>Title</small>!",
	text : uploadForm,
	html : true,
	type : "info",
	showCancelButton : false,
	closeOnConfirm : false,
	disableButtonsOnConfirm : true,
	showLoaderOnConfirm : true,
	}, function(onConfirm) {
		console.log("inside confirm");
	/*$.ajax({
		type : "POST",
		data : $("#uploadScreenRecordFile").serialize(),
		url : $("#uploadScreenRecordFile").attr("action"),
		success : function(response) {
			showSwalResponse(response);
		},
		error : function(response) {
			showSwalResponse(response);
		}
	});*/
		if(onConfirm) {
			console.log("bfore submit");
			$('uploadScreenRecordFile').submit();
					/*function() {
						console.log("inside submit");
						var formData = new FormData(this);

						$post($("#uploadScreenRecordFile").attr(
								"action"), formData, function(response) {
							showSwalResponse(response);
						});

						return false;
					});*/
		}
	});
		
};

showMainQuestionsStartDialog = function(continueFunction) {
	swal(
			{
				title : "Start Questions",
				text : "This starts the main questions",
				type : "info",
				closeOnConfirm : true,
			},
			function(isConfirm) {
				if(isConfirm)
					continueFunction();
			});
};

