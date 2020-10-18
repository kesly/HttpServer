package http.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


/**
 * WebPing class
 * This class provides a simulation of client to make HTTP request
 */
public class WebPing {

    /**
     * @param args Command line parameters (0 = server host, 1 = server port)
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage java WebPing <server host name> <server port number>");
            return;
        }

        String httpServerHost = args[0];
        int httpServerPort = Integer.parseInt(args[1]);
        httpServerHost = args[0];
        httpServerPort = Integer.parseInt(args[1]);

        try {
            InetAddress addr;
            Socket sock = new Socket(httpServerHost, httpServerPort);
            addr = sock.getInetAddress();
            System.out.println("Connected to " + addr);
            PrintStream socOut = new PrintStream(sock.getOutputStream());

            String request = "GET test.html";

//            String request = "DELETE teste.html";

//            String request = "HEAD index.html";

//            String request = "BAD request";

//            String request = "POST index.php HTTP/1.0 \n From: frog@jmarshall.com\n" +
//                    "User-Agent: HTTPTool/1.0\n" +
//                    "Content-Type: application/x-www-form-urlencoded\n" +
//                    "Content-Length: 32\n" +
//                    "\n" +
//                    "home=Cosby&favorite+flavor=flies";

//            String request = "PUT nouvelleRessource.php HTTP/1.0 \n From: frog@jmarshall.com\n" +
//                    "User-Agent: HTTPTool/1.0\n" +
//                    "Content-Type: application/x-www-form-urlencoded\n" +
//                    "Content-Length: 46\n" +
//                    "\n" +
//                    "home=Cosby&favorite_flavor=flies&Matler=Kepler";

            // send request
            socOut.println(request);
//            sock.close();
            BufferedReader socIn = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));

            String receive = null;
            while (true) {
                try {
                    receive = socIn.readLine();
                    if (receive != null) {
                        System.out.println(receive);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (java.io.IOException e) {
            System.out.println("Can't connect to " + httpServerHost + ":" + httpServerPort);
            System.out.println(e);
        }
    }

}