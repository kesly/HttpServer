package http.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class WebPing {
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

            String request = "GET test.jpg";

//            String request = "DELETE teste.html";

//            String request = "HEAD index.html";

//            String request = "POST index.php HTTP/1.0 \n From: frog@jmarshall.com\n" +
//                    "User-Agent: HTTPTool/1.0\n" +
//                    "Content-Type: application/x-www-form-urlencoded\n" +
//                    "Content-Length: 32\n" +
//                    "\n" +
//                    "home=Cosby&favorite+flavor=flies";

            // send request
            socOut.println(request);
            //sock.close();
            BufferedReader socIn = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));

            while (true) {
                try {
                    System.out.println(socIn.readLine());
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