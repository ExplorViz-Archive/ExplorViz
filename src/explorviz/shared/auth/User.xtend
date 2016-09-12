package explorviz.shared.auth

import java.util.List
import java.util.ArrayList
import com.google.gwt.user.client.rpc.IsSerializable
import org.eclipse.xtend.lib.annotations.Accessors

class User implements IsSerializable {
	@Accessors transient int id
	@Accessors String username
	@Accessors transient String hashedPassword
	@Accessors transient String salt
	@Accessors List<Role> roles = new ArrayList<Role>
	@Accessors boolean firstLogin
	@Accessors String questionnairePrefix = ""

	protected new() {
	}

	new(int id, String username, String hashedPassword, String salt, boolean firstLogin) {
		this.id = id
		this.username = username
		this.hashedPassword = hashedPassword
		this.salt = salt
		this.firstLogin = firstLogin
	}
	
		new(int id, String username, String hashedPassword, String salt, boolean firstLogin, String questionnairePrefix) {
		this.id = id
		this.username = username
		this.hashedPassword = hashedPassword
		this.salt = salt
		this.firstLogin = firstLogin
		this.questionnairePrefix = questionnairePrefix
	}
	
	def void addToRoles(Role role) {
		roles.add(role)
	}
}
