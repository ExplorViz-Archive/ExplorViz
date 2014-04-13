package explorviz.visualization.export

import explorviz.visualization.engine.Logging
import explorviz.visualization.engine.primitives.Box
import explorviz.visualization.model.helper.Draw3DNodeEntity
import java.util.List
import explorviz.visualization.model.LandscapeClientSide
import explorviz.visualization.model.SystemClientSide

class OpenSCADLandscapeExporter {
	val static heightScaleFactor = 3.0f

	def static void exportLandscapeAsOpenSCAD(LandscapeClientSide landscape) {
		Logging::log(
			"module landscape()" + "\n" + "{" + "\n" + "\t union() {" + "\n" + "\t\t" +
				createLandscapeSystems(landscape.systems) + "}" + "\n" + "}" + "\n" + "\n" +
				"landscape();"
		)
	}

	def static String createLandscapeSystems(List<SystemClientSide> systems) {
		var result = ""
		for (system : systems) {
			result = result + createLandscapeSystem(system)
		}
		result
	}
	
	def static String createLandscapeSystem(SystemClientSide side) {
		""
	}

	def static String createFromPrimitiveObjects(Draw3DNodeEntity entity) {
		var result = ""
		for (primitiveObject : entity.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				result = result + createFromBox(primitiveObject as Box)
			}
		}
		result
	}

	def static String createFromBox(Box box) {
		"translate([" + box.center.x + "," + -1f * box.center.z + "," + box.center.y * heightScaleFactor + "])" + " " + "cube(size= [" +
			box.extensionInEachDirection.x * 2f + "," + box.extensionInEachDirection.z * 2f + "," +
			box.extensionInEachDirection.y * 2.04f * heightScaleFactor + "], center = true);\n\t\t"
	}
}
