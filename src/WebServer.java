import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class WebServer
{
    public static final int PORT = 8080;
    private static final Executor exec = Executors.newCachedThreadPool();
	

	public static void main(String[] args) throws IOException, ConfigurationException {
		// create a server socket listening to port 8080
        ServerSocket sock = null;
		System.out.println("Web server is listening on port " + PORT + "...");
		String location = args[0];
		//Configuration configurator = new Configuration(location);
        // String location = args[0]; // location of the XML configuration file

		try {
			// establish the socket
			sock = new ServerSocket(PORT);
			Configuration configurator = new Configuration(location);
			while (true) {
				/**
				 * now listen for connections
				 * and service the connection in a separate thread.
				 */
				Runnable task = new Connection(sock.accept(), configurator);
				exec.execute(task);
			}
		}
		catch (IOException ioe) { }
		catch (ConfigurationException ce) { }
		finally {
			if (sock != null)
				sock.close();
		}
	}
}
