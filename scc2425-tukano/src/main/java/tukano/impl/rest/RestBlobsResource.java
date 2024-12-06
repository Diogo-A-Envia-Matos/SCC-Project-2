package tukano.impl.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Cookie;
import tukano.api.rest.RestBlobs;
import utils.BlobManager;
import utils.Authentication;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {
	static final String BLOB_STORAGE_BASE_URL = "http://blob-service:8080/blob-storage/rest/blobs";

	@Override
	public void upload(String blobId, byte[] bytes, String token, Cookie cookie) {
		Authentication.validateSession(cookie);

		BlobManager.upload(blobId, bytes, token);
	}

	@Override
	public byte[] download(String blobId, String token, Cookie cookie) {
		Authentication.validateSession(cookie);

		return BlobManager.download(blobId, token);
	}

	@Override
	public void delete(String blobId, String token, Cookie cookie) {
		Authentication.validateSession(cookie);
		
		BlobManager.delete(blobId, token);
	}

	@Override
	public void deleteAllBlobs(String userId, String token, Cookie cookie) {
		Authentication.validateSession(cookie);

		BlobManager.deleteAllBlobs(userId, token);
	}
}
