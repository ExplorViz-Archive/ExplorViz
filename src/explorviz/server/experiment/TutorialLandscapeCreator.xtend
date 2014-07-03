package explorviz.server.experiment

import explorviz.shared.model.Landscape
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.Node
import explorviz.shared.model.Application
import explorviz.shared.model.Communication
import explorviz.shared.model.Component
import explorviz.shared.model.Clazz
import explorviz.shared.model.CommunicationClazz
import explorviz.shared.model.System
//import java.util.Random

class TutorialLandscapeCreator {
	var static int applicationId = 0

	def static createTutorialLandscape() {
		applicationId = 0

		val landscape = new Landscape()
		//landscape.hash = java.lang.System.currentTimeMillis
		landscape.hash = 1
		//landscape.activities = new Random().nextInt(300000)
		landscape.activities = 5400
		
		val ocnEditor = new System()
		ocnEditor.name = "OCN Editor"
		ocnEditor.parent = landscape
		landscape.systems.add(ocnEditor)
		
		val ocnEditorNodeGroup = createNodeGroup("10.0.1.1", landscape, ocnEditor)
		val ocnEditorNode = createNode("10.0.1.1", ocnEditorNodeGroup)
		val ocnEditorApp = createApplicationWithPicture("Frontend", ocnEditorNode, "logos/jira.png")
		
		val ocnEditorNodeGroup2 = createNodeGroup("10.0.1.2", landscape, ocnEditor)
		val ocnEditorNode2 = createNode("10.0.1.2", ocnEditorNodeGroup2)
		val ocnEditorApp2 = createApplicationWithPicture("Database", ocnEditorNode2, "logos/jira.png")
		ocnEditorApp2.database = true

		ocnEditorNodeGroup.nodes.add(ocnEditorNode)
		ocnEditor.nodeGroups.add(ocnEditorNodeGroup)
		ocnEditorNodeGroup2.nodes.add(ocnEditorNode2)
		ocnEditor.nodeGroups.add(ocnEditorNodeGroup2)
		
		val ocnDatabase = new System()
		ocnDatabase.name = "OCN Database"
		ocnDatabase.parent = landscape
		landscape.systems.add(ocnDatabase)
		
		val ocnDatabaseNodeGroup = createNodeGroup("10.0.2.1", landscape, ocnDatabase)
		val ocnDatabaseNode = createNode("10.0.2.1", ocnDatabaseNodeGroup)
		val ocnDatabaseApp = createApplicationWithPicture("Interface", ocnDatabaseNode, "logos/jira.png")
		
		val ocnDatabaseNodeGroup2 = createNodeGroup("10.0.2.2", landscape, ocnDatabase)
		val ocnDatabaseNode2 = createNode("10.0.2.2", ocnDatabaseNodeGroup2)
		val ocnDatabaseApp2 = createApplicationWithPicture("Database", ocnDatabaseNode2, "logos/jira.png")
		ocnDatabaseApp2.database = true
		
		ocnDatabaseNodeGroup.nodes.add(ocnDatabaseNode)
		ocnDatabase.nodeGroups.add(ocnDatabaseNodeGroup)
		ocnDatabaseNodeGroup2.nodes.add(ocnDatabaseNode2)
		ocnDatabase.nodeGroups.add(ocnDatabaseNodeGroup2)
		
		val kielprints = new System()
		kielprints.name = "OceanRep"
		kielprints.parent = landscape
		landscape.systems.add(kielprints)
		
		val kielprintsNodeGroup = createNodeGroup("10.0.3.1", landscape, kielprints)
		val kielprintsNode = createNode("10.0.3.1", kielprintsNodeGroup)
		val kielprintsApp = createApplicationWithPicture("Webinterface", kielprintsNode, "logos/jira.png")
		
		val kielprintsApp2 = createApplicationWithPicture("Eprints", kielprintsNode, "logos/jira.png")
		
		val kielprintsNodeGroup2 = createNodeGroup("10.0.3.2", landscape, kielprints)
		val kielprintsNode2 = createNode("10.0.3.2", kielprintsNodeGroup2)
		val kielprintsApp3 = createApplicationWithPicture("Database", kielprintsNode2, "logos/jira.png")
		kielprintsApp3.database = true

		kielprintsNodeGroup.nodes.add(kielprintsNode)
		kielprints.nodeGroups.add(kielprintsNodeGroup)
		kielprintsNodeGroup2.nodes.add(kielprintsNode2)
		kielprints.nodeGroups.add(kielprintsNodeGroup2)
		
		val portal = new System()
		portal.name = "OSIS-Kiel"
		portal.parent = landscape
		landscape.systems.add(portal)
		
		val portalNodeGroup = createNodeGroup("10.0.4.1", landscape, portal)
		val portalNode = createNode("10.0.4.1", portalNodeGroup)
		val portalApp = createApplicationWithPicture("Wiki", portalNode, "logos/jira.png")
		
		val portalNodeGroup2 = createNodeGroup("10.0.4.2", landscape, portal)
		val portalNode2 = createNode("10.0.4.2", portalNodeGroup2)
		val portalApp2 = createApplicationWithPicture("Artifacts", portalNode2, "logos/jira.png")
		portalApp2.database = true

		portalNodeGroup.nodes.add(portalNode)
		portal.nodeGroups.add(portalNodeGroup)
		portalNodeGroup2.nodes.add(portalNode2)
		portal.nodeGroups.add(portalNodeGroup2)
		
		val pangea = new System()
		pangea.name = "WDC-Mare"
		pangea.parent = landscape
		landscape.systems.add(pangea)
		
		val pangeaNodeGroup = createNodeGroup("10.0.5.1", landscape, pangea)
		val pangeaNode = createNode("10.0.5.1", pangeaNodeGroup)
		val pangeaApp = createApplicationWithPicture("4D", pangeaNode, "logos/jira.png")
		
		val pangeaNodeGroup2 = createNodeGroup("10.0.5.2", landscape, pangea)
		val pangeaNode2 = createNode("10.0.5.2", pangeaNodeGroup2)
		val pangeaApp2 = createApplicationWithPicture("Jira", pangeaNode2, "logos/jira.png")
		
		val pangeaApp3 = createApplicationWithPicture("PostgreSQL", pangeaNode2, "logos/jira.png")
		pangeaApp3.database = true

		pangeaNodeGroup.nodes.add(pangeaNode)
		pangea.nodeGroups.add(pangeaNodeGroup)
		pangeaNodeGroup2.nodes.add(pangeaNode2)
		pangea.nodeGroups.add(pangeaNodeGroup2)

		val pubflow = new System()
		pubflow.name = "PubFlow"
		pubflow.parent = landscape
		landscape.systems.add(pubflow)

		val jiraNodeGroup = createNodeGroup("10.0.0.1 - 10.0.0.2", landscape, pubflow)

		val jira1Node = createNode("10.0.0.1", jiraNodeGroup)
		val jira1 = createApplicationWithPicture("Jira", jira1Node, "logos/jira.png")

		val jira2Node = createNode("10.0.0.2", jiraNodeGroup)
		val jira2 = createApplicationWithPicture("Jira", jira2Node, "logos/jira.png")

		jiraNodeGroup.nodes.add(jira1Node)
		jiraNodeGroup.nodes.add(jira2Node)
		pubflow.nodeGroups.add(jiraNodeGroup)

		val postgreSQLNodeGroup = createNodeGroup("10.0.0.3",landscape, pubflow)
		val postgreSQLNode = createNode("10.0.0.3", postgreSQLNodeGroup)
		val postgreSQL = createDatabaseWithPicture("PostgreSQL", postgreSQLNode, "logos/postgresql.png")

		postgreSQLNodeGroup.nodes.add(postgreSQLNode)
		pubflow.nodeGroups.add(postgreSQLNodeGroup)

		val workflowNodeGroup = createNodeGroup("10.0.0.4 - 10.0.0.7",landscape, pubflow)

		val workflow1Node = createNode("10.0.0.4", workflowNodeGroup)
		val workflow1 = createApplicationWithPicture("Workflow", workflow1Node, "logos/jBPM.png")
		val provenance1 = createApplication("Provenance", workflow1Node)

		val workflow2Node = createNode("10.0.0.5", workflowNodeGroup)
		val workflow2 = createApplicationWithPicture("Workflow", workflow2Node, "logos/jBPM.png")
		val provenance2 = createApplication("Provenance", workflow2Node)

		val workflow3Node = createNode("10.0.0.6", workflowNodeGroup)
		val workflow3 = createApplicationWithPicture("Workflow", workflow3Node, "logos/jBPM.png")
		val provenance3 = createApplication("Provenance", workflow3Node)

		val workflow4Node = createNode("10.0.0.7", workflowNodeGroup)
		val workflow4 = createApplicationWithPicture("Workflow", workflow4Node, "logos/jBPM.png")
		val provenance4 = createApplication("Provenance", workflow4Node)

		workflowNodeGroup.nodes.add(workflow1Node)
		workflowNodeGroup.nodes.add(workflow2Node)
		workflowNodeGroup.nodes.add(workflow3Node)
		workflowNodeGroup.nodes.add(workflow4Node)

		pubflow.nodeGroups.add(workflowNodeGroup)

		val neo4jNodeGroup = createNodeGroup("10.0.0.9",landscape, pubflow)
		val neo4jNode = createNode("10.0.0.9", neo4jNodeGroup)
		val neo4j = createDatabaseWithPicture("Neo4j", neo4jNode, "logos/Neo4J.png")
		//createJPetStoreDummyApplication(neo4j)
		createNeo4JDummyApplication(neo4j)

		neo4jNodeGroup.nodes.add(neo4jNode)
		pubflow.nodeGroups.add(neo4jNodeGroup)

		val cacheNodeGroup = createNodeGroup("10.0.0.8", landscape, pubflow)
		val cacheNode = createNode("10.0.0.8", cacheNodeGroup)
		val cache = createApplication("Cache", cacheNode)
		val hyperSQL = createDatabaseWithPicture("HyperSQL", cacheNode, "logos/hypersql.png")

		cacheNodeGroup.nodes.add(cacheNode)
		pubflow.nodeGroups.add(cacheNodeGroup)
		
		createCommunication(pangeaApp, pangeaApp2, landscape, 100)
		createCommunication(pangeaApp2, pangeaApp3, landscape, 100)
		createCommunication(ocnEditorApp, ocnDatabaseApp, landscape, 100)
		createCommunication(ocnDatabaseApp, ocnDatabaseApp2, landscape, 100)
		createCommunication(ocnEditorApp, ocnEditorApp2, landscape, 100)
		createCommunication(ocnDatabaseApp, workflow1, landscape, 100)
		createCommunication(workflow1, pangeaApp, landscape, 100)

		createCommunication(workflow1, kielprintsApp, landscape, 100)
		createCommunication(kielprintsApp, kielprintsApp2, landscape, 100)
		createCommunication(kielprintsApp2, kielprintsApp3, landscape, 100)
		
		createCommunication(workflow1, portalApp, landscape, 100)
		createCommunication(portalApp, portalApp2, landscape, 100)

		createCommunication(jira1, postgreSQL, landscape, 100)
		createCommunication(jira2, postgreSQL, landscape, 200)

		createCommunication(jira1, workflow1, landscape, 100)
		createCommunication(jira1, workflow2, landscape, 500)
		createCommunication(jira1, workflow3, landscape, 100)

		createCommunication(jira2, workflow4, landscape, 200)

		createCommunication(workflow1, provenance1, landscape, 400)
		createCommunication(workflow2, provenance2, landscape, 300)
		createCommunication(workflow3, provenance3, landscape, 500)
		createCommunication(workflow4, provenance4, landscape, 200)

		createCommunication(workflow1, cache, landscape, 100)
		createCommunication(workflow2, cache, landscape, 100)
		createCommunication(workflow3, cache, landscape, 300)
		createCommunication(workflow4, cache, landscape, 100)

		createCommunication(cache, hyperSQL, landscape, 300)

		createCommunication(provenance1, neo4j, landscape, 100)
		createCommunication(provenance2, neo4j, landscape, 200)
		createCommunication(provenance3, neo4j, landscape, 300)
		createCommunication(provenance4, neo4j, landscape, 100)
		
		val communication3 = new Communication()
		communication3.source = provenance1
		communication3.target = neo4j
		communication3.sourceClazz = new Clazz()
		communication3.sourceClazz.fullQualifiedName = "xxxx"
		communication3.targetClazz = new Clazz()
		communication3.targetClazz.fullQualifiedName = "org.neo4j.graphdb.Label"
		communication3.requests = 100
		landscape.applicationCommunication.add(communication3)

		landscape
	}

