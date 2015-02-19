package explorviz.plugin_server.rootcausedetection.algorithm;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.shared.model.*;
import explorviz.shared.model.System;

public class LocalAlgorithmTest {

	private RanCorrLandscape rcLandscape = null;

	/**
	 * This methods sets up a very specific test landscape.
	 */
	@Before
	public void setUpLandscape() {
		CommunicationClazz op1 = new CommunicationClazz();
		TreeMapLongDoubleIValue as1 = new TreeMapLongDoubleIValue();
		as1.put(1l, -0.2d);
		as1.put(2l, 0.4d);
		as1.put(5l, 0.6d);
		as1.put(6l, 0.6d); // Average: 0.45
		op1.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as1);

		CommunicationClazz op2 = new CommunicationClazz();
		TreeMapLongDoubleIValue as2 = new TreeMapLongDoubleIValue();
		as2.put(1l, 0.3d);
		as2.put(2l, 0.5d);
		as2.put(5l, 0.7d);
		as2.put(6l, 0.8d); // Average: 0.575
		op2.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as2);

		CommunicationClazz op3 = new CommunicationClazz();
		TreeMapLongDoubleIValue as3 = new TreeMapLongDoubleIValue();
		as3.put(1l, 0.1d);
		as3.put(2l, 0.1d);
		as3.put(5l, 0.1d);
		as3.put(6l, 0.1d); // Average: 0.1
		op3.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as3);

		CommunicationClazz op4 = new CommunicationClazz();
		TreeMapLongDoubleIValue as4 = new TreeMapLongDoubleIValue();
		as4.put(1l, 0.3d);
		as4.put(2l, -0.3d);
		as4.put(5l, 0.3d);
		as4.put(6l, -0.3d); // Average: 0.3
		op4.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as4);

		// c1: op1, op2 - Average = 0.5125
		Clazz c1 = new Clazz();
		op1.setSource(c1);
		op1.setTarget(c1);
		op2.setSource(c1);
		op2.setTarget(c1);

		// c2: op3 - Average = 0,1
		Clazz c2 = new Clazz();
		op3.setSource(c2);
		op3.setTarget(c2);

		// c3: op4 - Average = 0.3
		Clazz c3 = new Clazz();
		op4.setSource(c3);
		op4.setTarget(c3);

		// p1: c1, c2
		Component p1 = new Component();
		c1.setParent(p1);
		c2.setParent(p1);
		List<Clazz> cs1 = new ArrayList<>();
		cs1.add(c1);
		cs1.add(c2);
		p1.setClazzes(cs1);

		// p2: c3
		Component p2 = new Component();
		c3.setParent(p2);
		List<Clazz> cs2 = new ArrayList<>();
		cs2.add(c3);
		p2.setClazzes(cs2);

		// a1: p1
		Application a1 = new Application();
		p1.setBelongingApplication(a1);
		List<CommunicationClazz> ops123 = new ArrayList<>();
		ops123.add(op1);
		ops123.add(op2);
		ops123.add(op3);
		a1.setCommunications(ops123);
		List<Component> components1 = new ArrayList<>();
		components1.add(p1);
		a1.setComponents(components1);

		// a2: p2
		Application a2 = new Application();
		p2.setBelongingApplication(a2);
		List<CommunicationClazz> ops4 = new ArrayList<>();
		ops4.add(op4);
		a2.setCommunications(ops4);
		List<Component> components2 = new ArrayList<>();
		components2.add(p2);
		a2.setComponents(components2);

		// n1: a1, a2
		Node n1 = new Node();
		List<Application> apps = new ArrayList<>();
		apps.add(a1);
		apps.add(a2);
		n1.setApplications(apps);

		NodeGroup ng1 = new NodeGroup();
		List<Node> nodes = new ArrayList<>();
		nodes.add(n1);
		ng1.setNodes(nodes);

		System sys = new System();
		List<NodeGroup> ngs = new ArrayList<>();
		ngs.add(ng1);
		sys.setNodeGroups(ngs);

		Landscape landscape = new Landscape();
		List<System> systems = new ArrayList<>();
		systems.add(sys);
		landscape.setSystems(systems);

		rcLandscape = new RanCorrLandscape(landscape);
	}

	/**
	 * This method tests the LocalAlgorithm for various amounts of threads.
	 */
	@Test
	public void localAlgorithmTest() {
		// correct landscape?
		assertTrue(rcLandscape.getOperations().size() == 4);
		assertTrue("value=" + rcLandscape.getClasses().size(), rcLandscape.getClasses().size() == 3);
		assertTrue(rcLandscape.getPackages().size() == 2);
		assertTrue(rcLandscape.getApplications().size() == 2);

		// 1 Thread
		AbstractRanCorrAlgorithm alg = new LocalAlgorithm();
		RanCorrConfiguration.numberOfThreads = 1;
		doAlgorithm(alg);

		// 2 Threads
		RanCorrConfiguration.numberOfThreads = 2;
		for (int i = 0; i < 100; i++) {
			doAlgorithm(alg);
		}

		// 8 Threads
		RanCorrConfiguration.numberOfThreads = 8;
		for (int i = 0; i < 100; i++) {
			doAlgorithm(alg);
		}
	}

	private void doAlgorithm(AbstractRanCorrAlgorithm alg) {
		alg.calculate(rcLandscape);

		boolean cl1Done = false;
		boolean cl2Done = false;
		boolean cl3Done = false;
		for (Clazz clazz : rcLandscape.getClasses()) {
			if (withEpsilon(clazz.getRootCauseRating(), 0.5125d, 0.01d)) {
				cl1Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.3d, 0.01d)) {
				cl2Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.1d, 0.01d)) {
				cl3Done = true;
			} else {
				fail("Failed: RCR=" + clazz.getRootCauseRating());
			}
		}

		assertTrue(cl1Done && cl2Done && cl3Done);
	}

	private boolean withEpsilon(double is, double should, double epsilon) {
		return (Math.abs(is - should) - epsilon) <= 0.0d;
	}

}
