package explorviz.server.codeviewer

import java.io.BufferedReader
import java.io.FileReader

class CodeViewerGetCode {
    def static getCode(String sourceFolder, String filepath) { 
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