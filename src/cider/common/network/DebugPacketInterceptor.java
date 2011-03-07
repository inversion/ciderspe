package cider.common.network;

import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.packet.Packet;

public class DebugPacketInterceptor implements PacketInterceptor {

	@Override
	public void interceptPacket(Packet packet) {
		// TODO Auto-generated method stub
		System.out.println( "SENT: " + packet.toXML());
	}

}
