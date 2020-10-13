///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import http.server.service.ContentType;
import javafx.util.Pair;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 * <p>
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 *
 * @author Jeff Heaton
 * @version 1.0
 */
public class WebServer {

    private final String RESSOURCE_DIRECTORY = "src/http/server/web/";

    /**
     * WebServer constructor.
     */
    protected void start() {
        ServerSocket s;

        System.out.println("Webserver starting up on port 80");
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(3007);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");
        for (; ; ) {
            try {
                // wait for a connection
                Socket remote = s.accept();
                // remote is now the connected socket
                System.out.println("Connection, sending data.");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        remote.getInputStream()));
                PrintWriter out = new PrintWriter(remote.getOutputStream());

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


                // Send the response
                // Send the headers
//                out.println("HTTP/1.0 200 OK");
//                out.println("Content-Type: text/html");
//                out.println("Server: Bot");
//                // this blank line signals the end of the headers
//                out.println("");
//                // Send the HTML page
//                out.println("<H1>Welcome to the Ultra Mini-WebServer</H2>");
//                out.flush();
                //remote.close();
            } catch (Exception e) {
                System.out.println("Error: " + e);
                e.printStackTrace();
            }
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

    /**
     * Start the application.
     *
     * @param args Command line parameters are not used.
     */
    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start();
    }
}
