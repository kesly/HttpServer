///A Simple Web Server (WebServer.java)

package http.server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Oumar Kesly
 * <p>
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 *
 * @author Kesly Gassant, Oumar DIAKHABY
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
            s = new ServerSocket(3008);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");
        for (; ; ) {
            try {
                while (true) {
                    // wait for a connection
                    Socket remote = s.accept();
                    // remote is now the connected socket
                    System.out.println("Connection, sending data.");

                    ClientThread clientThread = new ClientThread(remote);
                    clientThread.start();
                }

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
