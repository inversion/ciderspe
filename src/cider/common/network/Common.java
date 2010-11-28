package cider.common.network;

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
	
		public static final String DOMAIN = "192.168.0.2";
		public static final String BOT_USERNAME = "bot";
		public static final String BOT_PASSWORD = "password";
		public static final String CLIENT_USERNAME = "andrew";
		public static final String CLIENT_PASSWORD = "password";
		public static final String SRCPATH = "src";
		
		public static void main( String[] args )
		{
			@SuppressWarnings("unused")
			Server server = new Server();
			Client client = new Client();
			client.getFileList();
			//client.getFile( SRCPATH + "\\cider\\common\\network\\Client.java" );
		}
		
}
