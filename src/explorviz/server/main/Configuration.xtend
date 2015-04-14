package explorviz.server.main

import java.util.ArrayList
import java.util.Arrays
import explorviz.shared.experiment.Step
import java.util.List

class Configuration {
	public static var selectedLanguage = "english"
	public static var languages = new ArrayList<String>(Arrays.asList("english", "german"));
	public static var secondLandscape = false
	public static var long tutorialStart = System.currentTimeMillis();
	public static var long secondLandscapeTime = System.currentTimeMillis();
	
	
	public static var tutorialSteps = new ArrayList<Step>(
		Arrays.asList(new Step(""), //0 text
					  new Step("OCN Editor", true, false, false, false), //1 close system
					  new Step("OCN Editor", true, false, false, false), //2 open system
					  new Step(""), //3 text
					  new Step("10.0.0.1 - 10.0.0.2", true, false, false, false), //4 open nodegroup
					  new Step("10.0.0.1 - 10.0.0.2", true, false, false, false), //5 close nodegroup
					  new Step("") //31 end text
		)
	);
	
		public static var tutorialControlGroupSteps = new ArrayList<Step>(
		Arrays.asList(new Step(""), //0 text
					  new Step(""), //1 text
					  new Step(""), //2 text
					  new Step(""), //3 text
					  new Step(""), //4 text
					  new Step("")  //5 end text
		)
	);

	
	public static var experiment = false
	public static var boolean skipQuestion = false
	
	public static var rsfExportEnabled = false
	public static var outputIntervalSeconds = 10
	public static val List<String> databaseNames = new ArrayList<String>()
	
	public static var TIMESHIFT_INTERVAL_IN_MINUTES = 10
	public static final String MODEL_EXTENSION = ".expl"
}