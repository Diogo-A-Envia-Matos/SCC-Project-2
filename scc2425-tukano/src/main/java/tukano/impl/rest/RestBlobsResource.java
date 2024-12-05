package tukano.impl.rest;

import static java.lang.String.format;
import static tukano.impl.rest.RestResource.statusCodeFrom;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

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

	static final String BLOB_STORAGE_BASE_URL = "http://blob-service:8080/rest";

	@Override
	public void upload(String blobId, byte[] bytes, String token, Cookie cookie) {
		Authentication.validateSession(cookie);
		// super.resultOrThrow( impl.upload(blobId, bytes, token));
		
		//TODO: Redirecionar para o RestBlobsResource do blob-service
		HttpURLConnection con = null;
		try {
			URL url = new URL(format("%s/%s?token=%s", BLOB_STORAGE_BASE_URL, blobId, token));
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/octet-stream");
			con.getOutputStream().write(bytes);
			successCodeOrThrow(con.getResponseCode());
		} catch (Exception e) {
			e.printStackTrace();
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
			URL url = new URL(format("%s/%s?token=%s", BLOB_STORAGE_BASE_URL, blobId, token));
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (con != null)
				con.disconnect();
		}

		// return super.resultOrThrow( impl.download( blobId, token ));
	}

	@Override
	public void delete(String blobId, String token, Cookie cookie) {
		Authentication.validateSession(cookie, ADMIN);
		super.resultOrThrow( impl.delete( blobId, token ));
	}
	
	@Override
	public void deleteAllBlobs(String userId, String password, Cookie cookie) {
		Authentication.validateSession(cookie, ADMIN);
		super.resultOrThrow( impl.deleteAllBlobs( userId, password ));
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
