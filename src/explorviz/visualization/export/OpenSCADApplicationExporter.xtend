package explorviz.visualization.export

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.visualization.engine.primitives.Box
import explorviz.visualization.layout.application.ApplicationLayoutInterface
import java.util.List

class OpenSCADApplicationExporter {

	/////////////////////////////////////// globals //////////////////////////////////////
	/**
	 * Scaling for boxes
	 */
	val static heightScaleFactor = 1.0f

	/**
	 * Enable lids for open boxes
	 */
	var static boolean enableLids = false

	/**
	 * Used for lids
	 */
	val static wallThickness = 1.2f

	/**
	 * Enable labels on model
	 */
	var static boolean enableLabels = true

	/**
	 * Font for labels
	 */
	val static font = "Consolas"

	/**
	 * Size of letters of labels
	 */
	val static charDimensionLength = 7.5f

	/**
	 * Size of letters of labels
	 */
	val static charDimensionWidth = 14.1f

	/**
	 * The height of a single letter
	 */
	val static labelHeight = 1.0f

	/**
	 * Default scaling factor for labels
	 */
	val static defaultLabelScale = 0.55f

	/**
	 * Minimum scale factor for letters of labels
	 */
	val static min_scale = 0.25f

	/////////////////////////////// OpenSCAD default code ////////////////////////////////
	
	/**
	 * Create the frame of the SCAD file source code
	 * @param application The application to transform to a 3D model
	 * @param exportType 1 => no cuts, 2 => x cuts, 3 => y cuts, 4 => both axes
	 */
	def static String exportApplicationAsOpenSCAD(Application application, int exportType) {
		var result = ""
		
		//puzzle settings
		if (exportType > 1) {
			result = result + "//puzzle settings" + "\n" + "stampSize=[500,500,100];" + "\n" + "cutSize=10;" + "\n" +
				"xCut1=[-425,-375,-325,-275,-225,-175,-125,-75,-25,25,75,125,175,225,275,325,375,425];" + "\n" +
				"yCut1=[-425,-375,-325,-275,-225,-175,-125,-75,-25,25,75,125,175,225,275,325,375,425];" + "\n" +
				"kerf = -0.5;" + "\n" + "makePuzzle();" + "\n\n"
		}
		
		//puzzle cuts
		switch exportType {
			case exportType == 2:
				//x axis
				result = result + "//cut x axis" + "\n" + "module makePuzzle()" + "\n" + "{" + "\n\t" +
					"xMaleCut() application();" + "\n\t" + "translate([0,10,0]) xFemaleCut() application();" + "\n" +
					"}" + "\n\n"
			case exportType == 3:
				//y axis			
				result = result + "//cut y axis" + "\n" + "module makePuzzle()" + "\n" + "{" + "\n\t" +
					"yMaleCut() application();" + "\n\t" + "translate([-10,0,0]) yFemaleCut() application();" + "\n" +
					"}" + "\n\n"
			case exportType == 4:
				//both axis
				result = result + "//cut both axis" + "\n" + "module makePuzzle()" + "\n" + "{" + "\n\t" +
					"yMaleCut() xMaleCut() application();" + "\n\t" +
					"translate([0,10,0]) yMaleCut() xFemaleCut() application();" + "\n\t" +
					"translate([-10,0,0]) yFemaleCut() xMaleCut() application();" + "\n\t" +
					"translate([-10,10,0]) yFemaleCut() xFemaleCut() application();" + "\n" + "}" + "\n\n"
			//TODO: do nothing?
			default:
				result = result
		}

		//create application
		result = result + "//application layout" + "\n" + "module application()" + "\n" + "{" + "\n" + "\t union() {" +
			"\n\t\t" + createApplication(application.components.get(0)) + "}" + "\n" + "}" + "\n\n"
		
		//draw application (only needed without puzzles)
		if (exportType == 1) {
			result = result + "//draw application" + "\n" + "application();"
		}

		//create puzzle library
		if (exportType > 1) {
			result = result + "//puzzle library" + "\n" + "//OpenSCAD PuzzleCut Library Demo - by Rich Olson" + "\n" + "//http://www.nothinglabs.com" + "\n" +
				"//License: http://creativecommons.org/licenses/by/3.0/" + "\n" + "module xMaleCut(offset=0, cut=xCut1)" + "\n" + "{" +
				"\n\t" + "difference()" + "\n\t" + "{" + "\n\t\t" + "children(0);" + "\n\t\t" +
				"translate([0,offset,0]) makePuzzleStamp(cutLocations=cut);" + "\n\t" + "}" + "\n" + "}" + "\n\n" +
				"module xFemaleCut(offset=0, cut=xCut1)" + "\n" + "{" + "\n\t" + "intersection()" + "\n\t" + "{" +
				"\n\t\t" + "children(0);" + "\n\t\t" +
				"translate([0,offset,0]) makePuzzleStamp(cutLocations=cut,kerf=kerf);" + "\n\t" + "}" + "\n" + "}" +
				"\n\n" + "module yMaleCut(offset=0, cut=yCut1)" + "\n" + "{" + "\n\t" + "difference()" + "\n\t" + "{" +
				"\n\t\t" + "children(0);" + "\n" + "\t\t" +
				"rotate([0,0,90]) translate([0,offset,0]) makePuzzleStamp(cutLocations=cut);" + "\n\t" + "}" + "\n" +
				"}" + "\n\n" + "module yFemaleCut(offset=0, cut=yCut1)" + "\n" + "{" + "\n\t" + "intersection()" +
				"\n\t" + "{" + "\n\t\t" + "children(0);" + "\n\t\t" +
				"rotate([0,0,90]) translate([0,offset,0]) makePuzzleStamp(cutLocations=cut,kerf=kerf);" + "\n\t" +
				"}" + "\n" + "}" + "\n\n" + "module makePuzzleStamp(kerf=0)" + "\n" + "{" + "\n\t" + "difference()" +
				"\n\t" + "{" + "\n\t\t" + "translate([0,stampSize[0]/2-kerf,0]) cube(stampSize,center=true);" +
				"\n\t\t" + "for(i=cutLocations)" + "\n\t\t" + "{" + "\n\t\t\t" +
				"translate([i,0,0]) cube([(cutSize/2)-kerf*2,cutSize-kerf*2,stampSize[2]],center=true);" + "\n\t\t\t" +
				"translate([i,cutSize/2,0]) cube([cutSize-kerf*2,(cutSize/2)-kerf*2,stampSize[2]],center=true);" +
				"\n\t\t" + "}" + "\n\t" + "}" + "\n" + "}"
		}
		result
	}

