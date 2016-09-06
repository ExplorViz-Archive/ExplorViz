/* Experimental Slider implementation with jQuery and handlebars.js */

Slider = function(formHeight, save, landscapeNames, loadLandscape,
		jsonQuestionnaire, loadExperimentToolsPage, isWelcome) {

	var showExceptionDialog = false;

	var questionPointer = 0;

	var questionnaire = JSON.parse(jsonQuestionnaire);

	setupComponents();
	setupSliderStyle();

//	if (!isWelcome)
//		setupAnswerHandler(0);

	function setupComponents() {

		can.Component.extend({
			tag : "slider-container",
			template : can.stache($('#slider_template').html()),
			viewModel : {
				isWelcome : isWelcome
			}
		});

		can.Component
				.extend({
					tag : "slider-question",
					template : can.stache($('#slider_question').html()),
					viewModel : {
						qNr : questionPointer,
						question : questionnaire.questions[questionPointer],
						landscapeNames : landscapeNames,
						loadExplorVizLandscape : function(viewModel, $element, ev) {
							var value = $element.val();
							loadLandscape(value);
							showExceptionDialog = false;
						},
						setSelected : function() {
							if (questionnaire.questions[questionPointer]) {
								var type = questionnaire.questions[questionPointer].type;
								if (type == null) {
									$('#exp_slider_question_type_select').val(
											"freeText");
								} else {
									$('#exp_slider_question_type_select').val(
											type);
								}
							} else {
								$('#exp_slider_question_type_select').val("freeText");
							}
						},
						updateQuestion : function() {
							console.log(questionnaire.questions[questionPointer]);
							this.attr("qNr", questionPointer);
							this.attr("question", questionnaire.questions[questionPointer]);
						}
					}
				});

		can.Component.extend({
			tag : "slider-question-free",
			template : can.stache($('#slider_question_free').html()),
			viewModel : {
				question : questionnaire.questions[questionPointer],
				updateQuestion : function() {
					this.attr("question", questionnaire.questions[questionPointer]);
				}
			}
		});
		
		can.Component.extend({
			tag : "slider-question-mc",
			template : can.stache($('#slider_question_multiple_choice').html()),
			viewModel : {
				question : questionnaire.questions[questionPointer],
				updateQuestion : function() {
					this.attr("question", questionnaire.questions[questionPointer]);
				}
			}
		});

		can.Component.extend({
			tag : "slider-buttons",
			template : can.stache($('#slider_buttons').html()),
			viewModel : {
				isWelcome : isWelcome
			},
			events : {
				"#exp_slider_question_nextButton click" : function() {
					var form = document
							.getElementById("exp_slider_question_form");					
					//TODO continue if completed form
					if(isFormCompleted(form)) {
						var jsonForm = formValuesToJSON(form);
						questionnaire.questions[questionPointer] = jsonForm;
						sendCompletedData();
						questionPointer++;
						
						if(!questionnaire.questions[questionPointer]) {
							console.log("create mofo object");
//							questionnaire.questions[questionPointer] = {
//								"answers" : [],
//								"workingTime" : "",
//								"type" : "",
//								"expLandscape" : "",
//								"questionText" : ""
//							};
						}

						$("slider-question").viewModel().updateQuestion();
						$("slider-question-free").viewModel().updateQuestion();
						
//						var type = questionnaire.questions[questionPointer].type;
//						if(type == "freeText" || type == null) {
//							$("slider-question-free").viewModel().updateQuestion();
//						}
//						else if(type == "multipleChoice") {
//							$("slider-question-mc").viewModel().updateQuestion();
//						}						
					}
					else {
						alert("Insert all data!")
					}

				},
				"#exp_slider_question_saveButton click" : function() {
					// TODO save
					loadExperimentToolsPage();
				},
				"#exp_slider_question_backButton click" : function() {
					var form = document
					.getElementById("exp_slider_question_form");
					var jsonForm = formValuesToJSON(form);
					questionnaire.questions[questionPointer] = jsonForm;
					
					if (questionPointer > 0) {
						questionPointer--;

						$("slider-question").viewModel().updateQuestion();
						
						var type = questionnaire.questions[questionPointer].type;
						if(type == "freeText" || type == null) {
							$("slider-question-free").viewModel().updateQuestion();
						}
						else if(type == "multipleChoice") {
							$("slider-question-mc").viewModel().updateQuestion();
						}
					}
				}
			}
		});

		var template = can.stache("<slider-container></slider-container>");
		$('#view').append(template());

	}

	function setupSliderStyle() {
		$('#expSliderInnerContainer').height(formHeight);
		$('#expQuestionForm').css('maxHeight', formHeight - 70);
		$('#expSlider').css('right', -315);
		$('#expSliderLabel').click(function(e) {
			e.preventDefault();
			toggle[c++ % 2]();
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
	}


	function setupAnswerHandler(index) {
		var inputID = "answerInput" + index.toString();

		document.getElementById(inputID).addEventListener("keyup", handler);

		function handler() {

			var type = $('#exp_slider_question_questiontype option:selected')
					.val();

			console.log(type);

			document.getElementById(inputID).removeEventListener("keyup",
					handler);

			var answerDiv = document.createElement('div');
			answerDiv.id = "answer" + (index + 1).toString();
			answerDiv.className = 'expAnswer';

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
			if (type == "freeText")
				correctAnswerCheckbox.style.display = 'none';

			answerDiv.appendChild(correctAnswerCheckbox);

			document.getElementById("answers").appendChild(answerDiv);

			setupAnswerHandler(index + 1);
		}
	}

	function sendCompletedData() {
		// filter for well-formed questions
		var wellFormedQuestions = questionnaire.questions.filter(function(
				elem, index, obj) {		
			
			var hasAnswer = elem.answers[0] != "";

			var hasText = elem.questionText.length > 0;
			var hasWorkingTime = elem.workingTime.length > 0;

			return hasAnswer && hasText && hasWorkingTime;
		});
		
		var wellFormQuestionnaire = JSON.parse(JSON.stringify(questionnaire));
		wellFormQuestionnaire.questions = wellFormedQuestions;

		// send to server
		save(JSON.stringify(wellFormQuestionnaire));
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

		var container = {};

		var obj = {};

		obj["type"] = "";
		obj["questionText"] = "";
		obj["workingTime"] = "";
		obj["answers"] = [];

		var elements = expQuestionForm.elements;
		var length = elements.length - 1;

		var answers = [];

		// add ExplorViz landscape identifier
		createProperty(obj, "expLandscape", $(
				'#exp_slider_question_landscape option:selected').val());

		// add type
		createProperty(obj, "type", $(
				'#exp_slider_question_questiontype option:selected').val());

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

					createProperty(answer, "answerText",
							elements[i].value.toString());
					
					createProperty(answer, "checkboxChecked",
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
		//createProperty(container, questionPointer, obj);
		return obj;
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