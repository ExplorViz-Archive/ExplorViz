package explorviz.plugin_server.capacitymanagement.execution;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.capacitymanagement.configuration.CapManConfiguration;
import explorviz.plugin_server.capacitymanagement.cloud_control.ICloudController;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.visualization.engine.main.SceneDrawer;

/**
 * The ExecutionOrganizer invokes the Execution of the ActionList of
 * {@link ExecutionAction}. It is the only connection to
 * {@link ICloudController}. Organizes the compensation in case of failed
 * Actions. <br>
 * Partly inspired by capacity-manager-project (class ScalingManager).
 *
 * @author jkr
 *
 */
public class ExecutionOrganizer {

	private final int MAX_TRIES_UNTIL_COMPENSATE;
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionOrganizer.class);

	private final ICloudController cloudController;
	private ScalingGroupRepository repository;

	static int maxRunningNodesLimit;
	static int MAX_TRIES_FOR_CLOUD;

	/**
	 * Constructor. <br>
	 * Initializes {@link CloudController}.
	 *
	 * @param configuration
	 *            configured in settings-file
	 * @throws Exception
	 */
	public ExecutionOrganizer(final CapManConfiguration configuration,
			ScalingGroupRepository scalingGroups) throws Exception {

		maxRunningNodesLimit = configuration.getCloudNodeLimit();
		MAX_TRIES_FOR_CLOUD = configuration.getMaxTriesForCloud();
		MAX_TRIES_UNTIL_COMPENSATE = configuration.getMaxTriesUntilCompensate();

		repository = scalingGroups;
		cloudController = createCloudController(configuration);

	}

	private static ICloudController createCloudController(final CapManConfiguration configuration)
			throws Exception {
		final Class<?> clazz = Class.forName(configuration.getCloudProvider());
		final ICloudController cloudManager = (ICloudController) clazz.getConstructor(
				CapManConfiguration.class).newInstance(configuration);
		return cloudManager;
	}

	/**
	 * Executes all {@link ExecutionAction}. Checks whether all actions finished
	 * successfully. Starts compensation otherwise.
	 *
	 * @param actionList
	 *            list of actions to be executed
	 */
	public void executeActionList(final ArrayList<ExecutionAction> actionList) {
		executeAllActions(actionList);
		if (!checkExecution(actionList, 1)) {
			compensate(actionList);
		}
		SceneDrawer.lastLandscape.putGenericBooleanData(IPluginKeys.ANOMALY_PRESENT, false);
		SceneDrawer.lastLandscape.putGenericBooleanData(IPluginKeys.CAPMAN_PLAN_IN_PROGRESS, false);
	}

	private void executeAllActions(final ArrayList<ExecutionAction> actionList) {
		LOGGER.info("Executing ActionList");
		final ThreadGroup actionThreads = new ThreadGroup("actions");
		for (final ExecutionAction action : actionList) {
			action.execute(cloudController, actionThreads, repository);
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

	/***
	 * Checks Execution_State of all actions in actionList. Not started or
	 * rejected actions are started again as long as tries < MAX_TRIES
	 * (recursion)
	 *
	 * @param actionList
	 *            List of ExecutionActions that were executed
	 * @param tries
	 *            number of iteration
	 * @return true if finally all Actions finished successfully
	 */
	private boolean checkExecution(final ArrayList<ExecutionAction> actionList, final int tries) {
		LOGGER.info("Checking results of execution");
		final ArrayList<ExecutionAction> remainingActions = new ArrayList<ExecutionAction>();
		for (final ExecutionAction action : actionList) {
			switch (action.getState()) {
				case SUCC_FINISHED:
					break;
				case ABORTED:
					return false;
				case REJECTED:

					if (tries < MAX_TRIES_UNTIL_COMPENSATE) {
						remainingActions.add(action);
					} else {
						return false;
					}
					break;
				case INITIAL:

					if (tries < MAX_TRIES_UNTIL_COMPENSATE) {
						remainingActions.add(action);
					} else {
						return false;
					}
					break;
			}
		}
		if ((tries < MAX_TRIES_UNTIL_COMPENSATE) && (remainingActions.size() > 0)) {
			LOGGER.info("Executing remaining actions, tries = " + tries);
			executeAllActions(remainingActions);
			return checkExecution(remainingActions, tries + 1);
		} else {
			LOGGER.info("All Actions executed successfully.");
			return true;
		}
	}

	/**
	 * Build new ActionList with compensate actions of all successfully finished
	 * actions and execute it.
	 *
	 * @param actionList
	 *            ActionList which to compensate
	 */
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
		if (checkExecution(compensateActions, MAX_TRIES_UNTIL_COMPENSATE - 1)) {
			LOGGER.info("Compensate successful");
		} else {
			LOGGER.info("Compensate did not terminate successfully");
		}
	}

}
