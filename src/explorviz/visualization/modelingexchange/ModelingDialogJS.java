package explorviz.visualization.modelingexchange;

import java.util.ArrayList;
import java.util.List;

import explorviz.shared.model.*;
import explorviz.shared.model.System;
import explorviz.shared.model.helper.CommunicationTileAccumulator;
import explorviz.shared.model.helper.ELanguage;
import explorviz.visualization.engine.main.SceneDrawer;
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager;

public class ModelingDialogJS {
	public static void configureSystem(final explorviz.shared.model.System system) {
		showDialog("Configuration of " + system.getName(), system.getParent(),
				createOneFormInput("Name", "name", system.getName(), true), 1, system);
	}

	private static String createOneFormInput(final String label, final String id,
			final String defaultValue, final boolean autofocus) {
		String autofocusStr = "";
		if (autofocus) {
			autofocusStr = "autofocus";
		}

		return "<label for='" + id + "'>" + label
				+ "</label><br><input type='text' style='width:95%;margin-left:10px;' name='" + id
				+ "' id='" + id + "' value='" + defaultValue + "' " + autofocusStr + "><br><br>";
	}

	public static void systemCallback(final explorviz.shared.model.System system,
			final String serializedValues) {
		final String[] valuePairs = serializedValues.split("&");
		for (final String valuePair : valuePairs) {
			final String[] keyValue = valuePair.split("=");
			if (keyValue[0].equals("name") && (keyValue[1] != null) && !keyValue[1].isEmpty()) {
				system.setName(keyValue[1]);
			}
		}
	}

	public static void configureNode(final Node node) {
		showDialog("Configuration of " + node.getName(), node.getParent().getParent().getParent(),
				createOneFormInput("Name", "name", node.getName(), true)
						+ createOneFormInput("IP Address", "ipaddress", node.getIpAddress(), false),
				2, node);
	}

	public static void nodeCallback(final explorviz.shared.model.Node node,
			final String serializedValues) {
		final String[] valuePairs = serializedValues.split("&");
		for (final String valuePair : valuePairs) {
			final String[] keyValue = valuePair.split("=");
			if (keyValue[0].equals("name") && (keyValue[1] != null) && !keyValue[1].isEmpty()) {
				node.setName(keyValue[1]);
			} else if (keyValue[0].equals("ipaddress") && !keyValue[1].equals("")) {
				node.setIpAddress(keyValue[1]);
			}
		}
	}

	public static void configureApplication(final Application application) {
		final List<String> options = new ArrayList<String>();
		for (final ELanguage lang : ELanguage.values()) {
			options.add(lang.toString());
		}

		showDialog("Configuration of " + application.getName(),
				application.getParent().getParent().getParent().getParent(),
				createOneFormInput("Name", "name", application.getName(), true)
						+ createOneFormDropdown("Language", "language",
								application.getProgrammingLanguage().toString(), options),
				3, application);
	}

	private static String createOneFormDropdown(final String label, final String id,
			final String defaultValue, final List<String> options) {
		final String start = "<label for='" + id + "'>" + label
				+ "</label><br><select style='width:95%;margin-left:10px;' name='" + id + "' id='"
				+ id + "'>";

		String optionsStr = "";
		for (final String option : options) {
			String selected = "";
			if (option.equalsIgnoreCase(defaultValue)) {
				selected = "selected";
			}
			optionsStr += "<option value='" + option + "' " + selected + ">" + option + "</option>";
		}
		final String end = "</select><br><br>";

		return start + optionsStr + end;

	}

	public static void applicationCallback(final explorviz.shared.model.Application app,
			final String serializedValues) {
		final String[] valuePairs = serializedValues.split("&");
		for (final String valuePair : valuePairs) {
			final String[] keyValue = valuePair.split("=");
			if (keyValue[0].equals("name") && (keyValue[1] != null) && !keyValue[1].isEmpty()) {
				app.setName(keyValue[1]);
			} else if (keyValue[0].equals("language") && !keyValue[1].equals("")) {
				app.setProgrammingLanguage(ELanguage.valueOf(keyValue[1]));
			}
		}
	}

	public static void addNewCommunication(final Application source) {
		final List<String> options = new ArrayList<String>();
		calcAllApplicableCommuTargets(options, source);

		showDialog("Add communication to source application " + source.getName(),
				source.getParent().getParent().getParent().getParent(),
				createOneFormDropdown("Target", "target", "", options), 4, source);
	}

	private static void calcAllApplicableCommuTargets(final List<String> options,
			final Application source) {
		final Landscape landscape = source.getParent().getParent().getParent().getParent();
		options.add("NONE");

		for (final System system : landscape.getSystems()) {
			for (final NodeGroup nodeGroup : system.getNodeGroups()) {
				for (final Node node : nodeGroup.getNodes()) {
					for (final Application app : node.getApplications()) {
						if (app != source) {
							if (!alreadyContainsCommu(source, app, landscape)) {
								options.add(
										app.getName() + "-on-" + app.getParent().getDisplayName());
							}
						}
					}
				}
			}
		}
	}

