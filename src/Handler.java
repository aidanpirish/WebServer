/**
 * Handler class containing the logic for sending 
 * results back to the client.
 *
 * @author Greg Gagne 
 */

import java.io.*;
import java.net.*;

public class Handler 
{
	public static final int BUFFER_SIZE = 256;

    /**
     * Finds the first occurrence of the character c 
     * beginning at the specified position in String s.
     * Returns the index of the character c.
     */
    public int findChar(char c, int pos, String s) {
        int index = pos;

        while (s.charAt(pos) != c && index < s.length())
            pos++;

        System.out.println("request = " + s + " pos = " + pos);
        return pos;
    }
	
	/**
	 * this method is invoked by a separate thread
	 */
	public void process(Socket client) throws java.io.IOException {

        BufferedReader in = null;		

		try {
            // read what the client sent
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // we just want the first line
            String requestLine = in.readLine();

            /* If we don't read a GET, just ignore it and close the socket */
            if ( requestLine == null || !requestLine.substring(0,3).equals("GET") ) {
                client.close();

                return;
            }

            /**
             * now parse the host
             */

            System.out.println(requestLine);

            int firstBlank = findChar(' ', 0, requestLine);
            int secondBlank = findChar(' ', firstBlank + 1, requestLine);

            int firstSlash = findChar('/', 0, requestLine);
            int secondSlash = findChar('/', firstSlash + 1, requestLine);

            System.out.println(firstBlank);
            System.out.println(secondBlank);
            System.out.println(firstSlash);
            System.out.println(secondSlash);

            String originHost;
            String resource;

            /* If it is a default query */
            if (secondBlank < secondSlash) {
                // default query
                originHost = requestLine.substring(firstSlash + 1, secondBlank);
                resource = "/";
            }
            else {
                originHost = requestLine.substring(firstSlash + 1, secondSlash);
                resource = requestLine.substring(secondSlash, secondBlank);
            }

            System.out.println(">>"+originHost+"<<");

            System.out.println(">>"+resource+"<<");

            // now open a socket to the origin server
            Socket originServer = new Socket(originHost,80);

            DataOutputStream toOrigin = new DataOutputStream(originServer.getOutputStream());
            BufferedInputStream fromOrigin = new BufferedInputStream(originServer.getInputStream());

            /**
             * Write the HTTP:
             * 1. GET
             * 2. Host
             * 3. Connection: close
             */
            toOrigin.writeBytes("GET " + resource + " HTTP/1.1\r\n");
            toOrigin.writeBytes("Host: " + originHost + "\r\n");
            toOrigin.writeBytes("Connection: close\r\n\r\n"); 

            /**
             * Now read back from the server
             */

             OutputStream toClient = new BufferedOutputStream(client.getOutputStream());
             int numBytes;
             byte[] buffer = new byte[BUFFER_SIZE];

             /** continually loop until the client closes the socket */
             while ( (numBytes = fromOrigin.read(buffer)) != -1) {
                 toClient.write(buffer,0,numBytes);
                 toClient.flush();
             }

   		}
		catch (IOException ioe) {
			System.err.println(ioe);
		}
		finally {
			// close streams and socket
            System.out.println("closing socket");
            in.close();
		}
	}
}
