package explorviz.visualization.export

import explorviz.visualization.engine.primitives.Box
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.ClazzClientSide
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.helper.Draw3DNodeEntity
import java.util.List

class OpenSCADApplicationExporter {

	/////////////////////////////////////// globals ///////////////////////////////////////
	
	/**
	 * Enable lids for open boxes
	 */
	val static boolean enableLids = false

	/**
	 * Used for lids
	 */
	val static wallThickness = 1.0f	//TODO: Test this value

	/**
	 * Scaling for boxes
	 */
	val static heightScaleFactor = 4.0f

	/////////////////////////////////////// OpenSCAD default code ///////////////////////////////////////

	/**
	 * Create the frame of the SCAD file source code
	 * @param application The application to transform to a 3D model
	 */
	def static String exportApplicationAsOpenSCAD(ApplicationClientSide application) {
		"module application()" + "\n" + "{" + "\n" + "\t union() {" + "\n" + "\t\t" +
			createApplicationComponents(application.components) + "}" + "\n" + "}" + "\n" + "\n" + "application();" +
			"\n"
	}

	/**
	 * Add all single components to the result
	 * @param components A list of all components of the application
	 */
	def private static String createApplicationComponents(List<ComponentClientSide> components) {
		var result = ""
		for (component : components) {
			result = result + createApplicationComponent(component) + createApplicationClazzes(component.clazzes)
		}
		result
	}

	/**
	 * Add subcomponents of components to the result
	 * @param component A component with optional subcomponents
	 */
	def private static String createApplicationComponent(ComponentClientSide component) {
		createApplicationComponents(component.children) + createFromPrimitiveObjects(component)	
	}

	/**
	 * Check every entity and convert it to a box
	 * @param entity The entity to check
	 */
	def private static String createFromPrimitiveObjects(Draw3DNodeEntity entity) {
		var result = ""
		for (primitiveObject : entity.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				result = result 
						+ createFromBox(primitiveObject as Box,
						  entity.name,
						if (entity instanceof ComponentClientSide) {
						(entity as ComponentClientSide).opened
						} else false)
			}
		}
		result
	}

	/**
	 * Add all classes to the result
	 */
	def private static String createApplicationClazzes(List<ClazzClientSide> clazzes) {
		var result = ""
		for (clazz : clazzes) {
			result = result + createFromPrimitiveClasses(clazz)
		}
		result
	}

	/**
	 * Check every class and convert it to a box
	 * @param entity The class to check
	 */
	def private static String createFromPrimitiveClasses(Draw3DNodeEntity entity) {
		var result = ""
		for (primitiveObject : entity.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				result = result + createFromBox(primitiveObject as Box)
			}
		}
		result
	}

	/////////////////////////////////////// cubes, boxes and lids ///////////////////////////////////////
	
