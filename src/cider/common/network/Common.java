package cider.common.network;

import java.io.IOException;

/**
 * This class just ties together the client and server
 * for testing at the moment. All it does is get the Client.java file from the server
 * and print out its contents.
 * 
 * @author Andrew
 *
 */

public class Common {
	
		public static final boolean DEBUG = true;
	
		public static final String HOST = "talk.google.com";
		public static final int PORT = 5222;
		public static final String SERVICE_NAME = "mossage.co.uk";
		public static final String BOT_USERNAME = "ciderbot@mossage.co.uk";
		public static final String BOT_PASSWORD = "botpassword";
		public static final String CLIENT_USERNAME = "ciderclient@mossage.co.uk";
		public static final String CLIENT_PASSWORD = "clientpw";
		public static final String SRCPATH = "src";
		
		public static void main( String[] args )
		{
			@SuppressWarnings("unused")
			Server server = new Server();
			Client client = new Client();
			client.getFileList();
			client.getFile( "src\\cider\\common\\network\\Server.java" );
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
}
