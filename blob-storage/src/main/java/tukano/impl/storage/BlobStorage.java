package tukano.impl.storage;

import tukano.api.Result;

public interface BlobStorage {
		
	Result<Void> write(String path, byte[] bytes );
		
	Result<Void> delete(String path);
	
	Result<byte[]> read(String path);

}
