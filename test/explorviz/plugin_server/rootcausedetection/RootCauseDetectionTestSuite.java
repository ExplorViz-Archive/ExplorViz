package explorviz.plugin_server.rootcausedetection;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import explorviz.plugin_server.rootcausedetection.algorithm.*;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscapeTest;

@RunWith(Suite.class)
@SuiteClasses({ MaximumAlgorithmTest.class, RGBAlgorithmTest.class, RanCorrLandscapeTest.class,
		LocalAlgorithmTest.class, NeighbourAlgorithmTest.class, MeshAlgorithmTest.class,
		RefinedMeshAlgorithmTest.class, RootCauseDetectionTest.class })
public class RootCauseDetectionTestSuite {

}
