package explorviz.server.codeviewer

import java.util.ArrayList

class TreeElement {
    @Property String name
    @Property val children = new ArrayList<TreeElement>
    
    new (String nameParam) {
        name = nameParam
    }
    
    def hasChildren() {
        !children.isEmpty()
    }
}