package explorviz.server.repository;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import explorviz.live_trace_processing.record.event.AbstractEventRecord;
import explorviz.live_trace_processing.record.event.normal.AfterOperationEventRecord;
import explorviz.live_trace_processing.record.event.normal.BeforeOperationEventRecord;
import explorviz.live_trace_processing.record.trace.*;
import explorviz.shared.model.*;

public class LandscapeRepositoryModelTest {

	@Test
	public void testgetLastPeriodLandscape() throws Exception {
		final LandscapeRepositoryModel repositoryModel = new LandscapeRepositoryModel();
		assertNotNull(repositoryModel.getLastPeriodLandscape());

		assertEquals(0, repositoryModel.getLastPeriodLandscape().getApplicationCommunication()
				.size());
		assertEquals(0, repositoryModel.getLastPeriodLandscape().getSystems().size());
		assertTrue(repositoryModel.getLastPeriodLandscape().getHash() > 0);

		RepositoryStorage.clearRepository();
	}

	@Test
	public void testReset() throws Exception {
		final LandscapeRepositoryModel repositoryModel = new LandscapeRepositoryModel();
		final Trace trace = createSimpleTrace();
		repositoryModel.insertIntoModel(trace);

		Thread.sleep(11000);

		assertEquals(1, repositoryModel.getLastPeriodLandscape().getSystems().size());

		repositoryModel.reset();

		Thread.sleep(11000);

		assertEquals(0, repositoryModel.getLastPeriodLandscape().getSystems().size());
		assertEquals(0, repositoryModel.getLastPeriodLandscape().getApplicationCommunication()
				.size());

		RepositoryStorage.clearRepository();
	}

	@Test
	public void testPeriodicTimeSignal() throws Exception {
		final LandscapeRepositoryModel repositoryModel = new LandscapeRepositoryModel();
		assertNotNull(repositoryModel.getLastPeriodLandscape());

		final Trace trace = createSimpleTrace();

		repositoryModel.insertIntoModel(trace);
		repositoryModel.insertIntoModel(trace);
		repositoryModel.insertIntoModel(trace);
		repositoryModel.insertIntoModel(trace);

		RepositoryStorage.clearRepository();
	}

	@Test
	public void testInsertIntoModel() throws Exception {
		final LandscapeRepositoryModel repositoryModel = new LandscapeRepositoryModel();
		assertNotNull(repositoryModel.getLastPeriodLandscape());

		final Trace trace = createSimpleTrace();

		repositoryModel.insertIntoModel(trace);
		repositoryModel.insertIntoModel(trace);
		repositoryModel.insertIntoModel(trace);
		repositoryModel.insertIntoModel(trace);

		Thread.sleep(11000);

		assertEquals(1, repositoryModel.getLastPeriodLandscape().getSystems().get(0)
				.getNodeGroups().size());
		assertEquals(0, repositoryModel.getLastPeriodLandscape().getApplicationCommunication()
				.size());
		assertTrue(repositoryModel.getLastPeriodLandscape().getHash() > 0);

		final Node node = repositoryModel.getLastPeriodLandscape().getSystems().get(0)
				.getNodeGroups().get(0).getNodes().get(0);
		assertEquals("testHost", node.getName());

		Application application = node.getApplications().get(0);
		assertEquals("testApp", application.getName());

		assertEquals(1, application.getComponents().size());
		assertEquals(1, application.getComponents().get(0).getChildren().size());
		Component testPackage = application.getComponents().get(0).getChildren().get(0);
		assertEquals("testpackage", testPackage.getFullQualifiedName());
		assertEquals("testpackage", testPackage.getName());
		assertEquals(0, testPackage.getChildren().size());
		assertEquals(1, testPackage.getClazzes().size());
		final Clazz testClazz = testPackage.getClazzes().get(0);
		assertEquals("TestClass", testClazz.getName());
		assertEquals("testpackage.TestClass", testClazz.getFullQualifiedName());

		assertEquals(1, testClazz.getInstanceCount());

		final Trace callTrace = createCallTrace();

		repositoryModel.insertIntoModel(callTrace);
		repositoryModel.insertIntoModel(callTrace);

		Thread.sleep(11000);

		application = repositoryModel.getLastPeriodLandscape().getSystems().get(0).getNodeGroups()
				.get(0).getNodes().get(0).getApplications().get(0);

		assertEquals(1, application.getComponents().size());
		assertEquals(1, application.getComponents().get(0).getChildren().size());
		testPackage = application.getComponents().get(0).getChildren().get(0);
		assertEquals("testpackage", testPackage.getFullQualifiedName());
		assertEquals("testpackage", testPackage.getName());
		assertEquals(0, testPackage.getChildren().size());
		assertEquals(2, testPackage.getClazzes().size());
		assertEquals("TestClass", testClazz.getName());
		assertEquals("testpackage.TestClass", testClazz.getFullQualifiedName());

		final Clazz testClazz2 = testPackage.getClazzes().get(1);
		assertEquals("TestClass2", testClazz2.getName());
		assertEquals("testpackage.TestClass2", testClazz2.getFullQualifiedName());

		assertEquals(1, application.getCommunications().size());
		final CommunicationClazz communicationClazz = application.getCommunications().get(0);
		assertEquals(2, communicationClazz.getRequests());

		assertEquals(testClazz.getFullQualifiedName(), communicationClazz.getSource()
				.getFullQualifiedName());
		assertEquals(testClazz2.getFullQualifiedName(), communicationClazz.getTarget()
				.getFullQualifiedName());

		RepositoryStorage.clearRepository();
	}

