package explorviz.shared.auth;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Role implements IsSerializable {
	private int id;
	private String rolename;

	protected Role() {
		// default constructor
		id = -1;
		rolename = "none";
	}

	public Role(final int id, final String rolename) {
		this.id = id;
		this.rolename = rolename;
	}

	protected void setRolename(final String rolename) {
		this.rolename = rolename;
	}

	public String getRolename() {
		return rolename;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}
}
