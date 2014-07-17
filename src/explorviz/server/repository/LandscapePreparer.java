package explorviz.server.repository;

import explorviz.shared.model.*;
import explorviz.visualization.renderer.ColorDefinitions;

public class LandscapePreparer {
	public static Landscape prepareLandscape(final Landscape landscape) {
		for (final explorviz.shared.model.System system : landscape.getSystems()) {
			for (final NodeGroup nodeGroup : system.getNodeGroups()) {
				for (final Node node : nodeGroup.getNodes()) {
					for (final Application application : node.getApplications()) {
						final Component foundationComponent = new Component();
						foundationComponent.setOpened(true);
						foundationComponent.setName(application.getName());
						foundationComponent.setFullQualifiedName(application.getName());
						foundationComponent.setBelongingApplication(application);
						foundationComponent.setColor(ColorDefinitions.componentFoundationColor);

						foundationComponent.getChildren().addAll(application.getComponents());

						for (final Component child : foundationComponent.getChildren()) {
							setComponentAttributes(child, 0, true);
						}

						application.getComponents().clear();
						application.getComponents().add(foundationComponent);
					}
				}

				nodeGroup.setOpened(false);
			}
		}

		for (final Communication commu : landscape.getApplicationCommunication()) {
			createApplicationInAndOutgoing(commu);
		}

		return landscape;
	}

	private static void setComponentAttributes(final Component component, final int index,
			final boolean shouldBeOpened) {
		boolean openNextLevel = shouldBeOpened;

		if (!openNextLevel) {
			component.setOpened(false);
		} else if (component.getChildren().size() == 1) {
			component.setOpened(true);
		} else {
			component.setOpened(true);
			openNextLevel = false;
		}

		if ((index % 2) == 1) {
			if (component.isSynthetic()) {
				component.setColor(ColorDefinitions.componentSyntheticColor);
			} else {
				component.setColor(ColorDefinitions.componentFirstColor);
			}
		} else {
			if (component.isSynthetic()) {
				component.setColor(ColorDefinitions.componentSyntheticSecondColor);
			} else {
				component.setColor(ColorDefinitions.componentSecondColor);
			}
		}

		for (final Component child : component.getChildren()) {
			setComponentAttributes(child, index + 1, openNextLevel);
		}
	}

	private static final void createApplicationInAndOutgoing(final Communication communication) {
		if ((communication.getSource() != null) && (communication.getSourceClazz() != null)) {
			communication.getSource().getOutgoingCommunications().add(communication);
		}
		if ((communication.getTarget() != null) && (communication.getTargetClazz() != null)) {
			communication.getTarget().getIncomingCommunications().add(communication);
		}
	}
}
