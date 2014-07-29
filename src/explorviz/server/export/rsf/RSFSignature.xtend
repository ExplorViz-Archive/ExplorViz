package explorviz.server.export.rsf

class RSFSignature implements Comparable<RSFSignature> {
	@Property String signature
	@Property String classname
	@Property int id
	
	override compareTo(RSFSignature o) {
		id <=> o.id
	}
}