package explorviz.server.export.rsf

class RSFSignature implements Comparable<RSFSignature> {
	@Property String signature
	@Property int id
	
	override compareTo(RSFSignature o) {
		id <=> o.id
	}
}