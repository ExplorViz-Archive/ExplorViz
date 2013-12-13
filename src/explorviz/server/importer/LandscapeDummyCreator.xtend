package explorviz.server.importer

import explorviz.shared.model.Landscape
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.Node
import explorviz.shared.model.Application
import explorviz.shared.model.Communication
import explorviz.shared.model.Component
import explorviz.shared.model.Clazz
import explorviz.shared.model.CommunicationClazz

class LandscapeDummyCreator {
	var static int applicationId = 0

	def static createDummyLandscape() {
		applicationId = 0

		val landscape = new Landscape()
		landscape.hash = 112120

		val jiraNodeGroup = createNodeGroup(landscape)

		val jira1Node = createNode("10.0.0.1", jiraNodeGroup)
		val jira1 = createApplicationWithPicture("Jira", jira1Node, "logos/jira.png")

		val jira2Node = createNode("10.0.0.2", jiraNodeGroup)
		val jira2 = createApplicationWithPicture("Jira", jira2Node, "logos/jira.png")

		jiraNodeGroup.nodes.add(jira1Node)
		jiraNodeGroup.nodes.add(jira2Node)
		landscape.nodeGroups.add(jiraNodeGroup)

		val postgreSQLNodeGroup = createNodeGroup(landscape)
		val postgreSQLNode = createNode("10.0.0.3", postgreSQLNodeGroup)
		val postgreSQL = createDatabaseWithPicture("PostgreSQL", postgreSQLNode, "logos/postgresql.png")

		postgreSQLNodeGroup.nodes.add(postgreSQLNode)
		landscape.nodeGroups.add(postgreSQLNodeGroup)

		val workflowNodeGroup = createNodeGroup(landscape)

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

		//        workflowNodeGroup.openedColor = new Vector4f(0.858f,0.933f,0.956f,1f)
		landscape.nodeGroups.add(workflowNodeGroup)

		val neo4jNodeGroup = createNodeGroup(landscape)
		val neo4jNode = createNode("10.0.0.9", neo4jNodeGroup)
		val neo4j = createDatabaseWithPicture("Neo4j", neo4jNode, "logos/Neo4J.png")
		//createJPetStoreDummyApplication(neo4j)
		createNeo4JDummyApplication(neo4j)

		neo4jNodeGroup.nodes.add(neo4jNode)
		landscape.nodeGroups.add(neo4jNodeGroup)

		val cacheNodeGroup = createNodeGroup(landscape)
		val cacheNode = createNode("10.0.0.8", cacheNodeGroup)
		val cache = createApplication("Cache", cacheNode)
		val hyperSQL = createDatabaseWithPicture("HyperSQL", cacheNode, "logos/hypersql.png")

		cacheNodeGroup.nodes.add(cacheNode)
		landscape.nodeGroups.add(cacheNodeGroup)

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

		landscape
	}

	def private static createNodeGroup(Landscape parent) {
		val nodeGroup = new NodeGroup()
		nodeGroup
	}

	def private static createNode(String ipAddress, NodeGroup parent) {
		val node = new Node()
		node.ipAddress = ipAddress
		node
	}

	def private static createApplication(String name, Node parent) {
		val application = new Application()

		val newId = applicationId
		application.id = newId
		applicationId = applicationId + 1

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
		int requestsPerSecond) {
		val communication = new Communication()
		communication.source = source
		communication.target = target
		communication.requestsPerSecond = requestsPerSecond
		landscape.applicationCommunication.add(communication)
	}

//	def private static createJPetStoreDummyApplication(Application application) {
//		val com = createComponent("com", null)
//		application.components.add(com)
//		val ibatis = createComponent("ibatis", com)
//		val jpetstore = createComponent("jpetstore", ibatis)
//
//		val domain = createComponent("domain", jpetstore)
//		val account = createClazz("Account", domain, 20)
//		createClazz("Cart", domain, 20)
//		createClazz("CartItem", domain, 30)
//		val category = createClazz("Category", domain, 30)
//		createClazz("Item", domain, 20)
//		createClazz("LineItem", domain, 40)
//		val order = createClazz("Order", domain, 20)
//		createClazz("Product", domain, 50)
//		createClazz("Sequence", domain, 10)
//
//		val service = createComponent("service", jpetstore)
//		val accountService = createClazz("AccountService", service, 30)
//		val categoryService = createClazz("CatalogService", service, 40)
//		val orderService = createClazz("OrderService", service, 35)
//
//		val persistence = createComponent("persistence", jpetstore)
//		createClazz("DaoConfig", persistence, 30)
//		
//		val iface = createComponent("iface", persistence)
//		val accountDao = createClazz("AccountDao", iface, 30)
//		createClazz("CategoryDao", iface, 10)
//		val catalogDao = createClazz("ItemDao", iface, 40)
//		val orderDao = createClazz("OrderDao", iface, 45)
//		createClazz("ProductDao", iface, 25)
//		createClazz("SequenceDao", iface, 20)
//		
//		val sqlmapdao = createComponent("sqlmapdao", persistence)
//		createClazz("AccountSqlMapDao", sqlmapdao, 5)
//		createClazz("BaseSqlMapDao", sqlmapdao, 20)
//		createClazz("CategorySqlMapDao", sqlmapdao, 30)
//		createClazz("ItemSqlMapDao", sqlmapdao, 35)
//		val orderSqlDao = createClazz("OrderSqlMapDao", sqlmapdao, 25)
//		createClazz("ProductSqlMapDao", sqlmapdao, 20)
//		createClazz("SequenceSqlMapDao", sqlmapdao, 15)
//
//		val presentation = createComponent("presentation", jpetstore)
//		createClazz("AbstractBean", presentation, 20)
//		val accountBean = createClazz("AccountBean", presentation, 30)
//		createClazz("CartBean", presentation, 40)
//		val catlogBean = createClazz("CatalogBean", presentation, 21)
//		val orderBean = createClazz("OrderBean", presentation, 25)
//
//		createCommuClazz(5, account, accountService, application)
//		createCommuClazz(20, category, categoryService, application)
//		createCommuClazz(60, order, orderService, application)
//		
//		createCommuClazz(30, accountService, accountDao, application)
//		createCommuClazz(35, categoryService, catalogDao, application)
//		
//		createCommuClazz(5, orderService, orderDao, application)
//		createCommuClazz(15, orderSqlDao, orderBean, application)
//		
//		createCommuClazz(40, accountDao, accountBean, application)
//		createCommuClazz(50, catalogDao, catlogBean, application)
//		createCommuClazz(20, orderDao, orderBean, application)
//	}

