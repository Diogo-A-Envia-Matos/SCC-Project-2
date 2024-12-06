package tukano.impl.rest;

import static java.lang.String.*;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import tukano.api.rest.RestBlobs;
import utils.Authentication;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {
	static final String BLOB_STORAGE_BASE_URL = "http://blob-service:8080/blob-storage/rest/blobs";

	@Override
	public void upload(String blobId, byte[] bytes, String token, Cookie cookie) {
		Authentication.validateSession(cookie);

		HttpURLConnection con = null;
		try {

			URL url = new URL(format("%s/%s?token=%s", BLOB_STORAGE_BASE_URL, blobId, token));
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/octet-stream");
			con.setDoOutput(true);
			con.connect();
			writeBytes(con.getOutputStream(), bytes);
			successCodeOrThrow(con.getResponseCode());

		} catch (WebApplicationException e) {

			e.printStackTrace();
			throw e;

		} catch (Exception e) {

			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);

		} finally {

			if (con != null)
				con.disconnect();

		}
	}

	private void writeBytes(OutputStream outputStream, byte[] bytes) throws IOException {
		outputStream.write(bytes);
		outputStream.flush();
	}

	@Override
	public byte[] download(String blobId, String token, Cookie cookie) {
		Authentication.validateSession(cookie);

		HttpURLConnection con = null;
		try {

			URL url = new URL(format("%s/%s?token=%s", BLOB_STORAGE_BASE_URL, blobId, token));
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setDoInput(true);
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
		Authentication.validateSession(cookie);
		
		HttpURLConnection con = null;
		try {

			URL url = new URL(format("%s/%s?token=%s", BLOB_STORAGE_BASE_URL, blobId, token));
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
		Authentication.validateSession(cookie);

		HttpURLConnection con = null;
		try {

			URL url = new URL(format("%s/%s/blobs?token=%s", BLOB_STORAGE_BASE_URL, userId, token));
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
