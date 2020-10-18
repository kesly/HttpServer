package http.server.service;

import http.server.WebServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ToolBox class
 * This service class provide all the methods currently used
 */
public class ToolBox {


    /**
     * get the value of uri from request by spliting it and take the second element
     * @Exemple request : GET /test.html
     *
     * @param request string
     * @return Uri from request
     */
    public static String getUri(String request) {
        return request.split(" ")[1];
    }

    /**
     * get the value of url from uri by spliting it and take the first element
     * @Exemple uri : www.test.com/test.html
     *
     * @param uri string
     * @return Url from uri
     */
    public static String getUrl(String uri) {

        return uri.split("\\?")[0];
    }

    /**
     * get the value of extension from url by spliting it and take the second element, this value allow to find file content-type
     * and return empty array if url has not extension
     * @Exemple url : test.html
     *
     * @param url string
     * @return extension from Url
     */
    public static String getExtension(String url) {
        String parts[] = url.split("\\.");
        return parts.length>2 ? parts[1] : "";
    }

    /**
     *
     * create path of file from base directory and resource name and return all content as binary array
     *
     * @param baseDirectory path of directory that contain  the file
     * @param name of file
     * @return file content as byte array
     * @throws IOException if pas th is not correct
     */
    public static byte[] readFileByte(String baseDirectory, String name) throws IOException {
        Path fileName = Path.of(baseDirectory, name);
        return Files.readAllBytes(fileName);
    }
}