	def private static createClazz(String name, Component component, int instanceCount) {
		val clazz = new Clazz()
		clazz.name = name
		clazz.fullQualifiedName = component.fullQualifiedName + "." + name
		clazz.instanceCount = instanceCount
		component.clazzes.add(clazz)
		clazz
	}
	
	def private static createComponent(String name, Component parent) {
		val component = new Component()
		component.name = name
		if (parent != null) {
			component.fullQualifiedName = parent.fullQualifiedName + "." + name
			parent.children.add(component)
		} else {
			component.fullQualifiedName = name
		}
		component
	}
	
	def private static createCommuClazz(int requestsPerSecond, Clazz source, Clazz target, Application application) {
		val commu = new CommunicationClazz()
		commu.requestsPerSecond = requestsPerSecond

		commu.source = source
		commu.target = target
		
		application.communcations.add(commu)
		
		commu
	}
	
	def private static createNeo4JDummyApplication(Application application) {
		val org = createComponent("org", null)
		application.components.add(org)
		val neo4j = createComponent("neo4j", org)

		val graphdb = createComponent("graphdb", neo4j)
		val graphDbClazz = createClazz("Label", graphdb, 20)
		createClazz("Label2", graphdb, 20)
		createClazz("Label3", graphdb, 20)
		createClazz("Label4", graphdb, 20)
		createClazz("Label5", graphdb, 20)

		val helpers = createComponent("helpers", neo4j)
		val helpersClazz = createClazz("x", helpers, 30)
		createClazz("x2", helpers, 40)
		createClazz("x3", helpers, 35)
		createClazz("x4", helpers, 35)
		createClazz("x5", helpers, 35)
		
		val tooling = createComponent("tooling", neo4j)
		val toolingClazz = createClazz("AccountSqlMapDao", tooling, 5)
		createClazz("BaseSqlMapDao", tooling, 20)
		createClazz("CategorySqlMapDao", tooling, 30)
		createClazz("ItemSqlMapDao", tooling, 35)
		createClazz("ProductSqlMapDao", tooling, 20)
		createClazz("SequenceSqlMapDao", tooling, 15)

		val unsafe = createComponent("unsafe", neo4j)
		val unsafeClazz = createClazz("AbstractBean", unsafe, 20)
		createClazz("CartBean", unsafe, 40)
		
		val kernel = createComponent("kernel", neo4j)
		
		val api = createComponent("api", kernel)
		val apiClazz = createClazz("cleanupX", api, 25)
		createClazz("cleanupX", api, 25)
		val configuration = createComponent("configuration", kernel)
		val configurationClazz = createClazz("cleanupX", configuration, 35)
		createClazz("cleanupX", configuration, 5)
		val myextension = createComponent("extension", kernel)
		createClazz("cleanupX", myextension, 25)
		createClazz("cleanupX", myextension, 5)
		val guard = createComponent("guard", kernel)
		val guardClazz = createClazz("cleanupX", guard, 35)
		createClazz("cleanupX", guard, 25)
		
		val impl = createComponent("impl", kernel)
		val implClazz = createClazz("cleanupX", impl, 45)
		val annotations = createComponent("annotations", impl)
		createClazz("cleanupX", annotations, 35)
		val apiImpl = createComponent("api", impl)
		val apiImplClazz = createClazz("cleanupX", apiImpl, 25)
		val cache = createComponent("cache", impl)
		createClazz("cleanupX", cache, 45)
		val persistence = createComponent("persistence", impl)
		createClazz("AccountSqlMapDao", persistence, 45)
		
		val info = createComponent("info", kernel)
		createClazz("AccountSqlMapDao", info, 5)
		createClazz("AccountSqlMapDao", info, 25)
		val lifecycle = createComponent("lifecycle", kernel)
		val lifecycleClazz = createClazz("AccountSqlMapDao", lifecycle, 25)
		createClazz("AccountSqlMapDao", lifecycle, 15)
		
		val logging = createComponent("logging", kernel)
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
}
