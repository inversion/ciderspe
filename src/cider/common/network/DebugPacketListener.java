package cider.common.network;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

public class DebugPacketListener implements PacketListener {

	@Override
	public void processPacket(Packet packet) {
		// TODO Auto-generated method stub
		System.out.println("RECEIVED: " + packet.toXML());
	}

}
