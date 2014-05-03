package explorviz.server.codeviewer

import java.io.BufferedReader
import java.io.FileReader
import explorviz.server.main.FileSystemHelper

class CodeViewerGetCode {
    def static getCode(String filepath) { 
        val breader = new BufferedReader(new FileReader(FileSystemHelper.getSourceDirectory() + "/" + filepath))
        var line = breader.readLine
        val sb = new StringBuilder
        while (line != null) {
            sb.append(line + "\n")
            line = breader.readLine
        }
        sb.toString
    }
}