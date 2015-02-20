package explorviz.server.repository.helper

import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.shared.model.Clazz

class RemoteRecordBuffer {
	@Accessors long timestampPutIntoBuffer = java.lang.System.nanoTime()
	
	@Accessors Clazz belongingClazz
}