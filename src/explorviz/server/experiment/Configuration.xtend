package explorviz.server.experiment

import java.util.ArrayList
import java.util.Arrays
import explorviz.shared.experiment.Step

class Configuration {
	public static var selectedLanguage = "english"
	public static var languages = new ArrayList<String>(Arrays.asList("english", "german"));
	
	public static var tutorialSteps = new ArrayList<Step>(
		Arrays.asList(new Step(""),
					  new Step("OCN Editor", true, false, false),
					  new Step("OCN Editor", true, false, false),
					  new Step("Eprints", false, true, false),
					  new Step("10.0.0.1 - 10.0.0.2", true, false, false),
					  new Step("10.0.0.1 - 10.0.0.2", true, false, false),
					  new Step("Neo4j", true, false, false),
					  new Step("tooling", true, false, false),
					  new Step("tooling", true, false, false),
					  new Step(true),
					  new Step(false)
		)
	);
}