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

public class MeshAlgorithmTest {

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
		as1.put(6l, 0.6d); // Average: 0,45 / -0.1
		op1.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as1);

		CommunicationClazz op2 = new CommunicationClazz();
		TreeMapLongDoubleIValue as2 = new TreeMapLongDoubleIValue();
		as2.put(1l, 0.3d);
		as2.put(2l, 0.5d);
		as2.put(5l, 0.7d);
		as2.put(6l, 0.8d); // Average: 0,575 / 0,15
		op2.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as2);

		CommunicationClazz op4 = new CommunicationClazz();
		TreeMapLongDoubleIValue as4 = new TreeMapLongDoubleIValue();
		as4.put(1l, 0.3d);
		as4.put(2l, -0.3d);
		as4.put(5l, 0.3d);
		as4.put(6l, -0.3d); // Average: 0.3 / -0.4
		op4.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as4);

		CommunicationClazz op5 = new CommunicationClazz();
		TreeMapLongDoubleIValue as5 = new TreeMapLongDoubleIValue();
		as5.put(1l, 0.4d);
		as5.put(2l, -0.3d);
		as5.put(5l, 0.3d);
		as5.put(6l, -0.4d); // Average: 0.35 / -0.3
		op5.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as5);

		CommunicationClazz op6 = new CommunicationClazz();
		TreeMapLongDoubleIValue as6 = new TreeMapLongDoubleIValue();
		as6.put(1l, 0.4d);
		as6.put(2l, -0.3d);
		as6.put(5l, 0.5d);
		as6.put(6l, -0.4d); // Average: 0.4 / -0.2
		op6.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as6);

		CommunicationClazz op7 = new CommunicationClazz();
		TreeMapLongDoubleIValue as7 = new TreeMapLongDoubleIValue();
		as7.put(1l, 0.2d);
		as7.put(2l, -0.2d);
		as7.put(5l, 0.2d);
		as7.put(6l, -0.2d); // Average: 0.2 / -0.6
		op7.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as7);

		CommunicationClazz op9 = new CommunicationClazz();
		TreeMapLongDoubleIValue as9 = new TreeMapLongDoubleIValue();
		as9.put(1l, 0.2d);
		as9.put(2l, -0.4d);
		as9.put(5l, 0.4d);
		as9.put(6l, -0.2d); // Average: 0.3 / -0.4
		op9.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as9);

		CommunicationClazz op10 = new CommunicationClazz();
		TreeMapLongDoubleIValue as10 = new TreeMapLongDoubleIValue();
		as10.put(1l, 0.2d);
		as10.put(2l, -0.4d);
		as10.put(5l, 0.4d);
		as10.put(6l, -0.2d); // Average: 0.3 / -0.4
		op10.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as10);

		// c1: op1 source + target, op2 target, op3 target, op5 source, op6
		// source
		Clazz c1 = new Clazz();
		c1.setName("c1");
		op1.setSource(c1);
		op1.setTarget(c1);
		op2.setTarget(c1);
		op5.setSource(c1);
		op6.setSource(c1);

		// c2: op2 source, op4 source, op7 source + target, op9 target
		Clazz c2 = new Clazz();
		c2.setName("c2");
		op2.setSource(c2);
		op4.setSource(c2);
		op7.setSource(c2);
		op7.setTarget(c2);
		op9.setTarget(c2);

		// c4: op4 Target
		Clazz c4 = new Clazz();
		c4.setName("c4");
		op4.setTarget(c4);

		// c5: op5 Target
		Clazz c5 = new Clazz();
		c5.setName("c5");
		op5.setTarget(c5);

		// c6: op6 Target
		Clazz c6 = new Clazz();
		c6.setName("c6");
		op6.setTarget(c6);

		// c7: op9 Source
		Clazz c7 = new Clazz();
		c7.setName("c7");
		op9.setSource(c7);
		op10.setSource(c7);
		op10.setTarget(c7);

		// p1: c1, c2, c5, c6, c7
		Component p1 = new Component();
		c1.setParent(p1);
		c2.setParent(p1);
		c5.setParent(p1);
		c6.setParent(p1);
		c7.setParent(p1);
		List<Clazz> cs1 = new ArrayList<>();
		cs1.add(c1);
		cs1.add(c2);
		cs1.add(c5);
		cs1.add(c6);
		cs1.add(c7);
		p1.setClazzes(cs1);

		// p2: c3, c4
		Component p2 = new Component();
		c4.setParent(p2);
		List<Clazz> cs2 = new ArrayList<>();
		cs2.add(c4);
		p2.setClazzes(cs2);

		// a1: p1, p2
		Application a1 = new Application();
		p1.setBelongingApplication(a1);
		p2.setBelongingApplication(a1);
		List<CommunicationClazz> ops123 = new ArrayList<>();
		ops123.add(op1);
		ops123.add(op2);
		ops123.add(op4);
		ops123.add(op5);
		ops123.add(op6);
		ops123.add(op7);
		ops123.add(op9);
		ops123.add(op10);
		a1.setCommunications(ops123);
		List<Component> components1 = new ArrayList<>();
		components1.add(p1);
		components1.add(p2);
		a1.setComponents(components1);

		// n1: a1
		Node n1 = new Node();
		List<Application> apps = new ArrayList<>();
		apps.add(a1);
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
	public void meshAlgorithmTest() {
		// correct landscape?
		assertTrue(rcLandscape.getOperations().size() == 8);
		assertTrue("value=" + rcLandscape.getClasses().size(), rcLandscape.getClasses().size() == 6);
		assertTrue(rcLandscape.getPackages().size() == 2);
		assertTrue(rcLandscape.getApplications().size() == 1);

		// 1 Thread
		AbstractRanCorrAlgorithm alg = new MeshAlgorithm();
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
		boolean cl4Done = false;
		boolean cl5Done = false;
		boolean cl6Done = false;
		boolean cl7Done = false;

		for (Clazz clazz : rcLandscape.getClasses()) {
			// fail("C1:" + clazz.getRootCauseRating());
			if (withEpsilon(clazz.getRootCauseRating(), 0.5d, 0.01d)) {
				cl1Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.26d, 0.01d)) {
				cl2Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.3d, 0.01d)) {
				cl4Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.357d, 0.01d)) {
				cl5Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.47d, 0.01d)) {
				cl6Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.321d, 0.01d)) {
				cl7Done = true;
			} else {
				fail("Failed: RCR=" + clazz.getRootCauseRating() + clazz.getName());
			}
		}
		assertTrue("cl1NotDone", cl1Done);
		assertTrue("cl2NotDone", cl2Done);
		assertTrue("cl4NotDone", cl4Done);
		assertTrue("cl5NotDone", cl5Done);
		assertTrue("cl6NotDone", cl6Done);
		assertTrue("cl7NotDone", cl7Done);
	}

	private boolean withEpsilon(double is, double should, double epsilon) {
		return (Math.abs(is - should) - epsilon) <= 0.0d;
	}
}
