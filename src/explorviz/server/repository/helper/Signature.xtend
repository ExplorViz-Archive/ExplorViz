package explorviz.server.repository.helper

import java.util.List
import java.util.ArrayList

class Signature {
	@Property val List<String> modifierList = new ArrayList<String>()
	@Property String returnType = null
	@Property String fullQualifiedName
	@Property String name
	@Property String operationName
	@Property val List<String> paramTypeList = new ArrayList<String>()
}