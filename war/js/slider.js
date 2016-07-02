Slider = function(label, formHeight, callback, landscapeNames, load) {
	var self = this;

	var questionPointer = -1;
	var filledForms = [];

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
	expSliderSelect.style.visibility = "hidden";

	var expSliderForm = document.createElement('div');
	expSliderForm.id = "expSliderForm";
	expSliderForm.className = "expScrollableDiv";

	var expSliderButton = document.createElement('div');
	expSliderButton.id = "expSliderButton";

	expSliderInnerContainer.style.height = formHeight + 'px';
	expSliderForm.style.maxHeight = (formHeight - 70) + 'px';
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

	expSliderButton.appendChild(backButton);
	expSliderButton.appendChild(saveButton);

	saveButton.addEventListener('click', function() {
		showNextForm();
		loadExplorViz()
	});

	backButton.addEventListener('click', function() {
		showPreviousForm();
		loadExplorViz()
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
	expSliderSelect.appendChild(document.createElement("br"));

	// setup landscape select
	var qtLandscape = document.createElement('select');
	qtLandscape.id = "expLandscape";
	qtLandscape.name = "expLandscape";

	var lengthN = landscapeNames.length;

	for (var i = 0; i < lengthN; i++) {
		var option = document.createElement('option');
		option.value = i;
		option.innerHTML = landscapeNames[i];
		option.innerHTML = landscapeNames[i];
		qtLandscape.appendChild(option);
	}

	expSliderSelect.appendChild(qtLandscape);

	// Listeners
	qtLandscape.onchange = function() {
		load(this.options[this.selectedIndex].innerHTML);
	}

	// Functions

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
		questionText.id = "questionText";
		questionText.name = "questionText";
		questionText.cols = "35";
		questionText.rows = "4";
		form.appendChild(questionText);
		form.appendChild(document.createElement("br"));

		var workingTimeLabel = document.createElement('label');
		workingTimeLabel.innerHTML = "Working time in minutes:";
		form.appendChild(workingTimeLabel);
		form.appendChild(document.createElement("br"));

		var workingTime = document.createElement('input');
		workingTime.id = "workingTime";
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
		freeAnswers.id = "freeAnswers";
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
			answersDiv.appendChild(document.createElement("br"));
		}

		form.appendChild(answersDiv);

		expSliderForm.appendChild(form);

		setupAnswerHandler(countOfAnswers - 1);
	}

	function setupAnswerHandler(index) {
		var inputID = "correctAnswer" + index.toString();

		document.getElementById(inputID).addEventListener("keyup", handler);

		function handler() {
			document.getElementById(inputID).removeEventListener("keyup",
					handler);

			var answerDiv = document.createElement('div');
			answerDiv.id = "answer" + (index + 1).toString();

			var answerInput = document.createElement('input');
			answerInput.id = "correctAnswer" + (index + 1).toString();
			answerInput.name = "correctAnswer" + (index + 1).toString();

			answerDiv.appendChild(answerInput);
			document.getElementById("answers").appendChild(answerDiv);
			document.getElementById("answers").appendChild(
					document.createElement("br"));

			setupAnswerHandler(index + 1);
		}
	}

	function showPreviousForm() {

		// save current form
		var jsonFORM = formValuesToJSON(expQuestionForm);
		filledForms[questionPointer] = jsonFORM;

		if (questionPointer > 0) {
			questionPointer--;
			createFormForJSON();

		}
	}

	function showNextForm() {
		var formCompleted = true;

		if (questionPointer >= 0) {
			formCompleted = isFormCompleted(expQuestionForm);
			if (formCompleted) {
				var jsonFORM = formValuesToJSON(expQuestionForm);
				filledForms[questionPointer] = jsonFORM;
				callback(JSON.stringify(filledForms[questionPointer]));
			}
		}

		if (formCompleted) {
			questionPointer++;
			expSliderSelect.selectedIndex = "1";
			expSliderSelect.style.visibility = "visible";
			if (filledForms[questionPointer] != undefined) {
				createFormForJSON();
			} else {
				createQuestForm(1, 1);
			}
		} else {
			alert("Please fill out all values. You need at least one answer.");
		}
	}

	var isFormCompleted = function(form) {

		var elements = form.elements;

		// check if at least one answer is set
		var answerInputs = Array.prototype.slice.call(document.getElementById(
				"answers").querySelectorAll('[id^=correctAnswer]'));

		var atLeastOneAnswer = answerInputs.filter(function(answer) {
			if (answer.value != "")
				return true;
		}).length > 0 ? true : false;

		// check if inputs before answers are all filled
		var upperBound = elements.length - answerInputs.length;

		for (var i = 0; i < upperBound; i++) {
			if (elements[i].value == "") {
				return false;
			}
		}
		return atLeastOneAnswer;
	}

	var createProperty = function(obj, key, value) {
		var config = {
			value : value,
			writable : true,
			enumerable : true,
			configurable : true
		};
		Object.defineProperty(obj, key, config);
	};

	function formValuesToJSON(form) {
		var obj = {};

		var elements = form.elements;
		var length = elements.length - 1;

		var answersContainer = {};
		var correctAnswers = [];

		// add ExplorViz landscape identifier
		createProperty(obj, "expLandscape",
				qtLandscape.options[qtLandscape.selectedIndex].innerHTML);

		// rename answer ids due to possible empty inputs
		// and create json
		for (var i = 0; i < length; i++) {
			if (elements[i].value != "") {
				if (elements[i].id.indexOf("correctAnswer") == 0) {
					if (correctAnswers.length == 0) {
						createProperty(obj, "correctAnswers", answersContainer);
						createProperty(answersContainer, "correctAnswer",
								correctAnswers);
					}
					correctAnswers.push(elements[i].value);
				} else {
					createProperty(obj, elements[i].id.toString(),
							elements[i].value);
				}
			}
		}

		if (correctAnswers.length == 0) {
			createProperty(obj, "correctAnswers", answersContainer);
			createProperty(answersContainer, "correctAnswer", correctAnswers);
			correctAnswers.push("");
		}

		return obj;
	}

	function createFormForJSON() {
		var previousForm = filledForms[questionPointer];

		var needeAnswerInputs = previousForm["correctAnswers"]["correctAnswer"].length;

		// needed for possible empty answer in current question when going back
		// to previous question
		if (previousForm["correctAnswers"]["correctAnswer"][0] == "")
			needeAnswerInputs = 0;

		createQuestForm(1, needeAnswerInputs + 1);

		var answercounter = 0;

		for ( var key in previousForm) {
			if (key == "correctAnswers") {
				var correctAnswers = previousForm[key]["correctAnswer"];

				for (var i = 0; i < needeAnswerInputs; i++) {
					document.getElementById("correctAnswer"
							+ (answercounter % needeAnswerInputs)).value = correctAnswers[i];
					answercounter++;
				}

			} else if (key == "expLandscape") {
				var length = qtLandscape.options.length;

				for (var i = 0; i < length; i++) {
					if (qtLandscape.options[i].text == previousForm[key]) {
						qtLandscape.selectedIndex = i;
						break;
					}
				}
			} else {
				document.getElementById(key).value = previousForm[key];
			}
		}
	}
	
	function loadExplorViz(){
		load(qtLandscape.options[qtLandscape.selectedIndex].innerHTML);
	}
}