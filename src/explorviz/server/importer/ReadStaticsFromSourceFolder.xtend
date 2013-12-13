package explorviz.server.importer

import explorviz.shared.model.Landscape
import explorviz.shared.model.NodeGroup
import explorviz.shared.model.Node
import explorviz.shared.model.Application
import java.io.File
import explorviz.shared.model.Component
import explorviz.shared.model.Clazz

class ReadStaticsFromSourceFolder {
    
    def static Landscape readInFolder(String folder) {
        val landscape = new Landscape()
        landscape.hash = folder.hashCode

        val nodeGroup = new NodeGroup()

        val node = new Node()
        node.ipAddress = "10.10.10.1"

        val application = createApplication(folder)

        node.applications.add(application)
        nodeGroup.nodes.add(node)
        landscape.nodeGroups.add(nodeGroup)

        landscape
    }

    def static private Application createApplication(String folderPath) {
        val folder = new File(folderPath)

        val application = new Application()
        application.id = 0
        application.name = folder.name
        
        val subfiles = folder.listFiles()
        subfiles.forEach [
            if (it.directory && it.name != "." && it.name != "..") {
                val subcomponent = new Component()
                subcomponent.name = it.name
                subcomponent.fullQualifiedName = it.name
                createComponents(subcomponent, it)
                application.components.add(subcomponent)
            }
        ]

        application
    }

    def static private void createComponents(Component currentComponent, File file) {
        val subfiles = file.listFiles()

        subfiles.forEach [
            if (it.directory) {
                val subcomponent = new Component()
                subcomponent.name = it.name
                subcomponent.fullQualifiedName = currentComponent + "." + it.name
                createComponents(subcomponent, it)
                if (subcomponent.children.size() > 0 || subcomponent.clazzes.size() > 0) {
                    currentComponent.children.add(subcomponent)
                }
            } else if (it.file) {
                if (it.name.endsWith(".java")) {
                    val clazz = new Clazz()
                    val clazzname = it.name.replace(".java", "")
                    clazz.name = clazzname
                    clazz.fullQualifiedName = currentComponent + "." + clazzname
                    clazz.instanceCount = 30
                    currentComponent.clazzes.add(clazz)
                }
            }
        ]
    }


}
