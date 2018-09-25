import java.net.*;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import com.sun.javafx.sg.prism.NGLightBase;

import java.io.*;

public class Connection implements Runnable
{
	private Socket client;
	BufferedReader in = null;
	Configuration config;	
	
	public Connection(Socket client, Configuration config) {
		this.client = client;
		this.config = config;
	}

	public void run() { 

		try {
			processRequest();
		}
		catch (Exception ioe) {
			System.err.println(ioe);
		}
	}

	private void processRequest() throws Exception, IOException {
		try {
			InputStream instream = client.getInputStream();
			DataOutputStream os = new DataOutputStream(client.getOutputStream());
				
			BufferedReader in = new BufferedReader(new InputStreamReader(instream));

			String requestLine = in.readLine();
			System.out.println(requestLine);

			StringTokenizer tokens = new StringTokenizer(requestLine);
			tokens.nextToken();
			String resource = tokens.nextToken();
			String fileName;

			if(resource.length() > 1 ) {
				fileName = config.getDocumentRoot() + resource;
			} else {
				fileName = config.getDefaultDocument();
			}

			//open the file
			FileInputStream fis = null;
			boolean fileExists = true;
			try {
				fis = new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				fileExists = false;
			}

			// construct response message
			String status = null;
			String date = null;
			String serverName = null;
			String contentType = null;
			String contentLength = null;
			String body = null;
			String close = null;
			

			String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());

			int len = fileName.length();
			int byteLen = fileName.getBytes().length;
			String length = Integer.toString(len);


			if(fileExists) {
				status = "HTTP/1.1 200 OK";
				date = "Date: " + dateString;
				serverName = "Server: " + config.getServerName();
				contentType = "Content-Type: " + contentType(fileName);
				contentLength = "Content-Length: " + length;
			}
			else {
				status = "HTTP/1.1 404 Not Found";
				date = "Date: " + dateString;
				serverName = "Server: " + config.getServerName();
				contentType = "Content-Type: text/html";

			}

			String header = status + serverName + contentType + contentLength + close; 

			os.writeBytes(status + "\r\n");
			os.writeBytes(date + "\r\n");
			os.writeBytes(serverName + "\r\n");
			os.writeBytes(contentType + "\r\n");
			//os.writeBytes("Connection: close" + "\r\n");
			os.writeBytes("\r\n");

			if(fileExists) {
				sendBytes(fis, os);
				fis.close();
			} else {
				fis = new FileInputStream(config.getFourOhFourDocument());
				sendBytes(fis, os);
			}

			// LOGGING
			OutputStream outFile = new BufferedOutputStream(new FileOutputStream(config.getLogFile()));

			InetAddress ipAddr = client.getInetAddress();
			String remoteHost = ipAddr.getHostAddress();

			BufferedWriter bw = null;
			FileWriter fw = null;

			String logString = remoteHost + " " + dateString + " " + requestLine + " " + status.substring(9) + " " + byteLen;
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(config.getLogFile(),true));
				out.write(logString);
				out.close();
			 } catch (IOException e) {
				System.out.println("exception occoured"+ e);
			 }

			os.close();
			in.close();
			client.close();
		

		} catch(Exception e) {
			System.out.println(e);
		}
		
	}

	public static String contentType(String file){
		if(file.endsWith(".htm") || file.endsWith(".html")) {
			return "text/html";
		}
		if(file.endsWith(".jpeg") || file.endsWith(".jpg")) {
			return "image/jpeg";
		}
		if(file.endsWith(".png")) {
			return "image/png";
		}
		if(file.endsWith(".gif")) {
			return "image/gif";
		}
		if(file.endsWith(".txt")) {
			return "text/plain";
		}
		else {
			return "text/plain";
		}
	}

	public static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
		byte[] BUFFER = new byte[1024];
		int bytes = 0;
		while ((bytes = fis.read(BUFFER)) != -1) {
			os.write(BUFFER, 0, bytes);
		}
	}
}

