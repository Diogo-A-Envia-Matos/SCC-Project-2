package blobStorage.impl.rest;

import blobStorage.api.rest.PodHealth;
import blobStorage.impl.Token;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import utils.IP;

public class BlobStorageRestServer extends Application {

	final private static Logger Log = Logger.getLogger(BlobStorageRestServer.class.getName());

	private static final String TOKEN_SECRET = "Token_secret";

	static String SERVER_BASE_URI = "http://%s:%s/blob-storage/rest";
	static String HOST_NAME = IP.hostAddress();
	public static final int PORT = 8080;

	public static String serverURI;

	private Set<Object> singletons = new HashSet<>();
	private Set<Class<?>> resources = new HashSet<>();

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	public BlobStorageRestServer() {
		Token.setSecret(TOKEN_SECRET);

		// Props.load("azurekeys-region.props"); //place the props file in resources folder under java/main

		serverURI = String.format(SERVER_BASE_URI, HOST_NAME, PORT);
		System.out.println("----------------------------------serverURI: " + serverURI);

		resources.add(PodHealth.class);

		resources.add(RestBlobsResource.class);
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
