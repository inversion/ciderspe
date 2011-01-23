package cider.common.network;

/**
 * This class just ties together the client and server for testing at the
 * moment. All it does is get the Client.java file from the server and print out
 * its contents.
 * 
 * @author Andrew
 * 
 */

public class Common
{

    public static final boolean DEBUG = true;

    // GOOGLE APPS
    public static final String HOST = "talk.google.com";
    public static final int PORT = 5222;// new
    // Random(System.currentTimeMillis())
    // .nextInt(1000);
    public static final String SERVICE_NAME = "mossage.co.uk";
    public static final String BOT_USERNAME = "ciderbot@mossage.co.uk";
    public static final String BOT_PASSWORD = "botpassword";
    public static final String CLIENT_USERNAME = "ciderclient@mossage.co.uk";
    public static final String CLIENT_PASSWORD = "clientpw";

    /*
     * // Localhost public static final String HOST = "192.168.0.2"; public
     * static final int PORT = 5222; public static final String SERVICE_NAME =
     * "192.168.0.2"; public static final String BOT_USERNAME =
     * "bot@192.168.0.2"; public static final String BOT_PASSWORD = "password";
     * public static final String CLIENT_USERNAME = "andrew@192.168.0.2"; public
     * static final String CLIENT_PASSWORD = "password";
     */

    public static final String SRCPATH = "src";

    /*
     * public static void main( String[] args ) {
     * 
     * @SuppressWarnings("unused") Server server = new Server(); Client client =
     * new Client(); client.getFileList(); //client.getFile(
     * "src\\cider\\common\\network\\Server.java" ); try { System.in.read(); }
     * catch (IOException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } }
     */

}
