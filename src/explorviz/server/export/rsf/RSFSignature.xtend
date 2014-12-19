package explorviz.server.export.rsf

import org.eclipse.xtend.lib.annotations.Accessors

class RSFSignature implements Comparable<RSFSignature> {
	@Accessors String signature
	@Accessors String classname
	@Accessors int id
	
	override compareTo(RSFSignature o) {
		id <=> o.id
	}
}