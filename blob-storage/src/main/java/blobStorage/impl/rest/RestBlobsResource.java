package main.java.blobStorage.impl.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Cookie;
import blobStorage.api.Blobs;
import blobStorage.api.rest.RestBlobs;
import blobStorage.impl.JavaFileBlobs;
import utils.Authentication;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

	private static final String ADMIN = "admin";

	static final Blobs impl = JavaFileBlobs.getInstance();

	@Override
	public void upload(String blobId, byte[] bytes, String token) {
		super.resultOrThrow( impl.upload(blobId, bytes, token));
	}

	@Override
	public byte[] download(String blobId, String token) {
		return super.resultOrThrow( impl.download( blobId, token ));
	}

	@Override
	public void delete(String blobId, String token) {
		super.resultOrThrow( impl.delete( blobId, token ));
	}
	
	@Override
	public void deleteAllBlobs(String userId, String token) {
		super.resultOrThrow( impl.deleteAllBlobs( userId, token ));
	}
}
