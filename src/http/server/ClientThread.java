package http.server;

import http.server.service.ContentType;
import http.server.service.ToolBox;
import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static http.server.service.HttpCode.*;

/**
 * Client thread
 * <p>
 *     Chaque client est tourne communique avec le serveur dans un thread indépendamment des autres.
 *     Cette classe fourni toutes les méthodes (GET, POST, HEAD,...) au client pour qu'il puisse faire des requêtes
 */
public class ClientThread extends Thread {

    /**
     * Path of resources directory
     */
    private final String RESSOURCE_DIRECTORY = "src/http/server/forbiden/";

    /**
     * Path of reponse pages directory
     */
    public final String RESPONSE_PAGE_DIRECTORY = "src/http/server/responsePages/";

    private final String CRLF = "\r\n";

    private Socket clientSocket;

    private String uri;
    private String url;
    private String extension;

    private Pair<Integer, String> statusCode;

    private OutputStream out;

    /**
     * ClientThread Constructeur
     * @param clientSocket
     */
    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Methode s'exécutant tant que le thread est vivant
     */
    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream() ) );
            this.out = clientSocket.getOutputStream();

            // read the data sent. We basically ignore it,
            // stop reading once a blank line is hit. This
            // blank line signals the end of the client HTTP
            // headers.
            String str = "";
            while (!(str != null && !str.equals(""))) {
                str = in.readLine();
            }

            handleRequest(str, in);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }
    }

    /**
     * get HTTP request method from request and call the right method for treating client request
     *
     * @param request
     * @param in
     * @throws IOException
     */
    public void handleRequest(String request, BufferedReader in) throws IOException {

        // vich method

        System.out.println("str : " + request);
        String method = getMethod(request);

        this.uri = ToolBox.getUri(request);
        this.url = ToolBox.getUrl(uri);
        this.extension = ToolBox.getExtension(url);

        System.out.println("method :" + method);
        switch (method) {
            case "GET":
                doGet();
                break;
            case "POST":
                doPost(in);
                break;
            case "HEAD":
                doHead();
                break;
            case "PUT":
                doPut(in);
                break;
            case "DELETE":
                doDelete();
                break;
            default:
                statusCode = BAD_REQUEST;
                this.sendHeader(statusCode, "text/html", null);
                break;
        }
        this.out.flush();
    }

    /**
     * get HTTP method by spliting request and take first element
     *
     * @param request
     * @return HTTP method
     */
    public String getMethod(String request) {
        return request.split(" ")[0];
    }

    /**
     *
     * @throws IOException
     */
    public void doGet() throws IOException {

        // search ressource
        byte[] contentByte = null;
        try {
            contentByte = ToolBox.readFileByte(RESSOURCE_DIRECTORY, this.url);
            this.statusCode = OK;
            ContentType contentType = new ContentType(this.extension);
            sendHeader(statusCode, contentType.getContentType(), contentByte.length);
        } catch (IOException e) {
            System.out.println("Ressource non trouvé");
            statusCode = NOT_FOUND;
            contentByte = ToolBox.readFileByte(RESPONSE_PAGE_DIRECTORY, "pageNotFound.html");
            sendHeader(statusCode, "text/html", contentByte.length);
        }

        this.sendBodyByte(contentByte);
    }


    public void doHead() throws IOException {

        // search ressource

        byte[] contentByte = null;
        try {
            contentByte = ToolBox.readFileByte(RESSOURCE_DIRECTORY, this.url);
            this.statusCode = OK;
            ContentType contentType = new ContentType(this.extension);
            sendHeader(statusCode, contentType.getContentType(), contentByte.length);
        } catch (IOException e) {
            System.out.println("Ressource non trouvé");
            statusCode = NOT_FOUND;
            contentByte = ToolBox.readFileByte(RESPONSE_PAGE_DIRECTORY, "pageNotFound.html");
            sendHeader(statusCode, "text/html", contentByte.length);
        }

    }

    /**
     *
     * @throws IOException
     */
    public void doDelete() throws IOException {

        // search ressource
        File file = new File(RESSOURCE_DIRECTORY + this.url);
        byte[] contentByte = null;

        if (file.delete()) {
            System.out.println("Le fichier " + file.getName() + " a été supprimé");
            this.statusCode = OK;
            contentByte = ToolBox.readFileByte(RESPONSE_PAGE_DIRECTORY, "ressourceDeleted.html");
            sendHeader(statusCode, "text/html", contentByte.length);
        } else {
            statusCode = NOT_FOUND;
            contentByte = ToolBox.readFileByte(RESPONSE_PAGE_DIRECTORY, "pageNotFound.html");
            sendHeader(statusCode, "text/html", contentByte.length);
        }

        sendBodyByte(contentByte);
    }

    /**
     *
     * @param in
     * @throws IOException
     */
    public void doPost(BufferedReader in) throws IOException {

        String otherContent = ".";

        int contentLength = 0;

        while (!otherContent.equals("")) {
            otherContent = in.readLine();
            if (otherContent.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(otherContent.split(" ")[1]);
            }
        }

        char[] buffer = new char[contentLength];
        in.read(buffer, 0, contentLength); // read parameter

        String parameters = decodeValue(new String(buffer));

        // traiter le buffer

        String[] fileType = this.url.split(".");

        Pair<Integer, String> statusCode = new Pair<>(302, "Found");

        String path = RESSOURCE_DIRECTORY + this.url;

        String output = this.execPHP(path, parameters);

        System.out.println("sorti : " + output);


//         determine the status code

        //Response to client
        sendHeader(statusCode, "text/html", output.length());
        if (!output.equals("")) {
            this.sendBodyByte(output.getBytes());
        } else {
            this.sendBodyByte("404".getBytes());
        }
    }

    public void doPut(BufferedReader in) throws IOException {

        String otherContent = ".";

        int contentLength = 0;

        while (!otherContent.equals("")) {
            otherContent = in.readLine();
            if (otherContent.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(otherContent.split(" ")[1]);
            }
        }

        char[] buffer = new char[contentLength];
        in.read(buffer, 0, contentLength); // read parameter

        String parameters = decodeValue(new String(buffer));
        System.out.println("Param "+parameters);
        byte[] contentByte = parameters.getBytes();
        // traiter le buffer

        String path = RESSOURCE_DIRECTORY + this.url;

        File file = new File(path);

        if(file.exists() && !file.isDirectory()){
            Files.write(Paths.get(path), contentByte);
            statusCode = NO_CONTENT;
        } else {
            if(file.createNewFile()){
                Files.write(Paths.get(path), contentByte);
                statusCode = CREATED;
            } else {
                statusCode = FORBIDEN;
            }

        }

        //Response to client
        sendHeader(statusCode, "text/html", contentByte.length);

    }

    public void sendHeader(Pair<Integer, String> statusCode, String contentType, Integer contentLength) throws IOException {


        this.out.write(("HTTP/1.1 " + statusCode.getKey() + " " + statusCode.getValue() + CRLF).getBytes());
        this.out.write(("Date: " + new Date().toString() + CRLF).getBytes());
        if (contentLength != null) {
            this.out.write(("Content-Type: " + contentType + CRLF).getBytes());
            this.out.write(("Content-Encoding: UTF-8" + CRLF).getBytes());
            this.out.write(("Content-Length: " + contentLength + CRLF).getBytes());
        }
        this.out.write(("" + CRLF).getBytes());
    }

    public String readFile(String path) throws IOException {
        Path fileName = Path.of(RESSOURCE_DIRECTORY, path);
        return Files.readString(fileName);
    }

    public void sendBodyByte(byte[] content) throws IOException {
        this.out.write(content);
    }

    public String execPHP(String scriptName, String param) {

        StringBuilder output = new StringBuilder();

        try {
            String line;

            Process p = Runtime.getRuntime().exec("php " + scriptName + " " + param);
            BufferedReader input =
                    new BufferedReader
                            (new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                output.append(line);
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return output.toString();
    }

    public int getSuccessCode() {
        return 200;
    }

    public static String decodeValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }


}