//TODO: 
//Automatische Deckelerstellung bei 2 oder mehr kindern
//h�he der deckel evaluieren (verh�ltnis muss stimmen)
//platzierung der deckel

	/**
	 * Create cube for SCAD files
	 * @param box The box to transform
	 */
	def private static String createFromBox(Box box, String name, boolean opened) {
		val cubeSizeMin = 5.0f
		val cubeSizeMax = 50.0f
		var result = ""
		if (enableLids && opened 
			&& cubeSizeMin <= (box.extensionInEachDirection.z * 2f) 
			&& (box.extensionInEachDirection.z * 2f) <= cubeSizeMax) {

			val wallHeight = 17.0f
			val wallOffest = 60.0f

			result = 
			//creating base 
			"translate([" + box.center.x + "," + -1f * box.center.z + "," + box.center.y * heightScaleFactor + "])" + " " + //position in axis
			"color([" + box.color.x + "," + box.color.y + "," + box.color.z + "]) " +	//apply color
			"cube(size = [" + (box.extensionInEachDirection.x * 2f - wallThickness) + "," //cube dimensions
			+ (box.extensionInEachDirection.z * 2f - wallThickness) + "," + //cube dimensions
			box.extensionInEachDirection.y * 2.04f * heightScaleFactor + "], center = true);\n\t\t" //cube dimensions
			
			+ "difference() {" + "\n\t\t\t" //creating lid

			+ "translate([" + box.center.x + "," + -1f * box.center.z + "," + wallOffest + "])" + " " + //position in axis
			"color([" + box.color.x + "," + box.color.y + "," + box.color.z + "]) " +	//apply color
			"cube(size = [" + (box.extensionInEachDirection.x * 2f)+ "," + //cube length
			(box.extensionInEachDirection.z * 2f) + "," + //cube width
			wallHeight + "], center = true);\n\t\t\t" //cube height

			+ "translate([" + box.center.x + "," + -1f * box.center.z + "," + (wallOffest + wallHeight/2f) + "])" + " " + //position in axis
			"cube(size = [" + (box.extensionInEachDirection.x * 2f - wallThickness)+ "," + //cube length
			(box.extensionInEachDirection.z * 2f - wallThickness) + "," + //cube width
			(wallHeight - wallThickness) + "], center = true);\n\t\t" + //cube height
			"}\n\t\t"

			+ labelCreate(name,box.extensionInEachDirection.z * 2.0f,
							box.center.x - box.extensionInEachDirection.x,
							box.center.z,
							wallOffest)
			
		} else {
			
		result = 
		"translate([" + box.center.x + "," + -1f * box.center.z + "," + box.center.y * heightScaleFactor + "])" + " " + //position in axis
		"color([" + box.color.x + "," + box.color.y + "," + box.color.z + "]) " +	//apply color
		"cube(size = [" + box.extensionInEachDirection.x * 2f + "," + box.extensionInEachDirection.z * 2f + "," + //cube dimensions
		box.extensionInEachDirection.y * 2.04f * heightScaleFactor + "], center = true);\n\t\t" //cube dimensions
			
		}	
		result = result + labelCreate(name, box, opened)
	}

	/**
	 * Create cube for SCAD files especially for classes
	 * @param box The box to transform
	 */
	def private static String createFromBox(Box box) {
		"translate([" + box.center.x + "," + -1f * box.center.z + "," + box.center.y * heightScaleFactor + "])" + " " + //position in axis
		"color([" + box.color.x + "," + box.color.y + "," + box.color.z + "]) " +	//apply color
		"cube(size = [" + box.extensionInEachDirection.x * 2f + "," + box.extensionInEachDirection.z * 2f + "," + //cube dimensions
		box.extensionInEachDirection.y * 2.04f * heightScaleFactor + "], center = true);\n\t\t" //cube dimensions
	}

	/////////////////////////////////////// labels ///////////////////////////////////////

	/**
	 * Creating a 3D label for a box; opened and closed
	 * @param text The text of the label
	 * @param box The object to label 
	 */
	def private static String labelCreate(String text, Box box, boolean opened) {
		val result = ""
		val charDimensions = 7.5f
		val min_scale = 0.25f
		var scale = 0.6f

		if (opened) {
			while (((text.length as float) * charDimensions * scale) > (box.extensionInEachDirection.z * 2.0f - wallThickness)) {
				scale = scale - 0.01f
			}
			
			if (scale >= min_scale) {
				val x = box.center.x - box.extensionInEachDirection.x +
					(charDimensions * scale) / 2f
				val y = (-1f * box.center.z) + ((text.length as float) * charDimensions * scale / 2f)
				val z = (box.center.y * heightScaleFactor) +
					(box.extensionInEachDirection.y * 1.02f * heightScaleFactor)
				return labelPosition(x, y, z, "-90") + labelText(text, scale) + "\n\t\t"
			}
		} else {
			while (((text.length as float) * charDimensions * scale) > (box.extensionInEachDirection.x * 2.0f) ||
				charDimensions * scale > (box.extensionInEachDirection.z * 2.0f - wallThickness)) {
				scale = scale - 0.01f
			}

			if (scale >= min_scale) {
				val x = box.center.x - ((text.length as float) * charDimensions * scale / 2f)
				val y = (-1f * box.center.z) - (charDimensions * scale) / 2f
				val z = (box.center.y * heightScaleFactor) +
					(box.extensionInEachDirection.y * 1.02f * heightScaleFactor)
				return labelPosition(x, y, z) + labelText(text, scale) + "\n\t\t"
			}
		}		
		//if (scale >= min_scale) fails
		return result
	}

	/**
	 * Creating a 3D label for a lid
	 * @param text The text of the label
	 * @param width The width of the ground to place the label
	 * @param x The x coordinate of the center of the ground
	 * @param y The y coordinate of the center of the ground
	 * @param z The z coordinate of the center of the ground
	 */
	def private static String labelCreate(String text, float width, float x, float y, float z) {
		val result = ""
		val charDimensions = 6f
		val min_scale = 0.2f
		var scale = 0.6f

		while (((text.length as float) * charDimensions * scale) > width) {
				scale = scale - 0.01f
		}

		if (scale >= min_scale) {
			return labelPosition(x,
								(-1f * y) - ((text.length as float) * charDimensions * scale / 2f),
								z,
								"a=[-90,0,90]")
					+ labelText(text, scale) + "\n\t\t"
		}
		//if (scale >= min_scale) fails
		return result
	}

	/////////////////////////////////////// common label stuff ///////////////////////////////////////
	
		/**
	 * Translating the label to a certain position
	 * @param x The x-coordinate on the axis
	 * @param y The y-coordinate on the axis
	 * @param z The z-coordinate on the axis
	 */
	def private static String labelPosition(float x, float y, float z) {
		"translate([" + x + "," + y + "," + z + "]) "
	}
	
	/**
	 * Translating the label to a certain position with a rotation
	 * @param x The x-coordinate on the axis
	 * @param y The y-coordinate on the axis
	 * @param z The z-coordinate on the axis
	 * @param angle The angle of the rotation
	 */
	def private static String labelPosition(float x, float y, float z, String angle) {
		"translate([" + x + "," + y + "," + z + "]) " + "rotate(" + angle + ") "
	}

	/**
	 * Printing the text of a label with a fixed scale
	 * @param text The text of the label 
	 */
	def private static String labelText(String text, float scale) {
		"color(\"white\") scale([" + scale + "," + scale + ",1.0]) linear_extrude(height = 1, center = true, convexity = 1000, twist = 0) text(t = \"" + text + "\",  font = \"Consolas\");"
	}
}
