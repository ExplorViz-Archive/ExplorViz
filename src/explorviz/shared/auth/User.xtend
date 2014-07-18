package explorviz.shared.auth

import java.util.List
import java.util.ArrayList
import com.google.gwt.user.client.rpc.IsSerializable

class User implements IsSerializable {
	@Property transient int id
	@Property String username
	@Property transient String hashedPassword
	@Property transient String salt
	@Property List<Role> roles = new ArrayList<Role>
	@Property boolean firstLogin

	protected new() {
	}

	new(int id, String username, String hashedPassword, String salt, boolean firstLogin) {
		this.id = id
		this.username = username
		this.hashedPassword = hashedPassword
		this.salt = salt
		this.firstLogin = firstLogin
	}
	
	def void addToRoles(Role role) {
		roles.add(role)
	}
}
