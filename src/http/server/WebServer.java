///A Simple Web Server (WebServer.java)

package http.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

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
                System.out.println("str1: " + str);

                int i = 0;
                while (!(str != null && !str.equals(""))) {
                    str = in.readLine();

                }
                handleRequest(str, out);


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

    public void handleRequest(String str, PrintWriter out) {

        // wich method

        String method = getMethod(str);

        switch (method) {
            case "GET":
                doGet(str, out);
                break;
        }
    }


    public String getMethod(String str) {
        return str.split(" ")[0];
    }

    public void doGet(String request, PrintWriter out) {

        String url = request.split(" ")[1];

        // search ressource

        // determine the status code

        //Response to client

        sendHeader(out, new Pair<>(200, "OK"), "text/html", 160);
        sendBody(out);

        out.flush();
    }

    public void sendHeader(PrintWriter out, Pair<Integer, String> statusCode, String contentType, int contentLength) {

        out.println(" HTTP/1.1 " + statusCode.getKey() + " " + statusCode.getValue());
        out.println("Date: " + new Date().toString());
        out.println("Content-Type: " + contentType);
        out.println("Content-Encoding: UTF-8");
        out.println("Content-Length: " + contentLength);
        out.println("");
    }

    public void sendBody(PrintWriter out) {

        out.println("<!DOCTYPE html>\n" +
                "<html lang=\"en\" dir=\"ltr\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title></title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1> hello world </h1>\n" +
                "  </body>\n" +
                "</html>");
    }

    public int getSuccessCode() {
        return 200;
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