	private Trace createSimpleTrace() {
		final HostApplicationMetaDataRecord hostApplicationMetaDataRecord = new HostApplicationMetaDataRecord(
				"testSystem", "testIp", "testHost", "testApp", "Java");

		final BeforeOperationEventRecord before = new BeforeOperationEventRecord(0, 0, 0,
				"public void testpackage.TestClass.testMethod(String param1)",
				"testpackage.TestClass", "", hostApplicationMetaDataRecord);
		final RuntimeStatisticInformation statisticInformation = new RuntimeStatisticInformation(1,
				1000, 10000);
		statisticInformation.makeAccumulator(0);
		final List<RuntimeStatisticInformation> runtimeList = new ArrayList<RuntimeStatisticInformation>();
		runtimeList.add(statisticInformation);
		before.setRuntimeStatisticInformationList(runtimeList);
		final AfterOperationEventRecord after = new AfterOperationEventRecord(1000, 0, 1,
				hostApplicationMetaDataRecord);

		final List<AbstractEventRecord> events = new ArrayList<AbstractEventRecord>();
		events.add(before);
		events.add(after);

		final Trace trace = new Trace(events, true, false);
		return trace;
	}

	private Trace createCallTrace() {
		final HostApplicationMetaDataRecord hostApplicationMetaDataRecord = new HostApplicationMetaDataRecord(
				"testSystem", "testIp", "testHost", "testApp", "Java");

		final BeforeOperationEventRecord before = new BeforeOperationEventRecord(0, 0, 0,
				"public void testpackage.TestClass.testMethod(String param1)",
				"testpackage.TestClass", "", hostApplicationMetaDataRecord);
		final List<RuntimeStatisticInformation> runtimeList = new ArrayList<RuntimeStatisticInformation>();
		runtimeList.add(new RuntimeStatisticInformation(1, 1000, 10000));
		before.setRuntimeStatisticInformationList(runtimeList);
		final BeforeOperationEventRecord before2 = new BeforeOperationEventRecord(0, 0, 0,
				"public void testpackage.TestClass2.testMethod2(String param1)",
				"testpackage.TestClass2", "", hostApplicationMetaDataRecord);
		final List<RuntimeStatisticInformation> runtimeList2 = new ArrayList<RuntimeStatisticInformation>();
		runtimeList2.add(new RuntimeStatisticInformation(1, 1000, 10000));
		before2.setRuntimeStatisticInformationList(runtimeList2);
		final AfterOperationEventRecord after2 = new AfterOperationEventRecord(1000, 0, 1,
				hostApplicationMetaDataRecord);
		final AfterOperationEventRecord after = new AfterOperationEventRecord(1000, 0, 1,
				hostApplicationMetaDataRecord);

		final List<AbstractEventRecord> events = new ArrayList<AbstractEventRecord>();
		events.add(before);
		events.add(before2);
		events.add(after2);
		events.add(after);

		final Trace trace = new Trace(events, true, false);
		return trace;
	}
}
