package explorviz.server.experiment

import java.util.ArrayList
import java.util.Arrays
import explorviz.shared.experiment.Step

class Configuration {
	public static var selectedLanguage = "english"
	public static var languages = new ArrayList<String>(Arrays.asList("english", "german"));
	
	public static var tutorialSteps = new ArrayList<Step>(
		Arrays.asList(new Step("OCN Editor", "", false, false),
					  new Step("OCN Editor", "", true, false),
					  new Step("10.0.0.1 - 10.0.0.2", "", true, false),
					  new Step("10.0.0.1 - 10.0.0.2", "", false, false),
					  new Step("Neo4J", "", true, false)
		)
	);
}