package explorviz.server.login;

import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;

public class MySaltedAuthentificationInfo implements SaltedAuthenticationInfo {

	private static final long serialVersionUID = -2342452442602696063L;

	private final String username;
	private final String password;
	private final String salt;

	public MySaltedAuthentificationInfo(final String username, final String password,
			final String salt) {
		this.username = username;
		this.password = password;
		this.salt = salt;
	}

	@Override
	public PrincipalCollection getPrincipals() {
		final PrincipalCollection coll = new SimplePrincipalCollection(username, username);
		return coll;
	}

	@Override
	public Object getCredentials() {
		return password;
	}

	@Override
	public ByteSource getCredentialsSalt() {
		return new SimpleByteSource(Base64.decode(salt));
	}

}