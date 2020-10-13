package http.server;

import http.server.service.ContentType;
import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class ClientThread extends Thread {

    private final String RESSOURCE_DIRECTORY = "src/http/server/web/";

    private Socket clientSocket;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

            // read the data sent. We basically ignore it,
            // stop reading once a blank line is hit. This
            // blank line signals the end of the client HTTP
            // headers.
            String str = "";
            int i = 0;
            while (!(str != null && !str.equals(""))) {
                str = in.readLine();
            }

            handleRequest(str, out, in);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }
    }


    public void handleRequest(String str, PrintWriter out, BufferedReader in) throws IOException {

        // wich method

        String method = getMethod(str);

        switch (method) {
            case "GET":
                doGet(str, out);
                break;
            case "POST":
                doPost(str, out, in);
                break;
            case "HEAD":
                doHead(str, out);
                break;
            case "PUT":
                doPut(str, out, in);
                break;
            case "DELETE":
                doDelete(str, out);
                break;
            default:
                break;

        }
    }


    public String getMethod(String str) {
        return str.split(" ")[0];
    }


    public void doGet(String request, PrintWriter out) {

        String uri = request.split(" ")[1];
        String url = uri.split("\\?")[0];
        System.out.print("URL " + url);
        String extension = url.split("\\.")[1];

        Pair<Integer, String> statusCode;

        // search ressource
        String content = "";
        try {
            content = this.readFile(url);
            statusCode = new Pair<>(200, "OK");
        } catch (IOException e) {
            System.out.println("Ressource non trouvé");
            statusCode = new Pair<>(400, "Bad Request");
        }

        // determine the status code

        //Response to client
        ContentType contentType = new ContentType(extension);

        sendHeader(out, statusCode, contentType.getContentType(), content.length());
        if (!content.equals("")) {
            this.sendBody(out, content);
        } else {
            this.sendBody(out, "404");
        }
        out.flush();
    }

    public void doHead(String request, PrintWriter out) {

        String uri = request.split(" ")[1];
        String url = uri.split("\\?")[0];

        Pair<Integer, String> statusCode;

        // search ressource
        String content = "";
        try {
            content = this.readFile(url);
            statusCode = new Pair<>(200, "OK");
        } catch (IOException e) {
            System.out.println("Ressource non trouvé");
            statusCode = new Pair<>(400, "Bad Request");
        }

        // determine the status code

        //Response to client
        sendHeader(out, statusCode, "text/html", content.length());

        out.flush();
    }

    public void doDelete(String request, PrintWriter out) {

        String uri = request.split(" ")[1];
        String url = uri.split("\\?")[0];

        Pair<Integer, String> statusCode;

        // search ressource
        File file = new File(RESSOURCE_DIRECTORY + url);
        if (file.delete()) {
            System.out.println("Le fichier " + file.getName() + " a été supprimé");
            statusCode = new Pair<>(200, "OK");
        } else {
            statusCode = new Pair<>(404, "Resource not found");
        }

        //Response to client
        sendHeader(out, statusCode, "text/html", null);

        out.flush();
    }

    public void doPost(String request, PrintWriter out, BufferedReader in) throws IOException {

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

        String uri = request.split(" ")[1];
        String url = uri.split("\\?")[0];

        String[] fileType = url.split(".");

        Pair<Integer, String> statusCode = new Pair<>(302, "Found");

        String path = RESSOURCE_DIRECTORY + url;

        String output = this.execPHP(path, parameters);

        System.out.println("sorti : " + output);


//         determine the status code

        //Response to client
        sendHeader(out, statusCode, "text/html", output.length());
        if (!output.equals("")) {
            this.sendBody(out, output);
        } else {
            this.sendBody(out, "404");
        }
        out.flush();
    }

    public void doPut(String request, PrintWriter out, BufferedReader in) throws IOException {

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

        String uri = request.split(" ")[1];
        String url = uri.split("\\?")[0];

        String[] fileType = url.split(".");

        Pair<Integer, String> statusCode = new Pair<>(302, "Found");

        String path = RESSOURCE_DIRECTORY + url;

        String output = this.execPHP(path, parameters);

        System.out.println("sorti : " + output);


//         determine the status code

        //Response to client
        sendHeader(out, statusCode, "text/html", output.length());
        if (!output.equals("")) {
            this.sendBody(out, output);
        } else {
            this.sendBody(out, "404");
        }
        out.flush();
    }

    public void sendHeader(PrintWriter out, Pair<Integer, String> statusCode, String contentType, Integer contentLength) {

        out.println(" HTTP/1.1 " + statusCode.getKey() + " " + statusCode.getValue());
        out.println("Date: " + new Date().toString());
        if (contentLength != null) {
            out.println("Content-Type: " + contentType);
            out.println("Content-Encoding: UTF-8");
            out.println("Content-Length: " + contentLength);
        }
        out.println("");
    }

    public String readFile(String path) throws IOException {
        Path fileName = Path.of(RESSOURCE_DIRECTORY, path);
        return Files.readString(fileName);
    }

    public void sendBody(PrintWriter out, String content) {
        out.println(content);
//        for (int i = 0; i < content.length(); i++) {
//
//        }
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
