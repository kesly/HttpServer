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
                while(true){
                    // wait for a connection
                    Socket remote = s.accept();
                    // remote is now the connected socket
                    System.out.println("Connection, sending data.");

                    ClientThread clientThread = new ClientThread(remote);
                    clientThread.start();
                }

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
