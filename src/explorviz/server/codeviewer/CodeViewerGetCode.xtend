package explorviz.server.codeviewer

import java.io.BufferedReader
import java.io.FileReader
import java.io.File

class CodeViewerGetCode {
    def static getCode(String sourceFolder, String filepath) { 
    	val file = new File(sourceFolder + "/" + filepath)
    	if (!file.exists) return "not found"
    	
        val breader = new BufferedReader(new FileReader(sourceFolder + "/" + filepath))
        var line = breader.readLine
        val sb = new StringBuilder
        while (line != null) {
            sb.append(line + "\n")
            line = breader.readLine
        }
        sb.toString
    }
}