package utils;

import static java.lang.String.*;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BlobManager {
    static final String BLOB_STORAGE_BASE_URL = "http://blob-service:8080/blob-storage/rest/blobs";

    public static void upload(String blobId, byte[] bytes, String token) {
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
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);

        } finally {

            if (con != null)
                con.disconnect();

        }
    }

    private static void writeBytes(OutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
    }

    public static byte[] download(String blobId, String token) {
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
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);

        }
        finally {

            if (con != null)
                con.disconnect();

        }
    }

    public static void delete(String blobId, String token) {
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
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);

        }
        finally {

            if (con != null)
                con.disconnect();

        }
    }

    public static void deleteAllBlobs(String userId, String token) {
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
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);

        }
        finally {

            if (con != null)
                con.disconnect();

        }
    }



    private static int successCodeOrThrow(int responseCode) {
        if (responseCode >= 200 && responseCode < 300)
            return responseCode;
        else
            throw new WebApplicationException(statusCodeFromInt(responseCode));
    }


    /**
     * Translates a Result<T> to a HTTP Status code
     */
    private static Response.Status statusCodeFromInt(int responseCode) {
        return switch (responseCode) {
            case 409 -> Response.Status.CONFLICT;
            case 404 -> Response.Status.NOT_FOUND;
            case 403 -> Response.Status.FORBIDDEN;
            case 400 -> Response.Status.BAD_REQUEST;
            case 500 -> Response.Status.INTERNAL_SERVER_ERROR;
            case 501 -> Response.Status.NOT_IMPLEMENTED;
            case 200 -> Response.Status.OK;
            case 204 -> Response.Status.NO_CONTENT;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };
    }
}
