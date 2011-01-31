package cider.common.network;

import java.util.Date;

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
		if( msg.getType() == Message.Type.groupchat )
			client.updateChatLog( msg.getFrom(), new Date(), msg.getBody() );
	}

}