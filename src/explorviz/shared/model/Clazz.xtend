package explorviz.shared.model

import com.google.gwt.user.client.rpc.IsSerializable

class Clazz implements IsSerializable {
    @Property var String name
    @Property var String fullQualifiedName
    
    @Property var int instanceCount = 0
}