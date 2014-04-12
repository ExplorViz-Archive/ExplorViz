package explorviz.visualization.export

import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.engine.Logging
import explorviz.visualization.model.ComponentClientSide
import java.util.List
import explorviz.visualization.engine.primitives.Box
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.model.ClazzClientSide
import explorviz.visualization.model.helper.Draw3DNodeEntity
import java.util.ArrayList

class STLExporter {

	def static void exportApplicationAsSTL(ApplicationClientSide application) {
		Logging::log(
			"module " + application.name + "()" + "\n" + "{" + "\n" + "\t union() {" + "\n" + "\t\t" +
				createApplicationComponents(application.components) + "}" + "\n" + "}" + "\n" + "\n" +
				application.name + "();"
		)
	}

	def static String createApplicationComponents(List<ComponentClientSide> components) {
		var result = ""
		for (component : components) {
			result = result + createApplicationComponent(component) + createApplicationClazzes(component.clazzes)
		}
		result
	}

	def static String createApplicationComponent(ComponentClientSide component) {
		createApplicationComponents(component.children) + createFromPrimitiveObjects(component)
	}

	def static String createApplicationClazzes(List<ClazzClientSide> clazzes) {
		var result = ""
		for (clazz : clazzes) {
			result = result + createFromPrimitiveObjects(clazz)
		}
		result
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
		"translate([" + box.center.x + "," + box.center.z + "," + box.center.y + "])" + " " + "cube(size= [" +
			box.extensionInEachDirection.x * 2f + "," + box.extensionInEachDirection.z * 2f + "," + box.extensionInEachDirection.y * 2f +
			"], center = true);\n\t\t"
	}
}
