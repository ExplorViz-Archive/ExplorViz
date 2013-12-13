package explorviz.server.repository.helper

import java.util.List
import java.util.ArrayList

class Signature {
	@Property
	private List<String> modifierList = new ArrayList<String>()
	@Property
	private String returnType = null
	@Property
	private String fullQualifiedName
	@Property
	private String name
	@Property
	private String operationName
	@Property
	private List<String> paramTypeList = new ArrayList<String>()
}