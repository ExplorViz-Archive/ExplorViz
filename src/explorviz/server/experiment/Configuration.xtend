package explorviz.server.experiment

import java.util.ArrayList
import java.util.Arrays
import explorviz.shared.experiment.Step

class Configuration {
	public static var selectedLanguage = "english"
	public static var languages = new ArrayList<String>(Arrays.asList("english", "german"));
	
	public static var tutorialSteps = new ArrayList<Step>(
		Arrays.asList(new Step(""), //0
					  new Step("OCN Editor", true, false, false), //1
					  new Step("OCN Editor", true, false, false), //2
					  new Step(""), //3
					  new Step("10.0.0.1 - 10.0.0.2", true, false, false), //4
					  new Step("10.0.0.1 - 10.0.0.2", true, false, false), //5
					  new Step("Eprints", false, true, false), //6
					  new Step("Neo4j", true, false, false), //7
					  new Step(""), //8
					  new Step("tooling", true, false, false), // 9
					  new Step("AccountSqlMapDao","kernel"), //just a test //10
					  new Step("tooling", true, false, false), //11
					  new Step(true), //12
					  new Step(false), //13
					  new Step("") //14
		)
	);
	
	public static var experiment = true
}