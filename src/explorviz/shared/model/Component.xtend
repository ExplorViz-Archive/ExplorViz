package explorviz.shared.model

import java.util.ArrayList
import com.google.gwt.user.client.rpc.IsSerializable

class Component implements IsSerializable {
    @Property var String name
    @Property var String fullQualifiedName
    
    @Property var children = new ArrayList<Component>
    
    @Property var clazzes = new ArrayList<Clazz>
}