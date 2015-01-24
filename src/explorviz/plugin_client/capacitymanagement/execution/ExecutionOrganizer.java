package explorviz.plugin_client.capacitymanagement.execution;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin_client.capacitymanagement.configuration.CapManConfiguration;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;

@SuppressWarnings("unused")
public class ExecutionOrganizer /* implements IScalingControl */{
	private final int MAX_TRIES = 2;
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionOrganizer.class);

	private final ICloudController cloudController;

	static int maxRunningNodesLimit = 5; // for test. in live overwritten by
	// configuration

	private final int shutdownDelayInMillis;
	static int waitTimeBeforeNewBootInMillis;

	// TODO: Reihenfolge der Aktionen organisieren
	// je Applikation nur 1 Aktion zur Zeit
	// je Node nur eine Node-Applikation zur Zeit
	// je Node aber mehrere Applikations Aktionen möglich, aber nicht
	// gleichzeitig mit Node-Aktion!

	public ExecutionOrganizer(final CapManConfiguration configuration) throws Exception {

		// LoadBalancersFacade.reset();

		shutdownDelayInMillis = configuration.getShutdownDelayInMillis();
		waitTimeBeforeNewBootInMillis = configuration.getWaitTimeBeforeNewBootInMillis();
		maxRunningNodesLimit = configuration.getCloudNodeLimit();

		cloudController = createCloudController(configuration);
	}

	private static ICloudController createCloudController(final CapManConfiguration configuration)
			throws Exception {
		final Class<?> clazz = Class.forName(configuration.getCloudProvider());
		final ICloudController cloudManager = (ICloudController) clazz.getConstructor(
				CapManConfiguration.class).newInstance(configuration);
		return cloudManager;
	}

	public void executeActionList(final ArrayList<ExecutionAction> actionList) {
		executeAllActions(actionList);
		if (checkExecution(actionList, 1)) {
			compensate(actionList);
		}
	}

	private void executeAllActions(final ArrayList<ExecutionAction> actionList) {
		LOGGER.info("Executing ActionList");
		// TODO: jkr/jek: Abhängigkeiten in der Reihenfolge beachten?
		final ThreadGroup actionThreads = new ThreadGroup("actions");
		for (final ExecutionAction action : actionList) {
			action.execute(cloudController, actionThreads);
		}
		final Thread[] threads = new Thread[actionThreads.activeCount()];
		actionThreads.enumerate(threads);
		for (final Thread t : threads) {
			try {
				t.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean checkExecution(final ArrayList<ExecutionAction> actionList, final int tries) {
		LOGGER.info("Checking results of execution");
		final boolean success = true;
		final ArrayList<ExecutionAction> remainingActions = new ArrayList<ExecutionAction>();
		for (final ExecutionAction action : actionList) {
			switch (action.getState()) {
				case SUCC_FINISHED:
					break;
				case ABORTED:
					return false;
				case REJECTED:
					// TODO jkr/jek how to deal with actions that were rejected?
					if (tries < MAX_TRIES) {
						remainingActions.add(action);
					} else {
						return false;
					}
					break;
				case INITIAL:
					// TODO jkr/jek how to deal with actions that were not even
					// started?
					if (tries < MAX_TRIES) {
						remainingActions.add(action);
					} else {
						return false;
					}
					break;
			}
		}
		if (tries < MAX_TRIES) {
			executeAllActions(remainingActions);
			return checkExecution(remainingActions, tries + 1);
		} else {
			LOGGER.info("All Actions executed successfully.");
			return success;
		}
	}

	private void compensate(final ArrayList<ExecutionAction> actionList) {
		LOGGER.info("Trying to compensate unsuccessful execution...");
		final ArrayList<ExecutionAction> compensateActions = new ArrayList<ExecutionAction>();
		for (final ExecutionAction action : actionList) {
			if (action.getState() == ExecutionActionState.SUCC_FINISHED) {
				final ExecutionAction compensate = action.getCompensateAction();
				if (compensate != null) {
					compensateActions.add(compensate);
				}
			}
		}
		executeAllActions(compensateActions);
		if (checkExecution(compensateActions, MAX_TRIES)) {
			LOGGER.info("Compensate successful");
		} else {
			LOGGER.info("Compensate did not terminate successfully");
		}
	}
}
