Slider = function(label, formHeight) {
	var self = this;

	var questionPointer = -1;

	var expSlider = document.createElement('div');
	expSlider.id = "expSlider";

	var expSliderLabel = document.createElement('div');
	expSliderLabel.id = "expSliderLabel";

	var labelFont = document.createElement('h4');
	labelFont.className = "expRotate";
	labelFont.innerHTML = label;

	var expSliderInnerContainer = document.createElement('div');
	expSliderInnerContainer.id = "expSliderInnerContainer";

	var expSliderSelect = document.createElement('div');
	expSliderSelect.id = "expSliderSelect";

	var expSliderForm = document.createElement('div');
	expSliderForm.id = "expSliderForm";
	expSliderForm.className = "expScrollableDiv";

	var expSliderButton = document.createElement('div');
	expSliderButton.id = "expSliderButton";

	expSliderInnerContainer.style.height = formHeight + 'px';
	expSliderForm.style.maxHeight = (formHeight - 100) + 'px';
	expSlider.style.right = -315 + 'px';

	expSliderInnerContainer.appendChild(expSliderSelect);
	expSliderInnerContainer.appendChild(expSliderForm);
	expSliderInnerContainer.appendChild(expSliderButton);

	expSliderLabel.appendChild(labelFont);

	expSlider.appendChild(expSliderLabel);
	expSlider.appendChild(expSliderInnerContainer);

	document.getElementById('view').appendChild(expSlider);

	// Setup toggle mechanism
	var toggle = [ slideOut, slideIn ], c = 0;

	expSliderLabel.addEventListener('click', function(e) {
		e.preventDefault();
		toggle[c++ % 2]();
	});

	function slideOut() {
		var right = -315;

		function slideOutFrame() {
			right += 5;
			expSlider.style.right = right + 'px';
			if (right == 0)
				clearInterval(id);
		}
		var id = setInterval(slideOutFrame, 7);
	}

	function slideIn() {
		var right = 0;

		function slideInFrame() {
			right -= 5;
			expSlider.style.right = right + 'px';
			if (right == -315)
				clearInterval(id);
		}
		var id = setInterval(slideInFrame, 7);
	}

	// Setup welcome form
	var welcomeText = "Ich bin der Geist, der stets verneint!<br>"
			+ "Und das mit Recht; denn alles, was entsteht,<br>"
			+ "Ist wert, dass es zugrunde geht;<br>"
			+ "Drum besser waers, dass nichts entstuende.<br>"
			+ "So ist denn alles, was ihr Suende,<br>"
			+ "Zerstoerung, kurz, das Boese nennt,<br>"
			+ "Mein eigentliches Element.<br>";

	expSliderForm.innerHTML = welcomeText;

	// setup buttons
	var saveButton = document.createElement('button');
	saveButton.id = "expSaveBtn";
	saveButton.innerHTML = "Next &gt;&gt;";

	var backButton = document.createElement('button');
	backButton.id = "expBackBtn";
	backButton.innerHTML = "&lt;&lt; Back";

	expSliderButton.appendChild(saveButton);
	expSliderButton.appendChild(backButton);

	saveButton.addEventListener('click', function() {
		saveQuestion();
	});

	// setup question type select
	var qtType = document.createElement('select');
	qtType.id = "qtType";
	qtType.name = "qtType";

	var opt1 = document.createElement('option');
	opt1.value = 1;
	opt1.innerHTML = "Free text";
	qtType.appendChild(opt1);

	var opt2 = document.createElement('option');
	opt2.value = 2;
	opt2.innerHTML = "Multiple-choice";
	qtType.appendChild(opt2);

	expSliderSelect.appendChild(qtType);

	expSliderSelect.style.visibility = "hidden";

	function saveQuestion() {
		var formCompleted = true;

		if (questionPointer >= 0) {
			J
			var jsonString = JSON.parse(expQuestionForm);
			// formCompleted =
			// @explorviz.visualization.experiment.NewExperiment::saveToServer(Lexplorviz/visualization/experiment/NewExperimentJS$ExplorVizJSArray;)(form);
		}

		if (formCompleted) {
			questionPointer++;
			expSliderSelect.selectedIndex = "1";
			expSliderSelect.style.visibility = "visible";
			createQuestForm(1, 1);
		} else {
			alert("Please fill out all values. You need at least one answer.");
		}
	}

	function createQuestForm(index, countOfAnswers) {
		expSliderForm.innerHTML = "";

		var form = document.createElement('form');
		form.id = "expQuestionForm";

		var questionLabel = document.createElement('label');
		questionLabel.innerHTML = "Question "
				+ (questionPointer + 1).toString();
		form.appendChild(questionLabel);
		form.appendChild(document.createElement("br"));

		var questionTextLabel = document.createElement('label');
		questionTextLabel.innerHTML = "Question Text:"
		form.appendChild(questionTextLabel);
		form.appendChild(document.createElement("br"));

		var questionText = document.createElement('textarea');
		questionText.className = "expTextArea";
		questionText.id = "inputQType";
		questionText.name = "inputQType";
		questionText.cols = "35";
		questionText.rows = "4";
		form.appendChild(questionText);
		form.appendChild(document.createElement("br"));

		var workingTimeLabel = document.createElement('label');
		workingTimeLabel.innerHTML = "Working time in minutes:";
		form.appendChild(workingTimeLabel);
		form.appendChild(document.createElement("br"));

		var workingTime = document.createElement('input');
		workingTime.type = "number";
		workingTime.min = "1";
		workingTime.max = "10";
		workingTime.step = "1";
		workingTime.value = "4";
		workingTime.size = "2";
		form.appendChild(workingTime);
		form.appendChild(document.createElement("br"));

		var freeAnswersLabel = document.createElement('label');
		freeAnswersLabel.innerHTML = "Free answers:";
		form.appendChild(freeAnswersLabel);
		form.appendChild(document.createElement("br"));

		var freeAnswers = document.createElement('input');
		freeAnswers.type = "number";
		freeAnswers.min = "1";
		freeAnswers.max = "10";
		freeAnswers.step = "1";
		freeAnswers.value = "4";
		freeAnswers.size = "2";
		form.appendChild(freeAnswers);
		form.appendChild(document.createElement("br"));

		var answerLabel = document.createElement('label');

		if (index == 1) {
			answerLabel.innerHTML = "Correct answers:";
		} else if (index == 2) {
			answerLabel.innerHTML = "Possible answers:";
		}

		form.appendChild(answerLabel);
		form.appendChild(document.createElement("br"));

		var answersDiv = document.createElement('div');
		answersDiv.id = "answers";

		for (var i = 0; i < countOfAnswers; i++) {
			var answerDiv = document.createElement('div');
			answerDiv.id = "answer" + i;

			var answerInput = document.createElement('input');
			answerInput.id = "correctAnswer" + i;
			answerInput.name = "correctAnswer" + i;

			answerDiv.appendChild(answerInput);
			answersDiv.appendChild(answerDiv);
		}

		form.appendChild(answersDiv);

		expSliderForm.appendChild(form);

		setupAnswerHandler(0);
	}

	function setupAnswerHandler(index) {
		var inputID = "correctAnswer0";

		document.getElementById(inputID).addEventListener("keyup", handler);
		
		function handler() {
			console.log("test");
			document.getElementById(inputID).removeEventListener("keyup", handler);
		}

		// $wnd
		// .jQuery(inputID)
		// .on(
		// "keyup change",
		// function() {
		// $wnd.jQuery(inputID).off("keyup change");
		// @explorviz.visualization.experiment.NewExperiment::numOfCorrectAnswers
		// += 1;
		// var i =
		// @explorviz.visualization.experiment.NewExperiment::numOfCorrectAnswers;
		//
		// var inputDiv = $doc.createElement("div");
		// inputDiv.id = "answer" + i;
		// inputDiv.name = "answer" + i;
		//
		// var inputText = $doc.createElement("input");
		// inputText.id = "correctAnswer" + i;
		// inputText.name = "correctAnswer" + i;
		//
		// $wnd.jQuery("#answers").append("<br>");
		// inputDiv.appendChild(inputText);
		//
		// if ($wnd.jQuery("#qtType").val() == "2") {
		// var inputBox = $doc.createElement("input");
		// inputBox.type = "checkbox";
		// inputBox.id = "correctAnswerCheckbox" + i;
		// inputBox.name = "correctAnswerCheckbox" + i;
		// inputBox.style.marginLeft = "4px";
		// inputDiv.appendChild(inputBox);
		// }
		//
		// $wnd.jQuery("#answers").append(inputDiv);
		//
		// @explorviz.visualization.experiment.NewExperimentJS::setupAnswerHandler(I)(i);
		// });
	}

}