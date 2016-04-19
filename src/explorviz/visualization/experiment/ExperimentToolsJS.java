package explorviz.visualization.experiment;

public class ExperimentToolsJS {
	public static native void init() /*-{
		// Form example with serialization: 
		// src/explorviz/visualization/view/ManageUsersAndRolesPage.java and
		// src/explorviz/visualization/view/ManageUsersAndRolesPageJS.java

		$wnd
				.jQuery("#startExperimentBtn")
				.on(
						"click touchstart",
						function() {
							// Example by Bootbox-dev
							$wnd.bootbox
									.dialog({
										title : "This is a form in a modal.",
										message : '<div class="row">  '
												+ '<div class="col-md-12"> '
												+ '<form class="form-horizontal"> '
												+ '<div class="form-group"> '
												+ '<label class="col-md-4 control-label" for="name">Name</label> '
												+ '<div class="col-md-4"> '
												+ '<input id="name" name="name" type="text" placeholder="Your name" class="form-control input-md"> '
												+ '<span class="help-block">Here goes your name</span> </div> '
												+ '</div> '
												+ '<div class="form-group"> '
												+ '<label class="col-md-4 control-label" for="awesomeness">How awesome is this?</label> '
												+ '<div class="col-md-4"> <div class="radio"> <label for="awesomeness-0"> '
												+ '<input type="radio" name="awesomeness" id="awesomeness-0" value="Really awesome" checked="checked"> '
												+ 'Really awesome </label> '
												+ '</div><div class="radio"> <label for="awesomeness-1"> '
												+ '<input type="radio" name="awesomeness" id="awesomeness-1" value="Super awesome"> Super awesome </label> '
												+ '</div> ' + '</div> </div>'
												+ '</form> </div>  </div>',
										buttons : {
											success : {
												label : "Save",
												className : "btn-success",
												callback : function() {
													var name = $('#name').val();
													var answer = $(
															"input[name='awesomeness']:checked")
															.val()
													Example
															.show("Hello "
																	+ name
																	+ ". You've chosen <b>"
																	+ answer
																	+ "</b>");
												}
											}
										}
									});
						});

		$wnd.jQuery("#newExperimentBtn").on("click touchstart", function() {
			@explorviz.visualization.experiment.ExperimentToolsPage::showNewExpWindow()()
		});

		$wnd
				.jQuery("#questionsSelect")
				.change(
						function() {
							var id = $wnd.jQuery(
									'#questionsSelect option:selected').attr(
									'id');
							var questionText = @explorviz.visualization.experiment.ExperimentToolsPage::getQuestionText(I)(id)

							$wnd.jQuery("#questionTextLabel")
									.text(questionText)

						});

	}-*/;
}
