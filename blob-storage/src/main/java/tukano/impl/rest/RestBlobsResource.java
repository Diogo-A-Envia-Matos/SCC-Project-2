package tukano.impl.rest;

import jakarta.inject.Singleton;
import tukano.api.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.impl.JavaFileBlobs;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

	static final Blobs impl = JavaFileBlobs.getInstance();

	@Override
	public void upload(String blobId, byte[] bytes, String token) {
		//var session = Authentication.validateSession(qualquer utilizador);
		super.resultOrThrow( impl.upload(blobId, bytes, token));
	}

	@Override
	public byte[] download(String blobId, String token) {
		//var session = Authentication.validateSession(qualquer utilizador);
		return super.resultOrThrow( impl.download( blobId, token ));
	}

	@Override
	public void delete(String blobId, String token) {
		//var session = Authentication.validateSession(ADMIN);
		super.resultOrThrow( impl.delete( blobId, token ));
	}
	
	@Override
	public void deleteAllBlobs(String userId, String password) {
		//var session = Authentication.validateSession(ADMIN);
		super.resultOrThrow( impl.deleteAllBlobs( userId, password ));
	}
}
