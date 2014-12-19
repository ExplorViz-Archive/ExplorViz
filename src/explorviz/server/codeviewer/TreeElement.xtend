package explorviz.server.codeviewer

import java.util.ArrayList
import org.eclipse.xtend.lib.annotations.Accessors

class TreeElement {
    @Accessors String name
    @Accessors val children = new ArrayList<TreeElement>
    
    new (String nameParam) {
        name = nameParam
    }
    
    def hasChildren() {
        !children.isEmpty()
    }
}