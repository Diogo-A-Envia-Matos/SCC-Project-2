package blob_storage.impl.rest.storage;

import java.util.function.Consumer;

import blob_storage.api.Result;

public interface BlobStorage {
		
	public Result<Void> write(String path, byte[] bytes );
		
	public Result<Void> delete(String path);
	
	public Result<byte[]> read(String path);

}
