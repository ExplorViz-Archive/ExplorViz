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
		Arrays.asList(new Step(""), //0
					  new Step("OCN Editor", true, false, false, false), //1
					  new Step("OCN Editor", true, false, false, false), //2
					  new Step(""), //3
					  new Step("10.0.0.1 - 10.0.0.2", true, false, false, false), //4
					  new Step("10.0.0.1 - 10.0.0.2", true, false, false, false), //5
					  new Step("Eprints", false, true, false, false), //6
					  new Step("Neo4j", true, false, false, false), //7
					  new Step(""), //8
					  new Step("tooling", true, false, false, false), // 9
					  new Step("ItemSqlMapDao", false,false,false,true), //10
					  new Step("AccountSqlMapDao", false, false,true, false), //11
					  new Step("AccountSqlMapDao","kernel", false, true), //12
					  new Step("tooling", true, false, false, false), //13
					  new Step(true), //14
					  new Step(false), //15
					  new Step("") //16
		)
	);
	
	public static var experiment = false
	
	public static var rsfExportEnabled = true
	public static var outputIntervalSeconds = 10
	public static val List<String> databaseNames = new ArrayList<String>()
	
	public static var TIMESHIFT_INTERVAL_IN_MINUTES = 10
	public static final String MODEL_EXTENSION = ".expl"
}