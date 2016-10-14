Slider = function(formHeight, save, landscapeNames, loadLandscape,
		jsonQuestionnaire, loadExperimentToolsPage, isWelcome, getMaybeApplication) {

	var showExceptionDialog = false;

	var questionPointer = 0;
	
	var landscapeChanged = false;

	var parsedQuestionnaire = JSON.parse(jsonQuestionnaire);
	
	// if new question => add empty question
	// (needed for template engine)
	if (!parsedQuestionnaire.questions[0]) {
		parsedQuestionnaire.questions.push({
           "answers": [{
                "answerText": "",
                "checkboxChecked": false
            }],
			"workingTime" : "",
			"type" : "",
			"expLandscape" : "",
			"expApplication" : "",
			"questionText" : ""
		})
	}
	
	// every change regarding this object 
	// will result in an auto-Re-rendering
	// of all components using this Map
	// as viewModel attribute
	var AppState = can.Map.extend({
		questionnaire : parsedQuestionnaire,
		questionPointer : 0,
		currentQuestion : parsedQuestionnaire.questions[0]
	});
	
    var appState = new AppState();

	setupComponents();
	setupSliderStyle();

	
	function setupComponents() {
		
		// html templates in:  war/exp_slider_template.html
		
		// container component for all following components
		can.Component.extend({
			tag : "slider-container",
			template : can.stache($('#slider_template').html()),
			viewModel : {
				showLandscapeInfo : landscapeNames.length > 0
			}
		});
		
		// shows a warning if no landscape file is found in .explorviz/replay
		can.Component.extend({
			tag : "slider-no-landscape",
			template : can.stache($('#slider_no_landscape').html()),
		});

		// shows all inputs for the question minus the 
		// answer inputs and checkboxes
		can.Component
				.extend({
					tag : "slider-question",
					template : can.stache($('#slider_question').html()),
					init: function() {
						var self = this;
						
						// update html selects based on 
						// currentQuestion or set default value
						// every time, currentQuestion is changed
						this.viewModel.bind('state.currentQuestion', function() {
							
							// new empty questions are always free text
							self.viewModel.attr("questionType", appState.attr("currentQuestion.type"));

							if(appState.attr("currentQuestion.expLandscape") != "") {
								self.viewModel.attr("landscapeSelect", appState.attr("currentQuestion.expLandscape"));
							} else {
								var previousQuestionPointer = appState.attr("questionPointer") - 1;
								var previousQuestion = appState.attr("questionnaire.questions." + previousQuestionPointer);
								
								if(previousQuestion) {
									self.viewModel.attr("landscapeSelect", previousQuestion.expLandscape);
								}
							}							
							self.viewModel.loadExplorVizLandscape(self.viewModel);
							
							if(appState.attr("currentQuestion.expLandscape")) {
								self.viewModel.attr("landscapeSelect", appState.attr("currentQuestion.expLandscape"));
							}
							
							if(appState.attr("currentQuestion.expLandscape")) {
								self.viewModel.attr("landscapeSelect", appState.attr("currentQuestion.expLandscape"));
							}
							
						});																
						
						// set initial data (first time load)
						
						var answers = appState.attr("currentQuestion.answers");
						
						if(answers){
						
							var length = answers.length;
						
							if(length > 0 && answers[length-1] != null && answers[length-1].answerText != "") {
								
								// add one empty answer for new input
								var answers = appState.attr("currentQuestion.answers");
								
								answers.push({
				                    "answerText": "",
				                    "checkboxChecked": false
				                });
				
								appState.attr("currentQuestion.answers", answers);								
							}
						}
						
						if(appState.attr("currentQuestion.expLandscape") == "") {
							// default value for landscapeSelect
						}
						else if(appState.attr("currentQuestion.expLandscape") != "") {
							this.viewModel.attr("landscapeSelect", appState.attr("currentQuestion.expLandscape"));
						} else if(appState.attr("questionnaire.questions").length > 0){
							var previousQuestionPointer = appState.attr("questionPointer") - 1;
							var previousQuestion = appState.attr("questionnaire.questions." + previousQuestionPointer);	
							this.viewModel.attr("landscapeSelect", previousQuestion.expLandscape);
						}
						this.viewModel.loadExplorVizLandscape(this.viewModel);
					},
					viewModel : {
						state: appState,
						landscapeNames : landscapeNames,
						loadExplorVizLandscape : function(viewModel) {
							var maybeApplication = appState.attr("currentQuestion.expApplication");
							
							// if landscape changes => don't show old application
							if(appState.attr("currentQuestion.expLandscape") != 
								viewModel.attr("landscapeSelect")) {
								maybeApplication = null;
							}
																	
							loadLandscape(viewModel.attr("landscapeSelect"), maybeApplication);							
							showExceptionDialog = false;
							
						},
						increment : function(n){return n+1;}
				}				
		});

		// shows all answer inputs for
		// free text questions and handles
		// creation of new input fields
		can.Component.extend({
			tag : "slider-question-free",
			template : can.stache($('#slider_question_free').html()),
			viewModel : {
				state: appState
			},
			events: {
				'.answerInput:last keydown': function() {
					var answers = this.viewModel.attr('state.currentQuestion.answers')
					answers.push({ 
						answerText: "", 
						checkboxChecked: false
						})
				}
			}
		});
		
		// shows all answer inputs and checkboxes
		// for mc questions and handles creation
		// of new input fields
		can.Component.extend({
			tag : "slider-question-mc",
			template : can.stache($('#slider_question_multiple_choice').html()),
			viewModel : {
				state: appState
			},
				events: {
					'.answerInput:last keydown': function() {
						var answers = this.viewModel.attr('state.currentQuestion.answers')
						answers.push({ 
							answerText: "", 
							checkboxChecked: false
							});
					}
				}
		});

		// shows all buttons 
		// and handles click events
		can.Component.extend({
			tag : "slider-buttons",
			template : can.stache($('#slider_buttons').html()),
			viewModel : {
				showDelete : function() {
					var questions = appState.attr("currentQuestion.answers");
					if(questions && questions[0]) {
						if(questions[0].answerText.length > 0){
							return "visible";
						}
						return "hidden";
					}
					return "hidden";
				}
			},
			events : {
				"#exp_slider_question_nextButton click" : function() {
					var form = document.getElementById("exp_slider_question_form");		
					
					if(isFormCompleted(form)) {
						var jsonForm = formValuesToJSON(form);			
						
						can.batch.start();
						
						appState.attr("questionnaire.questions." + appState.attr("questionPointer"), jsonForm);
						sendCompletedData(appState.attr("questionnaire").serialize());
						
						appState.attr("questionPointer", appState.attr("questionPointer") + 1);							
						appState.attr("currentQuestion", appState.attr("questionnaire.questions." + appState.attr("questionPointer")));
													
						handleCreationOfNewEmptyQuestion();
						updateDeleteStatus(this);
						handleNewAnswerInput();	
						can.batch.stop();
					}
					else {
						swal({
							title : "Insert all data!",
							text : "Not all necessary data is completed.",
							type : "warning",
							showCancelButton : false,
							confirmButtonColor : "#8cd4f5",
							confirmButtonText : "I understand.",
							closeOnConfirm : true
						});								
					}
	
				},
				"#exp_slider_question_saveButton click" : function() {
					sendCompletedData(appState.attr("questionnaire").serialize());
					loadExperimentToolsPage();
				},
				"#exp_slider_question_backButton click" : function() {
					var form = document
					.getElementById("exp_slider_question_form");
					var jsonForm = formValuesToJSON(form);
					
					can.batch.start();
					appState.attr("questionnaire.questions." + appState.attr("questionPointer"), jsonForm);
					
					if (appState.attr("questionPointer") > 0) {
						
						appState.attr("questionPointer", appState.attr("questionPointer") - 1);
						appState.attr("currentQuestion", appState.attr("questionnaire.questions." + appState.attr("questionPointer")));						
					
						handleNewAnswerInput();					
					}
					
					updateDeleteStatus(this);
					
					can.batch.stop();
				},
				"#exp_slider_question_removeButton click" : function() {
					
					var self = this;
					
					swal({
						title: "Are you sure about deleting this question?",
						text: "You will not be able to recover this data!",
						type: "warning",
						showCancelButton: true,
						confirmButtonColor: "#DD6B55",
						confirmButtonText: "Yes, delete it!",
						closeOnConfirm: true
						}, 
						function(){
							removeQuestion(self);				
						}
					);
					
					function removeQuestion(self){
						
						can.batch.start();
						
						var questions = appState.attr("questionnaire.questions");					
						questions.splice(appState.attr("questionPointer"), 1);
						appState.attr("questionnaire.questions", questions);
						
						appState.attr("currentQuestion", appState.attr("questionnaire.questions." + appState.attr("questionPointer")));
											
						handleCreationOfNewEmptyQuestion();
						
						can.batch.stop();	
						
						sendCompletedData(appState.attr("questionnaire").serialize());		
						
						updateDeleteStatus(self);
						handleNewAnswerInput();
					}
				}
			}
		});
		
		can.Component.extend({
			tag : "slider-error-input",
			template : can.stache($('#slider_error_input').html()),
		});		

		var template = can.stache("<slider-container></slider-container>");
		$('#view').append(template());

	}

	
	function setupSliderStyle() {
		$('#expSliderInnerContainer').height(formHeight);
		$('#expQuestionForm').css('maxHeight', formHeight - 70);
		
		$('#expScrollable').height(formHeight);
		$('#expScrollable').css('maxHeight', formHeight - 60);
		
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
	
	
	// Handles the visibility of 
	// the delete button
	function updateDeleteStatus(self){
		if(!appState.attr("currentQuestion")) {
			self.viewModel.attr("showDelete", "hidden");
		} 
		else {						
			
			if(appState.attr("currentQuestion.questionText").length == 0) {
				self.viewModel.attr("showDelete", "hidden");
			} else {
				self.viewModel.attr("showDelete", "visible");
			}						
		}
	}
	
	
	// creates empty dummy question 
	// if currentQuestion is undefined
	// (needed for template engine)
	function handleCreationOfNewEmptyQuestion(){		
		if(!appState.attr("currentQuestion")) {
			appState.attr("questionnaire.questions." + appState.attr("questionPointer") , {
				"answers": [{
					"answerText": "",
				    "checkboxChecked": false
				}],
				"workingTime" : "",
				"type" : "freeText",
				"expLandscape" : "",
				"questionText" : ""
			}); 
			appState.attr("currentQuestion", appState.attr("questionnaire.questions." + appState.attr("questionPointer")));
		}
	}
	
	
	// creates empty answer
	// if no answer is found
	// (needed for template engine)
	function handleNewAnswerInput(){
		var answers = appState.attr("currentQuestion.answers");
		if(answers) {
			var length = answers.length;
			
			if(!answers[length-1] || answers[length-1].answerText && answers[length-1].answerText != "") {
				
				// add one empty answer for new input
				var answers = appState.attr("currentQuestion.answers");
				
				answers.push({
	                "answerText": "",
	                "checkboxChecked": false
	            });					
	
				appState.attr("currentQuestion.answers", answers);								
			}
		}
	}

	
	function sendCompletedData(questionnaire) {
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
			if(elements[i].id == "workingTime") {
				var value = elements[i].value;
				if(value == "" || value <= 0 || value > 10)
					return false;
			}
			else if (elements[i].value == "") {
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

	
	// create JSON Object out of form
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

		// add ExplorViz landscape / application name
		var maybeApplicationName = getMaybeApplication();
		
		createProperty(obj, "expLandscape", $(
				'#exp_slider_question_landscape option:selected').val());
		
		createProperty(obj, "expApplication", maybeApplicationName);

		// add type
		createProperty(obj, "type", $(
				'#exp_slider_question_type_select option:selected').val());

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

		return obj;
	}

}