	//TODO: Florian fragen, ob eine Apllication auch mehrere first level components hat.
	//-> NEIN!
	/**
	 * Create the basic platform of the 3D model and add all components to the result string
	 * @param application A component containing a list of all components of the application
	 */
	def private static String createApplication(Component application) {
		var result = ""
		var entity = application as Draw3DNodeEntity
		for (primitiveObject : entity.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				result = createFromBoxPlatform(primitiveObject, entity.name) + createApplicationComponents(application.children) + result
			}
		}
		result	
	}	

	/**
	 * Transform all components of the application
	 * @param components A list of all components of the application
	 * @return A string containing OpenSCAD data
	 */
	def private static String createApplicationComponents(List<Component> components) {
		var result = ""
		for (component : components) {
			result = createApplicationComponent(component) + createApplicationClazzes(component.clazzes) + result 
		}
		result
	}

	/**
	 * Add packages and their subpackages represented by components to the result string
	 * @param component A component with optional subcomponents
	 * @return A string containing OpenSCAD data
	 */
	def private static String createApplicationComponent(Component component) {
		createFromPrimitiveObjects(component) + createApplicationComponents(component.children)
	}

	/**
	 * Transform every package represented by an entity into a box
	 * @param entity The entity to check
	 * @return A string containing OpenSCAD data
	 */
	def private static String createFromPrimitiveObjects(Draw3DNodeEntity entity) {
		var result = ""
		for (primitiveObject : entity.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				//TODO: Wenn weniger als 3 Kinder keinen Deckel
				if(enableLids && (entity as Component).opened){
					//TODO: Höhe der Subcomponenten bestimmen + Höhe der Componente
					result = result + createFromBoxLid(primitiveObject, entity.name, 0)
				} else {
					result = result + createFromBoxNormal(primitiveObject, entity.name, (entity as Component).opened)
				}
				
			}
		}
		result
	}

	/**
	 * Add all classes in the current package to the result string
	 * @param clazzes The list of classes to transform
	 * @return A string containing OpenSCAD data
	 */
	def private static String createApplicationClazzes(List<Clazz> clazzes) {
		var result = ""
		for (clazz : clazzes) {
			result = result + createFromPrimitiveClasses(clazz)
		}
		result
	}

	/**
	 * Transform a single class into a box
	 * @param entity The class to transform
	 * @return A string containing OpenSCAD data
	 */
	def private static String createFromPrimitiveClasses(Draw3DNodeEntity entity) {
		var result = ""
		for (primitiveObject : entity.primitiveObjects) {
			if (primitiveObject instanceof Box) {
				result = result + createFromBoxClass(primitiveObject)
			}
		}
		result
	}

	///////////////////////////////////// normal boxes ///////////////////////////////////
	
	/**
	 * Create cube for SCAD files
	 * @param box The box to transform
	 * @return A string containing OpenSCAD data
	 */
	def private static String createFromBoxNormal(Box box, String name, boolean opened) {
		"translate([" + box.center.x + "," + -1f * box.center.z + "," + box.center.y * heightScaleFactor +
				"])" + " " + //position in axis
				"color([" + box.color.x + "," + box.color.y + "," + box.color.z + "]) " + //apply color
				"cube(size = [" + box.extensionInEachDirection.x * 2f + "," + box.extensionInEachDirection.z * 2f + "," + //cube dimensions
				box.extensionInEachDirection.y * 2.04f * heightScaleFactor + "], center = true);\n\t\t" + //cube dimensions
		if (enableLabels) {
			labelCreate(name, box, opened)
		}
	}

	///////////////////////////////////// class boxes ////////////////////////////////////

	/**
	 * Create cube for SCAD files especially for classes
	 * @param box The box to transform
	 * @return A string containing OpenSCAD data
	 */
	def private static String createFromBoxClass(Box box) {
		"translate([" + box.center.x + "," + -1f * box.center.z + "," + box.center.y * heightScaleFactor + "])" + " " + //position in axis
			"color([" + box.color.x + "," + box.color.y + "," + box.color.z + "]) " +	//apply color
			"cube(size = [" + box.extensionInEachDirection.x * 2f + "," + box.extensionInEachDirection.z * 2f + "," + //cube dimensions
			box.extensionInEachDirection.y * 2.04f * heightScaleFactor + "], center = true);\n\t\t" //cube dimensions
	}
	
		//////////////////////////////// basic platform box //////////////////////////////
	
	/**
	 * Create cube for SCAD files
	 * @param box The box to transform
	 * @return A string containing OpenSCAD data
	 */
	def private static String createFromBoxPlatform(Box box, String name) {
		"translate([" + box.center.x + "," + -1f * box.center.z + "," + box.center.y * heightScaleFactor +
				"])" + " " + //position in axis
				"color([" + box.color.x + "," + box.color.y + "," + box.color.z + "]) " + //apply color
				"cube(size = [" + box.extensionInEachDirection.x * 2f + "," + box.extensionInEachDirection.z * 2f + "," + //cube dimensions
				box.extensionInEachDirection.y * 2.04f * heightScaleFactor + "], center = true);\n\t\t" + //cube dimensions
		if (enableLabels) {
			labelCreatePlatform(name, box)
		}
	}

	/////////////////////////////////// boxes with lids //////////////////////////////////

	/**
	 * Create cube for SCAD files
	 * @param box The box to transform
	 * @return A string containing OpenSCAD data
	 */
	def private static String createFromBoxLid(Box box, String name, int subcomponentHeight) {
		val cubeSizeMin = 5.0f
		val cubeSizeMax = 50.0f
		var result = ""
		
		if (cubeSizeMin <= (box.extensionInEachDirection.z * 2f) &&	(box.extensionInEachDirection.z * 2f) <= cubeSizeMax) {

			val wallHeight = subcomponentHeight + labelHeight + 1.0f
			val wallOffest = 60.0f

			result = 
			//creating base 
			"translate([" + box.center.x + "," + -1f * box.center.z + "," + box.center.y * heightScaleFactor + "])" + " " + //position in axis
				"color([" + box.color.x + "," + box.color.y + "," + box.color.z + "]) " + //apply color
				"cube(size = [" + (box.extensionInEachDirection.x * 2f - wallThickness) + "," //cube dimensions
				+ (box.extensionInEachDirection.z * 2f - wallThickness) + "," + //cube dimensions
				box.extensionInEachDirection.y * 2.04f * heightScaleFactor + "], center = true);\n\t\t" //cube dimensions
				
				//creating lid
				+ "difference() {" + "\n\t\t\t" 
				+ "translate([" + box.center.x + "," + -1f * box.center.z + "," + wallOffest + "])" + " " + //position in axis
				"color([" + box.color.x + "," + box.color.y + "," + box.color.z + "]) " + //apply color
				"cube(size = [" + (box.extensionInEachDirection.x * 2f) + "," + //cube length
				(box.extensionInEachDirection.z * 2f) + "," + //cube width
				wallHeight + "], center = true);\n\t\t\t" //cube height
				
				+ "translate([" + box.center.x + "," + -1f * box.center.z + "," + (wallOffest + (wallThickness / 2f)) + "])" + " " + //position in axis
				"cube(size = [" + (box.extensionInEachDirection.x * 2f - wallThickness) + "," + //cube length
				(box.extensionInEachDirection.z * 2f - wallThickness) + "," + //cube width
				wallHeight + "], center = true);\n\t\t" + //cube height
				
				"}\n\t\t"

			if (enableLabels) {
				result = result + labelCreateLid(name, box.extensionInEachDirection.z * 2.0f,
					box.center.x - box.extensionInEachDirection.x, box.center.z, wallOffest)
			}

		} else {

			result = "translate([" + box.center.x + "," + -1f * box.center.z + "," + box.center.y * heightScaleFactor +
				"])" + " " + //position in axis
				"color([" + box.color.x + "," + box.color.y + "," + box.color.z + "]) " + //apply color
				"cube(size = [" + box.extensionInEachDirection.x * 2f + "," + box.extensionInEachDirection.z * 2f + "," + //cube dimensions
				box.extensionInEachDirection.y * 2.04f * heightScaleFactor + "], center = true);\n\t\t" //cube dimensions

		}

		if (enableLabels) {
			result = result + labelCreate(name, box, true)
		}

	}

	////////////////////////////////// labels for boxes //////////////////////////////////
	
	/**
	 * Creating a 3D label for a box; opened and closed
	 * @param text The text of the label
	 * @param box The object to label
	 * @return A string containing OpenSCAD data
	 */
	def private static String labelCreate(String text, Box box, boolean opened) {
		val result = ""
		var scale = defaultLabelScale
		
		if (opened) {
			
			while (((text.length as float) * charDimensionLength * scale) >
				(box.extensionInEachDirection.z * 2.0f - wallThickness)) {
				scale = scale - 0.01f
			}

			if (scale >= min_scale) {
				val x = box.center.x - box.extensionInEachDirection.x + (ApplicationLayoutInterface.labelInsetSpace / 2f)
				val y = (-1f * box.center.z) + ((text.length as float) * charDimensionLength * scale / 2f)
				val z = (box.center.y * heightScaleFactor) +
					(box.extensionInEachDirection.y * 1.02f * heightScaleFactor)
				return labelPosition(x, y, z, "-90") + labelText(text, scale) + "\n\t\t"
			}
			
		} else {

			while (((charDimensionWidth * scale) > (box.extensionInEachDirection.z * 2.0f)) ||
				(((text.length as float) * charDimensionLength * scale) > (box.extensionInEachDirection.x * 2.0f))) {
				scale = scale - 0.01f
			}

			if (scale >= min_scale) {

				val x = box.center.x - ((text.length as float) * charDimensionLength * scale / 2f)
				val y = (-1f * box.center.z) - ((charDimensionWidth * scale) / 4.5f)
				val z = (box.center.y * heightScaleFactor)
						+ (box.extensionInEachDirection.y * 1.02f * heightScaleFactor)
				return labelPosition(x, y, z) + labelText(text, scale) + "\n\t\t"
			}
		}

		return result
	}

	////////////////////////////////// labels for lids ///////////////////////////////////

	/**
	 * Creating a 3D label for a lid
	 * @param text The text of the label
	 * @param width The width of the ground to place the label
	 * @param x The x coordinate of the center of the ground
	 * @param y The y coordinate of the center of the ground
	 * @param z The z coordinate of the center of the ground
	 * @return A string containing OpenSCAD data
	 */
	def private static String labelCreateLid(String text, float width, float x, float y, float z) {
		val result = ""
		var scale = defaultLabelScale

		while (((text.length as float) * charDimensionLength * scale) > width) {
			scale = scale - 0.01f
		}

		if (scale >= min_scale) {
			return labelPosition(x, (-1f * y) - ((text.length as float) * charDimensionLength * scale / 2f), z,
				"a=[-90,0,90]") + labelText(text, scale) + "\n\t\t"
		}

		return result
	}

	/////////////////////////////// label for the platform ///////////////////////////////

	/**
	 * Creating a 3D label for the platform
	 * @param text The text of the label
	 * @param box The object to label
	 * @return A string containing OpenSCAD data
	 */
	def private static String labelCreatePlatform(String text, Box box) {
		val result = ""
		var scale = defaultLabelScale
		
			while (((text.length as float) * charDimensionLength * scale) >
				(box.extensionInEachDirection.z * 2.0f - wallThickness)) {
				scale = scale - 0.01f
			}

			if (scale >= min_scale) {
				val x = box.center.x - box.extensionInEachDirection.x + (ApplicationLayoutInterface.labelInsetSpace / 2f)
				val y = (-1f * box.center.z) + ((text.length as float) * charDimensionLength * scale / 2f)
				val z = (box.center.y * heightScaleFactor) +
					(box.extensionInEachDirection.y * 1.02f * heightScaleFactor)
				return labelPosition(x, y, z, "-90") + labelTextBlack(text, scale) + "\n\t\t"
			}


		return result
	}

	///////////////////////////////// common label stuff /////////////////////////////////
	
	/**
	 * Translating the label to a certain position
	 * @param x The x-coordinate on the axis
	 * @param y The y-coordinate on the axis
	 * @param z The z-coordinate on the axis
	 * @return A string containing OpenSCAD data
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
	 * @return A string containing OpenSCAD data
	 */
	def private static String labelPosition(float x, float y, float z, String angle) {
		"translate([" + x + "," + y + "," + z + "]) " + "rotate(" + angle + ") "
	}

	/**
	 * Printing the text of a label with a fixed scale
	 * @param text The text of the label
	 * @param scale The scaling of letters
	 * @return A string containing OpenSCAD data
	 */
	def private static String labelText(String text, float scale) {
		"color(\"white\") scale([" + scale + "," + scale + "," + labelHeight +
			"]) linear_extrude(height = 1,center = true,convexity = 1000,twist = 0) text(\"" + text +
			"\",font = \"" + font + "\");"
	}
	
	/**
	 * Printing the text for the basic platform
	 * @param text The text of the label
	 * @param scale The scaling of letters
	 * @return A string containing OpenSCAD data
	 */
	def private static String labelTextBlack(String text, float scale) {
		"color(\"black\") scale([" + scale + "," + scale + "," + labelHeight +
			"]) linear_extrude(height = 1,center = true,convexity = 1000,twist = 0) text(\"" + text +
			"\",font = \"" + font + "\");"
	}
}
