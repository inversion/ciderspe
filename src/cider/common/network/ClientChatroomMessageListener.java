package cider.common.network;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class ClientChatroomMessageListener implements PacketListener {

	private Client client;

	public ClientChatroomMessageListener( Client source ) {
		client = source;
	}

	@Override
	public void processPacket(Packet packet) {
		// TODO Auto-generated method stub
		Message msg = (Message) packet;
		client.updateChatLog( "blah", null, msg.getBody() );
		System.out.println(msg.getBody());
	}

}
