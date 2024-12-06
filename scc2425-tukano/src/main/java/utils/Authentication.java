package utils;

import static tukano.api.Result.ErrorCode.*;
import static tukano.api.Result.*;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;
import tukano.api.Result;
import tukano.api.User;

public class Authentication {

	static final DB database = DBHibernate.getInstance();
    private static Logger Log = Logger.getLogger(Authentication.class.getName());

	public static final String COOKIE_KEY = "tukano_session";
	public static final String COOKIE_SECRET = "__encrypt_cookie";
	private static final int MAX_COOKIE_AGE = 3600;

	static public Response login( User user ) {
		String redisCookieKey = user.getId().concat(COOKIE_SECRET);
		String password = user.getPwd();
		System.out.println("user: " + user.getId() + " pwd:" + user.getPwd() );

		if (!validateUser(user).isOK()) {
			System.out.println("User is not authorized");
			throw new NotAuthorizedException("Incorrect login");
		}

		var cookie = new NewCookie.Builder(COOKIE_KEY)
				.value(redisCookieKey).path("/")
				.comment("sessionid")
				.maxAge(MAX_COOKIE_AGE)
				.secure(false) //ideally it should be true to only work for https requests
				.httpOnly(true)
				.build();

		RedisCache.getInstance().putSession( new Session( redisCookieKey, password));

		return Response.ok(user)
				.cookie(cookie)
				.build();
	}
	
	static public Session validateSession(Cookie cookie) throws NotAuthorizedException {

		if (cookie == null )
			throw new NotAuthorizedException("No session initialized");

		var session = RedisCache.getInstance().getSession( cookie.getValue());
		if( session == null )
			throw new NotAuthorizedException("No valid session initialized");

		if (session.uid() == null || session.uid().length() == 0)
			throw new NotAuthorizedException("No valid session initialized");

		return session;
	}

	static private Result<User> validateUser(User user) {
		var res = database.getOne(user.getId(), user.getId(), User.class);
		if( res.isOK())
			return res.value().getPwd().equals( user.getPwd() ) ? res : error(FORBIDDEN);
		else
			return res;
	}
}
