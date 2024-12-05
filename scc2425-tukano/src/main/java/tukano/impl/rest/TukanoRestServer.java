package tukano.impl.rest;

import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import tukano.api.rest.PodHealth;
import tukano.impl.Token;
import utils.Authentication;
import utils.IP;
import utils.Props;

public class TukanoRestServer extends Application {

	final private static Logger Log = Logger.getLogger(TukanoRestServer.class.getName());

	private static final String TOKEN_SECRET = "Token_secret";

	//TODO: Check if BLOB_URL path is "/rest/blobs", "/rest" or "/blobs"
	public static final String BLOB_STORAGE_BASE_URL = "http://blob-service:8080/rest/blobs";
	static String SERVER_BASE_URI = "http://%s:%s/tukano/rest";
	static String HOST_NAME = IP.hostAddress();
	public static final int PORT = 8080;

	public static String serverURI;

	private Set<Object> singletons = new HashSet<>();
	private Set<Class<?>> resources = new HashSet<>();

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	public TukanoRestServer() {
		Token.setSecret(TOKEN_SECRET);

		Props.load("azurekeys-region.props"); //place the props file in resources folder under java/main

		serverURI = String.format(SERVER_BASE_URI, HOST_NAME, PORT);
		System.out.println("----------------------------------serverURI: " + serverURI);

		resources.add(PodHealth.class);

		resources.add(RestBlobsResource.class);
		resources.add(RestUsersResource.class);
		resources.add(RestShortsResource.class);

        resources.add(Authentication.class);
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

	public static void main(String[] args) throws Exception {
		return;
	}
}
