package explorviz.server.export.rsf

import org.eclipse.xtend.lib.annotations.Accessors

class RSFCall {
	@Accessors RSFTreeNode caller
	@Accessors RSFTreeNode callee
	@Accessors RSFSignature signature
}