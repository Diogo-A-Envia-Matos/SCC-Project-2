package tukano.impl.rest;

import jakarta.inject.Singleton;
import java.util.Arrays;
import tukano.api.rest.RestBlobs;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {
	@Override
	public void upload(String blobId, byte[] bytes, String token) {
		System.out.println("____________ upload (POST) received:");
		System.out.println();
		System.out.println("____________ blobId (PathParam):");
		System.out.println("____________ " + blobId);
		System.out.println();
		System.out.println("____________ bytes (APPLICATION_OCTET_STREAM):");
		System.out.println("____________ " + Arrays.toString(bytes));
		System.out.println();
		System.out.println("____________ token (QueryParam(TOKEN)):");
		System.out.println("____________ " + token);
		System.out.println();
	}

	@Override
	public byte[] download(String blobId, String token) {
		System.out.println("____________ download (GET) received:");
		System.out.println();
		System.out.println("____________ blobId (PathParam):");
		System.out.println("____________ " + blobId);
		System.out.println();
		System.out.println("____________ token (QueryParam(TOKEN)):");
		System.out.println("____________ " + token);
		System.out.println();
		return new byte[0];
	}

	@Override
	public void delete(String blobId, String token) {
		System.out.println("____________ delete (DELETE) received:");
		System.out.println();
		System.out.println("____________ blobId (PathParam):");
		System.out.println("____________ " + blobId);
		System.out.println();
		System.out.println("____________ token (QueryParam(TOKEN)):");
		System.out.println("____________ " + token);
		System.out.println();
	}
	
	@Override
	public void deleteAllBlobs(String userId, String token) {
		System.out.println("____________ deleteAllBlobs (DELETE) received:");
		System.out.println();
		System.out.println("____________ userID (PathParam):");
		System.out.println("____________ " + userId);
		System.out.println();
		System.out.println("____________ token (QueryParam(TOKEN)):");
		System.out.println("____________ " + token);
		System.out.println();
	}
}
