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
 * @author Kesly and Oumar
 * Client thread
 * <p>
 * This class is a thread that allows you to manage client http requests independently
 * This class provides all the methods (GET, POST, HEAD, ...) to the client so that it can make requests
 * <p>
 */
public class ClientThread extends Thread {

    /**
     * Chemin relatif des ressources du serveur
     */
    private final String RESSOURCE_DIRECTORY = "src/http/server/ressources/";

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
    private BufferedReader in;

    /**
     * ClientThread Constructor
     *
     * @param clientSocket
     */
    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Default launch method after a client connection
     */
    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            this.out = clientSocket.getOutputStream();


            String str = "";
            while (!(str != null && !str.equals(""))) {
                str = in.readLine();
            }

            handleRequest(str);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }
    }

    /**
     * get HTTP request method from client request and call the right method for treating client request
     *
     * @param request, request of client
     * @throws IOException
     */
    public void handleRequest(String request) throws IOException {

        // vich method

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
                doPost();
                break;
            case "HEAD":
                doHead();
                break;
            case "PUT":
                doPut();
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
     * @param request request of client
     * @return HTTP method
     */
    public String getMethod(String request) {
        return request.split(" ")[0];
    }

    /**
     * Send response of client request on body with http header response
     *
     * @throws IOException if resource requested by client is not found
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

    /**
     * Send the http response header
     * Work like get method without body
     * @throws IOException if resource requested by client is not found
     */
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
     * Delete the resource on the request and load page of deleted to confirm that
     *
     * @throws IOException if resource is not found
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
     * get request and his body and send the body to the client specified
     *
     * @throws IOException
     */
    public void doPost() throws IOException {

        String otherContent = ".";

        int contentLength = 0;

        while (!otherContent.equals("")) {
            otherContent = in.readLine();
            if (otherContent.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(otherContent.split(" ")[1]);
            }
        }

        char[] buffer = new char[contentLength];
        this.in.read(buffer, 0, contentLength); // read parameter

        String parameters = decodeValue(new String(buffer));

        // traiter le buffer

        String[] fileType = this.url.split(".");


//        if (fileType[1].equals("php")) {

            String path = RESSOURCE_DIRECTORY + this.url;
            String output = this.execPHP(path, parameters);
            this.statusCode = OK;
            sendHeader(statusCode, "text/html", output.length());
            sendBodyByte(output.getBytes());
//        } else {
//
//            byte[] contentByte = null;
//
//            try {
//                contentByte = ToolBox.readFileByte(RESSOURCE_DIRECTORY, this.url);
//                this.statusCode = OK;
//                ContentType contentType = new ContentType(this.extension);
//                sendHeader(statusCode, contentType.getContentType(), contentByte.length);
//            } catch (IOException e) {
//                System.out.println("Ressource non trouvé");
//                statusCode = NOT_FOUND;
//                contentByte = ToolBox.readFileByte(RESPONSE_PAGE_DIRECTORY, "pageNotFound.html");
//                sendHeader(statusCode, "text/html", contentByte.length);
//            }
//            sendBodyByte(contentByte);
//        }
    }

    /**
     * get request and his body and replace content of resource of request by body of request,
     * if resource not existe, create a new one and put the body of request on it, if it cannot create resource
     * send header with 403 code (forbidden) to saying it haven't permission
     *
     * @throws IOException if cannot read line
     */
    public void doPut() throws IOException {

        String otherContent = ".";

        int contentLength = 0;

        while (!otherContent.equals("")) {
            otherContent = in.readLine();
            if (otherContent.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(otherContent.split(" ")[1]);
            }
        }

        char[] buffer = new char[contentLength];
        this.in.read(buffer, 0, contentLength); // read parameter

        String parameters = decodeValue(new String(buffer));
        System.out.println("Param " + parameters);
        byte[] contentByte = parameters.getBytes();
        // traiter le buffer

        String path = RESSOURCE_DIRECTORY + this.url;

        File file = new File(path);

        if (file.exists() && !file.isDirectory()) {
            Files.write(Paths.get(path), contentByte);
            statusCode = NO_CONTENT;
        } else {
            if (file.createNewFile()) {
                Files.write(Paths.get(path), contentByte);
                statusCode = CREATED;
            } else {
                statusCode = FORBIDEN;
            }

        }

        //Response to client
        sendHeader(statusCode, "text/html", contentByte.length);

    }

    /**
     * Send the http header to the client
     *
     * @param statusCode http status code, Pair of code and message
     * @param contentType content type of body response (Eg. text\html for response as html format)
     * @param contentLength the length of response
     * @throws IOException
     */
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

    /**
     * Send the body of request to the client as a byte
     *
     * @param content content of body response as a binary
     *
     * @throws IOException if cannot write
     */
    public void sendBodyByte(byte[] content) throws IOException {
        this.out.write(content);
    }

    /**
     * execute php code with param from the body of request (POST)
     * manage dynamic resource
     *
     * @param scriptName path of the php specified in post request file
     * @param param of php request commande (body of POST response)
     * @return output Stringbuilder
     */
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

    /**
     * Convert String on UTF-8 format and return it
     *
     * @param value string
     * @return same string on UTF-8 format
     */
    public static String decodeValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }


}
