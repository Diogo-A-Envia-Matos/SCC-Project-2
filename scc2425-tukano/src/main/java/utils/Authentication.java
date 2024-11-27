package utils;

import java.net.URI;
import java.util.UUID;

import static tukano.api.Result.ErrorCode.*;
import static tukano.api.Result.*;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.User;
import tukano.api.Result;
import tukano.impl.JavaHibernateShorts;
import tukano.impl.JavaNoSQLShorts;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import utils.auth.RequestCookies;

//TODO: Testar a autenticação
public class Authentication {

	static final DB database = Boolean.parseBoolean(Props.get("USE_SQL", "false")) ?
			DBHibernate.getInstance() : DBCosmos.getInstance();
	
	static final String PATH = "login";
	static final String USER = "username";
	static final String PWD = "password";
	public static final String COOKIE_KEY = "tukano:session";
	static final String LOGIN_PAGE = "login.html";
	private static final int MAX_COOKIE_AGE = 3600;
	// static final String REDIRECT_TO_AFTER_LOGIN = "/ctrl/version";

	//TODO: Alterar pwdOK
	//TODO: Alterar para só receber strings
	static public Response login( User user ) {
		String uid = user.getId();
		String password = user.getPwd();
		System.out.println("user: " + user.getId() + " pwd:" + user.getPwd() );
		// boolean pwdOk = true; // replace with code to check user password
		if (validateUser(user).isOK()) {
			var cookie = new NewCookie.Builder(COOKIE_KEY)
					.value(uid).path("/")
					.comment("sessionid")
					.maxAge(MAX_COOKIE_AGE)
					.secure(false) //ideally it should be true to only work for https requests
					.httpOnly(true)
					.build();
			
			RedisCache.getInstance().putSession( new Session( uid, password));	
			
			//TODO: Add User to response
			//TODO: Test user
            return Response.ok(user)
                    .cookie(cookie)
                    .build();
		} else
			throw new NotAuthorizedException("Incorrect login");
	}
	
	static public String login() {
		try {
			var in = Authentication.class.getClassLoader().getResourceAsStream(LOGIN_PAGE);
			return new String( in.readAllBytes() );			
		} catch( Exception x ) {
			throw new WebApplicationException( Status.INTERNAL_SERVER_ERROR );
		}
	}
	
	static public Session validateSession(Cookie cookie) throws NotAuthorizedException {

		if (cookie == null )
			throw new NotAuthorizedException("No session initialized");
		
		var session = RedisCache.getInstance().getSession( cookie.getValue());
		if( session == null )
			throw new NotAuthorizedException("No valid session initialized");
			
		if (session.user() == null || session.user().length() == 0) 
			throw new NotAuthorizedException("No valid session initialized");
		
		return session;
	}
	// static public Session validateSession(String userId) throws NotAuthorizedException {
	// 	var cookies = RequestCookies.get();
	// 	return validateSession( cookies.get(COOKIE_KEY ), userId );
	// }
	
	static public Session validateSession(Cookie cookie, String userId) throws NotAuthorizedException {

		if (cookie == null )
			throw new NotAuthorizedException("No session initialized");
		
		var session = RedisCache.getInstance().getSession( cookie.getValue());
		if( session == null )
			throw new NotAuthorizedException("No valid session initialized");
			
		if (session.user() == null || session.user().length() == 0) 
			throw new NotAuthorizedException("No valid session initialized");
		
		if (!session.user().equals(userId))
			throw new NotAuthorizedException("User is not Admin : " + session.user());
		
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
