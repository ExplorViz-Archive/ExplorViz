/ *Experimental Slider implementation with jQuery and mustache.js*/

Slider = function(label, formHeight, callback, landscapeNames, loadLandscape,
		existingJSONStringExp, loadExperimentToolsPage) {
	var self = this;

	// retrieve existing experiment
	var existingExp = existingJSONStringExp == null ? null : JSON
			.parse(existingJSONStringExp);
	var expTitle = existingExp == null ? "" : existingExp.title;
	var expPrefix = existingExp == null ? "" : existingExp.prefix;
	var questions = existingExp == null ? [] : existingExp.questions;

	var showExceptionDialog = false;

	var questionPointer = -1;
	var filledForms = {
		"title" : expTitle,
		"prefix" : expPrefix,
		"questions" : questions
	};
	
	var lengthN = landscapeNames.length;


	$.get('slider_template.html', function(template) {
		var rendered = Mustache.render(template);
		$('#view').prepend(rendered);
		$('#expSliderInnerContainer').height(formHeight);
		$('#expSliderForm').css('maxHeight', formHeight - 70);
		$('#expSlider').css('right', -315);
		// $('#expQuestionForm').css('visibility','hidden');

		$('#expSliderLabel').click(function(e) {
			e.preventDefault();
			toggle[c++ % 2]();
		});
		$('#expQuestionForm').hide();
		$('#expSliderSelect').hide();

		// Popover Tooltip with jquery and bootstrap
		$('#expPrefixPopover').popover();

		$('#saveButton').click(function() {
			loadExplorViz();
			showNextForm();
		});

		$('#backButton').click(function() {
			showPreviousForm();
			loadExplorViz();
		});

		$('#exitButton').click(function() {
			loadExperimentToolsPage();
		});

		for (var i = 0; i < lengthN; i++) {
			var option = document.createElement('option');
			option.value = i;
			option.innerHTML = landscapeNames[i];
			$('#expLandscape').append(option);
		}
	});

	// Setup toggle mechanism
	var toggle = [ slideOut, slideIn ], c = 0;

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
	// Functions
	function createNewForm(isMultipleChoice, countOfAnswers) {
		expSliderForm.innerHTML = "";

		var select = document.getElementById('type');

		form = document.createElement('form');
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
		questionText.title = "Insert your question here."
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
		workingTime.title = "Necessary time for solving this question."
		form.appendChild(workingTime);
		form.appendChild(document.createElement("br"));

		var answerLabel = document.createElement('label');

		if (isMultipleChoice) {

			answerLabel.innerHTML = "Possible answers:";
			select.value = "Multiple-Choice";

		}

		else {

			answerLabel.innerHTML = "Correct answers:";
			select.value = "Free text";

		}

		form.appendChild(answerLabel);
		form.appendChild(document.createElement("br"));

		var answersDiv = document.createElement('div');
		answersDiv.id = "answers";

		for (var i = 0; i < countOfAnswers; i++) {
			var answerDiv = document.createElement('div');
			answerDiv.id = "answer" + i;

			var answerInput = document.createElement('input');
			answerInput.id = "answerInput" + i;
			answerInput.name = "answerInput" + i;

			var correctAnswerCheckbox = document.createElement('input');
			correctAnswerCheckbox.type = "checkbox";
			correctAnswerCheckbox.name = "answerCheckbox" + i;
			correctAnswerCheckbox.id = "answerCheckbox" + i;
			correctAnswerCheckbox.title = "Mark this possible answer as correct answer.";

			// if Free text question => hide checkboxes
			if (!isMultipleChoice)
				correctAnswerCheckbox.style.display = 'none';

			answerDiv.appendChild(answerInput);
			answerDiv.appendChild(correctAnswerCheckbox);
			answersDiv.appendChild(answerDiv);
			answersDiv.appendChild(document.createElement("br"));
		}

		form.appendChild(answersDiv);

		expSliderForm.appendChild(form);

		setupAnswerHandler(countOfAnswers - 1);
	}

	function setupAnswerHandler(index) {
		var inputID = "answerInput" + index.toString();

		document.getElementById(inputID).addEventListener("keyup", handler);

		function handler() {

			var select = document.getElementById('type');
			var type = select.value;

			document.getElementById(inputID).removeEventListener("keyup",
					handler);

			var answerDiv = document.createElement('div');
			answerDiv.id = "answer" + (index + 1).toString();

			var answerInput = document.createElement('input');
			answerInput.id = "answerInput" + (index + 1).toString();
			answerInput.name = "answerInput" + (index + 1).toString();

			answerDiv.appendChild(answerInput);

			var correctAnswerCheckbox = document.createElement('input');
			correctAnswerCheckbox.type = "checkbox";
			correctAnswerCheckbox.name = "answerCheckbox"
					+ (index + 1).toString();
			correctAnswerCheckbox.id = "answerCheckbox"
					+ (index + 1).toString();
			correctAnswerCheckbox.title = "Mark this possible answer as correct answer.";

			// if Free text question => hide checkboxes
			if (type == "Free text")
				correctAnswerCheckbox.style.display = 'none';

			answerDiv.appendChild(correctAnswerCheckbox);

			document.getElementById("answers").appendChild(answerDiv);
			document.getElementById("answers").appendChild(
					document.createElement("br"));

			setupAnswerHandler(index + 1);
		}
	}

	function showPreviousForm() {

		// save current form
		var jsonFORM = formValuesToJSON(form);
		filledForms.questions[questionPointer] = jsonFORM;

		if (questionPointer > 0) {
			questionPointer--;
			createFormForJSON();

		}
	}

	function showNextForm() {

		var formCompleted = true;

		// insert title
		if (questionPointer == -1) {
			// special prove for the title form
			formCompleted = questionnaireTitle.value.length > 0
					&& questionnairePrefix.value.length > 0 ? true : false;
			if (formCompleted) {
				filledForms.title = questionnaireTitle.value;
				filledForms.prefix = questionnairePrefix.value;
				sendCompletedData();
			}
		}

		if (questionPointer >= 0) {
			formCompleted = isFormCompleted(form);
			if (formCompleted) {
				var jsonFORM = formValuesToJSON(form);
				filledForms.questions[questionPointer] = jsonFORM;
				sendCompletedData();
			}
		}

		if (formCompleted) {
			questionPointer++;
			expSliderSelect.selectedIndex = "1";
			expSliderSelect.style.visibility = "visible";

			// already filled form
			if (filledForms.questions[questionPointer] != undefined) {
				createFormForJSON();
			}

			// new form
			else {
				createNewForm(false, 1);
			}
		} else {
			alert("Please fill out all values. You need at least one answer.");
			return;
		}

		// if no landscape file is found => hide everything and show notice
		if (showExceptionDialog && formCompleted) {
			expSliderSelect.style.visibility = "hidden";
			expSliderForm.innerHTML = "No landscape files found. Copy files into &#60User&#62/.explorviz/replay and reload page.";
			expSliderForm.style.color = "red";
			expSliderButton.style.visibility = "hidden";
			return;
		}
	}

	function sendCompletedData() {
		// filter for well-formed questions
		var wellFormedQuestions = filledForms.questions.filter(function(elem) {

			var hasAnswer = elem.answers[0] != "";

			var hasText = elem.questionText.length >= 1;
			var hasWorkingTime = elem.workingTime.length >= 1;

			return hasAnswer && hasText && hasWorkingTime;
		});

		var newFilledForms = JSON.parse(JSON.stringify(filledForms));
		newFilledForms.questions = wellFormedQuestions;

		// send to server
		callback(JSON.stringify(newFilledForms));
	}

	var isFormCompleted = function(expQuestionForm) {

		var elements = expQuestionForm.elements;

		// check if at least one answer is set
		var answerInputs = Array.prototype.slice.call(document.getElementById(
				"answers").querySelectorAll('[id^=answerInput]'));

		var answerCheckboxes = Array.prototype.slice.call(document
				.getElementById("answers").querySelectorAll(
						'[id^=answerCheckbox]'));

		var atLeastOneAnswer = answerInputs.filter(function(answer) {
			if (answer.value != "")
				return true;
		}).length > 0 ? true : false;

		// check if inputs before answers are all filled
		var upperBound = elements.length
				- (answerInputs.length + answerCheckboxes.length);

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

	function formValuesToJSON(expQuestionForm) {
		var obj = {};

		obj["type"] = "";
		obj["questionText"] = "";
		obj["workingTime"] = "";
		obj["answers"] = [];

		var elements = expQuestionForm.elements;
		var length = elements.length - 1;

		var answers = [];

		// add ExplorViz landscape identifier
		createProperty(obj, "expLandscape",
				qtLandscape.options[qtLandscape.selectedIndex].innerHTML);

		// add type
		createProperty(obj, "type",
				qtType.options[qtType.selectedIndex].innerHTML);

		var answerCounter = 0;

		// rename answer ids due to possible empty inputs
		// and create json
		for (var i = 0; i < length; i++) {

			if (elements[i].value != "") {

				if (elements[i].id.indexOf("answerInput") == 0) {

					if (answers.length == 0) {
						createProperty(obj, "answers", answers);
					}

					var answer = {};

					var checked = elements[("answerCheckbox" + answerCounter)].checked;

					createProperty(answer, elements[i].value.toString(),
							checked);

					answers.push(answer);

					answerCounter++;

				} else if (elements[i].id.indexOf("answerCheckbox") != 0) {

					createProperty(obj, elements[i].id.toString(),
							elements[i].value);

				}
			}

			else if (elements[i].id.indexOf("answerInput") == 0) {

				answerCounter++;

			}
		}

		if (answers.length == 0) {
			createProperty(obj, "answers", answers);
			answers.push("");
		}
		return obj;
	}

	function createFormForJSON() {
		var previousForm = filledForms.questions[questionPointer];

		var needeAnswerInputs = previousForm["answers"].length;

		// needed for possible empty answer in current question when going back
		// to previous question
		if (previousForm["answers"][0] == "")
			needeAnswerInputs = 0;

		// create input field (and checkboxes)
		if (previousForm["type"] == "Free text") {
			createNewForm(false, needeAnswerInputs + 1);
		}

		else if (previousForm["type"] == "Multiple-Choice") {
			createNewForm(true, needeAnswerInputs + 1);
		}

		var answercounter = 0;

		// Now fill created fields
		for ( var key in previousForm) {

			// fill answer fields and check checkboxes
			if (key == "answers") {
				var answers = previousForm[key];

				for (var i = 0; i < needeAnswerInputs; i++) {

					var key = Object.keys(answers[i])[0];

					document.getElementById("answerInput"
							+ (answercounter % needeAnswerInputs)).value = key;

					document.getElementById("answerCheckbox"
							+ (answercounter % needeAnswerInputs)).checked = answers[i][key];

					answercounter++;

				}

			}

			// set landscape select
			else if (key == "expLandscape") {
				var length = qtLandscape.options.length;

				for (var i = 0; i < length; i++) {

					if (qtLandscape.options[i].text == previousForm[key]) {

						qtLandscape.selectedIndex = i;
						break;

					}
				}
			}

			// set other fields
			else if (!key.startsWith("answerCheckbox")) {

				document.getElementById(key).value = previousForm[key];

			}
		}
	}

	function loadExplorViz() {
		if (qtLandscape.options[qtLandscape.selectedIndex] == undefined) {
			showExceptionDialog = true;
		} else {
			loadLandscape(qtLandscape.options[qtLandscape.selectedIndex].innerHTML);
			showExceptionDialog = false;
		}

	}
}