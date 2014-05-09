package explorviz.visualization.export

import explorviz.visualization.engine.primitives.Box
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.ClazzClientSide
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.helper.Draw3DNodeEntity
import java.util.List

class OpenSCADApplicationExporter {
	val static heightScaleFactor = 4.0f

	def static String exportApplicationAsOpenSCAD(ApplicationClientSide application) {
			"module application()" + "\n" + "{" + "\n" + "\t union() {" + "\n" + "\t\t" +
				createApplicationComponents(application.components) + "}" + "\n" + "}" + "\n" + "\n" +
				"application();"
	}

	def private static String createApplicationComponents(List<ComponentClientSide> components) {
		var result = ""
		for (component : components) {
			result = result + createApplicationComponent(component) + createApplicationClazzes(component.clazzes)
		}
		result
	}

	def private static String createApplicationComponent(ComponentClientSide component) {
		createApplicationComponents(component.children) + createFromPrimitiveObjects(component)
	}

	def private static String createApplicationClazzes(List<ClazzClientSide> clazzes) {
		var result = ""
		for (clazz : clazzes) {
			result = result + createFromPrimitiveObjects(clazz)
		}
		result
	}

	def private static String createFromPrimitiveObjects(Draw3DNodeEntity entity) {
		var result = ""
		for (primitiveObject : entity.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				result = result + createFromBox(primitiveObject as Box)
			}
		}
		result
	}

	def private static String createFromBox(Box box) {
		"translate([" + box.center.x + "," + -1f * box.center.z + "," + box.center.y * heightScaleFactor + "])" + " " + "cube(size= [" +
			box.extensionInEachDirection.x * 2f + "," + box.extensionInEachDirection.z * 2f + "," +
			box.extensionInEachDirection.y * 2.04f * heightScaleFactor + "], center = true);\n\t\t"
	}
}
