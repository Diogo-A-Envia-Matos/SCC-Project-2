package blob_storage.impl;

import static java.lang.String.*;
import static blob_storage.api.Result.ErrorCode.*;
import static blob_storage.api.Result.*;

import java.util.logging.Logger;
import blob_storage.api.Blobs;
import blob_storage.api.Result;
import blob_storage.impl.rest.BlobStorageRestServer;
import blob_storage.impl.storage.BlobStorage;
import blob_storage.impl.storage.FileSystemStorage;
import utils.Hash;
import utils.Hex;

public class JavaFileBlobs implements Blobs {

	private static Blobs instance;
	private static Logger Log = Logger.getLogger(JavaFileBlobs.class.getName());

	public String baseURI;
	private BlobStorage storage;

	synchronized public static Blobs getInstance() {
		if( instance == null )
			instance = new JavaFileBlobs();
		return instance;
	}

	private JavaFileBlobs() {
		storage = new FileSystemStorage();
		baseURI = String.format("%s/%s/", BlobStorageRestServer.serverURI, Blobs.NAME);
	}
	
	@Override
	public Result<Void> upload(String blobId, byte[] bytes, String token) {
		Log.info(() -> format("upload : blobId = %s, sha256 = %s, token = %s\n", blobId, Hex.of(Hash.sha256(bytes)), token));

		if (!isValidBlobId(blobId, token))
			return error(FORBIDDEN);

		return storage.write( toPath( blobId ), bytes);
	}

	@Override
	public Result<byte[]> download(String blobId, String token) {
		Log.info(() -> format("download : blobId = %s, token=%s\n", blobId, token));

		if( ! isValidBlobId( blobId, token ) )
			return error(FORBIDDEN);

		return storage.read( toPath( blobId ) );
	}

	@Override
	public Result<Void> delete(String blobId, String token) {
		Log.info(() -> format("delete : blobId = %s, token=%s\n", blobId, token));
	
		if( ! isValidBlobId( blobId, token ) )
			return error(FORBIDDEN);

		return storage.delete( toPath(blobId));
	}
	
	@Override
	public Result<Void> deleteAllBlobs(String userId, String token) {
		Log.info(() -> format("deleteAllBlobs : userId = %s, token=%s\n", userId, token));

		if( ! Token.isValid( token, userId ) )
			return error(FORBIDDEN);
		
		return storage.delete( toPath(userId));
	}
	
	private boolean isValidBlobId(String blobId, String token) {
		System.out.println( "validate blob:" + toURL(blobId));
		return Token.isValid(token, toURL(blobId));
	}

	private String toPath(String blobId) {
		return blobId.replace("+", "/");
	}
	
	private String toURL( String blobId ) {
		return baseURI + blobId ;
	}
}