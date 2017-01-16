Slider = function(formHeight, save, landscapeNames, loadLandscape,
		jsonQuestionnaire, preAndPostQuestions, loadExperimentToolsPage, isWelcome, getMaybeApplication) {

	var showExceptionDialog = false;

	var questionPointer = 0;
	
	var landscapeChanged = false;

	var parsedQuestionnaire = JSON.parse(jsonQuestionnaire);
	
	// if new question => add empty question
	// (needed for template engine), empty dummy entry
	createDummyQuestions(preAndPostQuestions);
	
	var questionEnum = {
			PREQUESTION		: "prequestions",
			QUESTION		: "questions",
			POSTQUESTION	: "postquestions"
	};
	
	var currentQuestionType = questionEnum.QUESTION;
	var initCurrentQuestion = parsedQuestionnaire.questions[0];

	if(preAndPostQuestions) {
		currentQuestionType = questionEnum.PREQUESTION;
		initCurrentQuestion = parsedQuestionnaire.prequestions[0];	
	}
	
	// every change regarding this object 
	// will result in an auto-Re-rendering
	// of all components using this Map
	// as viewModel attribute
	var AppState = can.Map.extend({
		questionnaire : parsedQuestionnaire,
		questionPointer : 0,
		currentQuestion : initCurrentQuestion,
		currentQuestionType : currentQuestionType
	});
	
    var appState = new AppState();

	setupComponents();
	setupSliderStyle();

	
	function setupComponents() {
		
		// html templates in:  war/exp_slider_template.html
		
		// container component for all following components, main-body
		can.Component.extend({
			tag : "slider-container",
			template : can.stache($('#slider_template').html()),
			viewModel : {
				showLandscapeInfo : landscapeNames.length > 0,
				state : appState,
				showPrePostQuestions : preAndPostQuestions		
			},
			init : function() {
				var self = this;
				this.viewModel.bind('state.currentQuestionType', function() {
					var currentQuestionTypeBinding = self.viewModel.attr("state.currentQuestionType");
					var setupSomeStyle = true;
					if(currentQuestionTypeBinding == questionEnum.QUESTION) {
						self.viewModel.attr("showPrePostQuestions", false);
						console.log("prepost false");
					} else {
						self.viewModel.attr("showPrePostQuestions", true);
						console.log("prepost true");
					}
					
					setupSliderStyle(setupSomeStyle);
					console.log(self.viewModel.attr("state.currentQuestionType") + " binding");
					currentQuestionType
				});
				console.log(self.viewModel.attr("state.currentQuestionType") + " init " + self.viewModel.attr("showPrePostQuestions"));
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
								var previousQuestion = appState.attr("questionnaire." + appState.attr("currentQuestionType") + "." + previousQuestionPointer);
								
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
						} else if(appState.attr("questionnaire." + appState.attr("currentQuestionType")).length > 0){
							var previousQuestionPointer = appState.attr("questionPointer") - 1;
							var previousQuestion = appState.attr("questionnaire." + appState.attr("currentQuestionType") + "." + previousQuestionPointer);	
							this.viewModel.attr("landscapeSelect", previousQuestion.expLandscape);
						}
						
						var type = appState.attr("currentQuestion.type");
						
						if(type) {
							this.viewModel.attr("questionType", type);
						} else {
							this.viewModel.attr("questionType", "freeText");
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
				state : appState,
				showDelete : function() {
					var questions = appState.attr("currentQuestion.answers");
					if(questions && questions[0]) {
						if(questions[0].answerText.length > 0){
							return "visible";
						}
						return "hidden";
					}
					return "hidden";
				},
				showPostQuestion : function() {
					return disableButton(questionEnum.POSTQUESTION);
				},
				showQuestion : function() {
					console.log("showQuestion" + appState.currentQuestionType);
					return disableButton(questionEnum.QUESTION);
				},
				showPreQuestion : function() {
					return disableButton(questionEnum.PREQUESTION);
				}
			},
			events : {
				"#exp_slider_question_nextButton click" : function() {
					var form = document.getElementById("exp_slider_question_form");		
					
					if(isFormCompleted(form)) {
						var jsonForm = formValuesToJSON(form);			
						
						can.batch.start();
						
						appState.attr("questionnaire." + appState.attr("currentQuestionType") + "." + appState.attr("questionPointer"), jsonForm);
						sendCompletedData(appState.attr("questionnaire").serialize());
						
						appState.attr("questionPointer", appState.attr("questionPointer") + 1);							
						appState.attr("currentQuestion", appState.attr("questionnaire." + appState.attr("currentQuestionType") + "." + appState.attr("questionPointer")));
													
						handleCreationOfNewEmptyQuestion(appState.attr("currentQuestionType"));
						updateDeleteStatus(this);
						handleNewAnswerInput();	
						can.batch.stop();
					}
					else {
						swal({
							title : "Insert all data!",
							text : "Not all necessary data is completed / valid.",
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
					appState.attr("questionnaire." + appState.attr("currentQuestionType") + "." + appState.attr("questionPointer"), jsonForm);
					
					if (appState.attr("questionPointer") > 0) {
						
						appState.attr("questionPointer", appState.attr("questionPointer") - 1);
						appState.attr("currentQuestion", appState.attr("questionnaire." + appState.attr("currentQuestionType") + "." + appState.attr("questionPointer")));						
					
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
											
						handleCreationOfNewEmptyQuestion(appState.attr("currentQuestionType"));
						
						can.batch.stop();
						
						sendCompletedData(appState.attr("questionnaire").serialize());		
						
						updateDeleteStatus(self);
						handleNewAnswerInput();
					}
				},
				"#exp_slider_prequestion_gotoButton click" : function() {
						questionTabButtonClick(questionEnum.PREQUESTION, this);
						
				},
				"#exp_slider_question_gotoButton click" : function() {
					questionTabButtonClick(questionEnum.QUESTION, this);
					
				},
				"#exp_slider_postquestion_gotoButton click" : function() {
					questionTabButtonClick(questionEnum.POSTQUESTION, this);
					
				}
			}
		});
		
		can.Component.extend({
			tag : "slider-error-input",
			template : can.stache($('#slider_error_input').html()),
		});		
		
		//handles the bottom (answer) input boxes for pre/postquestion number ranges
		can.Component.extend({
			tag : "slider-prepost-question-nr",
			template : can.stache($('#slider_prepost_question_number_range').html()),
			viewModel : {
				state : appState
				
			}
		});	
		
		//handles the bottom (answer) input boxes for pre/postquestion multiple choice
		can.Component.extend({
			tag : "slider-prepost-question-mc",
			template : can.stache($('#slider_prepost_question_multiple_choice').html()),
			viewModel : {
				state : appState
				
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
		
		//handles the top input boxes for pre/postquestion 
		can.Component.extend({
			tag : "slider-prepost-question",
			template : can.stache($('#slider_prepost_question').html()),
			init: function() {
				var self = this;
				
				// update html selects based on 
				// currentQuestion or set default value
				// every time, currentQuestion is changed
				this.viewModel.bind('state.currentQuestion', function() {
					// new empty questions are always free text
					self.viewModel.attr("questionType", appState.attr("currentQuestion.type"));
				});
				
				// set initial data (first time load)
				handleNewAnswerInput();
				
				var type = appState.attr("currentQuestion.type");
				
				if(type) {
					this.viewModel.attr("questionType", type);
				} else {
					this.viewModel.attr("questionType", "freeText");
				}
			},
			viewModel : {
				state : appState
				
			}
		});

		var template = can.stache("<slider-container></slider-container>");
		$('#view').append(template());

	}

	
	function setupSliderStyle(setupSomeStyle) {
		if(!setupSomeStyle) {
			$('#expSliderInnerContainer').height(formHeight);
			$('#expSlider').css('right', -315);
			$('#expSliderLabel').click(function(e) {
				e.preventDefault();
				toggle[c++ % 2]();
			});
			// Setup toggle mechanism
			var toggle = [ slideOut, slideIn ], c = 0;
		}
		
		$('#expQuestionForm').css('maxHeight', formHeight - 70);
		
		$('#expScrollable').height(formHeight);
		$('#expScrollable').css('maxHeight', formHeight - 60);
		
		
		

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
	
	//handles disable of pre / post and normal Questions button during init
	function disableButton(currentButton, self) {
		var buttonState = false;
		if((appState.attr("currentQuestionType") == currentButton) || !preAndPostQuestions) {
			buttonState = true;
		}
		return buttonState;
	}
	
	//handles disable of pre / post and normal Questions button 
	function disableQuestionButtons(self) {
		if(self.viewModel.attr("state.currentQuestionType") == questionEnum.PREQUESTION) {
			self.viewModel.attr("showPreQuestion", true);
			self.viewModel.attr("showQuestion", false);
			self.viewModel.attr("showPostQuestion", false);
		} else if (appState.attr("currentQuestionType") == questionEnum.POSTQUESTION) {
			self.viewModel.attr("showPreQuestion", false);
			self.viewModel.attr("showQuestion", false);
			self.viewModel.attr("showPostQuestion", true);
		} else { //currentQuestionType is question
			self.viewModel.attr("showPreQuestion", false);
			self.viewModel.attr("showQuestion", true);
			self.viewModel.attr("showPostQuestion", false);
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
	function handleCreationOfNewEmptyQuestion(questionType){		
		if(!appState.attr("currentQuestion")) {
			appState.attr("questionnaire." + questionType + "." + appState.attr("questionPointer") , {
				"answers": [{
					"answerText": "",
				    "checkboxChecked": false
				}],
				"workingTime" : "",
				"type" : "freeText",
				"expLandscape" : "",
				"questionText" : ""
			}); 
			appState.attr("currentQuestion", appState.attr("questionnaire." + questionType + "." + appState.attr("questionPointer")));
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
		if(!preAndPostQuestions || currentQuestionType == "questions") {
			console.log("iSformCOmpleted");
			var answerInputs = Array.prototype.slice.call(document.getElementById(
			"answers").querySelectorAll('[id^=answerInput]'));	//TODO
			
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
		} else if (false){
			
		}
		
		return true;

		
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
	
	//handles what should be done after a question-tab click event
	//saves current input (if valid or canceled after warning-sweetaltert) or just skips to another question-tab
	function questionTabButtonClick(questionType, self) {
		var form = document.getElementById("exp_slider_question_form");		
		if(isFormCompleted(form)) {
			var jsonForm = formValuesToJSON(form);			
		
			can.batch.start();
			var appState = self.viewModel.attr("state");
			appState.attr("questionnaire." + appState.attr("currentQuestionType") + "." + appState.attr("questionPointer"), jsonForm);
				
			
			sendCompletedData(appState.attr("questionnaire").serialize());
			appState.attr("questionPointer", 0);						
			appState.attr("currentQuestion", appState.attr("questionnaire." + questionType + "." + appState.attr("questionPointer")));

			handleCreationOfNewEmptyQuestion(questionType);	//erstellt neue leere Form für 'questionType' wenn CurrentQuestion leer ist
			updateDeleteStatus(self);
			handleNewAnswerInput();
			
			
			appState.attr("currentQuestionType", questionType);
			self.viewModel.attr("state", appState);
			disableQuestionButtons(self);
			can.batch.stop();
			
			
		}
		else {
			swal({
				title : "Insert all data!",
				text : "Not all necessary data is completed / valid. If you go on, the data will be lost.",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#8cd4f5",
				confirmButtonText : "Still go on",
				cancelButtonText : "Edit data",
				closeOnConfirm : true,
				closeOnCancel: true
			},
			function(isConfirm) {
				if(isConfirm) {
					can.batch.start();
					var appState = self.viewModel.attr("state");
					
					appState.attr("questionPointer", 0);						
					appState.attr("currentQuestion", appState.attr("questionnaire." + questionType + "." + appState.attr("questionPointer")));
					
					appState.attr("currentQuestionType", questionType);	
					self.viewModel.attr("state", appState);
					
					handleCreationOfNewEmptyQuestion(questionType);	//erstellt neue leere Form für 'questionType' wenn CurrentQuestion leer ist
					handleNewAnswerInput();
					updateDeleteStatus(self);
					disableQuestionButtons(self);
					
					can.batch.stop();
				}
			});								
		}
	}
	
	//create empty questions entries for questions and if wanted, pre- and postquestions
	function createDummyQuestions(preAndPostQuestions) {
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
		if(preAndPostQuestions) {
			if (!parsedQuestionnaire.prequestions) {
				parsedQuestionnaire.prequestions = [{
			           "answers": [{
			                "answerText": "",
			                "checkboxChecked": false
			            }],
						"workingTime" : "",
						"type" : "",
						"expLandscape" : "",
						"expApplication" : "",
						"questionText" : ""
					}];
			}
			
			if (!parsedQuestionnaire.postquestions) {
				parsedQuestionnaire.postquestions = [{
			           "answers": [{
			                "answerText": "",
			                "checkboxChecked": false
			            }],
						"workingTime" : "",
						"type" : "",
						"expLandscape" : "",
						"expApplication" : "",
						"questionText" : ""
					}]
			}
		}	
	}

}