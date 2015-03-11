package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.shared.model.*;
import explorviz.shared.model.System;

public final class RCDTestLandscapeBuilder {

	private RCDTestLandscapeBuilder() {
	}

	/**
	 * We use this landscape to test the local algorithm.
	 *
	 * @return test landscape
	 */
	public static Landscape getLocalAlgorithmLandscape() {
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

		return landscape;
	}

	/**
	 * We use this landscape to test the neighbour algorithm.
	 *
	 * @return test landscape
	 */
	public static Landscape getNeighbourAlgorithmLandscape() {
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

		CommunicationClazz op3 = new CommunicationClazz();
		TreeMapLongDoubleIValue as3 = new TreeMapLongDoubleIValue();
		as3.put(1l, 0.1d);
		as3.put(2l, 0.1d);
		as3.put(5l, 0.1d);
		as3.put(6l, 0.1d); // Average: 0,1 / -0.8
		op3.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as3);

		CommunicationClazz op4 = new CommunicationClazz();
		TreeMapLongDoubleIValue as4 = new TreeMapLongDoubleIValue();
		as4.put(1l, 0.3d);
		as4.put(2l, -0.3d);
		as4.put(5l, 0.3d);
		as4.put(6l, -0.3d); // Average: -0.4
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
		as6.put(6l, -0.4d); // Average: -0.2
		op6.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as6);

		CommunicationClazz op7 = new CommunicationClazz();
		TreeMapLongDoubleIValue as7 = new TreeMapLongDoubleIValue();
		as7.put(1l, 0.2d);
		as7.put(2l, -0.2d);
		as7.put(5l, 0.2d);
		as7.put(6l, -0.2d); // Average: 0.2 / -0.6
		op7.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as7);

		CommunicationClazz op8 = new CommunicationClazz();
		TreeMapLongDoubleIValue as8 = new TreeMapLongDoubleIValue();
		as8.put(1l, 0.2d);
		as8.put(2l, -0.3d);
		as8.put(5l, 0.3d);
		as8.put(6l, -0.2d); // Average: -0.5
		op8.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as8);

		// c1: op1 source + target, op2 target, op3 target, op5 source, op6
		// source
		Clazz c1 = new Clazz();
		c1.setName("c1");
		op1.setSource(c1);
		op1.setTarget(c1);
		op2.setTarget(c1);
		op3.setTarget(c1);
		op5.setSource(c1);
		op6.setSource(c1);

		// c2: op2 source, op7 source + target
		Clazz c2 = new Clazz();
		c2.setName("c2");
		op2.setSource(c2);
		op7.setSource(c2);
		op7.setTarget(c2);

		// c3: op3 Source, op 4 source, op8 source + target
		Clazz c3 = new Clazz();
		c3.setName("c3");
		op3.setSource(c3);
		op4.setSource(c3);
		op8.setSource(c3);
		op8.setTarget(c3);

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

		// p1: c1, c2, c5, c6
		Component p1 = new Component();
		c1.setParent(p1);
		c2.setParent(p1);
		c5.setParent(p1);
		c6.setParent(p1);
		List<Clazz> cs1 = new ArrayList<>();
		cs1.add(c1);
		cs1.add(c2);
		cs1.add(c5);
		cs1.add(c6);
		p1.setClazzes(cs1);

		// p2: c3, c4
		Component p2 = new Component();
		c3.setParent(p2);
		c4.setParent(p2);
		List<Clazz> cs2 = new ArrayList<>();
		cs2.add(c3);
		cs2.add(c4);
		p2.setClazzes(cs2);

		// a1: p1, p2
		Application a1 = new Application();
		p1.setBelongingApplication(a1);
		p2.setBelongingApplication(a1);
		List<CommunicationClazz> ops123 = new ArrayList<>();
		ops123.add(op1);
		ops123.add(op2);
		ops123.add(op3);
		ops123.add(op4);
		ops123.add(op5);
		ops123.add(op6);
		ops123.add(op7);
		ops123.add(op8);
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

		return landscape;
	}

	/**
	 * We use this landscape to test the mesh algorithm.
	 *
	 * @return test landscape
	 */
	public static Landscape getMeshAlgorithmLandscape() {
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

		// c7: op9 Source, op10 Source & Target
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

		return landscape;
	}

	/**
	 * We use this landscape to test the advanced mesh algorithm.
	 *
	 * @return test landscape
	 */
	public static Landscape getRefinedMeshAlgorithmLandscape() {
		CommunicationClazz op1 = new CommunicationClazz();
		TreeMapLongDoubleIValue as1 = new TreeMapLongDoubleIValue();
		as1.put(1l, -0.2d);
		as1.put(2l, -0.4d);
		as1.put(5l, -0.6d);
		as1.put(6l, -0.6d); // Average: 0,45 / -0.1
		op1.putGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE, as1);

		CommunicationClazz op2 = new CommunicationClazz();
		TreeMapLongDoubleIValue as2 = new TreeMapLongDoubleIValue();
		as2.put(1l, -0.3d);
		as2.put(2l, -0.5d);
		as2.put(5l, -0.7d);
		as2.put(6l, -0.8d); // Average: 0,575 / 0,15
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

		return landscape;
	}

}