	def private static createNodeGroup(String name, Landscape parent, System system) {
		val nodeGroup = new NodeGroup()
		nodeGroup.name = name
		nodeGroup.parent = system
		nodeGroup
	}

	def private static createNode(String ipAddress, NodeGroup parent) {
		val node = new Node()
		node.ipAddress = ipAddress
		node.parent = parent
		node
	}

	def private static createApplication(String name, Node parent) {
		val application = new Application()

		val newId = applicationId
		application.id = newId
		applicationId = applicationId + 1
		application.parent = parent

		application.name = name
		parent.applications.add(application)
		application
	}

	def private static createApplicationWithPicture(String name, Node node, String relativeImagePath) {
		val application = createApplication(name, node)
		//application.image = relativeImagePath
		application
	}

	def private static createDatabase(String name, Node node) {
		val application = createApplication(name, node)
		application.database = true
		application
	}

	def private static createDatabaseWithPicture(String name, Node node, String relativeImagePath) {
		val application = createDatabase(name, node)
		//application.image = relativeImagePath
		application
	}

	def private static createCommunication(Application source, Application target, Landscape landscape,
		int requests) {
		val communication = new Communication()
		communication.source = source
		communication.target = target
		communication.requests = requests
		landscape.applicationCommunication.add(communication)
	}

