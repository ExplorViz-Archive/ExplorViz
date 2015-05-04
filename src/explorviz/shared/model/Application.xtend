package explorviz.shared.model

import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.DrawNodeEntity
import explorviz.shared.model.helper.ELanguage
import java.util.ArrayList
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors

class Application extends DrawNodeEntity {
	@Accessors var int id

	@Accessors var boolean database

	@Accessors var ELanguage programmingLanguage

	@Accessors long lastUsage

	@Accessors Node parent

	@Accessors var List<Component> components = new ArrayList<Component>

	@Accessors var List<CommunicationClazz> communications = new ArrayList<CommunicationClazz>

	@Accessors val transient List<CommunicationAppAccumulator> communicationsAccumulated = new ArrayList<CommunicationAppAccumulator>

	@Accessors var List<Communication> incomingCommunications = new ArrayList<Communication>
	@Accessors var List<Communication> outgoingCommunications = new ArrayList<Communication>
	
	@Accessors var List<DatabaseQuery> databaseQueries = new ArrayList<DatabaseQuery>

	override void destroy() {
		for (component : components)
			component.destroy()
		for (commuAccum : communicationsAccumulated)
			commuAccum.destroy()
		super.destroy()
	}

	def void clearAllPrimitiveObjects() {
		for (component : components)
			component.clearAllPrimitiveObjects()

	//		communicationsAccumulated.x[it.clearAllPrimitiveObjects()] done in extra method
	}

	def void unhighlight() {
		for (component : components)
			component.unhighlight()
	}

	def void openAllComponents() {
		for (component : components)
			component.openAllComponents()
	}

}
