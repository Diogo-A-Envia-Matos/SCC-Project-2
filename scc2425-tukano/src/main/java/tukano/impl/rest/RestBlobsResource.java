package tukano.impl.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Cookie;
import tukano.api.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.impl.JavaAzureBlobs;
import tukano.impl.JavaFileBlobs;
import utils.Authentication;
import utils.Props;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

	//TODO: Usar Autenthication

	private static final String ADMIN = "admin";

	static final Blobs impl = Boolean.parseBoolean(Props.get("USE_AZURE_BLOB_STORAGE", "true")) ?
			JavaAzureBlobs.getInstance() : JavaFileBlobs.getInstance();

	@Override
	public void upload(String blobId, byte[] bytes, String token, Cookie cookie) {
		//var session = Authentication.validateSession(qualquer utilizador);
		var session = Authentication.validateSession(cookie);
		super.resultOrThrow( impl.upload(blobId, bytes, token));
	}

	@Override
	public byte[] download(String blobId, String token, Cookie cookie) {
		//var session = Authentication.validateSession(qualquer utilizador);
		var session = Authentication.validateSession(cookie);
		return super.resultOrThrow( impl.download( blobId, token ));
	}

	@Override
	public void delete(String blobId, String token, Cookie cookie) {
		//var session = Authentication.validateSession(ADMIN);
		var session = Authentication.validateSession(cookie, ADMIN);
		super.resultOrThrow( impl.delete( blobId, token ));
	}
	
	@Override
	public void deleteAllBlobs(String userId, String password, Cookie cookie) {
		//var session = Authentication.validateSession(ADMIN);
		var session = Authentication.validateSession(cookie, ADMIN);
		super.resultOrThrow( impl.deleteAllBlobs( userId, password ));
	}
}