	private static boolean alreadyContainsCommu(final Application source, final Application target,
			final Landscape landscape) {
		for (final Communication commu : landscape.getApplicationCommunication()) {
			if (((commu.getSource() == source) && (commu.getTarget() == target))
					|| ((commu.getSource() == target) && (commu.getTarget() == source))) {
				return true;
			}
		}

		return false;
	}

	public static void addCommuCallback(final explorviz.shared.model.Application source,
			final String serializedValues) {
		final String[] valuePairs = serializedValues.split("&");
		for (final String valuePair : valuePairs) {
			final String[] keyValue = valuePair.split("=");
			if (keyValue[0].equals("target") && (keyValue[1] != null) && !keyValue[1].isEmpty()
					&& !keyValue[1].equalsIgnoreCase("NONE")) {
				final Landscape landscape = source.getParent().getParent().getParent().getParent();

				final Communication newCommu = new Communication();
				newCommu.setSource(source);
				final Application target = seekTarget(keyValue[1], landscape);
				if (target != null) {
					newCommu.setTarget(target);
					newCommu.setRequests(10);
					landscape.getApplicationCommunication().add(newCommu);
				}
			}
		}
	}

	private static Application seekTarget(final String encodedTarget, final Landscape landscape) {
		final String[] splitTarget = encodedTarget.split("-on-");
		final String appName = splitTarget[0];
		final String nodeName = splitTarget[1];

		for (final System system : landscape.getSystems()) {
			for (final NodeGroup nodeGroup : system.getNodeGroups()) {
				for (final Node node : nodeGroup.getNodes()) {
					if (node.getDisplayName().equals(nodeName)) {
						for (final Application app : node.getApplications()) {
							if (app.getName().equals(appName)) {
								return app;
							}
						}
					}
				}
			}
		}

		return null;
	}

	public static void configureCommunication(final CommunicationTileAccumulator commu) {
		final Communication firstCommu = commu.getCommunications().get(0);

		showDialog(
				"Configuration of " + firstCommu.getSource().getName() + " - "
						+ firstCommu.getTarget().getName(),
				firstCommu.getSource().getParent().getParent().getParent().getParent(),
				createOneFormInput("Requests", "requests", String.valueOf(firstCommu.getRequests()),
						true)
						+ createOneFormInput("Technology", "technology",
								firstCommu.getTechnology(), false),
				5, commu);
	}

	public static void commuCallback(final CommunicationTileAccumulator commu,
			final String serializedValues) {
		final Communication firstCommu = commu.getCommunications().get(0);

		final String[] valuePairs = serializedValues.split("&");
		for (final String valuePair : valuePairs) {
			final String[] keyValue = valuePair.split("=");
			if (keyValue[0].equals("requests") && (keyValue[1] != null) && !keyValue[1].isEmpty()) {
				firstCommu.setRequests(Integer.parseInt(keyValue[1]));
			} else if (keyValue[0].equals("technology") && (keyValue[1] != null)
					&& !keyValue[1].isEmpty()) {
				firstCommu.setTechnology(keyValue[1]);
			}
		}
	}

	public static void updatePerspective(final Landscape landscape) {
		LandscapeExchangeManager.saveTargetModelIfInModelingMode(landscape);

		SceneDrawer.createObjectsFromLandscape(landscape, true);
	}

	public static native void showDialog(final String title, final Landscape landscape,
			final String configElementsAsHTMLForm, final int typeCallback, final Object obj) /*-{
		$wnd.jQuery("#configurationModelingDialog").show();
		$wnd
				.jQuery("#configurationModelingDialog")
				.dialog(
						{
							closeOnEscape : true,
							modal : true,
							resizable : false,
							title : title,
							width : 400,
							height : 220,
							position : {
								my : 'center center',
								at : 'center center',
								of : $wnd.jQuery("#view")
							},
							close : function() {
								var seriForm = $wnd.jQuery(
										"#configModelingForm").serialize();
								if (typeCallback == 1) {
									@explorviz.visualization.modelingexchange.ModelingDialogJS::systemCallback(Lexplorviz/shared/model/System;Ljava/lang/String;)(obj, seriForm);
								} else if (typeCallback == 2) {
									@explorviz.visualization.modelingexchange.ModelingDialogJS::nodeCallback(Lexplorviz/shared/model/Node;Ljava/lang/String;)(obj, seriForm);
								} else if (typeCallback == 3) {
									@explorviz.visualization.modelingexchange.ModelingDialogJS::applicationCallback(Lexplorviz/shared/model/Application;Ljava/lang/String;)(obj, seriForm);
								} else if (typeCallback == 4) {
									@explorviz.visualization.modelingexchange.ModelingDialogJS::addCommuCallback(Lexplorviz/shared/model/Application;Ljava/lang/String;)(obj, seriForm);
								} else if (typeCallback == 5) {
									@explorviz.visualization.modelingexchange.ModelingDialogJS::commuCallback(Lexplorviz/shared/model/helper/CommunicationTileAccumulator;Ljava/lang/String;)(obj, seriForm);
								}

								@explorviz.visualization.modelingexchange.ModelingDialogJS::updatePerspective(Lexplorviz/shared/model/Landscape;)(landscape);
							}
						}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$wnd.jQuery("#configurationModelingDialog").html(
				"<form id='configModelingForm'>" + configElementsAsHTMLForm
						+ "</form>");
	}-*/;
}