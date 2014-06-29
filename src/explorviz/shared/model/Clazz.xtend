package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable
import java.util.HashSet
import java.util.Set

class Clazz implements IsSerializable {
    @Property var String name
    @Property var String fullQualifiedName
    
    @Property var int instanceCount = 0
    @Property val transient Set<Integer> objectIds = new HashSet<Integer>()
}