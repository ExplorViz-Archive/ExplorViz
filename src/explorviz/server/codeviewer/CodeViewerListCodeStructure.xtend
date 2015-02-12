package explorviz.server.codeviewer

import java.util.ArrayList
import java.util.List
import java.io.File

class CodeViewerListCodeStructure {
    def static getCodeTree(String sourceFolder) {
        val root = new ArrayList<TreeElement>
        
        val fileExtensions = new ArrayList<String>
        fileExtensions.add(".java")
        fileExtensions.add(".xtend")
        fileExtensions.add(".c")
        fileExtensions.add(".cpp")
        fileExtensions.add(".c#")
        fileExtensions.add(".h")
        fileExtensions.add(".hpp")
        
        createCodeStructure(new File(sourceFolder), root, fileExtensions)
        
        getCodeTreeHTML(root)
    }
    
    def static private void createCodeStructure(File directory, List<TreeElement> result, List<String> fileExtensions) {
        val files = directory.listFiles
        val realFiles = new ArrayList<String>
        
        if (files == null) return;
        
        for (file : files) {
            if (file.isDirectory) {
                if (file.name != "." && file.name != "..") {
                    val itemWithChildren = new TreeElement(file.name)
                    createCodeStructure(file,itemWithChildren.children, fileExtensions)
                    result.add(itemWithChildren)
                }
            } else {
            	for (fileExtension : fileExtensions)
                	if (file.name.endsWith(fileExtension))
                    	realFiles.add(file.name)
            }
        }
        for (realFile : realFiles)
            result.add(new TreeElement(realFile))
    }

    def static private getCodeTreeHTML(List<TreeElement> treeElements) {
        '''<ul id="codetree" class="treeview">
            «getCodeTreeHTMLHelper(treeElements)»
        </ul>'''.toString()
    }
    
    def static private String getCodeTreeHTMLHelper(List<TreeElement> treeElements) {
    	if (treeElements.empty) return "<li>empty source folder</li>"
    	
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