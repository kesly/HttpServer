package http.server.service;

import http.server.WebServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ToolBox {



    public static String getUri(String request) {
        return request.split(" ")[1];
    }

    public static String getUrl(String uri) {

        return uri.split("\\?")[0];
    }

    public static String getExtension(String url) {
        return url.split("\\.")[1];
    }

    public static byte[] readFileByte(String baseDirectory,String path) throws IOException {
        Path fileName = Path.of(baseDirectory, path);
        return Files.readAllBytes(fileName);
    }
}
