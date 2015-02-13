package explorviz.plugin.capacitymanagement.cloud_control;

import explorviz.shared.model.*;

public class CloudControllerForConcurrencyTest extends CloudControllerForTest {

	private int interruptStartNode;
	private final int interruptCloneNode;
	private final int interruptShutdownNode;
	private final int interruptRestartNode;
	private final int interruptRestartApplication;
	private final int interruptTerminateApplication;
	private final int interruptMigrateApplication;

	public CloudControllerForConcurrencyTest(final Node node, final int interruptStartNode,
			final int interruptCloneNode, final int interruptShutdownNode,
			final int interruptRestartNode, final int interruptRestartApplication,
			final int interruptTerminateApplication, final int interruptMigrateApplication) {
		super(node);
		this.interruptCloneNode = interruptCloneNode;
		this.interruptShutdownNode = interruptShutdownNode;
		this.interruptRestartNode = interruptRestartNode;
		this.interruptRestartApplication = interruptRestartApplication;
		this.interruptTerminateApplication = interruptTerminateApplication;
		this.interruptMigrateApplication = interruptMigrateApplication;
	}

	@Override
	public Node startNode(final NodeGroup nodegroup, Node node) throws Exception {
		sleepMillis(interruptStartNode);
		return super.startNode(nodegroup, node);
	}

	@Override
	public Node replicateNode(final NodeGroup nodegroup, final Node originalNode) {
		sleepMillis(interruptCloneNode);
		return super.replicateNode(nodegroup, originalNode);
	}

	@Override
	public boolean terminateNode(final Node node) {
		sleepMillis(interruptShutdownNode);
		return super.terminateNode(node);
	}

	@Override
	public boolean restartNode(final Node node) {
		sleepMillis(interruptRestartNode);
		return super.restartNode(node);
	}

	@Override
	public boolean restartApplication(final Application application) {
		sleepMillis(interruptRestartApplication);
		return super.restartApplication(application);
	}

	@Override
	public boolean terminateApplication(final Application application) {
		sleepMillis(interruptTerminateApplication);
		return super.terminateApplication(application);
	}

	@Override
	public boolean migrateApplication(final Application application, final Node node) {
		sleepMillis(interruptMigrateApplication);
		return super.migrateApplication(application, node);
	}

	private void sleepMillis(final int millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {

		}
	}
}
