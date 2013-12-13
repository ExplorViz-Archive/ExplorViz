package explorviz.server.codeviewer

import java.util.ArrayList
import java.util.List
import java.io.File

class CodeViewerListCodeStructure {
    def static getCodeTreeExample() {
        val root = new ArrayList<TreeElement>
        createCodeStructure(new File("source"), root, ".java")
        
        getCodeTreeHTML(root)
    }
    
    def static private void createCodeStructure(File directory, List<TreeElement> result, String fileExtension) {
        val files = directory.listFiles
        val realFiles = new ArrayList<String>
        
        files.forEach[
            if (it.isDirectory) {
                if (it.name != "." && it.name != "..") {
                    val itemWithChildren = new TreeElement(it.name)
                    createCodeStructure(it,itemWithChildren.children, fileExtension)
                    result.add(itemWithChildren)
                }
            } else {
                if (it.name.endsWith(fileExtension))
                    realFiles.add(it.name)
            }
        ]
        realFiles.forEach[
            result.add(new TreeElement(it))
        ]
    }

    def static private getCodeTreeHTML(List<TreeElement> treeElements) {
        '''<ul id="codetree" class="treeview">
            «getCodeTreeHTMLHelper(treeElements)»
        </ul>'''.toString()
    }
    
    def static private String getCodeTreeHTMLHelper(List<TreeElement> treeElements) {
        '''«FOR i : 0 .. treeElements.size-1»
            <li>«treeElements.get(i).name»
            «if (treeElements.get(i).hasChildren)
                "<ul>" +
                    getCodeTreeHTMLHelper(treeElements.get(i).children) +
                "</ul>"
            »</li>
            «ENDFOR»
        '''.toString()
    }
}