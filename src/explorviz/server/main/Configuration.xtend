package explorviz.server.main

import java.util.ArrayList
import java.util.Arrays
import explorviz.shared.experiment.Step
import java.util.List

class Configuration {
	public static var selectedLanguage = "english/eyeTrackingExperiment"	//path to textfiles for tutorial
	public static var languages = new ArrayList<String>(Arrays.asList("english", "german"));
	public static var secondLandscape = false
	public static var long tutorialStart = System.currentTimeMillis();
	public static var long secondLandscapeTime = System.currentTimeMillis();
	
//mit codeviewer und timeshift; for eyeTracking Experiment (without timeline and codeviewer)
	public static var tutorialSteps = new ArrayList<Step>(
		Arrays.asList(new Step(""), //0 text
					  new Step("OCN Editor", true, false, false, false), //1 close system
					  new Step("OCN Editor", true, false, false, false), //2 open system
					  new Step(""), //3 text
					  new Step("10.0.0.1 - 10.0.0.2", true, false, false, false), //4 open nodegroup
					  new Step("10.0.0.1 - 10.0.0.2", true, false, false, false), //5 close nodegroup
					  new Step("Neo4j", true, false, false, false), //6 enter application
					  new Step(""), //7 text
					  new Step("kernel", true, false, false, false), // 8 open package
					  new Step("main", false,false,false,true), //9 hover over package
					  new Step("main",true,false,false,false), //10 enter package
					  new Step("TransactionImpl", false,false,false,true), //11 hover over class
					  new Step("lifecycle", false, false, true, false), // 12 click component
					  new Step("FileUtils", false, false,true, false), //13 click class
					  new Step("FileUtils","TransactionImpl", false, true), //14 click communication
					  new Step("choosetrace"), //15 choose trace dialog description
					  new Step("startanalysis"), // 16 replayer description
					  new Step("pauseanalysis"), //17
					  new Step("nextanalysis"), //18
					  new Step("leaveanalysis"), //19
					  new Step("kernel", true, false, false, false), //20 close package
					  new Step("landscape"), //20 go back to landscape
					  new Step("") //21 text
		)
	);
	
	public static var experiment = false
	public static var boolean skipQuestion = false
	public static var String experimentFilename = null
	
	public static var rsfExportEnabled = false
	public static var outputIntervalSeconds = 10
	public static val List<String> databaseNames = new ArrayList<String>()
	
	public static var TIMESHIFT_INTERVAL_IN_MINUTES = 10
	public static final String MODEL_EXTENSION = ".expl"
}