	def private static createClazz(String name, Component component, int instanceCount) {
		val clazz = new Clazz()
		clazz.name = name
		clazz.fullQualifiedName = component.fullQualifiedName + "." + name
		clazz.instanceCount = instanceCount
		clazz.parent = component
		component.clazzes.add(clazz)
		clazz
	}
	
	def private static createComponent(String name, Component parent, Application app) {
		val component = new Component()
		component.name = name
		component.parentComponent = parent
		component.belongingApplication = app
		if (parent != null) {
			component.fullQualifiedName = parent.fullQualifiedName + "." + name
			parent.children.add(component)
		} else {
			component.fullQualifiedName = name
		}
		component
	}
	
	def private static createCommuClazz(int requests, Clazz source, Clazz target, Application application) {
		val commu = new CommunicationClazz()
		commu.addRuntimeInformation(0, requests, 10)

		commu.source = source
		commu.target = target
		
		application.communications.add(commu)
		
		commu
	}
	
	def private static createNeo4JDummyApplication(Application application) {
		val org = createComponent("org", null, application)
		application.components.add(org)
		val neo4j = createComponent("neo4j", org, application)

		val graphdb = createComponent("graphdb", neo4j, application)
		val graphDbClazz = createClazz("Label", graphdb, 20)
		createClazz("Label2", graphdb, 20)
		createClazz("Label3", graphdb, 20)
		createClazz("Label4", graphdb, 20)
		createClazz("Label5", graphdb, 20)

		val helpers = createComponent("helpers", neo4j, application)
		val helpersClazz = createClazz("x", helpers, 30)
		createClazz("x2", helpers, 40)
		createClazz("x3", helpers, 35)
		createClazz("x4", helpers, 35)
		createClazz("x5", helpers, 35)
		
		val tooling = createComponent("tooling", neo4j, application)
		val toolingClazz = createClazz("AccountSqlMapDao", tooling, 5)
		createClazz("BaseSqlMapDao", tooling, 20)
		createClazz("CategorySqlMapDao", tooling, 30)
		createClazz("ItemSqlMapDao", tooling, 35)
		createClazz("ProductSqlMapDao", tooling, 20)
		createClazz("SequenceSqlMapDao", tooling, 15)

		val unsafe = createComponent("unsafe", neo4j, application)
		val unsafeClazz = createClazz("AbstractBean", unsafe, 20)
		createClazz("CartBean", unsafe, 40)
		
		val kernel = createComponent("kernel", neo4j, application)
		
		val api = createComponent("api", kernel, application)
		val apiClazz = createClazz("cleanupX", api, 25)
		createClazz("cleanupX", api, 25)
		val configuration = createComponent("configuration", kernel, application)
		val configurationClazz = createClazz("cleanupX", configuration, 35)
		createClazz("cleanupX", configuration, 5)
		val myextension = createComponent("extension", kernel, application)
		createClazz("cleanupX", myextension, 25)
		createClazz("cleanupX", myextension, 5)
		val guard = createComponent("guard", kernel, application)
		val guardClazz = createClazz("cleanupX", guard, 35)
		createClazz("cleanupX", guard, 25)
		
		val impl = createComponent("impl", kernel, application)
		val implClazz = createClazz("cleanupX", impl, 45)
		val annotations = createComponent("annotations", impl, application)
		createClazz("cleanupX", annotations, 35)
		val apiImpl = createComponent("api", impl, application)
		val apiImplClazz = createClazz("cleanupX", apiImpl, 25)
		val cache = createComponent("cache", impl, application)
		createClazz("cleanupX", cache, 45)
		val persistence = createComponent("persistence", impl, application)
		createClazz("AccountSqlMapDao", persistence, 45)
		
		val info = createComponent("info", kernel, application)
		createClazz("AccountSqlMapDao", info, 5)
		createClazz("AccountSqlMapDao", info, 25)
		val lifecycle = createComponent("lifecycle", kernel, application)
		val lifecycleClazz = createClazz("AccountSqlMapDao", lifecycle, 25)
		createClazz("AccountSqlMapDao", lifecycle, 15)
		
		val logging = createComponent("logging", kernel, application)
		val loggingClazz = createClazz("AccountSqlMapDao", logging, 25)
		createClazz("AccountSqlMapDao2", logging, 5)
		
		createCommuClazz(40, graphDbClazz, helpersClazz, application)
		createCommuClazz(100, toolingClazz, implClazz, application)
		createCommuClazz(60, implClazz, helpersClazz, application)
		createCommuClazz(60, implClazz, apiImplClazz, application)
		createCommuClazz(1000, implClazz, loggingClazz, application)
		createCommuClazz(100, guardClazz, unsafeClazz, application)
		createCommuClazz(100, apiClazz, configurationClazz, application)
		createCommuClazz(150, lifecycleClazz, loggingClazz, application)
		createCommuClazz(1200, guardClazz, implClazz, application)
	}
	
	
		def static createTutorialLandscape2() {
		applicationId = 0

		val landscape = new Landscape()
		//landscape.hash = java.lang.System.currentTimeMillis
		landscape.hash = 2
		//landscape.activities = new Random().nextInt(300000)
		landscape.activities = 6000
		
		val ocnEditor = new System()
		ocnEditor.name = "OCN Editor"
		ocnEditor.parent = landscape
		landscape.systems.add(ocnEditor)
		
		val ocnEditorNodeGroup = createNodeGroup("10.0.1.1", landscape, ocnEditor)
		val ocnEditorNode = createNode("10.0.1.1", ocnEditorNodeGroup)
		val ocnEditorApp = createApplicationWithPicture("Frontend", ocnEditorNode, "logos/jira.png")
		
		val ocnEditorNodeGroup2 = createNodeGroup("10.0.1.2", landscape, ocnEditor)
		val ocnEditorNode2 = createNode("10.0.1.2", ocnEditorNodeGroup2)
		val ocnEditorApp2 = createApplicationWithPicture("Database", ocnEditorNode2, "logos/jira.png")
		ocnEditorApp2.database = true

		ocnEditorNodeGroup.nodes.add(ocnEditorNode)
		ocnEditor.nodeGroups.add(ocnEditorNodeGroup)
		ocnEditorNodeGroup2.nodes.add(ocnEditorNode2)
		ocnEditor.nodeGroups.add(ocnEditorNodeGroup2)
		
		val ocnDatabase = new System()
		ocnDatabase.name = "OCN Database"
		ocnDatabase.parent = landscape
		landscape.systems.add(ocnDatabase)
		
		val ocnDatabaseNodeGroup = createNodeGroup("10.0.2.1", landscape, ocnDatabase)
		val ocnDatabaseNode = createNode("10.0.2.1", ocnDatabaseNodeGroup)
		val ocnDatabaseApp = createApplicationWithPicture("Interface", ocnDatabaseNode, "logos/jira.png")
		
		val ocnDatabaseNodeGroup2 = createNodeGroup("10.0.2.2", landscape, ocnDatabase)
		val ocnDatabaseNode2 = createNode("10.0.2.2", ocnDatabaseNodeGroup2)
		val ocnDatabaseApp2 = createApplicationWithPicture("Database", ocnDatabaseNode2, "logos/jira.png")
		ocnDatabaseApp2.database = true
		
		ocnDatabaseNodeGroup.nodes.add(ocnDatabaseNode)
		ocnDatabase.nodeGroups.add(ocnDatabaseNodeGroup)
		ocnDatabaseNodeGroup2.nodes.add(ocnDatabaseNode2)
		ocnDatabase.nodeGroups.add(ocnDatabaseNodeGroup2)
		
		val kielprints = new System()
		kielprints.name = "OceanRep"
		kielprints.parent = landscape
		landscape.systems.add(kielprints)
		
		val kielprintsNodeGroup = createNodeGroup("10.0.3.1", landscape, kielprints)
		val kielprintsNode = createNode("10.0.3.1", kielprintsNodeGroup)
		val kielprintsApp = createApplicationWithPicture("Webinterface", kielprintsNode, "logos/jira.png")
		
		val kielprintsApp2 = createApplicationWithPicture("Eprints", kielprintsNode, "logos/jira.png")
		
		val kielprintsNodeGroup2 = createNodeGroup("10.0.3.2", landscape, kielprints)
		val kielprintsNode2 = createNode("10.0.3.2", kielprintsNodeGroup2)
		val kielprintsApp3 = createApplicationWithPicture("Database", kielprintsNode2, "logos/jira.png")
		kielprintsApp3.database = true

		kielprintsNodeGroup.nodes.add(kielprintsNode)
		kielprints.nodeGroups.add(kielprintsNodeGroup)
		kielprintsNodeGroup2.nodes.add(kielprintsNode2)
		kielprints.nodeGroups.add(kielprintsNodeGroup2)
		
		val portal = new System()
		portal.name = "OSIS-Kiel"
		portal.parent = landscape
		landscape.systems.add(portal)
		
		val portalNodeGroup = createNodeGroup("10.0.4.1", landscape, portal)
		val portalNode = createNode("10.0.4.1", portalNodeGroup)
		val portalApp = createApplicationWithPicture("Wiki", portalNode, "logos/jira.png")
		
		val portalNodeGroup2 = createNodeGroup("10.0.4.2", landscape, portal)
		val portalNode2 = createNode("10.0.4.2", portalNodeGroup2)
		val portalApp2 = createApplicationWithPicture("Artifacts", portalNode2, "logos/jira.png")
		portalApp2.database = true

		portalNodeGroup.nodes.add(portalNode)
		portal.nodeGroups.add(portalNodeGroup)
		portalNodeGroup2.nodes.add(portalNode2)
		portal.nodeGroups.add(portalNodeGroup2)
		
		val pangea = new System()
		pangea.name = "WDC-Mare"
		pangea.parent = landscape
		landscape.systems.add(pangea)
		
		val pangeaNodeGroup = createNodeGroup("10.0.5.1", landscape, pangea)
		val pangeaNode = createNode("10.0.5.1", pangeaNodeGroup)
		val pangeaApp = createApplicationWithPicture("4D", pangeaNode, "logos/jira.png")
		
		val pangeaNodeGroup2 = createNodeGroup("10.0.5.2", landscape, pangea)
		val pangeaNode2 = createNode("10.0.5.2", pangeaNodeGroup2)
		val pangeaApp2 = createApplicationWithPicture("Jira", pangeaNode2, "logos/jira.png")
		
		val pangeaApp3 = createApplicationWithPicture("PostgreSQL", pangeaNode2, "logos/jira.png")
		pangeaApp3.database = true

		pangeaNodeGroup.nodes.add(pangeaNode)
		pangea.nodeGroups.add(pangeaNodeGroup)
		pangeaNodeGroup2.nodes.add(pangeaNode2)
		pangea.nodeGroups.add(pangeaNodeGroup2)

		val pubflow = new System()
		pubflow.name = "PubFlow"
		pubflow.parent = landscape
		landscape.systems.add(pubflow)

		val jiraNodeGroup = createNodeGroup("10.0.0.1 - 10.0.0.2", landscape, pubflow)

		val jira1Node = createNode("10.0.0.1", jiraNodeGroup)
		val jira1 = createApplicationWithPicture("Jira", jira1Node, "logos/jira.png")

		val jira2Node = createNode("10.0.0.2", jiraNodeGroup)
		val jira2 = createApplicationWithPicture("Jira", jira2Node, "logos/jira.png")

		jiraNodeGroup.nodes.add(jira1Node)
		jiraNodeGroup.nodes.add(jira2Node)
		pubflow.nodeGroups.add(jiraNodeGroup)

		val postgreSQLNodeGroup = createNodeGroup("10.0.0.3",landscape, pubflow)
		val postgreSQLNode = createNode("10.0.0.3", postgreSQLNodeGroup)
		val postgreSQL = createDatabaseWithPicture("PostgreSQL", postgreSQLNode, "logos/postgresql.png")

		postgreSQLNodeGroup.nodes.add(postgreSQLNode)
		pubflow.nodeGroups.add(postgreSQLNodeGroup)

		val workflowNodeGroup = createNodeGroup("10.0.0.4 - 10.0.0.7",landscape, pubflow)

		val workflow1Node = createNode("10.0.0.4", workflowNodeGroup)
		val workflow1 = createApplicationWithPicture("Workflow", workflow1Node, "logos/jBPM.png")
		val provenance1 = createApplication("Provenance", workflow1Node)

		val workflow2Node = createNode("10.0.0.5", workflowNodeGroup)
		val workflow2 = createApplicationWithPicture("Workflow", workflow2Node, "logos/jBPM.png")
		val provenance2 = createApplication("Provenance", workflow2Node)

		val workflow3Node = createNode("10.0.0.6", workflowNodeGroup)
		val workflow3 = createApplicationWithPicture("Workflow", workflow3Node, "logos/jBPM.png")
		val provenance3 = createApplication("Provenance", workflow3Node)

		val workflow4Node = createNode("10.0.0.7", workflowNodeGroup)
		val workflow4 = createApplicationWithPicture("Workflow", workflow4Node, "logos/jBPM.png")
		val provenance4 = createApplication("Provenance", workflow4Node)

		workflowNodeGroup.nodes.add(workflow1Node)
		workflowNodeGroup.nodes.add(workflow2Node)
		workflowNodeGroup.nodes.add(workflow3Node)
		workflowNodeGroup.nodes.add(workflow4Node)

		pubflow.nodeGroups.add(workflowNodeGroup)

		val neo4jNodeGroup = createNodeGroup("10.0.0.9",landscape, pubflow)
		val neo4jNode = createNode("10.0.0.9", neo4jNodeGroup)
		val neo4j = createDatabaseWithPicture("Neo4j", neo4jNode, "logos/Neo4J.png")
		createNeo4JDummyApplication(neo4j)

		neo4jNodeGroup.nodes.add(neo4jNode)
		pubflow.nodeGroups.add(neo4jNodeGroup)

		val cacheNodeGroup = createNodeGroup("10.0.0.8", landscape, pubflow)
		val cacheNode = createNode("10.0.0.8", cacheNodeGroup)
		val cache = createApplication("Cache", cacheNode)
		val hyperSQL = createDatabaseWithPicture("HyperSQL", cacheNode, "logos/hypersql.png")

		cacheNodeGroup.nodes.add(cacheNode)
		pubflow.nodeGroups.add(cacheNodeGroup)
		
		createCommunication(pangeaApp, pangeaApp2, landscape, 100)
		createCommunication(pangeaApp2, pangeaApp3, landscape, 100)
		createCommunication(ocnEditorApp, ocnDatabaseApp, landscape, 100)
		createCommunication(ocnDatabaseApp, ocnDatabaseApp2, landscape, 100)
		createCommunication(ocnEditorApp, ocnEditorApp2, landscape, 100)
		createCommunication(ocnDatabaseApp, workflow1, landscape, 100)
		createCommunication(workflow1, pangeaApp, landscape, 100)

		createCommunication(workflow1, kielprintsApp, landscape, 700)
		createCommunication(kielprintsApp, kielprintsApp2, landscape, 100)
		createCommunication(kielprintsApp2, kielprintsApp3, landscape, 100)
		
		createCommunication(workflow1, portalApp, landscape, 100)
		createCommunication(portalApp, portalApp2, landscape, 100)

		createCommunication(jira1, postgreSQL, landscape, 100)
		createCommunication(jira2, postgreSQL, landscape, 200)

		createCommunication(jira1, workflow1, landscape, 100)
		createCommunication(jira1, workflow2, landscape, 500)
		createCommunication(jira1, workflow3, landscape, 100)

		createCommunication(jira2, workflow4, landscape, 200)

		createCommunication(workflow1, provenance1, landscape, 400)
		createCommunication(workflow2, provenance2, landscape, 300)
		createCommunication(workflow3, provenance3, landscape, 500)
		createCommunication(workflow4, provenance4, landscape, 200)

		createCommunication(workflow1, cache, landscape, 100)
		createCommunication(workflow2, cache, landscape, 100)
		createCommunication(workflow3, cache, landscape, 300)
		createCommunication(workflow4, cache, landscape, 100)

		createCommunication(cache, hyperSQL, landscape, 300)

		createCommunication(provenance1, neo4j, landscape, 100)
		createCommunication(provenance2, neo4j, landscape, 200)
		createCommunication(provenance3, neo4j, landscape, 300)
		createCommunication(provenance4, neo4j, landscape, 100)
		
		val communication3 = new Communication()
		communication3.source = provenance1
		communication3.target = neo4j
		communication3.sourceClazz = new Clazz()
		communication3.sourceClazz.fullQualifiedName = "xxxx"
		communication3.targetClazz = new Clazz()
		communication3.targetClazz.fullQualifiedName = "org.neo4j.graphdb.Label"
		communication3.requests = 100
		landscape.applicationCommunication.add(communication3)

		landscape
	}
	
}
