package tukano.impl.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import java.util.List;
import tukano.api.User;
import tukano.api.Users;
import tukano.api.rest.RestUsers;
import tukano.impl.JavaHibernateUsers;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers {
	
	static final Users impl = JavaHibernateUsers.getInstance();

	@Override
	public String createUser(User user) {
		return super.resultOrThrow( impl.createUser( user));
	}

	@Override
	public Response getUser(String name, String pwd) {
		return super.resultOrThrow( impl.getUser(name, pwd));
	}
	
	@Override
	public User updateUser(String name, String pwd, User user) {
		return super.resultOrThrow( impl.updateUser(name, pwd, user));
	}

	@Override
	public User deleteUser(String name, String pwd) {
		return super.resultOrThrow( impl.deleteUser(name, pwd));
	}

	@Override
	public List<User> searchUsers(String pattern) {
		return super.resultOrThrow( impl.searchUsers( pattern));
	}
}
