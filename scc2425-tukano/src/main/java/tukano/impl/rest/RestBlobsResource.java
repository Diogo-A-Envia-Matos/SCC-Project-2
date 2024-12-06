package tukano.impl.rest;

import static java.lang.String.format;
import static tukano.impl.rest.RestResource.statusCodeFrom;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.hsqldb.error.ErrorCode;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.Blobs;
import tukano.api.Result;
import tukano.api.rest.RestBlobs;
import tukano.clients.rest.RestClient;
import utils.Authentication;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

	private static final String ADMIN = "admin";

	// static final Blobs impl = JavaFileBlobs.getInstance();

	// static final String blobStorage_BASE_URL = "http://blob-service:8080/rest";

	@Override
	public void upload(String blobId, byte[] bytes, String token, Cookie cookie) {
		Authentication.validateSession(cookie);
		// super.resultOrThrow( impl.upload(blobId, bytes, token));
		
		//TODO: Redirecionar para o RestBlobsResource do blob-service
		HttpURLConnection con = null;
		try {
			URL url = new URL(format("%s/%s?token=%s", TukanoRestServer.blobStorage_BASE_URL, blobId, token));
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/octet-stream");
			con.connect();
			con.getOutputStream().write(bytes);
			successCodeOrThrow(con.getResponseCode());
		} catch (WebApplicationException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
		finally {
			if (con != null)
				con.disconnect();
		}

		// super.resultOrThrow( impl.upload(blobId, bytes, token));

	}

	@Override
	public byte[] download(String blobId, String token, Cookie cookie) {
		Authentication.validateSession(cookie);

		//TODO: Redirecionar para o RestBlobsResource do blob-service
		HttpURLConnection con = null;
		try {
			URL url = new URL(format("%s/%s?token=%s", TukanoRestServer.blobStorage_BASE_URL, blobId, token));
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.connect();
			int responseCode = con.getResponseCode();
			successCodeOrThrow(responseCode);
			byte[] bytes = con.getInputStream().readAllBytes();
			return bytes;
		} catch (WebApplicationException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
		finally {
			if (con != null)
				con.disconnect();
		}
	}

	@Override
	public void delete(String blobId, String token, Cookie cookie) {
		Authentication.validateSession(cookie, ADMIN);
		
		//TODO: Redirecionar para o RestBlobsResource do blob-service
		HttpURLConnection con = null;
		try {
			URL url = new URL(format("%s/%s?token=%s", TukanoRestServer.blobStorage_BASE_URL, blobId, token));
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("DELETE");
			con.connect();
			int responseCode = con.getResponseCode();
			successCodeOrThrow(responseCode);
		} catch (WebApplicationException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
		finally {
			if (con != null)
				con.disconnect();
		}
	}
	
	@Override
	public void deleteAllBlobs(String userId, String token, Cookie cookie) {
		Authentication.validateSession(cookie, ADMIN);

		//TODO: Redirecionar para o RestBlobsResource do blob-service
		HttpURLConnection con = null;
		try {
			URL url = new URL(format("%s/%s/blobs?token=%s", TukanoRestServer.blobStorage_BASE_URL, userId, token));
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("DELETE");
			con.connect();
			int responseCode = con.getResponseCode();
			successCodeOrThrow(responseCode);
		} catch (WebApplicationException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
		finally {
			if (con != null)
				con.disconnect();
		}
	}

	private int successCodeOrThrow(int responseCode) {
		if (responseCode >= 200 && responseCode < 300)
			return responseCode;
		else
			throw new WebApplicationException(statusCodeFromInt(responseCode));
	}

	
	/**
	 * Translates a Result<T> to a HTTP Status code
	 */
	private static Status statusCodeFromInt(int responseCode) {
		return switch (responseCode) {
			case 409 -> Status.CONFLICT;
			case 404 -> Status.NOT_FOUND;
			case 403 -> Status.FORBIDDEN;
			case 400 -> Status.BAD_REQUEST;
			case 500 -> Status.INTERNAL_SERVER_ERROR;
			case 501 -> Status.NOT_IMPLEMENTED;
			case 200 -> Status.OK;
			case 204 -> Status.NO_CONTENT;
			default -> Status.INTERNAL_SERVER_ERROR;
		};
	}
}
