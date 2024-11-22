package tukano.impl.rest;

import jakarta.inject.Singleton;
import tukano.api.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.impl.JavaAzureBlobs;
import tukano.impl.JavaFileBlobs;
import utils.Props;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

	//TODO: Usar

	private static final String ADMIN = "admin";

	static final Blobs impl = Boolean.parseBoolean(Props.get("USE_AZURE_BLOB_STORAGE", "true")) ?
			JavaAzureBlobs.getInstance() : JavaFileBlobs.getInstance();

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
