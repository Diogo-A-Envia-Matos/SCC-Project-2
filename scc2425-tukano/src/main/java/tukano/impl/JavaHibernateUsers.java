package tukano.impl;

import static java.lang.String.*;
import static tukano.api.Result.ErrorCode.*;
import static tukano.api.Result.*;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import tukano.api.Result;
import tukano.api.User;
import tukano.api.Users;
import utils.Authentication;
import utils.BlobManager;
import utils.DB;
import utils.DBHibernate;

public class JavaHibernateUsers implements Users {

	private static Logger Log = Logger.getLogger(JavaHibernateUsers.class.getName());

	private static Users instance;

	private static DB database;

	synchronized public static Users getInstance() {
		if( instance == null )
			instance = new JavaHibernateUsers();
		return instance;
	}

	private JavaHibernateUsers() {
		database = DBHibernate.getInstance();
	}
	
	@Override
	public Result<String> createUser(User user) {
		Log.info(() -> format("createUser : %s\n", user));

		if( badUserInfo( user ) )
				return error(BAD_REQUEST);

		return errorOrValue( database.insertOne( user), user.getId() );
	}

	@Override
	public Result<Response> getUser(String userId, String pwd) {
		Log.info( () -> format("getUser : userId = %s, pwd = %s\n", userId, pwd));

		if (userId == null)
			return error(BAD_REQUEST);
			
		// return validatedUserOrError( database.getOne( userId, userId, User.class), pwd);

		// var validUser = validatedUserOrError( database.getOne( userId, userId, User.class), pwd);
		// if (!validUser.isOK()) {
		// 	return validUser;
		// }

		return errorOrResult( validatedUserOrError(database.getOne( userId, userId, User.class), pwd), user -> createCookie(user));
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User other) {
		Log.info(() -> format("updateUser : userId = %s, pwd = %s, user: %s\n", userId, pwd, other));

		if (badUpdateUserInfo(userId, pwd, other))
			return error(BAD_REQUEST);

		return errorOrResult( validatedUserOrError(database.getOne( userId, userId, User.class), pwd), user -> database.updateOne( user.updateFrom(other)));
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {
		Log.info(() -> format("deleteUser : userId = %s, pwd = %s\n", userId, pwd));

		if (userId == null || pwd == null )
			return error(BAD_REQUEST);

		return errorOrResult( validatedUserOrError(database.getOne( userId, userId, User.class), pwd), user -> {

			// Delete user shorts and related info asynchronously in a separate thread
			Executors.defaultThreadFactory().newThread( () -> {
				JavaHibernateShorts.getInstance().deleteAllShorts(userId, pwd, Token.get(userId));
				BlobManager.deleteAllBlobs(userId, Token.get(userId));
			}).start();
			
			return database.deleteOne( user);
		});
	}

	@Override
	public Result<List<User>> searchUsers(String p) {
		String pattern = Objects.toString(p, "");
		Log.info(() -> format("searchUsers : patterns = '%s'\n", pattern));

		String query = format("SELECT * FROM DataUser u WHERE UPPER(u.id) LIKE '%%%s%%'", pattern.toUpperCase());
		var hits = database.sql(query, User.class)
				.stream()
				.map(User::copyWithoutPassword)
				.toList();

		Log.info(() -> format("searchUsers : nHits = %s\n", hits.size()));

		return ok(hits);
	}

	
	private Result<User> validatedUserOrError( Result<User> res, String pwd ) {
		if( res.isOK())
			return res.value().getPwd().equals( pwd ) ? res : error(FORBIDDEN);
		else
			return res;
	}
	
	private boolean badUserInfo( User user) {
		return (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null);
	}
	
	private boolean badUpdateUserInfo( String userId, String pwd, User info) {
		return (userId == null || pwd == null || info.getId() != null && ! userId.equals( info.getId()));
	}

	private Result<Response> createCookie(User user) {
		try {
			var resp = Authentication.login(user);
			return ok(resp);
		} catch (Exception e) {
			return error(INTERNAL_ERROR);
		}
	}
}
