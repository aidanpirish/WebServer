/**
 * An proxy server listening on port 8080. 
 *
 * This services each request in a separate thread.
 *
 * @author - Greg Gagne.
 */

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class  ProxyServer
{
	public static final int DEFAULT_PORT = 8080;

    // construct a thread pool for concurrency	
	private static final Executor exec = Executors.newCachedThreadPool();
	
	public static void main(String[] args) throws IOException {
		ServerSocket sock = null;

        System.out.println("Proudly serving at port 8080");
		
		try {
			// establish the socket
			sock = new ServerSocket(DEFAULT_PORT);
			
			while (true) {
				/**
				 * now listen for connections
				 * and service the connection in a separate thread.
				 */
				Runnable task = new Connection(sock.accept());
				exec.execute(task);
			}
		}
		catch (IOException ioe) { }
		finally {
			if (sock != null)
				sock.close();
		}
	}
}
